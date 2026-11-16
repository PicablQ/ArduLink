package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ChartHumidityColor
import com.example.ui.theme.ChartLightColor
import com.example.ui.theme.ChartTempColor
import com.example.ui.theme.ChartVoltageColor
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.TelemetryMonoSmall
import kotlin.math.abs
import kotlin.math.hypot

@Composable
fun GamepadScreen(
    lastTx: String,
    onSendCommand: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var sendStopOnRelease by remember { mutableStateOf(true) }
    var activeButton by remember { mutableStateOf<String?>(null) }
    var stickDir by remember { mutableStateOf("S") }
    val telemetryColors = LocalTelemetryColors.current

    fun press(code: String) {
        activeButton = code
        onSendCommand(code, true)
    }

    fun release(code: String) {
        if (activeButton == code) activeButton = null
        if (sendStopOnRelease) {
            onSendCommand("${code}_REL", false)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        val isWide = maxWidth >= 620.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.SportsEsports,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.gamepad_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.gamepad_stop_on_release),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = sendStopOnRelease,
                                onCheckedChange = { sendStopOnRelease = it }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShoulderButton(
                            label = "L2",
                            isActive = activeButton == "L2",
                            onPress = { press("L2") },
                            onRelease = { release("L2") },
                            modifier = Modifier.weight(1f)
                        )
                        ShoulderButton(
                            label = "L1",
                            isActive = activeButton == "L1",
                            onPress = { press("L1") },
                            onRelease = { release("L1") },
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.gamepad_last_tx),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = lastTx,
                                    style = TelemetryMonoSmall,
                                    color = telemetryColors.connected,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        ShoulderButton(
                            label = "R1",
                            isActive = activeButton == "R1",
                            onPress = { press("R1") },
                            onRelease = { release("R1") },
                            modifier = Modifier.weight(1f)
                        )
                        ShoulderButton(
                            label = "R2",
                            isActive = activeButton == "R2",
                            onPress = { press("R2") },
                            onRelease = { release("R2") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GamepadDPadCluster(
                            activeCode = activeButton,
                            onPress = { press(it) },
                            onRelease = {
                                activeButton = null
                                if (sendStopOnRelease) onSendCommand("S", false)
                            }
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                PillMetaButton(label = "SELECT", onClick = { press("SEL") })
                                PillMetaButton(label = "START", onClick = { press("STR") })
                            }
                            MiniAnalogStick(
                                onDirectionChange = { dir ->
                                    if (dir != stickDir) {
                                        stickDir = dir
                                        onSendCommand(dir, dir != "S")
                                    }
                                },
                                modifier = Modifier.size(116.dp)
                            )
                        }

                        GamepadABXYCluster(
                            activeCode = activeButton,
                            onPress = { press(it) },
                            onRelease = { release(it) }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GamepadDPadCluster(
                                activeCode = activeButton,
                                onPress = { press(it) },
                                onRelease = {
                                    activeButton = null
                                    if (sendStopOnRelease) onSendCommand("S", false)
                                }
                            )

                            GamepadABXYCluster(
                                activeCode = activeButton,
                                onPress = { press(it) },
                                onRelease = { release(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PillMetaButton(label = "SELECT", onClick = { press("SEL") })
                            MiniAnalogStick(
                                onDirectionChange = { dir ->
                                    if (dir != stickDir) {
                                        stickDir = dir
                                        onSendCommand(dir, dir != "S")
                                    }
                                },
                                modifier = Modifier.size(112.dp)
                            )
                            PillMetaButton(label = "START", onClick = { press("STR") })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoulderButton(
    label: String,
    isActive: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "shBg"
    )
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            }
            .testTag("gamepad_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GamepadDPadCluster(
    activeCode: String?,
    onPress: (String) -> Unit,
    onRelease: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GamepadDirKey(
            icon = Icons.Rounded.KeyboardArrowUp,
            code = "F",
            isActive = activeCode == "F",
            onPress = { onPress("F") },
            onRelease = onRelease
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GamepadDirKey(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                code = "L",
                isActive = activeCode == "L",
                onPress = { onPress("L") },
                onRelease = onRelease
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            )
            GamepadDirKey(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                code = "R",
                isActive = activeCode == "R",
                onPress = { onPress("R") },
                onRelease = onRelease
            )
        }
        GamepadDirKey(
            icon = Icons.Rounded.KeyboardArrowDown,
            code = "B",
            isActive = activeCode == "B",
            onPress = { onPress("B") },
            onRelease = onRelease
        )
    }
}

@Composable
private fun GamepadDirKey(
    icon: ImageVector,
    code: String,
    isActive: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 0.9f else 1f, label = "gpadDirScale")
    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            }
            .testTag("gamepad_dir_$code"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = code,
            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GamepadABXYCluster(
    activeCode: String?,
    onPress: (String) -> Unit,
    onRelease: (String) -> Unit
) {
    val telemetryColors = LocalTelemetryColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ActionFaceButton(
            label = "Y",
            accent = ChartVoltageColor,
            isActive = activeCode == "Y",
            onPress = { onPress("Y") },
            onRelease = { onRelease("Y") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ActionFaceButton(
                label = "X",
                accent = ChartHumidityColor,
                isActive = activeCode == "X",
                onPress = { onPress("X") },
                onRelease = { onRelease("X") }
            )
            ActionFaceButton(
                label = "B",
                accent = ChartTempColor,
                isActive = activeCode == "B",
                onPress = { onPress("B") },
                onRelease = { onRelease("B") }
            )
        }
        ActionFaceButton(
            label = "A",
            accent = telemetryColors.connected,
            isActive = activeCode == "A",
            onPress = { onPress("A") },
            onRelease = { onRelease("A") }
        )
    }
}

@Composable
private fun ActionFaceButton(
    label: String,
    accent: Color,
    isActive: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 0.9f else 1f, label = "faceScale")
    Box(
        modifier = Modifier
            .size(50.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isActive) accent else accent.copy(alpha = 0.16f))
            .border(1.5.dp, accent.copy(alpha = 0.8f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            }
            .testTag("gamepad_face_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.surface else accent
        )
    }
}

@Composable
private fun PillMetaButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("gamepad_$label")
    ) {
        Text(
            text = label,
            style = TelemetryMonoSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun MiniAnalogStick(
    onDirectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = ChartLightColor
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    var boxPx by remember { mutableStateOf(0) }
    var thumb by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { boxPx = minOf(it.width, it.height) }
            .pointerInput(boxPx) {
                if (boxPx <= 0) return@pointerInput
                val radius = boxPx * 0.34f
                val center = Offset(boxPx / 2f, boxPx / 2f)
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        val delta = change.position - center
                        val dist = hypot(delta.x, delta.y)
                        val clamped = if (dist > radius) delta * (radius / dist) else delta
                        val nx = clamped.x / radius
                        val ny = clamped.y / radius
                        thumb = Offset(nx, ny)
                        val dir = when {
                            abs(nx) < 0.3f && abs(ny) < 0.3f -> "S"
                            abs(ny) >= abs(nx) -> if (ny < 0) "F" else "B"
                            else -> if (nx < 0) "L" else "R"
                        }
                        onDirectionChange(dir)
                    },
                    onDragEnd = {
                        thumb = Offset.Zero
                        onDirectionChange("S")
                    },
                    onDragCancel = {
                        thumb = Offset.Zero
                        onDirectionChange("S")
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outer = size.minDimension * 0.45f
            val travel = size.minDimension * 0.32f
            drawCircle(color = surfaceVariant, radius = outer, center = center)
            drawCircle(
                color = outline.copy(alpha = 0.6f),
                radius = outer,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            val pos = Offset(center.x + thumb.x * travel, center.y + thumb.y * travel)
            drawLine(
                color = primary.copy(alpha = 0.5f),
                start = center,
                end = pos,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = primary.copy(alpha = 0.25f), radius = outer * 0.45f, center = pos)
            drawCircle(
                color = primary,
                radius = outer * 0.42f,
                center = pos,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
