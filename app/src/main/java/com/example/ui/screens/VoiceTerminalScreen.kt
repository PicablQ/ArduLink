package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.VoiceCommandRule
import com.example.ui.theme.LocalTelemetryColors
import com.example.ui.theme.TelemetryMonoSmall

@Composable
fun VoiceTerminalScreen(
    speechLocaleTag: String,
    rules: List<VoiceCommandRule>,
    sendUnmatchedRaw: Boolean,
    lastVoiceMatch: String?,
    onVoiceTranscript: (String) -> Unit,
    onAddRule: (phrase: String, tx: String) -> Unit,
    onDeleteRule: (String) -> Unit,
    onToggleSendUnmatched: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentContext by rememberUpdatedState(context)
    val telemetryColors = LocalTelemetryColors.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isListening by remember { mutableStateOf(false) }
    var spokenText by remember { mutableStateOf("") }
    var statusOverride by remember { mutableStateOf<String?>(null) }
    var rmsDb by remember { mutableFloatStateOf(0f) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    val speechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context.applicationContext) }

    val speechRecognizer = remember(speechAvailable) {
        if (speechAvailable) SpeechRecognizer.createSpeechRecognizer(context.applicationContext) else null
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                statusOverride = currentContext.getString(R.string.voice_listening)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                rmsDb = rmsdB.coerceIn(0f, 12f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                rmsDb = 0f
                statusOverride = currentContext.getString(R.string.voice_processing)
            }

            override fun onError(error: Int) {
                isListening = false
                rmsDb = 0f
                statusOverride = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> currentContext.getString(R.string.voice_no_match)
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> currentContext.getString(R.string.voice_timeout)
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> currentContext.getString(R.string.voice_perm_needed)
                    else -> currentContext.getString(R.string.voice_ready_code, error)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                rmsDb = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull().orEmpty()
                if (best.isNotBlank()) {
                    spokenText = best
                    statusOverride = currentContext.getString(R.string.voice_recognized)
                    onVoiceTranscript(best)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!partial.isNullOrBlank()) {
                    spokenText = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val intentSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val best = matches?.firstOrNull().orEmpty()
        if (best.isNotBlank()) {
            spokenText = best
            statusOverride = currentContext.getString(R.string.voice_recognized)
            onVoiceTranscript(best)
        }
    }

    fun startListeningFlow() {
        if (speechRecognizer != null) {
            if (isListening) {
                speechRecognizer.stopListening()
                isListening = false
            } else {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                speechRecognizer.startListening(intent)
            }
        } else {
            // Fallback system speech activity if available
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocaleTag)
                }
                intentSpeechLauncher.launch(intent)
            } catch (_: Exception) {
                statusOverride = currentContext.getString(R.string.voice_unavailable)
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            startListeningFlow()
        } else {
            statusOverride = currentContext.getString(R.string.voice_perm_denied)
        }
    }

    val orbScale by animateFloatAsState(
        targetValue = if (isListening) 1f + (rmsDb * 0.025f) else 1f,
        label = "orbScale"
    )
    val orbColor by animateColorAsState(
        targetValue = if (isListening) telemetryColors.connected else MaterialTheme.colorScheme.primary,
        label = "orbColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Voice Mic Orb Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.voice_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.voice_raw_toggle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = sendUnmatchedRaw,
                            onCheckedChange = onToggleSendUnmatched
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(orbScale)
                        .clip(CircleShape)
                        .background(orbColor.copy(alpha = 0.16f))
                        .border(2.dp, orbColor, CircleShape)
                        .clickable {
                            if (!hasAudioPermission) {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                startListeningFlow()
                            }
                        }
                        .testTag("voice_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            !hasAudioPermission -> Icons.Rounded.MicOff
                            isListening -> Icons.Rounded.GraphicEq
                            else -> Icons.Rounded.Mic
                        },
                        contentDescription = stringResource(R.string.voice_tap_to_speak),
                        tint = orbColor,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = statusOverride ?: stringResource(R.string.voice_tap_to_speak),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isListening) telemetryColors.connected else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (spokenText.isNotBlank() || lastVoiceMatch != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (spokenText.isNotBlank()) {
                                Text(
                                    text = stringResource(R.string.voice_recognized_text, spokenText),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            lastVoiceMatch?.let { match ->
                                Text(
                                    text = match,
                                    style = TelemetryMonoSmall,
                                    color = telemetryColors.connected
                                )
                            }
                        }
                    }
                }
            }
        }

        // Voice Rules Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.voice_rules_header),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { showAddRuleDialog = true },
                modifier = Modifier.testTag("add_voice_rule_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.voice_add_rule))
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable {
                            spokenText = rule.phrase
                            onVoiceTranscript(rule.phrase)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"${rule.phrase}\"",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "TX → ${rule.txCommand}",
                                style = TelemetryMonoSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDeleteRule(rule.id) }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        var phrase by remember { mutableStateOf("") }
        var tx by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text(stringResource(R.string.voice_add_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = phrase,
                        onValueChange = { phrase = it },
                        label = { Text(stringResource(R.string.voice_phrase_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tx,
                        onValueChange = { tx = it },
                        label = { Text(stringResource(R.string.voice_tx_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddRule(phrase, tx)
                        showAddRuleDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
