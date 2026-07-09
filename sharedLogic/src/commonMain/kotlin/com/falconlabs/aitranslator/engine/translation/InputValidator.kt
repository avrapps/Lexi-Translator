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
 * Validates and preprocesses translation input (Requirement 3.1, 3.3, 3.9, 3.10).
 */
object InputValidator {
    /** Maximum input length for text translation (Requirement 3.1). */
    const val MAX_TRANSLATION_CHARS = 10_000

    /** Minimum characters needed for language detection (Requirement 3.3). */
    const val MIN_DETECTION_CHARS = 3

    /** Maximum word count for dictionary display (Requirement 3.9). */
    const val DICTIONARY_WORD_THRESHOLD = 5

    /** Validates input text is within the maximum character limit. */
    fun validateLength(text: String): Boolean = text.length <= MAX_TRANSLATION_CHARS

    /** Returns true if input has enough characters for reliable language detection. */
    fun canDetectLanguage(text: String): Boolean = text.trim().length >= MIN_DETECTION_CHARS

    /** Returns true if input is short enough to display dictionary info. */
    fun shouldShowDictionary(text: String): Boolean {
        val wordCount = text.trim().split("\\s+".toRegex()).size
        return wordCount in 1..DICTIONARY_WORD_THRESHOLD
    }

    /**
     * Returns true if source and target languages use different writing systems.
     * This triggers transliteration display (Requirement 3.10).
     */
    fun requiresTransliteration(source: LanguageCode, target: LanguageCode): Boolean {
        val latinScripts = setOf("en", "de", "fr", "es", "it", "pt", "nl", "pl")
        val nonLatinScripts = setOf("zh", "ja", "ko", "ar", "hi", "th", "ru", "el")

        val sourceIsLatin = source.code in latinScripts
        val targetIsLatin = target.code in latinScripts
        val sourceIsNonLatin = source.code in nonLatinScripts
        val targetIsNonLatin = target.code in nonLatinScripts

        return (sourceIsLatin && targetIsNonLatin) || (sourceIsNonLatin && targetIsLatin)
    }

    /** Counts words in input text. */
    fun wordCount(text: String): Int = text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
}
