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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.falconlabs.aitranslator.domain.model.ConversationCard
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.TranslationConfidence
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.CaptionSize
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.LiveInterpreterIntent
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.LiveInterpreterState
import com.falconlabs.aitranslator.ui.interpreter.viewmodel.LiveInterpreterViewModel
import com.falconlabs.aitranslator.ui.widgets.AiOrb
import com.falconlabs.aitranslator.ui.widgets.OrbState

/** Supported languages for the interpreter dropdowns. */
private val INTERPRETER_LANGUAGES = listOf(
    LanguageCode("en") to "English",
    LanguageCode("de") to "German",
    LanguageCode("fr") to "French",
    LanguageCode("es") to "Spanish",
    LanguageCode("ja") to "Japanese",
    LanguageCode("hi") to "Hindi",
    LanguageCode("zh") to "Chinese",
)

@Composable
fun LiveInterpreterScreen(
    modifier: Modifier = Modifier,
    onNavigateToModels: (() -> Unit)? = null,
    viewModel: LiveInterpreterViewModel = viewModel {
        LiveInterpreterViewModel(
            org.koin.java.KoinJavaComponent.get(TranslationEngine::class.java),
            org.koin.java.KoinJavaComponent.get(com.falconlabs.aitranslator.engine.stt.SttEngine::class.java),
            org.koin.java.KoinJavaComponent.get(
                com.falconlabs.aitranslator.data.repository.ModelRepository::class.java
            ),
        )
    },
) {
    val state by viewModel.state.collectAsState()
    LiveInterpreterContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToModels = onNavigateToModels,
        modifier = modifier,
    )
}

@Composable
internal fun LiveInterpreterContent(
    state: LiveInterpreterState,
    onIntent: (LiveInterpreterIntent) -> Unit,
    onNavigateToModels: (() -> Unit)?,
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
            // ─── Language dropdowns (same style as Translate) ────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                com.falconlabs.aitranslator.ui.widgets.LexiDropdown(
                    selectedLabel =
                    INTERPRETER_LANGUAGES.find { it.first.code == state.sourceLang.code }?.second
                        ?: state.sourceLang.code,
                    options = INTERPRETER_LANGUAGES.map { (code, label) -> code to label },
                    onSelected = { onIntent(LiveInterpreterIntent.SelectSourceLang(it)) },
                    modifier = Modifier.weight(1f),
                    label = "From",
                )
                FilledTonalIconButton(
                    onClick = { onIntent(LiveInterpreterIntent.SwapLanguages) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Text("\u21C4", style = MaterialTheme.typography.titleMedium)
                }
                com.falconlabs.aitranslator.ui.widgets.LexiDropdown(
                    selectedLabel =
                    INTERPRETER_LANGUAGES.find { it.first.code == state.targetLang.code }?.second
                        ?: state.targetLang.code,
                    options = INTERPRETER_LANGUAGES.map { (code, label) -> code to label },
                    onSelected = { onIntent(LiveInterpreterIntent.SelectTargetLang(it)) },
                    modifier = Modifier.weight(1f),
                    label = "To",
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Missing models warning ─────────────────────────────────
            if (state.missingModels.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        state.missingModels.forEach { msg ->
                            Text(
                                text = "\u26A0 $msg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onNavigateToModels?.invoke() }) {
                            Text("Download Models")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

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

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Single button: Start Listening / Stop & Translate ──────
            val isListening = state.orbState == OrbState.LISTENING
            Button(
                onClick = {
                    if (isListening) {
                        onIntent(LiveInterpreterIntent.StopListening)
                    } else {
                        onIntent(LiveInterpreterIntent.StartListening)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.orbState != OrbState.THINKING && state.missingModels.isEmpty(),
            ) {
                Text(if (isListening) "Stop & Translate" else "Start Listening")
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
private fun ConversationCardItem(card: ConversationCard, captionSize: CaptionSize) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    card.sourceLanguage.code.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                ConfidenceDot(card.confidence)
            }
            Text(
                card.sourceText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                card.targetLanguage.code.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                card.translatedText,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = captionSize.sp.sp),
                color = MaterialTheme.colorScheme.onSurface
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
    Text("\u25CF", color = color, style = MaterialTheme.typography.labelSmall)
}
