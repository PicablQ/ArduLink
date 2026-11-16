package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CarControlMode
import com.example.model.CarDriveDirection
import com.example.model.CarState
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.TelemetryMonoSmall
import com.example.ui.theme.TelemetryValueMedium
import java.util.Locale
import kotlin.math.hypot

@Composable
fun CarControlScreen(
    carState: CarState,
    onSetControlMode: (CarControlMode) -> Unit,
    onTwoHandThrottle: (Int) -> Unit,
    onTwoHandSteer: (Int) -> Unit,
    onJoystickMove: (Float, Float) -> Unit,
    onDirectionPress: (CarDriveDirection) -> Unit,
    onSpeedPreview: (Int) -> Unit,
    onSpeedCommit: (Int) -> Unit,
    onEmergencyStop: () -> Unit,
    onToggleHeadlights: () -> Unit,
    onToggleHazards: () -> Unit,
    onHornChange: (Boolean) -> Unit,
    onToggleAutonomous: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscapeCockpit = maxWidth > maxHeight && maxWidth >= 560.dp

        if (isLandscapeCockpit) {
            LandscapeTwoHandCockpit(
                carState = carState,
                onSetControlMode = onSetControlMode,
                onTwoHandThrottle = onTwoHandThrottle,
                onTwoHandSteer = onTwoHandSteer,
                onJoystickMove = onJoystickMove,
                onDirectionPress = onDirectionPress,
                onSpeedPreview = onSpeedPreview,
                onSpeedCommit = onSpeedCommit,
                onEmergencyStop = onEmergencyStop,
                onToggleHeadlights = onToggleHeadlights,
                onToggleHazards = onToggleHazards,
                onHornChange = onHornChange,
                onToggleAutonomous = onToggleAutonomous
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 580.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoverTelemetryBanner(
                        carState = carState,
                        onSetControlMode = onSetControlMode
                    )
                    DriveControlDeck(
                        carState = carState,
                        onTwoHandThrottle = onTwoHandThrottle,
                        onTwoHandSteer = onTwoHandSteer,
                        onJoystickMove = onJoystickMove,
                        onDirectionPress = onDirectionPress
                    )
                    SpeedControlCard(
                        speedPwm = carState.speedPwm,
                        onSpeedPreview = onSpeedPreview,
                        onSpeedCommit = onSpeedCommit
                    )
                    AuxiliarySystemsCard(
                        carState = carState,
                        onToggleHeadlights = onToggleHeadlights,
                        onToggleHazards = onToggleHazards,
                        onHornChange = onHornChange,
                        onToggleAutonomous = onToggleAutonomous
                    )
                    EmergencyStopButton(onEmergencyStop = onEmergencyStop)
                }
            }
        }
    }
}

@Composable
private fun LandscapeTwoHandCockpit(
    carState: CarState,
    onSetControlMode: (CarControlMode) -> Unit,
    onTwoHandThrottle: (Int) -> Unit,
    onTwoHandSteer: (Int) -> Unit,
    onJoystickMove: (Float, Float) -> Unit,
    onDirectionPress: (CarDriveDirection) -> Unit,
    onSpeedPreview: (Int) -> Unit,
    onSpeedCommit: (Int) -> Unit,
    onEmergencyStop: () -> Unit,
    onToggleHeadlights: () -> Unit,
    onToggleHazards: () -> Unit,
    onHornChange: (Boolean) -> Unit,
    onToggleAutonomous: () -> Unit
) {
    val telemetryColors = LocalTelemetryColors.current
    val distCm = carState.ultrasonicCm
    val distColor = when {
        distCm == null -> MaterialTheme.colorScheme.onSurfaceVariant
        distCm < 20f -> MaterialTheme.colorScheme.error
        distCm < 45f -> telemetryColors.warning
        else -> telemetryColors.connected
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                when (carState.controlMode) {
                    CarControlMode.TWO_HAND -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.car_throttle_header),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ErgonomicHoldButton(
                                icon = Icons.Rounded.KeyboardArrowUp,
                                label = stringResource(R.string.car_label_forward),
                                isActive = carState.throttleAxis > 0,
                                activeColor = MaterialTheme.colorScheme.primary,
                                onPressChange = { pressed -> onTwoHandThrottle(if (pressed) 1 else 0) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("twohand_forward")
                            )
                            ErgonomicHoldButton(
                                icon = Icons.Rounded.KeyboardArrowDown,
                                label = stringResource(R.string.car_label_reverse),
                                isActive = carState.throttleAxis < 0,
                                activeColor = telemetryColors.warning,
                                onPressChange = { pressed -> onTwoHandThrottle(if (pressed) -1 else 0) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("twohand_backward")
                            )
                        }
                    }

                    CarControlMode.JOYSTICK -> {
                        VirtualJoystick(
                            x = carState.joystickX,
                            y = carState.joystickY,
                            onMove = onJoystickMove,
                            modifier = Modifier
                                .size(196.dp)
                                .testTag("car_virtual_joystick")
                        )
                    }

                    CarControlMode.DPAD -> {
                        PrecisionDPad(
                            activeDirection = carState.direction,
                            onDirectionPress = onDirectionPress,
                            buttonSize = 56.dp,
                            modifier = Modifier.testTag("car_dpad")
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .weight(1.25f)
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (carState.direction == CarDriveDirection.IDLE) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = "${stringResource(carState.direction.labelRes)} (${carState.direction.command})",
                            style = TelemetryMonoSmall,
                            color = if (carState.direction == CarDriveDirection.IDLE) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = distCm?.let { String.format(Locale.US, "%.0f cm", it) } ?: "-- cm",
                            style = TelemetryMonoSmall,
                            color = distColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = carState.batteryVoltage?.let { String.format(Locale.US, "%.1fV", it) } ?: "--V",
                            style = TelemetryMonoSmall,
                            color = telemetryColors.warning
                        )
                    }
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CarControlMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = carState.controlMode == mode,
                            onClick = { onSetControlMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = CarControlMode.entries.size
                            )
                        ) {
                            Text(
                                text = stringResource(mode.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PWM ${carState.speedPwm}",
                        style = TelemetryMonoSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = carState.speedPwm.toFloat(),
                        onValueChange = { onSpeedPreview(it.toInt()) },
                        onValueChangeFinished = { onSpeedCommit(carState.speedPwm) },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AuxCompactChip(
                        icon = Icons.Rounded.Highlight,
                        label = stringResource(R.string.car_headlights),
                        isActive = carState.headlightsOn,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = onToggleHeadlights,
                        modifier = Modifier.weight(1f)
                    )
                    AuxCompactChip(
                        icon = Icons.Rounded.WarningAmber,
                        label = stringResource(R.string.car_hazards),
                        isActive = carState.hazardsOn,
                        activeColor = telemetryColors.warning,
                        onClick = onToggleHazards,
                        modifier = Modifier.weight(1f)
                    )
                    AuxCompactChip(
                        icon = Icons.Rounded.AutoAwesome,
                        label = stringResource(R.string.car_auto_mode),
                        isActive = carState.autonomousMode,
                        activeColor = telemetryColors.connected,
                        onClick = onToggleAutonomous,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (carState.hornActive) telemetryColors.warningContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(0.45f)
                            .height(48.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        onHornChange(true)
                                        tryAwaitRelease()
                                        onHornChange(false)
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.Campaign,
                                contentDescription = stringResource(R.string.car_horn),
                                tint = if (carState.hornActive) telemetryColors.warning else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.car_horn),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Button(
                        onClick = onEmergencyStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(0.55f)
                            .height(48.dp)
                            .testTag("emergency_stop_button")
                    ) {
                        Icon(Icons.Rounded.Dangerous, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.car_emergency_stop),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.car_steering_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ErgonomicHoldButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        label = stringResource(R.string.car_label_left),
                        isActive = carState.steerAxis < 0 || carState.direction == CarDriveDirection.LEFT,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onPressChange = { pressed -> onTwoHandSteer(if (pressed) -1 else 0) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("twohand_left")
                    )
                    ErgonomicHoldButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        label = stringResource(R.string.car_label_right),
                        isActive = carState.steerAxis > 0 || carState.direction == CarDriveDirection.RIGHT,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onPressChange = { pressed -> onTwoHandSteer(if (pressed) 1 else 0) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("twohand_right")
                    )
                }
            }
        }
    }
}

@Composable
private fun AuxCompactChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isActive) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RoverTelemetryBanner(
    carState: CarState,
    onSetControlMode: (CarControlMode) -> Unit
) {
    val telemetryColors = LocalTelemetryColors.current
    val distCm = carState.ultrasonicCm
    val distColor = when {
        distCm == null -> MaterialTheme.colorScheme.onSurfaceVariant
        distCm < 20f -> MaterialTheme.colorScheme.error
        distCm < 45f -> telemetryColors.warning
        else -> telemetryColors.connected
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_rover_blueprint),
                contentDescription = stringResource(R.string.car_blueprint_cd),
                contentScale = ContentScale.Crop,
                alpha = if (telemetryColors.isDark) 0.28f else 0.14f,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.car_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (carState.direction == CarDriveDirection.IDLE) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                Text(
                                    text = "${stringResource(carState.direction.labelRes)} (${carState.direction.command})",
                                    style = TelemetryMonoSmall,
                                    color = if (carState.direction == CarDriveDirection.IDLE) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (carState.autonomousMode) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = telemetryColors.connectedContainer
                                ) {
                                    Text(
                                        text = stringResource(R.string.car_auto_badge),
                                        style = TelemetryMonoSmall,
                                        color = telemetryColors.connected,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Radar,
                                contentDescription = stringResource(R.string.car_distance_label),
                                tint = distColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (distCm != null) String.format(Locale.US, "%.0f cm", distCm) else "-- cm",
                                style = TelemetryValueMedium,
                                color = distColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = stringResource(R.string.car_battery_cd),
                                tint = telemetryColors.warning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = carState.batteryVoltage?.let {
                                    String.format(Locale.US, "%.1f V", it)
                                } ?: "-- V",
                                style = TelemetryMonoSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CarControlMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = carState.controlMode == mode,
                            onClick = { onSetControlMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = CarControlMode.entries.size
                            ),
                            icon = {
                                Icon(
                                    imageVector = when (mode) {
                                        CarControlMode.TWO_HAND -> Icons.Rounded.Swipe
                                        CarControlMode.JOYSTICK -> Icons.Rounded.SportsEsports
                                        CarControlMode.DPAD -> Icons.Rounded.Gamepad
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("car_mode_${mode.name.lowercase()}")
                        ) {
                            Text(stringResource(mode.labelRes), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveControlDeck(
    carState: CarState,
    onTwoHandThrottle: (Int) -> Unit,
    onTwoHandSteer: (Int) -> Unit,
    onJoystickMove: (Float, Float) -> Unit,
    onDirectionPress: (CarDriveDirection) -> Unit
) {
    val telemetryColors = LocalTelemetryColors.current
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (carState.controlMode) {
                CarControlMode.TWO_HAND -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.car_twohand_hint),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.car_landscape_hint),
                                style = TelemetryMonoSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(188.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ErgonomicHoldButton(
                                    icon = Icons.Rounded.KeyboardArrowUp,
                                    label = stringResource(R.string.car_label_forward),
                                    isActive = carState.throttleAxis > 0,
                                    activeColor = MaterialTheme.colorScheme.primary,
                                    onPressChange = { p -> onTwoHandThrottle(if (p) 1 else 0) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .testTag("twohand_forward")
                                )
                                ErgonomicHoldButton(
                                    icon = Icons.Rounded.KeyboardArrowDown,
                                    label = stringResource(R.string.car_label_reverse),
                                    isActive = carState.throttleAxis < 0,
                                    activeColor = telemetryColors.warning,
                                    onPressChange = { p -> onTwoHandThrottle(if (p) -1 else 0) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .testTag("twohand_backward")
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .weight(0.55f)
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ErgonomicHoldButton(
                                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                    label = stringResource(R.string.car_label_left),
                                    isActive = carState.steerAxis < 0,
                                    activeColor = MaterialTheme.colorScheme.primary,
                                    onPressChange = { p -> onTwoHandSteer(if (p) -1 else 0) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(118.dp)
                                        .testTag("twohand_left")
                                )
                                ErgonomicHoldButton(
                                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    label = stringResource(R.string.car_label_right),
                                    isActive = carState.steerAxis > 0,
                                    activeColor = MaterialTheme.colorScheme.primary,
                                    onPressChange = { p -> onTwoHandSteer(if (p) 1 else 0) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(118.dp)
                                        .testTag("twohand_right")
                                )
                            }
                        }
                    }
                }

                CarControlMode.JOYSTICK -> {
                    VirtualJoystick(
                        x = carState.joystickX,
                        y = carState.joystickY,
                        onMove = onJoystickMove,
                        modifier = Modifier
                            .size(228.dp)
                            .testTag("car_virtual_joystick")
                    )
                }

                CarControlMode.DPAD -> {
                    PrecisionDPad(
                        activeDirection = carState.direction,
                        onDirectionPress = onDirectionPress,
                        modifier = Modifier.testTag("car_dpad")
                    )
                }
            }
        }
    }
}

@Composable
private fun ErgonomicHoldButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isActive) 0.95f else 1f, label = "ergoScale")
    val bg by animateColorAsState(
        if (isActive) activeColor else MaterialTheme.colorScheme.surfaceVariant,
        label = "ergoBg"
    )
    val fg by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "ergoFg"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                1.5.dp,
                if (isActive) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fg,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VirtualJoystick(
    x: Float,
    y: Float,
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val outline = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    var boxSizePx by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    val animX by animateFloatAsState(
        targetValue = if (isDragging) dragOffset.x else x,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "joyX"
    )
    val animY by animateFloatAsState(
        targetValue = if (isDragging) dragOffset.y else y,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "joyY"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onSizeChanged { boxSizePx = minOf(it.width, it.height) }
            .pointerInput(boxSizePx) {
                if (boxSizePx <= 0) return@pointerInput
                val maxRadius = boxSizePx * 0.34f
                val center = Offset(boxSizePx / 2f, boxSizePx / 2f)

                detectDragGestures(
                    onDragStart = { start ->
                        isDragging = true
                        val delta = start - center
                        val dist = hypot(delta.x, delta.y)
                        val clamped = if (dist > maxRadius) delta * (maxRadius / dist) else delta
                        val nx = (clamped.x / maxRadius).coerceIn(-1f, 1f)
                        val ny = (clamped.y / maxRadius).coerceIn(-1f, 1f)
                        dragOffset = Offset(nx, ny)
                        onMove(nx, ny)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val delta = change.position - center
                        val dist = hypot(delta.x, delta.y)
                        val clamped = if (dist > maxRadius) delta * (maxRadius / dist) else delta
                        val nx = (clamped.x / maxRadius).coerceIn(-1f, 1f)
                        val ny = (clamped.y / maxRadius).coerceIn(-1f, 1f)
                        dragOffset = Offset(nx, ny)
                        onMove(nx, ny)
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension * 0.46f
            val maxThumbTravel = size.minDimension * 0.34f

            drawCircle(color = surfaceVariant, radius = outerRadius, center = center)
            drawCircle(
                color = outline.copy(alpha = 0.7f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = outline.copy(alpha = 0.35f),
                radius = outerRadius * 0.65f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = outline.copy(alpha = 0.25f),
                radius = outerRadius * 0.32f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawLine(
                color = outline.copy(alpha = 0.3f),
                start = Offset(center.x - outerRadius, center.y),
                end = Offset(center.x + outerRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = outline.copy(alpha = 0.3f),
                start = Offset(center.x, center.y - outerRadius),
                end = Offset(center.x, center.y + outerRadius),
                strokeWidth = 1.dp.toPx()
            )

            val thumbCenter = Offset(
                center.x + animX * maxThumbTravel,
                center.y + animY * maxThumbTravel
            )
            if (hypot(animX, animY) > 0.05f) {
                drawLine(
                    color = primary.copy(alpha = 0.65f),
                    start = center,
                    end = thumbCenter,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            val thumbRadius = size.minDimension * 0.145f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.45f), Color.Transparent),
                    center = thumbCenter,
                    radius = thumbRadius * 1.65f
                ),
                radius = thumbRadius * 1.65f,
                center = thumbCenter
            )
            drawCircle(color = primaryContainer, radius = thumbRadius, center = thumbCenter)
            drawCircle(
                color = primary,
                radius = thumbRadius,
                center = thumbCenter,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(color = primary, radius = thumbRadius * 0.35f, center = thumbCenter)
        }
    }
}

@Composable
private fun PrecisionDPad(
    activeDirection: CarDriveDirection,
    onDirectionPress: (CarDriveDirection) -> Unit,
    buttonSize: Dp = 70.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DPadButton(
            icon = Icons.Rounded.KeyboardArrowUp,
            label = stringResource(R.string.car_label_forward),
            isActive = activeDirection == CarDriveDirection.FORWARD,
            buttonSize = buttonSize,
            onPressChange = { pressed ->
                onDirectionPress(if (pressed) CarDriveDirection.FORWARD else CarDriveDirection.IDLE)
            },
            modifier = Modifier.testTag("dpad_forward")
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DPadButton(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                label = stringResource(R.string.car_label_left),
                isActive = activeDirection == CarDriveDirection.LEFT,
                buttonSize = buttonSize,
                onPressChange = { pressed ->
                    onDirectionPress(if (pressed) CarDriveDirection.LEFT else CarDriveDirection.IDLE)
                },
                modifier = Modifier.testTag("dpad_left")
            )
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onDirectionPress(CarDriveDirection.IDLE) }
                    .testTag("dpad_center_stop"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.car_label_stop),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DPadButton(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                label = stringResource(R.string.car_label_right),
                isActive = activeDirection == CarDriveDirection.RIGHT,
                buttonSize = buttonSize,
                onPressChange = { pressed ->
                    onDirectionPress(if (pressed) CarDriveDirection.RIGHT else CarDriveDirection.IDLE)
                },
                modifier = Modifier.testTag("dpad_right")
            )
        }
        DPadButton(
            icon = Icons.Rounded.KeyboardArrowDown,
            label = stringResource(R.string.car_label_reverse),
            isActive = activeDirection == CarDriveDirection.BACKWARD,
            buttonSize = buttonSize,
            onPressChange = { pressed ->
                onDirectionPress(if (pressed) CarDriveDirection.BACKWARD else CarDriveDirection.IDLE)
            },
            modifier = Modifier.testTag("dpad_backward")
        )
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    buttonSize: Dp,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isActive) 0.93f else 1f, label = "dpadScale")
    val bg by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "dpadBg"
    )
    val fg by animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "dpadFg"
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(
                1.5.dp,
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(18.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        tryAwaitRelease()
                        onPressChange(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = fg,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun SpeedControlCard(
    speedPwm: Int,
    onSpeedPreview: (Int) -> Unit,
    onSpeedCommit: (Int) -> Unit
) {
    val percent = ((speedPwm / 255f) * 100).toInt()
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.car_speed_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "$percent% • $speedPwm",
                    style = TelemetryValueMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = speedPwm.toFloat(),
                onValueChange = { onSpeedPreview(it.toInt()) },
                onValueChangeFinished = { onSpeedCommit(speedPwm) },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("car_speed_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    stringResource(R.string.car_speed_slow) to 110,
                    stringResource(R.string.car_speed_med) to 185,
                    stringResource(R.string.car_speed_turbo) to 255
                ).forEach { (title, pwm) ->
                    FilterChip(
                        selected = speedPwm == pwm,
                        onClick = { onSpeedCommit(pwm) },
                        label = { Text("$title ($pwm)", maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuxiliarySystemsCard(
    carState: CarState,
    onToggleHeadlights: () -> Unit,
    onToggleHazards: () -> Unit,
    onHornChange: (Boolean) -> Unit,
    onToggleAutonomous: () -> Unit
) {
    val telemetryColors = LocalTelemetryColors.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.car_aux_header),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AuxToggleTile(
                    icon = Icons.Rounded.Highlight,
                    label = stringResource(R.string.car_headlights),
                    isActive = carState.headlightsOn,
                    activeColor = MaterialTheme.colorScheme.primary,
                    onClick = onToggleHeadlights,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("car_headlights_button")
                )
                AuxToggleTile(
                    icon = Icons.Rounded.WarningAmber,
                    label = stringResource(R.string.car_hazards),
                    isActive = carState.hazardsOn,
                    activeColor = telemetryColors.warning,
                    onClick = onToggleHazards,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("car_hazards_button")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val hornBg by animateColorAsState(
                    if (carState.hornActive) telemetryColors.warningContainer else MaterialTheme.colorScheme.surfaceVariant,
                    label = "hornBg"
                )
                val hornFg by animateColorAsState(
                    if (carState.hornActive) telemetryColors.warning else MaterialTheme.colorScheme.onSurface,
                    label = "hornFg"
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = hornBg,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .border(
                            1.dp,
                            if (carState.hornActive) telemetryColors.warning else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(14.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onHornChange(true)
                                    tryAwaitRelease()
                                    onHornChange(false)
                                }
                            )
                        }
                        .testTag("car_horn_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.Campaign, contentDescription = null, tint = hornFg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.car_horn),
                            style = MaterialTheme.typography.labelLarge,
                            color = hornFg
                        )
                    }
                }

                AuxToggleTile(
                    icon = Icons.Rounded.AutoAwesome,
                    label = stringResource(R.string.car_auto_mode),
                    isActive = carState.autonomousMode,
                    activeColor = telemetryColors.connected,
                    onClick = onToggleAutonomous,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("car_auto_button")
                )
            }
        }
    }
}

@Composable
private fun AuxToggleTile(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        if (isActive) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "auxBg"
    )
    val fg by animateColorAsState(
        if (isActive) activeColor else MaterialTheme.colorScheme.onSurface,
        label = "auxFg"
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmergencyStopButton(onEmergencyStop: () -> Unit) {
    Button(
        onClick = onEmergencyStop,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emergency_stop_button")
    ) {
        Icon(
            imageVector = Icons.Rounded.Dangerous,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.car_emergency_stop),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
