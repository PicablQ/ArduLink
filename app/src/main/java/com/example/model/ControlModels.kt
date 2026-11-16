package com.example.model

import androidx.annotation.StringRes
import com.example.R

enum class CarControlMode(@StringRes val labelRes: Int) {
    TWO_HAND(R.string.car_mode_two_hand),
    JOYSTICK(R.string.car_mode_joystick),
    DPAD(R.string.car_mode_dpad)
}

enum class CarDriveDirection(val command: String, @StringRes val labelRes: Int) {
    IDLE("S", R.string.car_dir_idle),
    FORWARD("F", R.string.car_dir_forward),
    BACKWARD("B", R.string.car_dir_backward),
    LEFT("L", R.string.car_dir_left),
    RIGHT("R", R.string.car_dir_right),
    FORWARD_LEFT("G", R.string.car_dir_fwd_left),
    FORWARD_RIGHT("I", R.string.car_dir_fwd_right),
    BACKWARD_LEFT("H", R.string.car_dir_bwd_left),
    BACKWARD_RIGHT("J", R.string.car_dir_bwd_right)
}

data class CarState(
    val direction: CarDriveDirection = CarDriveDirection.IDLE,
    val controlMode: CarControlMode = CarControlMode.TWO_HAND,
    val speedPwm: Int = 185,
    val headlightsOn: Boolean = false,
    val hazardsOn: Boolean = false,
    val hornActive: Boolean = false,
    val autonomousMode: Boolean = false,
    val joystickX: Float = 0f,
    val joystickY: Float = 0f,
    val throttleAxis: Int = 0, // -1 backward, 0 idle, 1 forward
    val steerAxis: Int = 0,    // -1 left, 0 center, 1 right
    val ultrasonicCm: Float? = null,
    val batteryVoltage: Float? = null
)

enum class DeviceControlType(@StringRes val labelRes: Int) {
    TOGGLE(R.string.type_toggle),
    MOMENTARY(R.string.type_momentary),
    SLIDER(R.string.type_slider)
}

enum class DeviceIconType(@StringRes val labelRes: Int) {
    RELAY(R.string.icon_relay),
    LIGHT(R.string.icon_light),
    FAN(R.string.icon_fan),
    SERVO(R.string.icon_servo),
    PUMP(R.string.icon_pump),
    BUZZER(R.string.icon_buzzer),
    LOCK(R.string.icon_lock),
    CUSTOM(R.string.icon_custom)
}

data class CircuitDevice(
    val id: String,
    val name: String,
    val subtitle: String,
    val pinLabel: String,
    val type: DeviceControlType,
    val iconType: DeviceIconType,
    val commandOn: String,
    val commandOff: String = "",
    val sliderPrefix: String = "",
    val sliderMin: Int = 0,
    val sliderMax: Int = 180,
    val isOn: Boolean = false,
    val sliderValue: Int = 90
)

data class MultiSwitchItem(
    val index: Int,
    val name: String,
    val commandOn: String,
    val commandOff: String,
    val isOn: Boolean = false
)

data class VoiceCommandRule(
    val id: String,
    val phrase: String,
    val txCommand: String
)

fun defaultMultiSwitches(isTurkish: Boolean = true): List<MultiSwitchItem> = (1..10).map { idx ->
    MultiSwitchItem(
        index = idx,
        name = if (isTurkish) "Buton $idx" else "Button $idx",
        commandOn = "B${idx}_ON",
        commandOff = "B${idx}_OFF",
        isOn = false
    )
}

fun defaultVoiceRules(isTurkish: Boolean = true): List<VoiceCommandRule> =
    if (isTurkish) {
        listOf(
            VoiceCommandRule("v1", "ileri", "F"),
            VoiceCommandRule("v2", "geri", "B"),
            VoiceCommandRule("v3", "sol", "L"),
            VoiceCommandRule("v4", "sağ", "R"),
            VoiceCommandRule("v5", "dur", "S"),
            VoiceCommandRule("v6", "ışıkları aç", "W"),
            VoiceCommandRule("v7", "ışıkları kapat", "w"),
            VoiceCommandRule("v8", "led aç", "B1_ON"),
            VoiceCommandRule("v9", "led kapat", "B1_OFF"),
            VoiceCommandRule("v10", "buzzer çal", "B2_ON")
        )
    } else {
        listOf(
            VoiceCommandRule("v1", "forward", "F"),
            VoiceCommandRule("v2", "reverse", "B"),
            VoiceCommandRule("v3", "left", "L"),
            VoiceCommandRule("v4", "right", "R"),
            VoiceCommandRule("v5", "stop", "S"),
            VoiceCommandRule("v6", "lights on", "W"),
            VoiceCommandRule("v7", "lights off", "w"),
            VoiceCommandRule("v8", "led on", "B1_ON"),
            VoiceCommandRule("v9", "led off", "B1_OFF"),
            VoiceCommandRule("v10", "buzzer on", "B2_ON")
        )
    }

fun defaultCircuitDevices(isTurkish: Boolean = true): List<CircuitDevice> = listOf(
    CircuitDevice(
        id = "relay_1",
        name = if (isTurkish) "Röle Kanal 1" else "Relay Channel 1",
        subtitle = if (isTurkish) "Ana Güç Hattı" else "Main Power Line",
        pinLabel = if (isTurkish) "D4 • RÖLE" else "D4 • RELAY",
        type = DeviceControlType.TOGGLE,
        iconType = DeviceIconType.RELAY,
        commandOn = "R1_ON",
        commandOff = "R1_OFF"
    ),
    CircuitDevice(
        id = "led_1",
        name = if (isTurkish) "Ortam LED Şeridi" else "Ambient LED Strip",
        subtitle = if (isTurkish) "Beyaz Aydınlatma" else "White Lighting",
        pinLabel = if (isTurkish) "D7 • ÇIKIŞ" else "D7 • OUT",
        type = DeviceControlType.TOGGLE,
        iconType = DeviceIconType.LIGHT,
        commandOn = "L1_ON",
        commandOff = "L1_OFF"
    ),
    CircuitDevice(
        id = "fan_1",
        name = if (isTurkish) "Soğutma Fanı" else "Cooling Fan",
        subtitle = if (isTurkish) "Termal Kontrol" else "Thermal Control",
        pinLabel = "D8 • MOS",
        type = DeviceControlType.TOGGLE,
        iconType = DeviceIconType.FAN,
        commandOn = "FAN_ON",
        commandOff = "FAN_OFF"
    ),
    CircuitDevice(
        id = "pump_1",
        name = if (isTurkish) "Sulama Pompası" else "Water Pump",
        subtitle = if (isTurkish) "12V Su Motoru" else "12V Water Motor",
        pinLabel = if (isTurkish) "D12 • RÖLE" else "D12 • RELAY",
        type = DeviceControlType.TOGGLE,
        iconType = DeviceIconType.PUMP,
        commandOn = "PUMP_ON",
        commandOff = "PUMP_OFF"
    ),
    CircuitDevice(
        id = "gate_pulse",
        name = if (isTurkish) "Garaj Kapısı Tetik" else "Garage Gate Trigger",
        subtitle = if (isTurkish) "Anlık Darbe Sinyali" else "Momentary Pulse",
        pinLabel = if (isTurkish) "D2 • DARBE" else "D2 • PULSE",
        type = DeviceControlType.MOMENTARY,
        iconType = DeviceIconType.LOCK,
        commandOn = "GATE_TRIG"
    ),
    CircuitDevice(
        id = "buzzer_ping",
        name = if (isTurkish) "Buzzer Uyarı" else "Buzzer Alert",
        subtitle = if (isTurkish) "Sesli Sinyal" else "Audio Tone",
        pinLabel = "D11 • TONE",
        type = DeviceControlType.MOMENTARY,
        iconType = DeviceIconType.BUZZER,
        commandOn = "BEEP"
    ),
    CircuitDevice(
        id = "servo_arm",
        name = if (isTurkish) "Kamera / Kol Servosu" else "Camera / Arm Servo",
        subtitle = if (isTurkish) "Açı Konumlandırma" else "Angle Positioning",
        pinLabel = "D9 • SERVO",
        type = DeviceControlType.SLIDER,
        iconType = DeviceIconType.SERVO,
        commandOn = "SRV:",
        sliderPrefix = "SRV:",
        sliderMin = 0,
        sliderMax = 180,
        sliderValue = 90
    ),
    CircuitDevice(
        id = "dimmer_pwm",
        name = if (isTurkish) "LED Dimmer Gücü" else "LED Dimmer Power",
        subtitle = if (isTurkish) "PWM Parlaklık" else "PWM Brightness",
        pinLabel = "D6 • PWM",
        type = DeviceControlType.SLIDER,
        iconType = DeviceIconType.LIGHT,
        commandOn = "DIM:",
        sliderPrefix = "DIM:",
        sliderMin = 0,
        sliderMax = 255,
        sliderValue = 128
    )
)
