package com.example.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.ChartDistanceColor
import com.example.ui.theme.ChartHumidityColor
import com.example.ui.theme.ChartLightColor
import com.example.ui.theme.ChartTempColor
import com.example.ui.theme.ChartVoltageColor

enum class SensorChannel(
    @StringRes val titleRes: Int,
    @StringRes val shortNameRes: Int,
    val unit: String,
    val accentColor: Color,
    val defaultMin: Float,
    val defaultMax: Float
) {
    TEMPERATURE(R.string.ch_temp, R.string.ch_temp_short, "°C", ChartTempColor, 0f, 50f),
    HUMIDITY(R.string.ch_hum, R.string.ch_hum_short, "%", ChartHumidityColor, 0f, 100f),
    DISTANCE(R.string.ch_dist, R.string.ch_dist_short, "cm", ChartDistanceColor, 0f, 200f),
    VOLTAGE(R.string.ch_volt, R.string.ch_volt_short, "V", ChartVoltageColor, 0f, 15f),
    LIGHT(R.string.ch_light, R.string.ch_light_short, "%", ChartLightColor, 0f, 100f)
}

data class SensorPoint(
    val timestampMs: Long,
    val value: Float
)

data class ChannelSeries(
    val channel: SensorChannel,
    val points: List<SensorPoint> = emptyList(),
    val latestValue: Float? = null,
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val avgValue: Float? = null
)

data class TelemetryState(
    val selectedChannel: SensorChannel = SensorChannel.TEMPERATURE,
    val isPaused: Boolean = false,
    val totalPackets: Long = 0L,
    val lastPacketTimeMs: Long = 0L,
    val seriesByChannel: Map<SensorChannel, ChannelSeries> =
        SensorChannel.entries.associateWith { ChannelSeries(channel = it) }
)
