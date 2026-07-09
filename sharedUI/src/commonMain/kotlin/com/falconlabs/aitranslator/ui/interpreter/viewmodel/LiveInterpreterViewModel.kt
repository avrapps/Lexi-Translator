/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.interpreter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falconlabs.aitranslator.domain.model.ConversationCard
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.TranslationConfidence
import com.falconlabs.aitranslator.domain.model.TranslationRequest
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import com.falconlabs.aitranslator.ui.widgets.OrbState
import com.falconlabs.aitranslator.util.currentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Caption size options (Requirement 2.13). */
enum class CaptionSize(val sp: Int) { SMALL(14), MEDIUM(18), LARGE(24), HUGE(32), PRESENTATION(48) }

/** Interaction mode for the interpreter. */
enum class InteractionMode { CONTINUOUS, PUSH_TO_TALK }

/** UI state for the Live Interpreter. */
data class LiveInterpreterState(
    val orbState: OrbState = OrbState.IDLE,
    val sourceLang: LanguageCode = LanguageCode("en"),
    val targetLang: LanguageCode = LanguageCode("de"),
    val partialTranscription: String = "",
    val conversations: List<ConversationCard> = emptyList(),
    val audioLevel: Float = 0f,
    val isAutoSpeak: Boolean = true,
    val isDualLanguage: Boolean = false,
    val interactionMode: InteractionMode = InteractionMode.CONTINUOUS,
    val captionSize: CaptionSize = CaptionSize.MEDIUM,
    val isFullScreen: Boolean = false,
    val error: String? = null,
)

/** User intents for the Live Interpreter. */
sealed interface LiveInterpreterIntent {
    data object StartListening : LiveInterpreterIntent
    data object StopListening : LiveInterpreterIntent
    data object SwapLanguages : LiveInterpreterIntent
    data class SelectSourceLang(val lang: LanguageCode) : LiveInterpreterIntent
    data class SelectTargetLang(val lang: LanguageCode) : LiveInterpreterIntent
    data object ToggleAutoSpeak : LiveInterpreterIntent
    data object ToggleDualLanguage : LiveInterpreterIntent
    data object TogglePushToTalk : LiveInterpreterIntent
    data class SetCaptionSize(val size: CaptionSize) : LiveInterpreterIntent
    data object ToggleFullScreen : LiveInterpreterIntent
    data object ClearConversations : LiveInterpreterIntent
    data object DismissError : LiveInterpreterIntent
    // Push-to-talk events
    data object PushToTalkPressed : LiveInterpreterIntent
    data object PushToTalkReleased : LiveInterpreterIntent
    // Simulate speech input (for testing without mic)
    data class SimulateInput(val text: String) : LiveInterpreterIntent
}

/**
 * MVI ViewModel for the Live Interpreter screen.
 * Manages STT listening, translation, and conversation history.
 */
class LiveInterpreterViewModel(
    private val translationEngine: TranslationEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(LiveInterpreterState())
    val state: StateFlow<LiveInterpreterState> = _state.asStateFlow()

    private var conversationCounter = 0

    fun onIntent(intent: LiveInterpreterIntent) {
        when (intent) {
            is LiveInterpreterIntent.StartListening -> startListening()
            is LiveInterpreterIntent.StopListening -> stopListening()
            is LiveInterpreterIntent.SwapLanguages -> swapLanguages()
            is LiveInterpreterIntent.SelectSourceLang -> _state.update { it.copy(sourceLang = intent.lang) }
            is LiveInterpreterIntent.SelectTargetLang -> _state.update { it.copy(targetLang = intent.lang) }
            is LiveInterpreterIntent.ToggleAutoSpeak -> _state.update { it.copy(isAutoSpeak = !it.isAutoSpeak) }
            is LiveInterpreterIntent.ToggleDualLanguage -> _state.update { it.copy(isDualLanguage = !it.isDualLanguage) }
            is LiveInterpreterIntent.TogglePushToTalk -> togglePushToTalk()
            is LiveInterpreterIntent.SetCaptionSize -> _state.update { it.copy(captionSize = intent.size) }
            is LiveInterpreterIntent.ToggleFullScreen -> _state.update { it.copy(isFullScreen = !it.isFullScreen) }
            is LiveInterpreterIntent.ClearConversations -> _state.update { it.copy(conversations = emptyList()) }
            is LiveInterpreterIntent.DismissError -> _state.update { it.copy(error = null) }
            is LiveInterpreterIntent.PushToTalkPressed -> startListening()
            is LiveInterpreterIntent.PushToTalkReleased -> finalizeAndTranslate()
            is LiveInterpreterIntent.SimulateInput -> simulateInput(intent.text)
        }
    }

    private fun startListening() {
        _state.update { it.copy(orbState = OrbState.LISTENING, partialTranscription = "", error = null) }
        // TODO: Wire real SttEngine.startListening() when Whisper model is integrated
    }

    private fun stopListening() {
        _state.update { it.copy(orbState = OrbState.IDLE, partialTranscription = "") }
    }

    private fun swapLanguages() {
        _state.update { it.copy(sourceLang = it.targetLang, targetLang = it.sourceLang) }
    }

    private fun togglePushToTalk() {
        _state.update { current ->
            val newMode = if (current.interactionMode == InteractionMode.CONTINUOUS)
                InteractionMode.PUSH_TO_TALK else InteractionMode.CONTINUOUS
            current.copy(interactionMode = newMode, orbState = OrbState.IDLE)
        }
    }

    private fun finalizeAndTranslate() {
        val text = _state.value.partialTranscription
        if (text.isNotBlank()) {
            translateAndAddCard(text)
        }
        _state.update { it.copy(orbState = OrbState.IDLE, partialTranscription = "") }
    }

    /** Simulates STT input (for demo/testing — types text and translates it). */
    private fun simulateInput(text: String) {
        if (text.isBlank()) return
        _state.update { it.copy(orbState = OrbState.THINKING) }
        translateAndAddCard(text)
    }

    private fun translateAndAddCard(sourceText: String) {
        val current = _state.value
        _state.update { it.copy(orbState = OrbState.THINKING) }

        viewModelScope.launch {
            try {
                val request = TranslationRequest(
                    text = sourceText,
                    sourceLang = current.sourceLang,
                    targetLang = current.targetLang,
                )
                val result = translationEngine.translate(request)

                val card = ConversationCard(
                    id = "conv_${++conversationCounter}",
                    sourceText = sourceText,
                    translatedText = result.translatedText,
                    sourceLanguage = current.sourceLang,
                    targetLanguage = current.targetLang,
                    confidence = result.confidence,
                    timestamp = currentTimeMillis(),
                    durationMs = result.durationMs,
                )

                _state.update { state ->
                    val updatedConversations = (state.conversations + card).takeLast(10_000)
                    state.copy(
                        orbState = if (state.isAutoSpeak) OrbState.SPEAKING else OrbState.IDLE,
                        conversations = updatedConversations,
                        partialTranscription = "",
                    )
                }

                // Auto-return to IDLE after brief "speaking" indication
                if (current.isAutoSpeak) {
                    kotlinx.coroutines.delay(1500)
                    _state.update { it.copy(orbState = OrbState.IDLE) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(orbState = OrbState.ERROR, error = e.message) }
            }
        }
    }
}
