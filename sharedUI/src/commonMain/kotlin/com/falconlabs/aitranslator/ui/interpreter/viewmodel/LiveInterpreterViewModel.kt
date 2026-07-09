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
import com.falconlabs.aitranslator.domain.model.TranslationRequest
import com.falconlabs.aitranslator.engine.stt.SttConfig
import com.falconlabs.aitranslator.engine.stt.SttEngine
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import com.falconlabs.aitranslator.ui.widgets.OrbState
import com.falconlabs.aitranslator.util.currentTimeMillis

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    // Missing model messages
    val missingModels: List<String> = emptyList(),
    // Legacy prompt fields (kept for compat)
    val showModelPrompt: Boolean = false,
    val modelPromptType: String = "",
    val modelPromptInfo: String = "",
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
 * Checks model availability and prompts user to download if needed.
 */
class LiveInterpreterViewModel(
    private val translationEngine: TranslationEngine,
    private val sttEngine: SttEngine,
    private val modelRepository: com.falconlabs.aitranslator.data.repository.ModelRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LiveInterpreterState())
    val state: StateFlow<LiveInterpreterState> = _state.asStateFlow()

    private var conversationCounter = 0
    private var listeningJob: kotlinx.coroutines.Job? = null

    init {
        checkModelAvailability()
    }

    fun onIntent(intent: LiveInterpreterIntent) {
        when (intent) {
            is LiveInterpreterIntent.StartListening -> startListening()
            is LiveInterpreterIntent.StopListening -> stopListening()
            is LiveInterpreterIntent.SwapLanguages -> {
                swapLanguages()
                checkModelAvailability()
            }
            is LiveInterpreterIntent.SelectSourceLang -> {
                _state.update { it.copy(sourceLang = intent.lang) }
                checkModelAvailability()
            }
            is LiveInterpreterIntent.SelectTargetLang -> {
                _state.update { it.copy(targetLang = intent.lang) }
                checkModelAvailability()
            }
            is LiveInterpreterIntent.ToggleAutoSpeak -> _state.update { it.copy(isAutoSpeak = !it.isAutoSpeak) }
            is LiveInterpreterIntent.ToggleDualLanguage -> _state.update {
                it.copy(isDualLanguage = !it.isDualLanguage)
            }
            is LiveInterpreterIntent.TogglePushToTalk -> togglePushToTalk()
            is LiveInterpreterIntent.SetCaptionSize -> _state.update { it.copy(captionSize = intent.size) }
            is LiveInterpreterIntent.ToggleFullScreen -> _state.update { it.copy(isFullScreen = !it.isFullScreen) }
            is LiveInterpreterIntent.ClearConversations -> _state.update { it.copy(conversations = emptyList()) }
            is LiveInterpreterIntent.DismissError -> _state.update { it.copy(error = null, showModelPrompt = false) }
            is LiveInterpreterIntent.PushToTalkPressed -> startListening()
            is LiveInterpreterIntent.PushToTalkReleased -> finalizeAndTranslate()
            is LiveInterpreterIntent.SimulateInput -> simulateInput(intent.text)
        }
    }

    /** Checks which models are missing and updates state.missingModels. */
    private fun checkModelAvailability() {
        viewModelScope.launch {
            val current = _state.value
            val missing = mutableListOf<String>()

            // Get all installed model IDs
            val installedModels = modelRepository.getAllInstalled().first()
            val installedIds = installedModels.map { it.id.id }.toSet()

            // Check STT model (any whisper model)
            val hasWhisper = installedIds.any { it.startsWith("whisper-") }
            if (!hasWhisper) {
                missing.add("You do not have ${getLanguageName(current.sourceLang)} STT (Whisper) model.")
            }

            // Check Translation model
            if (!translationEngine.isModelAvailable(current.sourceLang, current.targetLang)) {
                missing.add(
                    "You do not have ${getLanguageName(
                        current.sourceLang
                    )}\u2192${getLanguageName(current.targetLang)} Translation model."
                )
            }

            // Check TTS model (any voice model for the target language)
            val hasTts = installedIds.any {
                it.startsWith("kokoro-") || it.startsWith("piper-") || it.startsWith("vits-")
            }
            if (!hasTts) {
                missing.add("You do not have ${getLanguageName(current.targetLang)} TTS (Kokoro/Piper) model.")
            }

            _state.update { it.copy(missingModels = missing) }
        }
    }

    private fun getLanguageName(code: LanguageCode): String = when (code.code) {
        "en" -> "English"
        "de" -> "German"
        "fr" -> "French"
        "es" -> "Spanish"
        "ja" -> "Japanese"
        "hi" -> "Hindi"
        "zh" -> "Chinese"
        else -> code.code.uppercase()
    }

    private fun startListening() {
        val current = _state.value

        // Don't start if models are missing
        if (current.missingModels.isNotEmpty()) return

        _state.update { it.copy(orbState = OrbState.LISTENING, partialTranscription = "", error = null) }

        // Start real STT listening
        listeningJob = viewModelScope.launch {
            val config = SttConfig(
                primaryLanguage = current.sourceLang,
                secondaryLanguage = if (current.isDualLanguage) current.targetLang else null,
                enableDualLanguage = current.isDualLanguage,
            )
            sttEngine.startListening(config).collect { event ->
                handleSttEvent(event)
            }
        }
    }

    private fun handleSttEvent(event: com.falconlabs.aitranslator.engine.stt.SttEvent) {
        when (event) {
            is com.falconlabs.aitranslator.engine.stt.SttEvent.AudioLevel -> {
                _state.update { it.copy(audioLevel = event.amplitude) }
            }
            is com.falconlabs.aitranslator.engine.stt.SttEvent.PartialTranscription -> {
                _state.update { it.copy(partialTranscription = event.text) }
            }
            is com.falconlabs.aitranslator.engine.stt.SttEvent.FinalTranscription -> {
                // Transcribe + translate this segment, but KEEP LISTENING
                val text = event.text
                println("[Interpreter] FinalTranscription: '$text'")
                if (text.isNotBlank() && !text.startsWith("[") && !text.startsWith("(")) {
                    println("[Interpreter] Translating: '$text'")
                    translateAndAddCard(text)
                } else {
                    println("[Interpreter] SKIPPED (blank or placeholder)")
                }
                // Return orb to LISTENING — mic stays active for next segment
                _state.update { it.copy(orbState = OrbState.LISTENING, partialTranscription = "") }
            }
            is com.falconlabs.aitranslator.engine.stt.SttEvent.SilenceDetected -> {
                println("[Interpreter] SilenceDetected")
                _state.update { it.copy(orbState = OrbState.THINKING) }
            }
            is com.falconlabs.aitranslator.engine.stt.SttEvent.Error -> {
                _state.update { it.copy(orbState = OrbState.ERROR, error = event.error.message) }
                listeningJob?.cancel()
                listeningJob = null
            }
        }
    }

    private fun stopListening() {
        // Stop mic completely
        listeningJob?.cancel()
        listeningJob = null
        viewModelScope.launch { sttEngine.stopListening() }
        // Transition to SPEAKING — play TTS of translated text (Wave 6 will add real audio)
        _state.update { it.copy(orbState = OrbState.SPEAKING, partialTranscription = "", audioLevel = 0f) }
        // TODO: Play TTS audio of all conversation cards’ translated text
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _state.update { it.copy(orbState = OrbState.IDLE) }
        }
    }

    private fun swapLanguages() {
        _state.update { it.copy(sourceLang = it.targetLang, targetLang = it.sourceLang) }
    }

    private fun togglePushToTalk() {
        _state.update { current ->
            val newMode = if (current.interactionMode == InteractionMode.CONTINUOUS) {
                InteractionMode.PUSH_TO_TALK
            } else {
                InteractionMode.CONTINUOUS
            }
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
        println("[Interpreter] translateAndAddCard: '$sourceText'")
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
