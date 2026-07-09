/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.translation.view

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.TranslationConfidence
import com.falconlabs.aitranslator.engine.translation.InputValidator
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import com.falconlabs.aitranslator.ui.translation.viewmodel.TextTranslateIntent
import com.falconlabs.aitranslator.ui.translation.viewmodel.TextTranslateState
import com.falconlabs.aitranslator.ui.translation.viewmodel.TextTranslateViewModel
import com.falconlabs.aitranslator.ui.widgets.InputAction
import com.falconlabs.aitranslator.ui.widgets.LexiDropdown
import com.falconlabs.aitranslator.ui.widgets.PlatformCapabilities
import com.falconlabs.aitranslator.ui.widgets.PlatformFilePicker
import com.falconlabs.aitranslator.ui.widgets.TranslationInputBox
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.translate_action_translate
import aitranslator.sharedui.generated.resources.translate_result_label
import aitranslator.sharedui.generated.resources.translate_confidence_high
import aitranslator.sharedui.generated.resources.translate_confidence_medium
import aitranslator.sharedui.generated.resources.translate_confidence_low
import aitranslator.sharedui.generated.resources.translate_alternatives_label
import aitranslator.sharedui.generated.resources.translate_dictionary_label
import aitranslator.sharedui.generated.resources.translate_transliteration_label
import aitranslator.sharedui.generated.resources.translate_input_placeholder

/** Supported source/target languages for the dropdown selectors. */
private val SUPPORTED_LANGUAGES = listOf(
    LanguageCode("en") to "English",
    LanguageCode("de") to "German",
    LanguageCode("fr") to "French",
    LanguageCode("es") to "Spanish",
    LanguageCode("ja") to "Japanese",
    LanguageCode("hi") to "Hindi",
    LanguageCode("zh") to "Chinese",
)

@Composable
fun TextTranslateScreen(
    modifier: Modifier = Modifier,
    onNavigateToModels: ((String, String) -> Unit)? = null,
    viewModel: TextTranslateViewModel = viewModel {
        TextTranslateViewModel(
            org.koin.java.KoinJavaComponent.get(TranslationEngine::class.java)
        )
    },
) {
    val state by viewModel.state.collectAsState()
    TextTranslateContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToModels = onNavigateToModels,
        modifier = modifier,
    )
}

@Composable
internal fun TextTranslateContent(
    state: TextTranslateState,
    onIntent: (TextTranslateIntent) -> Unit,
    onNavigateToModels: ((String, String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ─── Language selectors + Swap + Clear ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Source language dropdown
                LanguageDropdown(
                    selected = state.sourceLang,
                    onSelected = { onIntent(TextTranslateIntent.SelectSourceLang(it)) },
                    modifier = Modifier.weight(1f),
                    label = "From",
                )

                // Swap button
                FilledTonalIconButton(
                    onClick = { onIntent(TextTranslateIntent.SwapLanguages) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Text("⇄", style = MaterialTheme.typography.titleMedium)
                }

                // Target language dropdown
                LanguageDropdown(
                    selected = state.targetLang,
                    onSelected = { onIntent(TextTranslateIntent.SelectTargetLang(it)) },
                    modifier = Modifier.weight(1f),
                    label = "To",
                )

                // Clear button
                FilledTonalIconButton(
                    onClick = { onIntent(TextTranslateIntent.ClearInput) },
                    modifier = Modifier.size(40.dp),
                ) {
                    Text("✕", style = MaterialTheme.typography.titleMedium)
                }
            }

            // ─── Input box (fixed height, scrollable) ───────────────────
            val inputActions = buildInputActions(
                onVoice = { /* TODO: wire platform voice input */ },
                onFile = {
                    coroutineScope.launch {
                        val text = PlatformFilePicker.pickTextFile()
                        if (text != null) onIntent(TextTranslateIntent.UpdateInput(text))
                    }
                },
                onPaste = {
                    val clip = clipboardManager.getText()
                    if (clip != null) onIntent(TextTranslateIntent.UpdateInput(state.inputText + clip.text))
                },
            )

            TranslationInputBox(
                text = state.inputText,
                onTextChange = { onIntent(TextTranslateIntent.UpdateInput(it)) },
                actions = inputActions,
                maxChars = InputValidator.MAX_TRANSLATION_CHARS,
                fixedHeight = 160,
                placeholder = stringResource(Res.string.translate_input_placeholder),
                modifier = Modifier.fillMaxWidth(),
            )

            // ─── Translate button ───────────────────────────────────────
            Button(
                onClick = { onIntent(TextTranslateIntent.Translate) },
                enabled = state.canTranslate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(Res.string.translate_action_translate))
            }

            // ─── Error / Model not installed ────────────────────────────
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        // Show "Get Models" if error is model-related
                        if (state.error?.contains("not installed", ignoreCase = true) == true) {
                            TextButton(onClick = {
                                onNavigateToModels?.invoke(state.sourceLang.code, state.targetLang.code)
                            }) {
                                Text("Show Models →")
                            }
                        }
                    }
                }
            }

            // ─── Output box (fixed height, scrollable, read-only) ───────
            if (state.translatedText.isNotEmpty()) {
                val outputActions = buildOutputActions(
                    onCopy = { clipboardManager.setText(AnnotatedString(state.translatedText)) },
                    onShare = { /* TODO: platform share sheet */ },
                )

                TranslationInputBox(
                    text = state.translatedText,
                    onTextChange = null,
                    actions = outputActions,
                    maxChars = InputValidator.MAX_TRANSLATION_CHARS,
                    fixedHeight = 160,
                    label = stringResource(Res.string.translate_result_label),
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Confidence badge
                state.confidence?.let { confidence ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        ConfidenceBadge(confidence)
                    }
                }

                // Transliteration
                state.transliteration?.let { translit ->
                    CompactInfoCard(
                        label = stringResource(Res.string.translate_transliteration_label),
                        content = translit,
                    )
                }

                // Alternatives
                if (state.alternatives.isNotEmpty()) {
                    CompactInfoCard(
                        label = stringResource(Res.string.translate_alternatives_label),
                        content = state.alternatives.joinToString("\n") { "• $it" },
                    )
                }

                // Dictionary
                state.dictionaryEntry?.let { entry ->
                    CompactInfoCard(
                        label = stringResource(Res.string.translate_dictionary_label),
                        content = entry.meaning,
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun LanguageDropdown(
    selected: LanguageCode,
    onSelected: (LanguageCode) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val selectedLabel = SUPPORTED_LANGUAGES.find { it.first.code == selected.code }?.second ?: selected.code
    val options = SUPPORTED_LANGUAGES.map { (code, displayLabel) -> code to displayLabel }

    LexiDropdown(
        selectedLabel = selectedLabel,
        options = options,
        onSelected = onSelected,
        modifier = modifier,
        label = label,
    )
}

@Composable
private fun CompactInfoCard(label: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: TranslationConfidence) {
    val (text, color) = when (confidence) {
        TranslationConfidence.HIGH -> stringResource(Res.string.translate_confidence_high) to MaterialTheme.colorScheme.primary
        TranslationConfidence.MEDIUM -> stringResource(Res.string.translate_confidence_medium) to MaterialTheme.colorScheme.tertiary
        TranslationConfidence.LOW -> stringResource(Res.string.translate_confidence_low) to MaterialTheme.colorScheme.error
    }
    Text(text = "● $text", style = MaterialTheme.typography.labelSmall, color = color)
}

/** Input box toolbar: voice (if supported), file import, paste */
@Composable
private fun buildInputActions(
    onVoice: () -> Unit,
    onFile: () -> Unit,
    onPaste: () -> Unit,
): List<InputAction> {
    val actions = mutableListOf<InputAction>()

    if (PlatformCapabilities.supportsVoiceInput) {
        actions.add(InputAction(icon = "Mic", contentDescription = "Voice input", onClick = onVoice))
    }
    actions.add(InputAction(icon = "File", contentDescription = "Import text file", onClick = onFile))
    actions.add(InputAction(icon = "Paste", contentDescription = "Paste from clipboard", onClick = onPaste))

    return actions
}

/** Output box toolbar: copy, share */
@Composable
private fun buildOutputActions(
    onCopy: () -> Unit,
    onShare: () -> Unit,
): List<InputAction> {
    val actions = mutableListOf<InputAction>()
    actions.add(InputAction(icon = "Copy", contentDescription = "Copy to clipboard", onClick = onCopy))
    if (PlatformCapabilities.supportsShareSheet) {
        actions.add(InputAction(icon = "Share", contentDescription = "Share", onClick = onShare))
    }
    return actions
}
