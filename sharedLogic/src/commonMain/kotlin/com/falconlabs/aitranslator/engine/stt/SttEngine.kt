/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.engine.stt

import com.falconlabs.aitranslator.domain.model.LanguageCode
import kotlinx.coroutines.flow.Flow

/**
 * Speech-to-Text engine interface for on-device audio transcription.
 * Implementations use Whisper ONNX models for multilingual ASR.
 *
 * Supports Requirements 2.3 (partial transcription), 2.4 (silence detection),
 * and 2.7 (dual language mode).
 */
interface SttEngine {
    /** Starts listening for audio and emits STT events as a Flow. */
    fun startListening(config: SttConfig): Flow<SttEvent>

    /** Stops the active listening session. */
    suspend fun stopListening()

    /** Returns true if the engine is currently listening. */
    val isListening: Boolean
}

/**
 * Configuration for an STT session.
 */
data class SttConfig(
    val primaryLanguage: LanguageCode = LanguageCode("en"),
    val secondaryLanguage: LanguageCode? = null,
    val silenceThresholdMs: Long = 1500L,
    val sampleRate: Int = 16000,
    val enableDualLanguage: Boolean = false,
)

/**
 * Events emitted by the STT engine during a listening session.
 */
sealed interface SttEvent {
    /** Partial (in-progress) transcription — updates frequently during speech. */
    data class PartialTranscription(
        val text: String,
        val detectedLanguage: LanguageCode? = null,
    ) : SttEvent

    /** Final transcription after silence detection completes a segment. */
    data class FinalTranscription(
        val text: String,
        val detectedLanguage: LanguageCode,
        val confidence: Float,
        val durationMs: Long,
    ) : SttEvent

    /** Real-time audio level (0.0 to 1.0) for waveform visualization. */
    data class AudioLevel(val amplitude: Float) : SttEvent

    /** Silence detected — triggers translation of accumulated text. */
    data class SilenceDetected(val durationMs: Long) : SttEvent

    /** Error during STT processing. */
    data class Error(val error: SttError) : SttEvent
}

/** Error types for STT operations. */
sealed class SttError(val message: String) {
    class ModelNotLoaded : SttError("STT model not loaded")
    class AudioCaptureError(reason: String) : SttError("Audio capture failed: $reason")
    class InferenceFailed(reason: String) : SttError("STT inference failed: $reason")
    class PermissionDenied : SttError("Microphone permission denied")
}
