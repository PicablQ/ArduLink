package com.example.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.TelemetryMonoSmall
import com.example.ui.theme.TelemetryValueLarge

private enum class KeypadMode(@StringRes val labelRes: Int) {
    DIRECT(R.string.keypad_mode_direct),
    BUFFER(R.string.keypad_mode_buffer)
}

private val keypadMatrix = listOf(
    listOf("1", "2", "3", "A"),
    listOf("4", "5", "6", "B"),
    listOf("7", "8", "9", "C"),
    listOf("*", "0", "#", "D")
)

@Composable
fun Keypad4x4Screen(
    onSendKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(KeypadMode.DIRECT) }
    var buffer by remember { mutableStateOf("") }
    var lastTx by remember { mutableStateOf("--") }
    val telemetryColors = LocalTelemetryColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LCD Display Header
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Dialpad,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.keypad_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = stringResource(R.string.keypad_last_tx, lastTx),
                            style = TelemetryMonoSmall,
                            color = telemetryColors.connected
                        )
                    }

                    // LCD Readout Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (mode == KeypadMode.BUFFER) {
                                    buffer.ifEmpty { "_ _ _ _" }
                                } else {
                                    if (lastTx == "--") {
                                        stringResource(R.string.keypad_direct_hint)
                                    } else {
                                        stringResource(R.string.keypad_direct_sent, lastTx)
                                    }
                                },
                                style = if (mode == KeypadMode.BUFFER) TelemetryValueLarge else MaterialTheme.typography.bodyMedium,
                                color = if (mode == KeypadMode.BUFFER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )

                            if (mode == KeypadMode.BUFFER) {
                                Row {
                                    IconButton(
                                        onClick = { if (buffer.isNotEmpty()) buffer = buffer.dropLast(1) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                            contentDescription = stringResource(R.string.action_delete)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            if (buffer.isNotEmpty()) {
                                                onSendKey(buffer)
                                                lastTx = buffer
                                                buffer = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("keypad_send_buffer")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Send,
                                            contentDescription = stringResource(R.string.action_send),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        KeypadMode.entries.forEachIndexed { idx, m ->
                            SegmentedButton(
                                selected = mode == m,
                                onClick = { mode = m },
                                shape = SegmentedButtonDefaults.itemShape(idx, KeypadMode.entries.size)
                            ) {
                                Text(stringResource(m.labelRes))
                            }
                        }
                    }
                }
            }

            // 4x4 Key Matrix
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    keypadMatrix.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowKeys.forEach { key ->
                                val isLetter = key in listOf("A", "B", "C", "D")
                                val isStar = key == "*"
                                val isHash = key == "#"

                                val keyBg = when {
                                    isLetter -> MaterialTheme.colorScheme.primaryContainer
                                    isStar -> telemetryColors.warningContainer
                                    isHash -> telemetryColors.connectedContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val keyFg = when {
                                    isLetter -> MaterialTheme.colorScheme.primary
                                    isStar -> telemetryColors.warning
                                    isHash -> telemetryColors.connected
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(66.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(keyBg)
                                        .border(
                                            1.dp,
                                            keyFg.copy(alpha = 0.28f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            if (mode == KeypadMode.DIRECT) {
                                                lastTx = key
                                                onSendKey(key)
                                            } else {
                                                when (key) {
                                                    "*" -> buffer = ""
                                                    "#" -> {
                                                        if (buffer.isNotEmpty()) {
                                                            lastTx = buffer
                                                            onSendKey(buffer)
                                                            buffer = ""
                                                        }
                                                    }
                                                    else -> if (buffer.length < 16) buffer += key
                                                }
                                            }
                                        }
                                        .testTag("keypad_key_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = keyFg
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
