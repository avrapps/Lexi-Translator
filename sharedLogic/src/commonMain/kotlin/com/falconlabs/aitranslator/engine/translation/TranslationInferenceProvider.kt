/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.engine.translation

import com.falconlabs.aitranslator.domain.model.LanguageCode

/**
 * Platform-specific ONNX inference provider for translation.
 * Implementations load the ONNX model and tokenizer, then run
 * encode → inference → decode to produce translated text.
 */
interface TranslationInferenceProvider {
    /**
     * Translates text from source to target language using the installed ONNX model.
     *
     * @param text Input text to translate.
     * @param source Source language code.
     * @param target Target language code.
     * @return Translated text, or null if model is not available.
     */
    suspend fun runInference(text: String, source: LanguageCode, target: LanguageCode): String?

    /**
     * Returns true if the model files for the given language pair are available on disk.
     */
    fun hasModel(source: LanguageCode, target: LanguageCode): Boolean
}
