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

/**
 * Platform-specific Whisper inference provider.
 * Implementations load the Whisper ONNX model and run mel spectrogram + encoder + decoder.
 */
interface WhisperInferenceProvider {
    /** Transcribes audio samples to text. Returns null if model not available. */
    suspend fun transcribe(samples: FloatArray, languageCode: String): String?

    /** Returns true if the Whisper model is loaded and ready. */
    fun isReady(): Boolean

    /** Loads the Whisper model from disk. */
    suspend fun load()
}
