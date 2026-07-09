/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.interpreter.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.falconlabs.aitranslator.domain.model.ConversationCard
import com.falconlabs.aitranslator.domain.model.TranslationConfidence
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.CaptionSize
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.InteractionMode
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.LiveInterpreterIntent
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.LiveInterpreterState
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.LiveInterpreterViewModel
import com.falconlabs.aitranslator.ui.widgets.AiOrb
import com.falconlabs.aitranslator.ui.widgets.OrbState
import org.jetbrains.compose.resources.stringResource
import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.screen_live_interpreter

/**
 * Live Interpreter screen with AI Orb, conversation cards, and mode controls.
 */
@Composable
fun LiveInterpreterScreen(
    modifier: Modifier = Modifier,
    viewModel: LiveInterpreterViewModel = viewModel {
        LiveInterpreterViewModel(
            org.koin.java.KoinJavaComponent.get(TranslationEngine::class.java)
        )
    },
) {
    val state by viewModel.state.collectAsState()
    LiveInterpreterContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun LiveInterpreterContent(
    state: LiveInterpreterState,
    onIntent: (LiveInterpreterIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ─── Language pair row ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text(state.sourceLang.code.uppercase(), style = MaterialTheme.typography.labelLarge)
                }
                FilledTonalIconButton(
                    onClick = { onIntent(LiveInterpreterIntent.SwapLanguages) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Text("⇄", style = MaterialTheme.typography.titleMedium)
                }
                FilledTonalButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text(state.targetLang.code.uppercase(), style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Mode chips ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                FilterChip(
                    selected = state.isAutoSpeak,
                    onClick = { onIntent(LiveInterpreterIntent.ToggleAutoSpeak) },
                    label = { Text("Auto-Speak", style = MaterialTheme.typography.labelSmall) },
                )
                FilterChip(
                    selected = state.isDualLanguage,
                    onClick = { onIntent(LiveInterpreterIntent.ToggleDualLanguage) },
                    label = { Text("Dual Lang", style = MaterialTheme.typography.labelSmall) },
                )
                FilterChip(
                    selected = state.interactionMode == InteractionMode.PUSH_TO_TALK,
                    onClick = { onIntent(LiveInterpreterIntent.TogglePushToTalk) },
                    label = { Text("Push-to-Talk", style = MaterialTheme.typography.labelSmall) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── AI Orb ─────────────────────────────────────────────────
            AiOrb(
                state = state.orbState,
                audioLevel = state.audioLevel,
            )

            // ─── Partial transcription ──────────────────────────────────
            if (state.partialTranscription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.partialTranscription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Listen / Simulate input ────────────────────────────────
            if (state.interactionMode == InteractionMode.PUSH_TO_TALK) {
                Button(
                    onClick = {
                        if (state.orbState == OrbState.LISTENING) {
                            onIntent(LiveInterpreterIntent.PushToTalkReleased)
                        } else {
                            onIntent(LiveInterpreterIntent.PushToTalkPressed)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (state.orbState == OrbState.LISTENING) "Release to Translate" else "Hold to Speak")
                }
            } else {
                // Continuous mode: simulate input box (until real STT is wired)
                SimulateInputRow(onSubmit = { onIntent(LiveInterpreterIntent.SimulateInput(it)) })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Error ──────────────────────────────────────────────────
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ─── Conversation cards ─────────────────────────────────────
            val listState = rememberLazyListState()
            LaunchedEffect(state.conversations.size) {
                if (state.conversations.isNotEmpty()) {
                    listState.animateScrollToItem(state.conversations.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.conversations, key = { it.id }) { card ->
                    ConversationCardItem(card = card, captionSize = state.captionSize)
                }
            }
        }
    }
}

@Composable
private fun SimulateInputRow(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type to simulate speech...") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = {
                if (text.isNotBlank()) { onSubmit(text); text = "" }
            },
            enabled = text.isNotBlank(),
        ) {
            Text("→")
        }
    }
}

@Composable
private fun ConversationCardItem(
    card: ConversationCard,
    captionSize: CaptionSize,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Source text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = card.sourceLanguage.code.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                ConfidenceDot(card.confidence)
            }
            Text(
                text = card.sourceText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Translated text
            Text(
                text = card.targetLanguage.code.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = card.translatedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = captionSize.sp.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ConfidenceDot(confidence: TranslationConfidence) {
    val color = when (confidence) {
        TranslationConfidence.HIGH -> MaterialTheme.colorScheme.primary
        TranslationConfidence.MEDIUM -> MaterialTheme.colorScheme.tertiary
        TranslationConfidence.LOW -> MaterialTheme.colorScheme.error
    }
    Text("●", color = color, style = MaterialTheme.typography.labelSmall)
}
