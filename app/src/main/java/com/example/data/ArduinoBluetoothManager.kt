package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.AppLanguage
import com.example.model.BluetoothDeviceItem
import com.example.model.ConnectionState
import com.example.model.LineEnding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.Locale
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ArduinoBluetoothManager(private val context: Context) {

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var appLanguage: AppLanguage = AppLanguage.SYSTEM

    fun setLanguage(language: AppLanguage) {
        appLanguage = language
    }

    private fun str(@StringRes resId: Int, vararg args: Any): String {
        return context.withAppLanguage(appLanguage).getString(resId, *args)
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceItem>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceItem>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceItem>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceItem>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _incomingLines = MutableSharedFlow<String>(extraBufferCapacity = 128)
    val incomingLines: SharedFlow<String> = _incomingLines.asSharedFlow()

    private val _systemEvents = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val systemEvents: SharedFlow<String> = _systemEvents.asSharedFlow()

    private var activeSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var readerJob: Job? = null
    private var connectJob: Job? = null
    private var simulationJob: Job? = null
    private var isReceiverRegistered = false

    @Volatile
    private var simDistance = 64f
    @Volatile
    private var simCarActive = false

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                    val rssiShort = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    val rssi = if (rssiShort != Short.MIN_VALUE) rssiShort.toInt() else null

                    device?.let { btDev ->
                        if (!hasConnectPermission()) return
                        try {
                            val name = btDev.name ?: str(R.string.bt_unknown_device)
                            val address = btDev.address ?: return
                            val bonded = btDev.bondState == BluetoothDevice.BOND_BONDED
                            val item = BluetoothDeviceItem(
                                name = name,
                                address = address,
                                isBonded = bonded,
                                rssi = rssi
                            )
                            val current = _discoveredDevices.value.toMutableList()
                            if (current.none { it.address == address }) {
                                current.add(item)
                                _discoveredDevices.value = current
                            }
                        } catch (_: SecurityException) {
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    fun hasAllPermissions(): Boolean {
        return requiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val adapter = bluetoothAdapter ?: run {
            _pairedDevices.value = emptyList()
            return
        }
        if (!hasConnectPermission()) {
            _pairedDevices.value = emptyList()
            return
        }
        try {
            val bonded = adapter.bondedDevices.orEmpty().map { dev ->
                BluetoothDeviceItem(
                    name = dev.name ?: str(R.string.bt_default_module),
                    address = dev.address,
                    isBonded = true
                )
            }.sortedBy { it.name }
            _pairedDevices.value = bonded
        } catch (_: SecurityException) {
            _pairedDevices.value = emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val adapter = bluetoothAdapter ?: run {
            _systemEvents.tryEmit(str(R.string.bt_err_no_adapter))
            return
        }
        if (!adapter.isEnabled) {
            _systemEvents.tryEmit(str(R.string.bt_err_disabled))
            return
        }
        if (!hasAllPermissions()) {
            _systemEvents.tryEmit(str(R.string.bt_err_scan_perm))
            return
        }

        registerDiscoveryReceiverIfNeeded()
        _discoveredDevices.value = emptyList()
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            val started = adapter.startDiscovery()
            _isScanning.value = started
            if (!started) {
                _systemEvents.tryEmit(str(R.string.bt_err_scan_start))
            }
        } catch (se: SecurityException) {
            _isScanning.value = false
            _systemEvents.tryEmit(str(R.string.bt_err_scan_denied, se.localizedMessage ?: ""))
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        _isScanning.value = false
        val adapter = bluetoothAdapter ?: return
        if (!hasAllPermissions()) return
        try {
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
        } catch (_: SecurityException) {
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(deviceItem: BluetoothDeviceItem) {
        stopSimulation()
        stopDiscovery()
        closeSocketInternal()

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _connectionState.value = ConnectionState.Error(str(R.string.bt_err_connect_closed))
            return
        }
        if (!hasConnectPermission()) {
            _connectionState.value = ConnectionState.Error(str(R.string.bt_err_connect_perm))
            return
        }

        connectJob?.cancel()
        connectJob = scope.launch {
            _connectionState.value = ConnectionState.Connecting(deviceItem.name)
            _systemEvents.tryEmit(str(R.string.bt_evt_connecting, deviceItem.name, deviceItem.address))

            try {
                val remoteDevice = adapter.getRemoteDevice(deviceItem.address)
                val socket = remoteDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                try {
                    if (adapter.isDiscovering) adapter.cancelDiscovery()
                } catch (_: SecurityException) {
                }
                socket.connect()

                activeSocket = socket
                outputStream = socket.outputStream
                _connectionState.value = ConnectionState.Connected(deviceItem)
                _systemEvents.tryEmit(str(R.string.bt_evt_connected, deviceItem.name))

                startReadLoop(socket)
            } catch (e: IOException) {
                closeSocketInternal()
                val reason = e.localizedMessage ?: str(R.string.bt_evt_device_unreachable)
                val msg = str(R.string.bt_evt_connect_failed, deviceItem.name, reason)
                _connectionState.value = ConnectionState.Error(msg)
                _systemEvents.tryEmit(msg)
            } catch (_: SecurityException) {
                closeSocketInternal()
                val msg = str(R.string.bt_err_connect_perm)
                _connectionState.value = ConnectionState.Error(msg)
                _systemEvents.tryEmit(msg)
            }
        }
    }

    private fun startReadLoop(socket: BluetoothSocket) {
        readerJob?.cancel()
        readerJob = scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                while (isActive && socket.isConnected) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) {
                        _incomingLines.emit(line)
                    }
                }
            } catch (e: IOException) {
                if (isActive) {
                    val reason = e.localizedMessage ?: str(R.string.bt_evt_serial_closed)
                    _systemEvents.tryEmit(str(R.string.bt_evt_disconnected_io, reason))
                }
            } finally {
                if (_connectionState.value is ConnectionState.Connected) {
                    closeSocketInternal()
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    suspend fun sendCommand(command: String, lineEnding: LineEnding): Boolean {
        val state = _connectionState.value
        if (state is ConnectionState.SimulationMode) {
            handleSimulatedCommand(command)
            return true
        }
        val out = outputStream ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val payload = (command + lineEnding.value).toByteArray(Charsets.UTF_8)
                out.write(payload)
                out.flush()
                true
            } catch (e: IOException) {
                _systemEvents.tryEmit(str(R.string.bt_evt_send_failed, e.localizedMessage ?: ""))
                closeSocketInternal()
                _connectionState.value = ConnectionState.Disconnected
                false
            }
        }
    }

    fun disconnect() {
        connectJob?.cancel()
        stopSimulation()
        closeSocketInternal()
        _connectionState.value = ConnectionState.Disconnected
        _systemEvents.tryEmit(str(R.string.bt_evt_disconnected))
    }

    fun startSimulation() {
        connectJob?.cancel()
        closeSocketInternal()
        stopDiscovery()
        simulationJob?.cancel()

        _connectionState.value = ConnectionState.SimulationMode
        _systemEvents.tryEmit(str(R.string.bt_evt_sim_started))

        simulationJob = scope.launch {
            var tick = 0
            while (isActive) {
                val phase = tick * 0.18
                val temp = 24.5f + 2.8f * sin(phase).toFloat() + (Random.nextFloat() - 0.5f) * 0.4f
                val hum = 55.0f + 6.5f * cos(phase * 0.7).toFloat() + (Random.nextFloat() - 0.5f) * 0.8f
                val targetDist = if (simCarActive) 28f + 16f * sin(phase * 1.6).toFloat() else 78f + 20f * sin(phase * 0.5).toFloat()
                simDistance = (simDistance * 0.65f + targetDist * 0.35f).coerceIn(8f, 195f)
                val volt = 11.9f - (if (simCarActive) 0.45f else 0.0f) + 0.15f * sin(phase * 0.3).toFloat()
                val light = (72f + 18f * sin(phase * 0.4).toFloat()).coerceIn(5f, 100f)

                val line = String.format(
                    Locale.US,
                    "TEMP:%.1f,HUM:%.1f,DIST:%.1f,VOLT:%.2f,LIGHT:%.0f",
                    temp,
                    hum,
                    simDistance,
                    volt,
                    light
                )
                _incomingLines.emit(line)
                tick++
                delay(650L)
            }
        }
    }

    private suspend fun handleSimulatedCommand(command: String) {
        when (command) {
            "F", "B", "L", "R", "G", "I", "H", "J" -> simCarActive = true
            "S" -> simCarActive = false
            "STATUS" -> _incomingLines.emit("ACK:SIM_OK,MODE:TEST,FW:ArduLink-v1.0")
            else -> {
                if (command.startsWith("R") || command.startsWith("L") || command.startsWith("FAN") || command.startsWith("PUMP")) {
                    _incomingLines.emit("ACK:$command")
                }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        simCarActive = false
    }

    private fun closeSocketInternal() {
        readerJob?.cancel()
        readerJob = null
        try {
            outputStream?.close()
        } catch (_: IOException) {
        }
        outputStream = null
        try {
            activeSocket?.close()
        } catch (_: IOException) {
        }
        activeSocket = null
    }

    private fun registerDiscoveryReceiverIfNeeded() {
        if (isReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        ContextCompat.registerReceiver(
            context,
            discoveryReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        isReceiverRegistered = true
    }

    fun release() {
        stopDiscovery()
        disconnect()
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(discoveryReceiver)
            } catch (_: IllegalArgumentException) {
            }
            isReceiverRegistered = false
        }
        scope.cancel()
    }
}
