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

package com.falconlabs.aitranslator.ui.models.viewmodel

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.CpuRequirement
import com.falconlabs.aitranslator.domain.model.EngineType
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.LanguagePair
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId

/**
 * Hardcoded mock models for the Model Store screen.
 * Provides representative data for Translation, STT, and TTS categories.
 */
object MockModelData {

    private const val MB = 1_000_000L

    val translationModels: List<AiModel> = listOf(
        AiModel(
            id = ModelId("opus-mt-en-de"),
            name = "Xenova/opus-mt-en-de",
            category = ModelCategory.TRANSLATION,
            version = "1.0.0",
            sizeBytes = 32 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("de")),
            engineType = EngineType.OPUS_MT,
            qualityRating = 4.2f,
            ramRequirementMb = 80,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "Xenova",
        ),
        AiModel(
            id = ModelId("opus-mt-en-fr"),
            name = "Xenova/opus-mt-en-fr",
            category = ModelCategory.TRANSLATION,
            version = "1.0.0",
            sizeBytes = 34 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("fr")),
            engineType = EngineType.OPUS_MT,
            qualityRating = 4.5f,
            ramRequirementMb = 85,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "Xenova",
        ),
        AiModel(
            id = ModelId("opus-mt-en-jap"),
            name = "Xenova/opus-mt-en-jap",
            category = ModelCategory.TRANSLATION,
            version = "1.0.0",
            sizeBytes = 38 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("ja")),
            engineType = EngineType.OPUS_MT,
            qualityRating = 3.8f,
            ramRequirementMb = 90,
            cpuRequirement = CpuRequirement.MEDIUM,
            license = "MIT",
            publisher = "Xenova",
        ),
        AiModel(
            id = ModelId("opus-mt-en-es"),
            name = "Xenova/opus-mt-en-es",
            category = ModelCategory.TRANSLATION,
            version = "1.0.0",
            sizeBytes = 31 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("es")),
            engineType = EngineType.OPUS_MT,
            qualityRating = 4.6f,
            ramRequirementMb = 78,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "Xenova",
        ),
        AiModel(
            id = ModelId("opus-mt-en-zh"),
            name = "Xenova/opus-mt-en-zh",
            category = ModelCategory.TRANSLATION,
            version = "1.0.0",
            sizeBytes = 36 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("zh")),
            engineType = EngineType.OPUS_MT,
            qualityRating = 4.0f,
            ramRequirementMb = 88,
            cpuRequirement = CpuRequirement.MEDIUM,
            license = "MIT",
            publisher = "Xenova",
        ),
        AiModel(
            id = ModelId("opus-mt-de-en"),
            name = "Xenova/opus-mt-de-en",
            category = ModelCategory.TRANSLATION,
            version = "1.0.0",
            sizeBytes = 33 * MB,
            languagePair = LanguagePair(LanguageCode("de"), LanguageCode("en")),
            engineType = EngineType.OPUS_MT,
            qualityRating = 4.3f,
            ramRequirementMb = 82,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "Xenova",
        ),
    )

    val sttModels: List<AiModel> = listOf(
        AiModel(
            id = ModelId("whisper-tiny"),
            name = "Whisper Tiny",
            category = ModelCategory.STT,
            version = "1.0.0",
            sizeBytes = 75 * MB,
            languagePair = null,
            engineType = EngineType.WHISPER,
            qualityRating = 3.2f,
            ramRequirementMb = 150,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "OpenAI",
        ),
        AiModel(
            id = ModelId("whisper-small"),
            name = "Whisper Small",
            category = ModelCategory.STT,
            version = "1.0.0",
            sizeBytes = 240 * MB,
            languagePair = null,
            engineType = EngineType.WHISPER,
            qualityRating = 4.0f,
            ramRequirementMb = 350,
            cpuRequirement = CpuRequirement.MEDIUM,
            license = "MIT",
            publisher = "OpenAI",
        ),
        AiModel(
            id = ModelId("whisper-medium"),
            name = "Whisper Medium",
            category = ModelCategory.STT,
            version = "1.0.0",
            sizeBytes = 750 * MB,
            languagePair = null,
            engineType = EngineType.WHISPER,
            qualityRating = 4.7f,
            ramRequirementMb = 800,
            cpuRequirement = CpuRequirement.HIGH,
            license = "MIT",
            publisher = "OpenAI",
        ),
    )

    val ttsModels: List<AiModel> = listOf(
        AiModel(
            id = ModelId("kokoro-en-female"),
            name = "Kokoro English Female",
            category = ModelCategory.TTS,
            version = "1.0.0",
            sizeBytes = 45 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("en")),
            engineType = EngineType.KOKORO,
            qualityRating = 4.5f,
            ramRequirementMb = 120,
            cpuRequirement = CpuRequirement.MEDIUM,
            license = "Apache-2.0",
            publisher = "Kokoro",
        ),
        AiModel(
            id = ModelId("piper-en-male"),
            name = "Piper English Male",
            category = ModelCategory.TTS,
            version = "1.0.0",
            sizeBytes = 28 * MB,
            languagePair = LanguagePair(LanguageCode("en"), LanguageCode("en")),
            engineType = EngineType.PIPER,
            qualityRating = 4.0f,
            ramRequirementMb = 80,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "Rhasspy",
        ),
        AiModel(
            id = ModelId("vits-ja-female"),
            name = "VITS Japanese Female",
            category = ModelCategory.TTS,
            version = "1.0.0",
            sizeBytes = 52 * MB,
            languagePair = LanguagePair(LanguageCode("ja"), LanguageCode("ja")),
            engineType = EngineType.VITS,
            qualityRating = 4.3f,
            ramRequirementMb = 140,
            cpuRequirement = CpuRequirement.MEDIUM,
            license = "MIT",
            publisher = "VITS Community",
        ),
        AiModel(
            id = ModelId("piper-es-female"),
            name = "Piper Spanish Female",
            category = ModelCategory.TTS,
            version = "1.0.0",
            sizeBytes = 30 * MB,
            languagePair = LanguagePair(LanguageCode("es"), LanguageCode("es")),
            engineType = EngineType.PIPER,
            qualityRating = 3.9f,
            ramRequirementMb = 85,
            cpuRequirement = CpuRequirement.LOW,
            license = "MIT",
            publisher = "Rhasspy",
        ),
    )
}
