package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ConnectionState
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.ThemeMode

@Composable
fun TopStatusBar(
    connectionState: ConnectionState,
    themeMode: ThemeMode,
    onOpenBluetoothSheet: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetryColors = LocalTelemetryColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (pillBg, pillContent, pillBorder, statusText, statusIcon) = when (connectionState) {
        is ConnectionState.Connected -> StatusVisuals(
            bg = telemetryColors.connectedContainer,
            fg = telemetryColors.connected,
            border = telemetryColors.connected.copy(alpha = 0.5f),
            text = connectionState.device.name,
            icon = Icons.Rounded.BluetoothConnected
        )

        is ConnectionState.Connecting -> StatusVisuals(
            bg = MaterialTheme.colorScheme.primaryContainer,
            fg = MaterialTheme.colorScheme.primary,
            border = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            text = stringResource(R.string.status_connecting),
            icon = Icons.Rounded.Bluetooth
        )

        is ConnectionState.SimulationMode -> StatusVisuals(
            bg = telemetryColors.warningContainer,
            fg = telemetryColors.warning,
            border = telemetryColors.warning.copy(alpha = 0.7f),
            text = stringResource(R.string.status_simulation),
            icon = Icons.Rounded.Science
        )

        is ConnectionState.Error -> StatusVisuals(
            bg = MaterialTheme.colorScheme.errorContainer,
            fg = MaterialTheme.colorScheme.error,
            border = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
            text = stringResource(R.string.status_error_connect),
            icon = Icons.Rounded.BluetoothDisabled
        )

        ConnectionState.Disconnected -> StatusVisuals(
            bg = MaterialTheme.colorScheme.surfaceVariant,
            fg = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            text = stringResource(R.string.status_disconnected),
            icon = Icons.Rounded.Bluetooth
        )
    }

    val animatedBg by animateColorAsState(pillBg, label = "pillBg")
    val animatedFg by animateColorAsState(pillContent, label = "pillFg")

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.app_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .testTag("bt_connection_pill")
                        .clip(RoundedCornerShape(50))
                        .background(animatedBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(50))
                        .clickable(onClick = onOpenBluetoothSheet)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val isLive = connectionState is ConnectionState.Connected ||
                        connectionState is ConnectionState.Connecting ||
                        connectionState is ConnectionState.SimulationMode
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(if (isLive) pulseAlpha else 1f)
                            .clip(CircleShape)
                            .background(animatedFg)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = stringResource(R.string.cd_bluetooth_status),
                        tint = animatedFg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = animatedFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (telemetryColors.isDark) {
                            Icons.Rounded.LightMode
                        } else {
                            Icons.Rounded.DarkMode
                        },
                        contentDescription = stringResource(
                            R.string.cd_toggle_theme,
                            stringResource(themeMode.labelRes)
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class StatusVisuals(
    val bg: androidx.compose.ui.graphics.Color,
    val fg: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
