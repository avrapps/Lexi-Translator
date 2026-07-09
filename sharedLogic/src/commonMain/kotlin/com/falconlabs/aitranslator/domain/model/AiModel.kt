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

/** Category of an AI model within the Lexi platform. */
enum class ModelCategory { TRANSLATION, STT, TTS }

/** Inference engine type used to execute a model. */
enum class EngineType { OPUS_MT, WHISPER, KOKORO, PIPER, VITS }

/** Relative CPU demand level for running a model. */
enum class CpuRequirement { LOW, MEDIUM, HIGH }

/**
 * Metadata describing an AI model available in the Model Store.
 * Corresponds to Requirement 6.2 (model metadata display).
 */
data class AiModel(
    val id: ModelId,
    val name: String,
    val category: ModelCategory,
    val version: String,
    val sizeBytes: Long,
    val languagePair: LanguagePair?,
    val engineType: EngineType,
    val qualityRating: Float,
    val ramRequirementMb: Int,
    val cpuRequirement: CpuRequirement,
    val license: String,
    val publisher: String
)

/**
 * Represents a model that has been downloaded and installed on the device.
 * Tracks file location, integrity checksum, and usage timestamps.
 */
data class InstalledModel(
    val id: ModelId,
    val name: String,
    val category: ModelCategory,
    val version: String,
    val sizeBytes: Long,
    val languagePair: LanguagePair?,
    val engineType: EngineType,
    val filePath: String,
    val sha256Checksum: String,
    val installedAt: Long,
    val lastUsedAt: Long?,
    val qualityRating: Float,
    val ramRequirementMb: Int
)
