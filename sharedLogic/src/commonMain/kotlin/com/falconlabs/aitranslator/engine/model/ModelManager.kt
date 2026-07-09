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

package com.falconlabs.aitranslator.engine.model

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.DeleteResult
import com.falconlabs.aitranslator.domain.model.DeviceProfile
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.InstalledModel
import com.falconlabs.aitranslator.domain.model.IntegrityResult
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.domain.model.ModelRecommendation
import com.falconlabs.aitranslator.domain.model.StorageUsage
import kotlinx.coroutines.flow.Flow

/**
 * Core interface for managing AI model lifecycle: discovery, download,
 * integrity verification, deletion, and device-aware recommendations.
 *
 * Platform-specific implementations provide actual download and storage logic.
 * Supports Requirements 6.1 (Model Store display) and 6.6 (storage tracking).
 */
interface ModelManager {
    /** Emits the full catalog of models available for download. */
    fun getAvailableModels(): Flow<List<AiModel>>

    /** Emits the list of models currently installed on this device. */
    fun getInstalledModels(): Flow<List<InstalledModel>>

    /** Initiates a model download and returns a Flow emitting progress updates. */
    suspend fun downloadModel(modelId: ModelId): Flow<DownloadProgress>

    /** Pauses an active download, retaining partial file data for resume. */
    suspend fun pauseDownload(modelId: ModelId)

    /** Resumes a previously paused download from the last byte position. */
    suspend fun resumeDownload(modelId: ModelId)

    /** Cancels an active or paused download and removes partial data. */
    suspend fun cancelDownload(modelId: ModelId)

    /** Deletes an installed model from disk and database. */
    suspend fun deleteModel(modelId: ModelId): DeleteResult

    /** Verifies SHA-256 checksum integrity of an installed model. */
    suspend fun verifyIntegrity(modelId: ModelId): IntegrityResult

    /** Emits aggregated storage usage for all installed models. */
    fun getStorageUsage(): Flow<StorageUsage>

    /** Returns ranked model recommendations based on device capabilities. */
    fun getRecommendations(deviceProfile: DeviceProfile): List<ModelRecommendation>
}
