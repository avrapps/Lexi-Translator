/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.engine.model

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.CpuRequirement
import com.falconlabs.aitranslator.domain.model.EngineType
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.LanguagePair
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Bundled model catalog providing the list of all available AI models.
 * In v1, this is a static in-memory list. Future versions will fetch
 * from a remote manifest with local caching.
 *
 * Download URLs point to Hugging Face model repository (ONNX exports).
 */
class BundledModelCatalog : ModelCatalogProvider {

    private companion object {
        const val MB = 1_000_000L
        const val HF_BASE = "https://huggingface.co"
    }

    private val catalog: List<AiModel> = buildCatalog()
    private val catalogFlow = MutableStateFlow(catalog)

    override fun getCatalog(): Flow<List<AiModel>> = catalogFlow

    override fun getCatalogSnapshot(): List<AiModel> = catalog

    override fun getModelById(modelId: ModelId): AiModel? =
        catalog.find { it.id == modelId }

    override fun getDownloadUrl(modelId: ModelId): String {
        // Map model IDs to their Hugging Face ONNX download URLs
        return when (modelId.id) {
            "opus-mt-en-de" -> "$HF_BASE/Xenova/opus-mt-en-de/resolve/main/onnx/encoder_model_quantized.onnx"
            "opus-mt-en-fr" -> "$HF_BASE/Xenova/opus-mt-en-fr/resolve/main/onnx/encoder_model_quantized.onnx"
            "opus-mt-en-es" -> "$HF_BASE/Xenova/opus-mt-en-es/resolve/main/onnx/encoder_model_quantized.onnx"
            "opus-mt-en-ja" -> "$HF_BASE/Xenova/opus-mt-en-ja/resolve/main/onnx/encoder_model_quantized.onnx"
            "opus-mt-en-hi" -> "$HF_BASE/Xenova/opus-mt-en-hi/resolve/main/onnx/encoder_model_quantized.onnx"
            "opus-mt-en-zh" -> "$HF_BASE/Xenova/opus-mt-en-zh/resolve/main/onnx/encoder_model_quantized.onnx"
            "whisper-tiny" -> "$HF_BASE/Xenova/whisper-tiny/resolve/main/onnx/encoder_model_quantized.onnx"
            "whisper-small" -> "$HF_BASE/Xenova/whisper-small/resolve/main/onnx/encoder_model_quantized.onnx"
            "whisper-medium" -> "$HF_BASE/Xenova/whisper-medium/resolve/main/onnx/encoder_model_quantized.onnx"
            "kokoro-en-female" -> "$HF_BASE/hexgrad/Kokoro-82M/resolve/main/kokoro-v1.0.onnx"
            "piper-en-male" -> "$HF_BASE/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx"
            "vits-ja-female" -> "$HF_BASE/Xenova/mms-tts-jpn/resolve/main/onnx/model_quantized.onnx"
            else -> "$HF_BASE/Xenova/${modelId.id}/resolve/main/onnx/model_quantized.onnx"
        }
    }

    private fun buildCatalog(): List<AiModel> = listOf(
        // ── Translation Models ──────────────────────────────────────
        AiModel(
            id = ModelId("opus-mt-en-de"), name = "English → German",
            category = ModelCategory.TRANSLATION, version = "1.0.0",
            sizeBytes = 32 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("de")),
            engineType = EngineType.OPUS_MT, qualityRating = 4.2f,
            ramRequirementMb = 80, cpuRequirement = CpuRequirement.LOW,
            license = "CC-BY-4.0", publisher = "Helsinki-NLP"
        ),
        AiModel(
            id = ModelId("opus-mt-en-fr"), name = "English → French",
            category = ModelCategory.TRANSLATION, version = "1.0.0",
            sizeBytes = 34 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("fr")),
            engineType = EngineType.OPUS_MT, qualityRating = 4.4f,
            ramRequirementMb = 85, cpuRequirement = CpuRequirement.LOW,
            license = "CC-BY-4.0", publisher = "Helsinki-NLP"
        ),
        AiModel(
            id = ModelId("opus-mt-en-es"), name = "English → Spanish",
            category = ModelCategory.TRANSLATION, version = "1.0.0",
            sizeBytes = 33 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("es")),
            engineType = EngineType.OPUS_MT, qualityRating = 4.3f,
            ramRequirementMb = 82, cpuRequirement = CpuRequirement.LOW,
            license = "CC-BY-4.0", publisher = "Helsinki-NLP"
        ),
        AiModel(
            id = ModelId("opus-mt-en-ja"), name = "English → Japanese",
            category = ModelCategory.TRANSLATION, version = "1.0.0",
            sizeBytes = 38 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("ja")),
            engineType = EngineType.OPUS_MT, qualityRating = 3.8f,
            ramRequirementMb = 95, cpuRequirement = CpuRequirement.LOW,
            license = "CC-BY-4.0", publisher = "Helsinki-NLP"
        ),
        AiModel(
            id = ModelId("opus-mt-en-hi"), name = "English → Hindi",
            category = ModelCategory.TRANSLATION, version = "1.0.0",
            sizeBytes = 36 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("hi")),
            engineType = EngineType.OPUS_MT, qualityRating = 3.9f,
            ramRequirementMb = 90, cpuRequirement = CpuRequirement.LOW,
            license = "CC-BY-4.0", publisher = "Helsinki-NLP"
        ),
        AiModel(
            id = ModelId("opus-mt-en-zh"), name = "English → Chinese",
            category = ModelCategory.TRANSLATION, version = "1.0.0",
            sizeBytes = 40 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("zh")),
            engineType = EngineType.OPUS_MT, qualityRating = 4.0f,
            ramRequirementMb = 100, cpuRequirement = CpuRequirement.MEDIUM,
            license = "CC-BY-4.0", publisher = "Helsinki-NLP"
        ),

        // ── STT Models (Whisper) ────────────────────────────────────
        AiModel(
            id = ModelId("whisper-tiny"), name = "Whisper Tiny (Multilingual)",
            category = ModelCategory.STT, version = "1.0.0",
            sizeBytes = 75 * MB, languagePair = null,
            engineType = EngineType.WHISPER, qualityRating = 3.2f,
            ramRequirementMb = 200, cpuRequirement = CpuRequirement.LOW,
            license = "MIT", publisher = "OpenAI"
        ),
        AiModel(
            id = ModelId("whisper-small"), name = "Whisper Small (Multilingual)",
            category = ModelCategory.STT, version = "1.0.0",
            sizeBytes = 240 * MB, languagePair = null,
            engineType = EngineType.WHISPER, qualityRating = 4.0f,
            ramRequirementMb = 500, cpuRequirement = CpuRequirement.MEDIUM,
            license = "MIT", publisher = "OpenAI"
        ),
        AiModel(
            id = ModelId("whisper-medium"), name = "Whisper Medium (Multilingual)",
            category = ModelCategory.STT, version = "1.0.0",
            sizeBytes = 750 * MB, languagePair = null,
            engineType = EngineType.WHISPER, qualityRating = 4.6f,
            ramRequirementMb = 1200, cpuRequirement = CpuRequirement.HIGH,
            license = "MIT", publisher = "OpenAI"
        ),

        // ── TTS Models ──────────────────────────────────────────────
        AiModel(
            id = ModelId("kokoro-en-female"), name = "Kokoro English Female",
            category = ModelCategory.TTS, version = "1.0.0",
            sizeBytes = 45 * MB, languagePair = null,
            engineType = EngineType.KOKORO, qualityRating = 4.5f,
            ramRequirementMb = 150, cpuRequirement = CpuRequirement.LOW,
            license = "Apache-2.0", publisher = "hexgrad"
        ),
        AiModel(
            id = ModelId("piper-en-male"), name = "Piper English Male (Lessac)",
            category = ModelCategory.TTS, version = "1.0.0",
            sizeBytes = 62 * MB, languagePair = null,
            engineType = EngineType.PIPER, qualityRating = 4.1f,
            ramRequirementMb = 120, cpuRequirement = CpuRequirement.LOW,
            license = "MIT", publisher = "rhasspy"
        ),
        AiModel(
            id = ModelId("vits-ja-female"), name = "VITS Japanese Female",
            category = ModelCategory.TTS, version = "1.0.0",
            sizeBytes = 52 * MB, languagePair = null,
            engineType = EngineType.VITS, qualityRating = 3.9f,
            ramRequirementMb = 130, cpuRequirement = CpuRequirement.LOW,
            license = "CC-BY-4.0", publisher = "Facebook MMS"
        ),
    )
}
