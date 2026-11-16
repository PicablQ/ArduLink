package com.example.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ArduinoBluetoothManager
import com.example.data.PreferencesRepository
import com.example.data.TelemetryParser
import com.example.data.isTurkishLocale
import com.example.data.resolveLocale
import com.example.data.withAppLanguage
import com.example.model.AppLanguage
import com.example.model.BluetoothDeviceItem
import com.example.model.CarControlMode
import com.example.model.CarDriveDirection
import com.example.model.CarState
import com.example.model.ChannelSeries
import com.example.model.CircuitDevice
import com.example.model.ConnectionState
import com.example.model.DeviceControlType
import com.example.model.DeviceIconType
import com.example.model.LineEnding
import com.example.model.MultiSwitchItem
import com.example.model.PacketDirection
import com.example.model.SensorChannel
import com.example.model.SensorPoint
import com.example.model.SerialLogEntry
import com.example.model.TelemetryState
import com.example.model.VoiceCommandRule
import com.example.model.defaultMultiSwitches
import com.example.model.defaultVoiceRules
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

class ArduLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = ArduinoBluetoothManager(application.applicationContext)
    private val preferencesRepository = PreferencesRepository(application.applicationContext)

    val connectionState: StateFlow<ConnectionState> = bluetoothManager.connectionState
    val pairedDevices: StateFlow<List<BluetoothDeviceItem>> = bluetoothManager.pairedDevices
    val discoveredDevices: StateFlow<List<BluetoothDeviceItem>> = bluetoothManager.discoveredDevices
    val isScanning: StateFlow<Boolean> = bluetoothManager.isScanning

    private val _appLanguage = MutableStateFlow(AppLanguage.SYSTEM)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _lineEnding = MutableStateFlow(LineEnding.LF)
    val lineEnding: StateFlow<LineEnding> = _lineEnding.asStateFlow()

    private val _hasBluetoothPermissions = MutableStateFlow(bluetoothManager.hasAllPermissions())
    val hasBluetoothPermissions: StateFlow<Boolean> = _hasBluetoothPermissions.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothManager.isBluetoothEnabled())
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _carState = MutableStateFlow(CarState())
    val carState: StateFlow<CarState> = _carState.asStateFlow()

    private val _circuitDevices = MutableStateFlow<List<CircuitDevice>>(emptyList())
    val circuitDevices: StateFlow<List<CircuitDevice>> = _circuitDevices.asStateFlow()

    private val _multiSwitches = MutableStateFlow(defaultMultiSwitches(isTurkishLocale(AppLanguage.SYSTEM)))
    val multiSwitches: StateFlow<List<MultiSwitchItem>> = _multiSwitches.asStateFlow()

    private val _voiceRules = MutableStateFlow(defaultVoiceRules(isTurkishLocale(AppLanguage.SYSTEM)))
    val voiceRules: StateFlow<List<VoiceCommandRule>> = _voiceRules.asStateFlow()

    private val _sendUnmatchedVoice = MutableStateFlow(false)
    val sendUnmatchedVoice: StateFlow<Boolean> = _sendUnmatchedVoice.asStateFlow()

    private val _lastVoiceMatch = MutableStateFlow<String?>(null)
    val lastVoiceMatch: StateFlow<String?> = _lastVoiceMatch.asStateFlow()

    private val _lastGamepadTx = MutableStateFlow("--")
    val lastGamepadTx: StateFlow<String> = _lastGamepadTx.asStateFlow()

    private val _telemetryState = MutableStateFlow(TelemetryState())
    val telemetryState: StateFlow<TelemetryState> = _telemetryState.asStateFlow()

    private val _serialLogs = MutableStateFlow<List<SerialLogEntry>>(emptyList())
    val serialLogs: StateFlow<List<SerialLogEntry>> = _serialLogs.asStateFlow()

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val uiMessages: SharedFlow<String> = _uiMessages.asSharedFlow()

    private var logCounter = 0L
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    init {
        observePreferences()
        observeBluetoothStreams()
        refreshBluetoothEnvironment()
    }

    private fun str(@StringRes resId: Int, vararg args: Any): String {
        return getApplication<Application>()
            .withAppLanguage(_appLanguage.value)
            .getString(resId, *args)
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collect { prefs ->
                _appLanguage.value = prefs.appLanguage
                bluetoothManager.setLanguage(prefs.appLanguage)

                _themeMode.value = prefs.themeMode
                _lineEnding.value = prefs.lineEnding
                _carState.update { it.copy(controlMode = prefs.carControlMode) }

                val currentActiveDevices = _circuitDevices.value.associate { it.id to it.isOn }
                _circuitDevices.value = prefs.circuitDevices.map { dev ->
                    dev.copy(isOn = currentActiveDevices[dev.id] ?: dev.isOn)
                }

                val currentSwitchStates = _multiSwitches.value.associate { it.index to it.isOn }
                _multiSwitches.value = prefs.multiSwitches.map { sw ->
                    sw.copy(isOn = currentSwitchStates[sw.index] ?: sw.isOn)
                }

                _voiceRules.value = prefs.voiceRules
                _sendUnmatchedVoice.value = prefs.sendUnmatchedVoice
            }
        }
    }

    private fun observeBluetoothStreams() {
        viewModelScope.launch {
            bluetoothManager.incomingLines.collect { line ->
                appendLog(PacketDirection.RX, line)
                processTelemetryLine(line)
            }
        }
        viewModelScope.launch {
            bluetoothManager.systemEvents.collect { msg ->
                appendLog(PacketDirection.SYS, msg)
                _uiMessages.tryEmit(msg)
            }
        }
    }

    private fun processTelemetryLine(line: String) {
        val parsed = TelemetryParser.parseLine(line)
        if (parsed.isEmpty()) return

        val now = System.currentTimeMillis()
        parsed[SensorChannel.DISTANCE]?.let { dist ->
            _carState.update { it.copy(ultrasonicCm = dist) }
        }
        parsed[SensorChannel.VOLTAGE]?.let { volt ->
            _carState.update { it.copy(batteryVoltage = volt) }
        }

        _telemetryState.update { current ->
            if (current.isPaused) {
                return@update current.copy(
                    totalPackets = current.totalPackets + 1,
                    lastPacketTimeMs = now
                )
            }

            val updatedMap = current.seriesByChannel.toMutableMap()
            parsed.forEach { (channel, value) ->
                val existing = updatedMap[channel] ?: ChannelSeries(channel)
                val nextPoints = (existing.points + SensorPoint(now, value)).takeLast(60)
                val values = nextPoints.map { it.value }
                updatedMap[channel] = existing.copy(
                    points = nextPoints,
                    latestValue = value,
                    minValue = values.minOrNull(),
                    maxValue = values.maxOrNull(),
                    avgValue = if (values.isNotEmpty()) values.average().toFloat() else null
                )
            }

            current.copy(
                totalPackets = current.totalPackets + 1,
                lastPacketTimeMs = now,
                seriesByChannel = updatedMap
            )
        }
    }

    // --- Bluetooth & Connection Actions ---

    fun requiredBluetoothPermissions(): Array<String> = bluetoothManager.requiredPermissions()

    fun isBluetoothSupported(): Boolean = bluetoothManager.isBluetoothSupported()

    fun refreshBluetoothEnvironment() {
        _hasBluetoothPermissions.value = bluetoothManager.hasAllPermissions()
        _isBluetoothEnabled.value = bluetoothManager.isBluetoothEnabled()
        if (_hasBluetoothPermissions.value) {
            bluetoothManager.refreshPairedDevices()
        }
    }

    fun startDiscovery() {
        refreshBluetoothEnvironment()
        bluetoothManager.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothManager.stopDiscovery()
    }

    fun connectToDevice(device: BluetoothDeviceItem) {
        bluetoothManager.connect(device)
    }

    fun disconnect() {
        bluetoothManager.disconnect()
    }

    fun startSimulationMode() {
        bluetoothManager.startSimulation()
    }

    // --- Car Control Actions ---

    fun setCarControlMode(mode: CarControlMode) {
        _carState.update {
            it.copy(
                controlMode = mode,
                throttleAxis = 0,
                steerAxis = 0,
                direction = CarDriveDirection.IDLE
            )
        }
        viewModelScope.launch {
            preferencesRepository.setCarControlMode(mode)
        }
    }

    fun setTwoHandThrottle(axis: Int) {
        val current = _carState.value
        val clamped = axis.coerceIn(-1, 1)
        if (current.throttleAxis == clamped) return
        val nextDir = combineTwoHandAxes(clamped, current.steerAxis)
        val prevDir = current.direction
        _carState.update { it.copy(throttleAxis = clamped, direction = nextDir) }
        if (nextDir != prevDir) {
            sendCommandInternal(nextDir.command, notifyIfDisconnected = nextDir != CarDriveDirection.IDLE)
        }
    }

    fun setTwoHandSteer(axis: Int) {
        val current = _carState.value
        val clamped = axis.coerceIn(-1, 1)
        if (current.steerAxis == clamped) return
        val nextDir = combineTwoHandAxes(current.throttleAxis, clamped)
        val prevDir = current.direction
        _carState.update { it.copy(steerAxis = clamped, direction = nextDir) }
        if (nextDir != prevDir) {
            sendCommandInternal(nextDir.command, notifyIfDisconnected = nextDir != CarDriveDirection.IDLE)
        }
    }

    private fun combineTwoHandAxes(throttle: Int, steer: Int): CarDriveDirection {
        return when {
            throttle > 0 && steer < 0 -> CarDriveDirection.FORWARD_LEFT
            throttle > 0 && steer > 0 -> CarDriveDirection.FORWARD_RIGHT
            throttle < 0 && steer < 0 -> CarDriveDirection.BACKWARD_LEFT
            throttle < 0 && steer > 0 -> CarDriveDirection.BACKWARD_RIGHT
            throttle > 0 -> CarDriveDirection.FORWARD
            throttle < 0 -> CarDriveDirection.BACKWARD
            steer < 0 -> CarDriveDirection.LEFT
            steer > 0 -> CarDriveDirection.RIGHT
            else -> CarDriveDirection.IDLE
        }
    }

    fun updateCarJoystick(x: Float, y: Float) {
        val nextDirection = resolveDirectionFromJoystick(x, y)
        val prevDirection = _carState.value.direction
        _carState.update {
            it.copy(
                joystickX = x,
                joystickY = y,
                direction = nextDirection
            )
        }
        if (nextDirection != prevDirection) {
            sendCommandInternal(nextDirection.command, notifyIfDisconnected = false)
        }
    }

    fun setCarDirection(direction: CarDriveDirection) {
        val prev = _carState.value.direction
        _carState.update {
            it.copy(
                direction = direction,
                joystickX = when (direction) {
                    CarDriveDirection.LEFT, CarDriveDirection.FORWARD_LEFT, CarDriveDirection.BACKWARD_LEFT -> -0.8f
                    CarDriveDirection.RIGHT, CarDriveDirection.FORWARD_RIGHT, CarDriveDirection.BACKWARD_RIGHT -> 0.8f
                    else -> 0f
                },
                joystickY = when (direction) {
                    CarDriveDirection.FORWARD, CarDriveDirection.FORWARD_LEFT, CarDriveDirection.FORWARD_RIGHT -> -0.8f
                    CarDriveDirection.BACKWARD, CarDriveDirection.BACKWARD_LEFT, CarDriveDirection.BACKWARD_RIGHT -> 0.8f
                    else -> 0f
                }
            )
        }
        if (direction != prev) {
            sendCommandInternal(direction.command, notifyIfDisconnected = direction != CarDriveDirection.IDLE)
        }
    }

    fun updateCarSpeedPreview(pwm: Int) {
        _carState.update { it.copy(speedPwm = pwm.coerceIn(0, 255)) }
    }

    fun commitCarSpeed(pwm: Int) {
        val clamped = pwm.coerceIn(0, 255)
        _carState.update { it.copy(speedPwm = clamped) }
        sendCommandInternal("SPD:$clamped", notifyIfDisconnected = false)
    }

    fun emergencyStop() {
        _carState.update {
            it.copy(
                direction = CarDriveDirection.IDLE,
                joystickX = 0f,
                joystickY = 0f,
                throttleAxis = 0,
                steerAxis = 0,
                autonomousMode = false,
                hornActive = false
            )
        }
        sendCommandInternal("S", notifyIfDisconnected = true)
    }

    fun toggleHeadlights() {
        val next = !_carState.value.headlightsOn
        _carState.update { it.copy(headlightsOn = next) }
        sendCommandInternal(if (next) "W" else "w")
    }

    fun toggleHazards() {
        val next = !_carState.value.hazardsOn
        _carState.update { it.copy(hazardsOn = next) }
        sendCommandInternal(if (next) "X" else "x")
    }

    fun setHornActive(active: Boolean) {
        if (_carState.value.hornActive == active) return
        _carState.update { it.copy(hornActive = active) }
        sendCommandInternal(if (active) "V" else "v", notifyIfDisconnected = active)
    }

    fun toggleAutonomousMode() {
        val next = !_carState.value.autonomousMode
        _carState.update { it.copy(autonomousMode = next) }
        sendCommandInternal(if (next) "A" else "a")
    }

    private fun resolveDirectionFromJoystick(x: Float, y: Float): CarDriveDirection {
        val deadzone = 0.24f
        if (abs(x) < deadzone && abs(y) < deadzone) return CarDriveDirection.IDLE

        val forward = y < -deadzone
        val backward = y > deadzone
        val left = x < -deadzone
        val right = x > deadzone

        return when {
            forward && left -> CarDriveDirection.FORWARD_LEFT
            forward && right -> CarDriveDirection.FORWARD_RIGHT
            backward && left -> CarDriveDirection.BACKWARD_LEFT
            backward && right -> CarDriveDirection.BACKWARD_RIGHT
            forward -> CarDriveDirection.FORWARD
            backward -> CarDriveDirection.BACKWARD
            left -> CarDriveDirection.LEFT
            right -> CarDriveDirection.RIGHT
            else -> CarDriveDirection.IDLE
        }
    }

    // --- 10-Button Multi-Control Panel ---

    fun toggleMultiSwitch(index: Int) {
        val target = _multiSwitches.value.find { it.index == index } ?: return
        val nextState = !target.isOn
        _multiSwitches.value = _multiSwitches.value.map {
            if (it.index == index) it.copy(isOn = nextState) else it
        }
        val cmd = if (nextState) target.commandOn else target.commandOff
        if (cmd.isNotBlank()) {
            sendCommandInternal(cmd)
        }
    }

    fun updateMultiSwitchConfig(
        index: Int,
        newName: String,
        newCommandOn: String,
        newCommandOff: String
    ) {
        val cleanName = newName.trim().ifEmpty { str(R.string.multi_default_name, index) }
        val cleanOn = newCommandOn.trim().ifEmpty { "B${index}_ON" }
        val cleanOff = newCommandOff.trim().ifEmpty { "B${index}_OFF" }
        val updated = _multiSwitches.value.map {
            if (it.index == index) {
                it.copy(name = cleanName, commandOn = cleanOn, commandOff = cleanOff)
            } else it
        }
        _multiSwitches.value = updated
        viewModelScope.launch {
            preferencesRepository.saveMultiSwitches(updated)
        }
    }

    fun setAllMultiSwitches(turnOn: Boolean) {
        val current = _multiSwitches.value
        val changed = current.filter { it.isOn != turnOn }
        if (changed.isEmpty()) return
        _multiSwitches.value = current.map { it.copy(isOn = turnOn) }
        changed.forEach { sw ->
            val cmd = if (turnOn) sw.commandOn else sw.commandOff
            if (cmd.isNotBlank()) {
                sendCommandInternal(cmd, notifyIfDisconnected = false)
            }
        }
        _uiMessages.tryEmit(str(if (turnOn) R.string.msg_multi_all_on else R.string.msg_multi_all_off))
    }

    // --- Circuit Devices Control ---

    fun toggleCircuitDevice(deviceId: String) {
        val target = _circuitDevices.value.find { it.id == deviceId } ?: return
        val nextState = !target.isOn
        val updated = _circuitDevices.value.map {
            if (it.id == deviceId) it.copy(isOn = nextState) else it
        }
        _circuitDevices.value = updated
        val cmd = if (nextState) target.commandOn else target.commandOff
        if (cmd.isNotBlank()) {
            sendCommandInternal(cmd)
        }
    }

    fun triggerMomentaryDevice(deviceId: String) {
        val target = _circuitDevices.value.find { it.id == deviceId } ?: return
        sendCommandInternal(target.commandOn)
    }

    fun updateDeviceSliderPreview(deviceId: String, value: Int) {
        _circuitDevices.value = _circuitDevices.value.map {
            if (it.id == deviceId) it.copy(sliderValue = value.coerceIn(it.sliderMin, it.sliderMax)) else it
        }
    }

    fun commitDeviceSlider(deviceId: String, value: Int) {
        val target = _circuitDevices.value.find { it.id == deviceId } ?: return
        val clamped = value.coerceIn(target.sliderMin, target.sliderMax)
        val updated = _circuitDevices.value.map {
            if (it.id == deviceId) it.copy(sliderValue = clamped, isOn = clamped > target.sliderMin) else it
        }
        _circuitDevices.value = updated
        viewModelScope.launch {
            preferencesRepository.saveCircuitDevices(updated)
        }
        val prefix = target.sliderPrefix.ifBlank { target.commandOn }
        sendCommandInternal("$prefix$clamped")
    }

    fun allDevicesOff() {
        val current = _circuitDevices.value
        val activeToggles = current.filter { it.type == DeviceControlType.TOGGLE && it.isOn }
        if (activeToggles.isEmpty()) {
            sendCommandInternal("ALL_OFF")
            return
        }
        val updated = current.map {
            if (it.type == DeviceControlType.TOGGLE) it.copy(isOn = false) else it
        }
        _circuitDevices.value = updated
        activeToggles.forEach { dev ->
            if (dev.commandOff.isNotBlank()) {
                sendCommandInternal(dev.commandOff, notifyIfDisconnected = false)
            }
        }
        _uiMessages.tryEmit(str(R.string.msg_all_devices_off))
    }

    fun addCustomDevice(
        name: String,
        subtitle: String,
        pinLabel: String,
        type: DeviceControlType,
        iconType: DeviceIconType,
        commandOn: String,
        commandOff: String,
        sliderMin: Int,
        sliderMax: Int
    ) {
        val isTr = isTurkishLocale(_appLanguage.value)
        val cleanName = name.trim().ifEmpty { if (isTr) "Özel Devre" else "Custom Circuit" }
        val cleanOn = commandOn.trim().ifEmpty { "CMD_ON" }
        val cleanOff = commandOff.trim().ifEmpty { "CMD_OFF" }
        val newDevice = CircuitDevice(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = cleanName,
            subtitle = subtitle.trim().ifEmpty { if (isTr) "Özel Arduino Çıkışı" else "Custom Arduino Output" },
            pinLabel = pinLabel.trim().ifEmpty { "GPIO" },
            type = type,
            iconType = iconType,
            commandOn = cleanOn,
            commandOff = if (type == DeviceControlType.TOGGLE) cleanOff else "",
            sliderPrefix = if (type == DeviceControlType.SLIDER) cleanOn else "",
            sliderMin = sliderMin,
            sliderMax = sliderMax.coerceAtLeast(sliderMin + 1),
            sliderValue = (sliderMin + sliderMax) / 2
        )
        val updated = _circuitDevices.value + newDevice
        _circuitDevices.value = updated
        viewModelScope.launch {
            preferencesRepository.saveCircuitDevices(updated)
        }
    }

    fun deleteDevice(deviceId: String) {
        val updated = _circuitDevices.value.filterNot { it.id == deviceId }
        _circuitDevices.value = updated
        viewModelScope.launch {
            preferencesRepository.saveCircuitDevices(updated)
        }
    }

    // --- Gamepad & 4x4 Keypad ---

    fun sendGamepadCommand(command: String, notifyIfDisconnected: Boolean = true) {
        _lastGamepadTx.value = command
        sendCommandInternal(command, notifyIfDisconnected = notifyIfDisconnected)
    }

    fun sendKeypadCommand(command: String) {
        sendCommandInternal(command, notifyIfDisconnected = true)
    }

    // --- Voice Terminal ---

    fun processVoiceTranscript(transcript: String) {
        val activeLocale = resolveLocale(_appLanguage.value)
        val normalized = transcript.trim().lowercase(activeLocale)
        if (normalized.isEmpty()) return

        val matchedRule = _voiceRules.value
            .sortedByDescending { it.phrase.length }
            .firstOrNull { rule ->
                normalized.contains(rule.phrase.trim().lowercase(activeLocale))
            }

        if (matchedRule != null) {
            _lastVoiceMatch.value = str(R.string.voice_match_rule, matchedRule.phrase, matchedRule.txCommand)
            sendCommandInternal(matchedRule.txCommand, notifyIfDisconnected = true)
        } else if (_sendUnmatchedVoice.value) {
            _lastVoiceMatch.value = str(R.string.voice_match_raw, transcript)
            sendCommandInternal(transcript.trim(), notifyIfDisconnected = true)
        } else {
            _lastVoiceMatch.value = str(R.string.voice_match_none, transcript)
        }
    }

    fun addVoiceRule(phrase: String, txCommand: String) {
        val cleanPhrase = phrase.trim()
        val cleanTx = txCommand.trim()
        if (cleanPhrase.isEmpty() || cleanTx.isEmpty()) return
        val updated = _voiceRules.value + VoiceCommandRule(
            id = "vr_${UUID.randomUUID().toString().take(8)}",
            phrase = cleanPhrase,
            txCommand = cleanTx
        )
        _voiceRules.value = updated
        viewModelScope.launch {
            preferencesRepository.saveVoiceRules(updated)
        }
    }

    fun deleteVoiceRule(id: String) {
        val updated = _voiceRules.value.filterNot { it.id == id }
        _voiceRules.value = updated
        viewModelScope.launch {
            preferencesRepository.saveVoiceRules(updated)
        }
    }

    fun setSendUnmatchedVoice(enabled: Boolean) {
        _sendUnmatchedVoice.value = enabled
        viewModelScope.launch {
            preferencesRepository.setSendUnmatchedVoice(enabled)
        }
    }

    // --- Telemetry Actions ---

    fun selectSensorChannel(channel: SensorChannel) {
        _telemetryState.update { it.copy(selectedChannel = channel) }
    }

    fun toggleTelemetryPause() {
        _telemetryState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun clearTelemetryHistory() {
        _telemetryState.update { current ->
            current.copy(
                totalPackets = 0L,
                lastPacketTimeMs = 0L,
                seriesByChannel = SensorChannel.entries.associateWith { ChannelSeries(channel = it) }
            )
        }
    }

    // --- Serial Terminal & Settings ---

    fun sendManualSerialCommand(rawCommand: String) {
        val trimmed = rawCommand.trim()
        if (trimmed.isEmpty()) return
        sendCommandInternal(trimmed, notifyIfDisconnected = true)
    }

    fun clearSerialLogs() {
        _serialLogs.value = emptyList()
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        bluetoothManager.setLanguage(language)
        viewModelScope.launch {
            preferencesRepository.setAppLanguage(language)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun cycleDarkLightTheme() {
        val next = if (_themeMode.value == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
        setThemeMode(next)
    }

    fun setLineEnding(lineEnding: LineEnding) {
        _lineEnding.value = lineEnding
        viewModelScope.launch {
            preferencesRepository.setLineEnding(lineEnding)
        }
    }

    private fun sendCommandInternal(command: String, notifyIfDisconnected: Boolean = true) {
        val state = connectionState.value
        val canSend = state is ConnectionState.Connected || state is ConnectionState.SimulationMode
        if (!canSend) {
            if (notifyIfDisconnected) {
                _uiMessages.tryEmit(str(R.string.msg_not_connected))
            }
            return
        }
        viewModelScope.launch {
            val ok = bluetoothManager.sendCommand(command, _lineEnding.value)
            if (ok) {
                appendLog(PacketDirection.TX, command)
            }
        }
    }

    private fun appendLog(direction: PacketDirection, payload: String) {
        val entry = SerialLogEntry(
            id = ++logCounter,
            timestamp = timeFormat.format(Date()),
            direction = direction,
            payload = payload
        )
        _serialLogs.update { list -> (list + entry).takeLast(200) }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothManager.release()
    }
}
