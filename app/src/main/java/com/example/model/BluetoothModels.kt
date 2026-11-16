package com.example.model

import androidx.annotation.StringRes
import com.example.R

enum class AppLanguage(@StringRes val labelRes: Int, val tag: String?) {
    SYSTEM(R.string.lang_system, null),
    TURKISH(R.string.lang_turkish, "tr"),
    ENGLISH(R.string.lang_english, "en")
}

enum class LineEnding(@StringRes val labelRes: Int, val fixedLabel: String?, val value: String) {
    LF(R.string.line_ending_none, "LF (\\n)", "\n"),
    CRLF(R.string.line_ending_none, "CRLF (\\r\\n)", "\r\n"),
    NONE(R.string.line_ending_none, null, "")
}

enum class PacketDirection {
    RX,
    TX,
    SYS
}

data class BluetoothDeviceItem(
    val name: String,
    val address: String,
    val isBonded: Boolean,
    val rssi: Int? = null
)

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val deviceName: String) : ConnectionState
    data class Connected(val device: BluetoothDeviceItem) : ConnectionState
    data object SimulationMode : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class SerialLogEntry(
    val id: Long,
    val timestamp: String,
    val direction: PacketDirection,
    val payload: String
)
