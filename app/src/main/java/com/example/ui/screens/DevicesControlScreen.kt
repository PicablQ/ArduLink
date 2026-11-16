package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CircuitDevice
import com.example.model.DeviceControlType
import com.example.model.DeviceIconType
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.TelemetryMonoSmall
import com.example.ui.theme.TelemetryValueMedium

@Composable
fun DevicesControlScreen(
    devices: List<CircuitDevice>,
    onToggleDevice: (String) -> Unit,
    onTriggerMomentary: (String) -> Unit,
    onSliderPreview: (String, Int) -> Unit,
    onSliderCommit: (String, Int) -> Unit,
    onAllOff: () -> Unit,
    onAddDevice: (
        name: String,
        subtitle: String,
        pinLabel: String,
        type: DeviceControlType,
        iconType: DeviceIconType,
        commandOn: String,
        commandOff: String,
        sliderMin: Int,
        sliderMax: Int
    ) -> Unit,
    onDeleteDevice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val activeCount = devices.count { it.type == DeviceControlType.TOGGLE && it.isOn }
    val telemetryColors = LocalTelemetryColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.devices_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.devices_active_count, activeCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activeCount > 0) telemetryColors.connected else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAllOff,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("devices_all_off_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.devices_all_off),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_device_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_add))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = devices,
                key = { it.id },
                span = { item ->
                    if (item.type == DeviceControlType.SLIDER) GridItemSpan(maxLineSpan) else GridItemSpan(1)
                }
            ) { device ->
                when (device.type) {
                    DeviceControlType.TOGGLE -> ToggleDeviceCard(
                        device = device,
                        onToggle = { onToggleDevice(device.id) },
                        onDelete = { onDeleteDevice(device.id) }
                    )

                    DeviceControlType.MOMENTARY -> MomentaryDeviceCard(
                        device = device,
                        onTrigger = { onTriggerMomentary(device.id) },
                        onDelete = { onDeleteDevice(device.id) }
                    )

                    DeviceControlType.SLIDER -> SliderDeviceCard(
                        device = device,
                        onValuePreview = { v -> onSliderPreview(device.id, v) },
                        onValueCommit = { v -> onSliderCommit(device.id, v) },
                        onDelete = { onDeleteDevice(device.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomDeviceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, subtitle, pin, type, icon, cmdOn, cmdOff, min, max ->
                onAddDevice(name, subtitle, pin, type, icon, cmdOn, cmdOff, min, max)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ToggleDeviceCard(
    device: CircuitDevice,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val telemetryColors = LocalTelemetryColors.current
    val bg by animateColorAsState(
        if (device.isOn) telemetryColors.connectedContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
        label = "devCardBg"
    )
    val borderColor by animateColorAsState(
        if (device.isOn) telemetryColors.connected.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        label = "devCardBorder"
    )
    val iconColor = if (device.isOn) telemetryColors.connected else MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        tonalElevation = if (device.isOn) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onToggle)
            .testTag("device_toggle_${device.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (device.isOn) telemetryColors.connected.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForDevice(device.iconType),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Switch(
                    checked = device.isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = telemetryColors.connected
                    )
                )
            }

            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${device.pinLabel} • ${if (device.isOn) device.commandOn else device.commandOff}",
                        style = TelemetryMonoSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (device.id.startsWith("custom_")) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentaryDeviceCard(
    device: CircuitDevice,
    onTrigger: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForDevice(device.iconType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (device.id.startsWith("custom_")) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = device.pinLabel,
                            style = TelemetryMonoSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = device.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FilledTonalButton(
                onClick = onTrigger,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_trigger_${device.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.devices_trigger, device.commandOn),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SliderDeviceCard(
    device: CircuitDevice,
    onValuePreview: (Int) -> Unit,
    onValueCommit: (Int) -> Unit,
    onDelete: () -> Unit
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForDevice(device.iconType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${device.pinLabel} • ${device.sliderPrefix}${device.sliderValue}",
                            style = TelemetryMonoSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (device.sliderMax == 180) "${device.sliderValue}°" else "${device.sliderValue}",
                        style = TelemetryValueMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (device.id.startsWith("custom_")) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Slider(
                value = device.sliderValue.toFloat(),
                onValueChange = { onValuePreview(it.toInt()) },
                onValueChangeFinished = { onValueCommit(device.sliderValue) },
                valueRange = device.sliderMin.toFloat()..device.sliderMax.toFloat(),
                modifier = Modifier.testTag("device_slider_${device.id}")
            )
        }
    }
}

@Composable
private fun AddCustomDeviceDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        subtitle: String,
        pinLabel: String,
        type: DeviceControlType,
        iconType: DeviceIconType,
        commandOn: String,
        commandOff: String,
        sliderMin: Int,
        sliderMax: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var pinLabel by remember { mutableStateOf("D5") }
    var type by remember { mutableStateOf(DeviceControlType.TOGGLE) }
    var iconType by remember { mutableStateOf(DeviceIconType.RELAY) }
    var commandOn by remember { mutableStateOf("R2_ON") }
    var commandOff by remember { mutableStateOf("R2_OFF") }
    var sliderMax by remember { mutableStateOf("180") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.devices_add_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.devices_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pinLabel,
                        onValueChange = { pinLabel = it },
                        label = { Text(stringResource(R.string.devices_pin_hint)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.4f)
                    )
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text(stringResource(R.string.devices_desc_hint)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                Text(
                    text = stringResource(R.string.devices_type_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DeviceControlType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(stringResource(t.labelRes)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = commandOn,
                    onValueChange = { commandOn = it },
                    label = {
                        Text(
                            stringResource(
                                when (type) {
                                    DeviceControlType.TOGGLE -> R.string.devices_cmd_on_hint
                                    DeviceControlType.MOMENTARY -> R.string.devices_cmd_trig_hint
                                    DeviceControlType.SLIDER -> R.string.devices_cmd_prefix_hint
                                }
                            )
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == DeviceControlType.TOGGLE) {
                    OutlinedTextField(
                        value = commandOff,
                        onValueChange = { commandOff = it },
                        label = { Text(stringResource(R.string.devices_cmd_off_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (type == DeviceControlType.SLIDER) {
                    OutlinedTextField(
                        value = sliderMax,
                        onValueChange = { sliderMax = it.filter { ch -> ch.isDigit() } },
                        label = { Text(stringResource(R.string.devices_max_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    text = stringResource(R.string.devices_icon_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        DeviceIconType.RELAY,
                        DeviceIconType.LIGHT,
                        DeviceIconType.FAN,
                        DeviceIconType.SERVO,
                        DeviceIconType.PUMP
                    ).forEach { ic ->
                        FilterChip(
                            selected = iconType == ic,
                            onClick = { iconType = ic },
                            label = {
                                Icon(
                                    imageVector = iconForDevice(ic),
                                    contentDescription = stringResource(ic.labelRes),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        subtitle,
                        pinLabel,
                        type,
                        iconType,
                        commandOn,
                        commandOff,
                        0,
                        sliderMax.toIntOrNull() ?: 180
                    )
                },
                modifier = Modifier.testTag("confirm_add_device_button")
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private fun iconForDevice(type: DeviceIconType): ImageVector = when (type) {
    DeviceIconType.RELAY -> Icons.Rounded.Bolt
    DeviceIconType.LIGHT -> Icons.Rounded.Lightbulb
    DeviceIconType.FAN -> Icons.Rounded.Air
    DeviceIconType.SERVO -> Icons.Rounded.PrecisionManufacturing
    DeviceIconType.PUMP -> Icons.Rounded.WaterDrop
    DeviceIconType.BUZZER -> Icons.Rounded.NotificationsActive
    DeviceIconType.LOCK -> Icons.Rounded.LockOpen
    DeviceIconType.CUSTOM -> Icons.Rounded.DeveloperBoard
}
