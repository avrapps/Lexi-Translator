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

import com.falconlabs.aitranslator.data.repository.ModelRepository
import com.falconlabs.aitranslator.domain.model.DictionaryEntry
import com.falconlabs.aitranslator.domain.model.Formality
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.LanguageDetectionResult
import com.falconlabs.aitranslator.domain.model.TranslationConfidence
import com.falconlabs.aitranslator.domain.model.TranslationRequest
import com.falconlabs.aitranslator.domain.model.TranslationResult
import com.falconlabs.aitranslator.util.currentTimeMillis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Offline translation engine that uses downloaded OPUS-MT ONNX models.
 * Delegates actual inference to [TranslationInferenceProvider] which handles
 * ONNX Runtime session management and tokenization on each platform.
 *
 * The engine checks ModelRepository to verify the required language pair model
 * is installed before attempting translation.
 */
class OfflineTranslationEngine(
    private val modelRepository: ModelRepository,
    private val inferenceProvider: TranslationInferenceProvider,
) : TranslationEngine {

    override suspend fun translate(request: TranslationRequest): TranslationResult {
        val startTime = currentTimeMillis()

        // Validate input length
        if (!InputValidator.validateLength(request.text)) {
            throw TranslationException.InputTooLong(
                request.text.length,
                InputValidator.MAX_TRANSLATION_CHARS
            )
        }

        val sourceLang = request.sourceLang
            ?: detectLanguage(request.text).detectedLang
            ?: LanguageCode("en")

        // Check model availability
        if (!isModelAvailable(sourceLang, request.targetLang)) {
            throw TranslationException.ModelNotInstalled(
                sourceLang.code to request.targetLang.code
            )
        }

        // Perform ONNX inference (placeholder — actual ONNX Runtime integration in future task)
        val translated = performInference(request.text, sourceLang, request.targetLang)
        val durationMs = currentTimeMillis() - startTime

        // Build dictionary entry for short inputs
        val dictionary = if (InputValidator.shouldShowDictionary(request.text)) {
            buildDictionaryEntry(request.text, request.targetLang)
        } else {
            null
        }

        // Determine transliteration need
        val transliteration = if (InputValidator.requiresTransliteration(sourceLang, request.targetLang)) {
            transliterate(translated, request.targetLang)
        } else {
            null
        }

        return TranslationResult(
            translatedText = translated,
            detectedSourceLang = sourceLang,
            alternatives = generateAlternatives(request.text, sourceLang, request.targetLang),
            confidence = computeConfidence(request.text.length, durationMs),
            transliteration = transliteration,
            dictionaryEntry = dictionary,
            durationMs = durationMs,
        )
    }

    override suspend fun detectLanguage(text: String): LanguageDetectionResult {
        if (!InputValidator.canDetectLanguage(text)) {
            return LanguageDetectionResult(null, 0f, false)
        }
        // Simple heuristic-based detection (placeholder for full language-id model)
        val detectedLang = heuristicDetect(text)
        val confidence = if (text.length > 20) 0.92f else 0.65f
        return LanguageDetectionResult(
            detectedLang = detectedLang,
            confidence = confidence,
            isConfident = confidence >= 0.7f,
        )
    }

    override fun getAvailableLanguagePairs(): List<Pair<LanguageCode, LanguageCode>> {
        // Will be populated from installed models reactively
        return listOf(
            LanguageCode("en") to LanguageCode("de"),
            LanguageCode("en") to LanguageCode("fr"),
            LanguageCode("en") to LanguageCode("es"),
            LanguageCode("en") to LanguageCode("ja"),
            LanguageCode("en") to LanguageCode("hi"),
            LanguageCode("en") to LanguageCode("zh"),
        )
    }

    override fun isModelAvailable(source: LanguageCode, target: LanguageCode): Boolean =
        inferenceProvider.hasModel(source, target)

    /**
     * Runs ONNX inference via the platform-specific provider.
     * For long text, splits into sentences and translates each one individually
     * to avoid the 512-token model limit and prevent repetition loops.
     */
    private suspend fun performInference(
        text: String,
        source: LanguageCode,
        target: LanguageCode
    ): String = withContext(Dispatchers.Default) {
        val sentences = splitIntoSentences(text)

        if (sentences.size <= 1) {
            val result = inferenceProvider.runInference(text.trim(), source, target)
            result ?: throw TranslationException.ModelNotInstalled(source.code to target.code)
        } else {
            // Translate sentence by sentence, join with spaces
            val translated = sentences.mapNotNull { sentence ->
                val trimmed = sentence.trim()
                if (trimmed.isEmpty()) {
                    null
                } else {
                    inferenceProvider.runInference(trimmed, source, target)
                }
            }
            if (translated.isEmpty()) throw TranslationException.ModelNotInstalled(source.code to target.code)
            translated.joinToString(" ")
        }
    }

    /**
     * Splits text into sentences on sentence-ending punctuation.
     * Preserves punctuation at the end of each sentence.
     */
    private fun splitIntoSentences(text: String): List<String> {
        // Split on . ! ? followed by space or newline, keeping the delimiter
        val sentences = mutableListOf<String>()
        val pattern = "(?<=[.!?])\\s+".toRegex()
        val parts = text.trim().split(pattern)
        parts.forEach { part ->
            val trimmed = part.trim()
            if (trimmed.isNotEmpty()) sentences.add(trimmed)
        }
        return sentences.ifEmpty { listOf(text) }
    }

    private fun generateAlternatives(
        text: String,
        source: LanguageCode,
        target: LanguageCode
    ): List<String> {
        // Placeholder: real engine produces beam search alternatives
        if (InputValidator.wordCount(text) > 10) return emptyList()
        return listOf("[${target.code} alt] $text").take(5)
    }

    private fun computeConfidence(inputLength: Int, durationMs: Long): TranslationConfidence = when {
        inputLength < 5 -> TranslationConfidence.LOW
        durationMs < 200 -> TranslationConfidence.HIGH
        else -> TranslationConfidence.MEDIUM
    }

    private fun transliterate(text: String, targetLang: LanguageCode): String {
        // Placeholder: real implementation uses transliteration model/library
        return text
    }

    private fun buildDictionaryEntry(text: String, targetLang: LanguageCode): DictionaryEntry {
        // Placeholder: real implementation queries an offline dictionary DB
        return DictionaryEntry(
            word = text.trim(),
            meaning = "Translation of '${text.trim()}'",
            usageExamples = listOf("Example sentence using '${text.trim()}'"),
            grammarNotes = null,
            gender = null,
            formality = Formality.NEUTRAL,
        )
    }

    private fun heuristicDetect(text: String): LanguageCode {
        // Very basic character-set heuristic (placeholder for real language-id model)
        val lower = text.lowercase()
        return when {
            lower.any { it in '\u3040'..'\u30FF' || it in '\u4E00'..'\u9FFF' } -> LanguageCode("ja")
            lower.any { it in '\u4E00'..'\u9FFF' } -> LanguageCode("zh")
            lower.any { it in '\u0900'..'\u097F' } -> LanguageCode("hi")
            lower.any { it in '\u0600'..'\u06FF' } -> LanguageCode("ar")
            lower.contains("the ") || lower.contains(" is ") -> LanguageCode("en")
            lower.contains(" der ") || lower.contains(" die ") -> LanguageCode("de")
            lower.contains(" le ") || lower.contains(" la ") -> LanguageCode("fr")
            lower.contains(" el ") || lower.contains(" los ") -> LanguageCode("es")
            else -> LanguageCode("en")
        }
    }
}
