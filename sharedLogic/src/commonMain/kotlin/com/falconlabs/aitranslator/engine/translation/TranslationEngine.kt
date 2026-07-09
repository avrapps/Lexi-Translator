/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * COPYLEFT: Using any part of this code requires you to publish your
 * ENTIRE source code under AGPL-3.0. No exceptions. No closed-source use.
 */

package com.falconlabs.aitranslator.engine.translation

import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.LanguageDetectionResult
import com.falconlabs.aitranslator.domain.model.TranslationRequest
import com.falconlabs.aitranslator.domain.model.TranslationResult

/**
 * Platform-agnostic interface for on-device neural translation.
 * Implementations use ONNX Runtime with OPUS-MT int8 models.
 *
 * Supports Requirements 3.2 (translation) and 3.3 (language detection).
 */
interface TranslationEngine {
    /**
     * Translates the given text and returns a [TranslationResult].
     * Must complete within 300ms for inputs ≤500 characters on a reference device.
     *
     * @throws TranslationException if the model is not loaded or inference fails.
     */
    suspend fun translate(request: TranslationRequest): TranslationResult

    /**
     * Detects the language of the input text.
     * Requires at least 3 characters for reliable detection.
     *
     * @return [LanguageDetectionResult] with detected language and confidence.
     */
    suspend fun detectLanguage(text: String): LanguageDetectionResult

    /**
     * Returns the list of language pair model IDs currently loaded and ready to use.
     */
    fun getAvailableLanguagePairs(): List<Pair<LanguageCode, LanguageCode>>

    /**
     * Returns true if a model for the given source→target pair is installed and ready.
     */
    fun isModelAvailable(source: LanguageCode, target: LanguageCode): Boolean
}

/** Thrown when translation fails for any reason (Requirement 3.12). */
sealed class TranslationException(message: String) : Exception(message) {
    class ModelNotInstalled(val pair: Pair<String, String>) :
        TranslationException("Model not installed for: ${pair.first} → ${pair.second}")
    class InferenceFailed(val reason: String) : TranslationException("Translation inference failed: $reason")
    class InputTooLong(val length: Int, val maxLength: Int) :
        TranslationException("Input too long: $length chars (max $maxLength)")
}
