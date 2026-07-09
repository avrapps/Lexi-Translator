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

package com.falconlabs.aitranslator.data.repository

import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.DownloadState
import com.falconlabs.aitranslator.domain.model.InstalledModel
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for CRUD operations on installed models and active downloads.
 * Abstracts the underlying data source (SQLDelight) behind a clean domain API.
 *
 * Supports Requirements 6.1 (Model Store) and 6.6 (storage usage tracking).
 */
interface ModelRepository {
    /** Emits all installed models, updating reactively on changes. */
    fun getAllInstalled(): Flow<List<InstalledModel>>

    /** Returns a single installed model by ID, or null if not found. */
    fun getInstalledById(id: ModelId): InstalledModel?

    /** Emits installed models filtered by category, updating reactively. */
    fun getInstalledByCategory(category: ModelCategory): Flow<List<InstalledModel>>

    /** Persists a newly installed model record. */
    suspend fun insert(model: InstalledModel)

    /** Updates the last-used timestamp for a model. */
    suspend fun updateLastUsed(id: ModelId, timestamp: Long)

    /** Removes an installed model record by ID. */
    suspend fun delete(id: ModelId)

    /** Emits currently active (non-completed, non-failed) downloads reactively. */
    fun getActiveDownloads(): Flow<List<DownloadProgress>>

    /** Creates a new download record for the given model. */
    suspend fun insertDownload(modelId: ModelId, totalBytes: Long)

    /** Updates download progress bytes and state for an active download. */
    suspend fun updateDownloadProgress(
        modelId: ModelId,
        bytesDownloaded: Long,
        state: DownloadState
    )

    /** Removes a download record (completed or cancelled). */
    suspend fun deleteDownload(modelId: ModelId)
}
