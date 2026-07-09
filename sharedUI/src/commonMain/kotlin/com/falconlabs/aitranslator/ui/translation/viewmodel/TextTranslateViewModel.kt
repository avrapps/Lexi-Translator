/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.translation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falconlabs.aitranslator.domain.model.DictionaryEntry
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.TranslationConfidence
import com.falconlabs.aitranslator.domain.model.TranslationMode
import com.falconlabs.aitranslator.domain.model.TranslationRequest
import com.falconlabs.aitranslator.engine.translation.InputValidator
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the Text Translate screen. */
data class TextTranslateState(
    val inputText: String = "",
    val translatedText: String = "",
    val sourceLang: LanguageCode = LanguageCode("en"),
    val targetLang: LanguageCode = LanguageCode("de"),
    val mode: TranslationMode = TranslationMode.DEFAULT,
    val confidence: TranslationConfidence? = null,
    val alternatives: List<String> = emptyList(),
    val transliteration: String? = null,
    val dictionaryEntry: DictionaryEntry? = null,
    val isTranslating: Boolean = false,
    val error: String? = null,
    val durationMs: Long = 0L,
) {
    val charCount: Int get() = inputText.length
    val maxChars: Int get() = InputValidator.MAX_TRANSLATION_CHARS
    val canTranslate: Boolean get() = inputText.isNotBlank() && !isTranslating
}

/** User intents for the Text Translate screen. */
sealed interface TextTranslateIntent {
    data class UpdateInput(val text: String) : TextTranslateIntent
    data class SelectSourceLang(val lang: LanguageCode) : TextTranslateIntent
    data class SelectTargetLang(val lang: LanguageCode) : TextTranslateIntent
    data object SwapLanguages : TextTranslateIntent
    data class SelectMode(val mode: TranslationMode) : TextTranslateIntent
    data object Translate : TextTranslateIntent
    data object ClearInput : TextTranslateIntent
    data object DismissError : TextTranslateIntent
    data object CopyResult : TextTranslateIntent
}

/**
 * MVI ViewModel for the Text Translate screen.
 * Orchestrates translation requests via [TranslationEngine].
 */
class TextTranslateViewModel(
    private val translationEngine: TranslationEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(TextTranslateState())
    val state: StateFlow<TextTranslateState> = _state.asStateFlow()

    fun onIntent(intent: TextTranslateIntent) {
        when (intent) {
            is TextTranslateIntent.UpdateInput -> updateInput(intent.text)
            is TextTranslateIntent.SelectSourceLang -> _state.update { it.copy(sourceLang = intent.lang) }
            is TextTranslateIntent.SelectTargetLang -> _state.update { it.copy(targetLang = intent.lang) }
            is TextTranslateIntent.SwapLanguages -> swapLanguages()
            is TextTranslateIntent.SelectMode -> _state.update { it.copy(mode = intent.mode) }
            is TextTranslateIntent.Translate -> translate()
            is TextTranslateIntent.ClearInput -> clearInput()
            is TextTranslateIntent.DismissError -> _state.update { it.copy(error = null) }
            is TextTranslateIntent.CopyResult -> { /* Handled at UI layer via clipboard */ }
        }
    }

    private fun updateInput(text: String) {
        val truncated = if (text.length > InputValidator.MAX_TRANSLATION_CHARS) {
            text.take(InputValidator.MAX_TRANSLATION_CHARS)
        } else {
            text
        }
        _state.update { it.copy(inputText = truncated, error = null) }
    }

    private fun swapLanguages() {
        _state.update { current ->
            current.copy(
                sourceLang = current.targetLang,
                targetLang = current.sourceLang,
                inputText = current.translatedText.ifEmpty { current.inputText },
                translatedText = "",
                alternatives = emptyList(),
                transliteration = null,
                dictionaryEntry = null,
                confidence = null,
            )
        }
    }

    private fun clearInput() {
        _state.update {
            it.copy(
                inputText = "",
                translatedText = "",
                alternatives = emptyList(),
                transliteration = null,
                dictionaryEntry = null,
                confidence = null,
                error = null,
            )
        }
    }

    private fun translate() {
        val current = _state.value
        if (current.inputText.isBlank()) return

        _state.update { it.copy(isTranslating = true, error = null, translatedText = "") }

        viewModelScope.launch {
            try {
                val request = TranslationRequest(
                    text = current.inputText,
                    sourceLang = current.sourceLang,
                    targetLang = current.targetLang,
                    mode = current.mode,
                )

                // Check if multi-sentence
                val hasParagraphs = current.inputText.contains("\n") ||
                    current.inputText.split("(?<=[.!?])\\s+".toRegex()).size > 1

                if (!hasParagraphs) {
                    // Short text — single shot
                    val result = translationEngine.translate(request)
                    _state.update { state ->
                        state.copy(
                            translatedText = cleanPunctuation(result.translatedText),
                            confidence = result.confidence,
                            alternatives = result.alternatives.take(5),
                            transliteration = result.transliteration,
                            dictionaryEntry = result.dictionaryEntry,
                            durationMs = result.durationMs,
                            isTranslating = false,
                        )
                    }
                } else {
                    // Multi-sentence: stream sentence by sentence, preserving paragraph structure
                    // Split on double-newline (paragraph break) first, then single newlines
                    val paragraphs = current.inputText.split(Regex("\\n\\s*\\n|\\n"))
                    val builder = StringBuilder()

                    for ((pIdx, paragraph) in paragraphs.withIndex()) {
                        if (paragraph.isBlank()) {
                            continue
                        }
                        val sentences = paragraph.trim()
                            .split("(?<=[.!?])\\s+".toRegex())
                            .filter { it.isNotBlank() }

                        for (sentence in sentences) {
                            val sentenceRequest = TranslationRequest(
                                text = sentence,
                                sourceLang = current.sourceLang,
                                targetLang = current.targetLang,
                                mode = current.mode,
                            )
                            val result = translationEngine.translate(sentenceRequest)
                            if (builder.isNotEmpty() && builder.last() != '\n') builder.append(" ")
                            builder.append(result.translatedText)
                            _state.update { state ->
                                state.copy(
                                    translatedText = cleanPunctuation(builder.toString()),
                                    confidence = result.confidence,
                                )
                            }
                        }
                        // Add paragraph break between paragraphs
                        if (pIdx < paragraphs.size - 1) builder.append("\n\n")
                    }
                    _state.update { it.copy(isTranslating = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isTranslating = false, error = e.message) }
            }
        }
    }

    /** Cleans up common punctuation artifacts from sentence-by-sentence translation. */
    private fun cleanPunctuation(text: String): String {
        return text
            // Remove duplicate punctuation with spaces: ". ." → "."
            .replace(Regex("""([.!?]) +\1"""), "$1")
            // Remove space before punctuation: "wahr? ." → "wahr?"
            .replace(Regex(""" +([.!?,;:])"""), "$1")
            // Remove double punctuation: ".." → "." but keep "..." (ellipsis)
            .replace(Regex("""([.!?])\1(?!\1)"""), "$1")
            // Fix punctuation followed immediately by letter without space: ".Das" → ". Das"
            .replace(Regex("""([.!?])([A-ZÄÖÜ])"""), "$1 $2")
            // Collapse multiple horizontal spaces (NOT newlines)
            .replace(Regex("""[^\S\n]{2,}"""), " ")
            // Collapse multiple newlines to single newline
            .replace(Regex("""\n{2,}"""), "\n")
            .trim()
    }
}
