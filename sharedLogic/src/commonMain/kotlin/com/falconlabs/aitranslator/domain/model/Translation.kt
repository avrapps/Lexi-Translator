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

package com.falconlabs.aitranslator.domain.model

/** Translation engine quality/speed mode (Requirement 3.6). */
enum class TranslationMode { DEFAULT, FAST, ACCURATE, EXPERIMENTAL }

/** Translation confidence level (Requirement 3.11). */
enum class TranslationConfidence { LOW, MEDIUM, HIGH }

/**
 * Input request for a translation operation (Requirement 3.1).
 * Max 10,000 characters per request.
 */
data class TranslationRequest(
    val text: String,
    val sourceLang: LanguageCode?,
    val targetLang: LanguageCode,
    val mode: TranslationMode = TranslationMode.DEFAULT,
)

/**
 * Output of a translation operation (Requirement 3.2).
 * Includes the primary translation, up to 5 alternatives,
 * confidence level, transliteration for cross-script pairs,
 * and an optional dictionary entry for short inputs.
 */
data class TranslationResult(
    val translatedText: String,
    val detectedSourceLang: LanguageCode?,
    val alternatives: List<String> = emptyList(),
    val confidence: TranslationConfidence,
    val transliteration: String? = null,
    val dictionaryEntry: DictionaryEntry? = null,
    val durationMs: Long = 0L,
)

/**
 * Result of a language detection operation (Requirement 3.3, 3.4).
 * Confidence is 0.0-1.0; below threshold the user must pick manually.
 */
data class LanguageDetectionResult(
    val detectedLang: LanguageCode?,
    val confidence: Float,
    val isConfident: Boolean,
)

/**
 * Rich dictionary entry displayed for short inputs (≤5 words) (Requirement 3.9).
 */
data class DictionaryEntry(
    val word: String,
    val meaning: String,
    val usageExamples: List<String> = emptyList(),
    val grammarNotes: String? = null,
    val gender: String? = null,
    val formality: Formality = Formality.NEUTRAL,
)

/** Formality register for dictionary entries (Requirement 3.9). */
enum class Formality { FORMAL, INFORMAL, NEUTRAL }
