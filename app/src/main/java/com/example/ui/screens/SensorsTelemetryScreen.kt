package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChannelSeries
import com.example.model.SensorChannel
import com.example.model.SensorPoint
import com.example.model.TelemetryState
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.TelemetryMonoSmall
import com.example.ui.theme.TelemetryValueLarge
import com.example.ui.theme.TelemetryValueMedium
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SensorsTelemetryScreen(
    telemetryState: TelemetryState,
    onSelectChannel: (SensorChannel) -> Unit,
    onTogglePause: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeChannel = telemetryState.selectedChannel
    val activeSeries = telemetryState.seriesByChannel[activeChannel] ?: ChannelSeries(activeChannel)
    val telemetryColors = LocalTelemetryColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Channel Filter Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SensorChannel.entries.forEach { channel ->
                val series = telemetryState.seriesByChannel[channel]
                val latest = series?.latestValue
                val selected = channel == activeChannel
                FilterChip(
                    selected = selected,
                    onClick = { onSelectChannel(channel) },
                    leadingIcon = {
                        Icon(
                            imageVector = iconForChannel(channel),
                            contentDescription = null,
                            tint = if (selected) channel.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        val valStr = latest?.let { String.format(Locale.US, "%.1f%s", it, channel.unit) } ?: "--"
                        Text("${stringResource(channel.shortNameRes)} $valStr")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = channel.accentColor.copy(alpha = 0.18f),
                        selectedLabelColor = channel.accentColor
                    ),
                    modifier = Modifier.testTag("sensor_chip_${channel.name.lowercase()}")
                )
            }
        }

        // Main Live Chart Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(activeChannel.accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(activeChannel.titleRes),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeSeries.latestValue?.let {
                                String.format(Locale.US, "%.1f %s", it, activeChannel.unit)
                            } ?: "-- ${activeChannel.unit}",
                            style = TelemetryValueLarge,
                            color = activeChannel.accentColor
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onTogglePause,
                            modifier = Modifier.testTag("telemetry_pause_button")
                        ) {
                            Icon(
                                imageVector = if (telemetryState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = if (telemetryState.isPaused) {
                                    stringResource(R.string.sensors_resume)
                                } else {
                                    stringResource(R.string.sensors_pause)
                                },
                                tint = if (telemetryState.isPaused) telemetryColors.warning else MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onClearHistory,
                            modifier = Modifier.testTag("telemetry_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = stringResource(R.string.sensors_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (activeSeries.points.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.sensors_empty_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.sensors_empty_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    InteractiveLiveLineChart(
                        channel = activeChannel,
                        points = activeSeries.points,
                        gridColor = telemetryColors.chartGrid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .testTag("telemetry_live_chart")
                    )
                }

                // Min / Avg / Max / Packets Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        label = stringResource(R.string.stat_min),
                        value = activeSeries.minValue?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = stringResource(R.string.stat_avg),
                        value = activeSeries.avgValue?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = stringResource(R.string.stat_max),
                        value = activeSeries.maxValue?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = stringResource(R.string.stat_packets),
                        value = "${telemetryState.totalPackets}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // All Channels Sparkline Cards
        Text(
            text = stringResource(R.string.sensors_all_channels),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SensorChannel.entries.forEach { channel ->
                val series = telemetryState.seriesByChannel[channel] ?: ChannelSeries(channel)
                SensorSparklineRowCard(
                    channel = channel,
                    series = series,
                    isSelected = channel == activeChannel,
                    onClick = { onSelectChannel(channel) }
                )
            }
        }
    }
}

@Composable
private fun InteractiveLiveLineChart(
    channel: SensorChannel,
    points: List<SensorPoint>,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    var inspectedIndex by remember(points.size, channel) { mutableStateOf<Int?>(null) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(950),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconRadius"
    )

    val rawMin = points.minOf { it.value }
    val rawMax = points.maxOf { it.value }
    val padding = ((rawMax - rawMin) * 0.2f).coerceAtLeast(2f)
    val yMin = (rawMin - padding).coerceAtLeast(0f)
    val yMax = rawMax + padding
    val ySpan = (yMax - yMin).coerceAtLeast(1f)

    Column(modifier = modifier) {
        inspectedIndex?.let { idx ->
            val pt = points.getOrNull(idx)
            if (pt != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = channel.accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "${timeFormatter.format(Date(pt.timestampMs))}  •  ${
                            String.format(Locale.US, "%.2f %s", pt.value, channel.unit)
                        }",
                        style = TelemetryMonoSmall,
                        color = channel.accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            if (points.size >= 2 && size.width > 0) {
                                val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                                inspectedIndex = (ratio * (points.size - 1)).roundToInt()
                            }
                        }
                    }
                    .pointerInput(points) {
                        detectHorizontalDragGestures(
                            onDragEnd = { inspectedIndex = null },
                            onDragCancel = { inspectedIndex = null },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                if (points.size >= 2 && size.width > 0) {
                                    val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                                    inspectedIndex = (ratio * (points.size - 1)).roundToInt()
                                }
                            }
                        )
                    }
            ) {
                val leftPad = 6.dp.toPx()
                val rightPad = 10.dp.toPx()
                val topPad = 12.dp.toPx()
                val bottomPad = 12.dp.toPx()

                val chartW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                val chartH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                // Horizontal dashed grid lines
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                for (i in 0..4) {
                    val y = topPad + (chartH / 4f) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, y),
                        end = Offset(leftPad + chartW, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                if (points.size == 1) {
                    val normY = ((points[0].value - yMin) / ySpan).coerceIn(0f, 1f)
                    val pt = Offset(leftPad + chartW / 2f, topPad + chartH * (1f - normY))
                    drawCircle(color = channel.accentColor, radius = 6.dp.toPx(), center = pt)
                    return@Canvas
                }

                val coords = points.mapIndexed { idx, pt ->
                    val x = leftPad + (idx.toFloat() / (points.size - 1)) * chartW
                    val normY = ((pt.value - yMin) / ySpan).coerceIn(0f, 1f)
                    val y = topPad + chartH * (1f - normY)
                    Offset(x, y)
                }

                val strokePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 0 until coords.lastIndex) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(coords.last().x, topPad + chartH)
                    lineTo(coords.first().x, topPad + chartH)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            channel.accentColor.copy(alpha = 0.34f),
                            channel.accentColor.copy(alpha = 0.02f)
                        ),
                        startY = topPad,
                        endY = topPad + chartH
                    )
                )

                drawPath(
                    path = strokePath,
                    color = channel.accentColor,
                    style = Stroke(
                        width = 2.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Latest point pulsing beacon
                val lastPt = coords.last()
                drawCircle(
                    color = channel.accentColor.copy(alpha = 0.25f),
                    radius = 5.dp.toPx() * beaconRadiusFactor,
                    center = lastPt
                )
                drawCircle(
                    color = channel.accentColor,
                    radius = 4.5.dp.toPx(),
                    center = lastPt
                )

                // Touch inspection crosshair
                inspectedIndex?.let { idx ->
                    val target = coords.getOrNull(idx)
                    if (target != null) {
                        drawLine(
                            color = channel.accentColor.copy(alpha = 0.7f),
                            start = Offset(target.x, topPad),
                            end = Offset(target.x, topPad + chartH),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = target
                        )
                        drawCircle(
                            color = channel.accentColor,
                            radius = 4.dp.toPx(),
                            center = target
                        )
                    }
                }
            }

            // Min / Max Y-Axis Overlays
            Text(
                text = String.format(Locale.US, "%.1f", yMax),
                style = TelemetryMonoSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 2.dp)
            )
            Text(
                text = String.format(Locale.US, "%.1f", yMin),
                style = TelemetryMonoSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = TelemetryValueMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SensorSparklineRowCard(
    channel: SensorChannel,
    series: ChannelSeries,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            channel.accentColor.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isSelected) channel.accentColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(channel.accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForChannel(channel),
                        contentDescription = null,
                        tint = channel.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = stringResource(channel.titleRes), style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = series.latestValue?.let {
                            String.format(Locale.US, "%.1f %s", it, channel.unit)
                        } ?: stringResource(R.string.sensors_waiting_data),
                        style = TelemetryValueMedium,
                        color = if (series.latestValue != null) channel.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mini Sparkline Canvas
            Canvas(
                modifier = Modifier
                    .width(96.dp)
                    .height(34.dp)
            ) {
                val pts = series.points
                if (pts.size < 2) {
                    drawLine(
                        color = channel.accentColor.copy(alpha = 0.35f),
                        start = Offset(0f, size.height / 2f),
                        end = Offset(size.width, size.height / 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                    return@Canvas
                }
                val minV = pts.minOf { it.value }
                val maxV = pts.maxOf { it.value }
                val span = (maxV - minV).coerceAtLeast(1f)
                val path = Path()
                pts.forEachIndexed { idx, p ->
                    val x = (idx.toFloat() / (pts.size - 1)) * size.width
                    val y = size.height - ((p.value - minV) / span) * (size.height * 0.8f) - size.height * 0.1f
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = channel.accentColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

private fun iconForChannel(channel: SensorChannel): ImageVector = when (channel) {
    SensorChannel.TEMPERATURE -> Icons.Rounded.Thermostat
    SensorChannel.HUMIDITY -> Icons.Rounded.WaterDrop
    SensorChannel.DISTANCE -> Icons.Rounded.Radar
    SensorChannel.VOLTAGE -> Icons.Rounded.BatteryChargingFull
    SensorChannel.LIGHT -> Icons.Rounded.WbSunny
}
