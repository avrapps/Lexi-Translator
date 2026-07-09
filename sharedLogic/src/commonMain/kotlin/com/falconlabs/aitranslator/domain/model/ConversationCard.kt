/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.domain.model

/**
 * A single utterance in a live interpreter conversation.
 * Contains source text, translated text, and metadata.
 *
 * Supports Requirement 2.11 (ConversationCard with complete metadata).
 */
data class ConversationCard(
    val id: String,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: LanguageCode,
    val targetLanguage: LanguageCode,
    val confidence: TranslationConfidence,
    val timestamp: Long,
    val durationMs: Long = 0L,
)
