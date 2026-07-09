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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.falconlabs.aitranslator.db.LexiDatabase
import com.falconlabs.aitranslator.db.Installed_model
import com.falconlabs.aitranslator.db.Model_download
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.DownloadState
import com.falconlabs.aitranslator.domain.model.EngineType
import com.falconlabs.aitranslator.domain.model.InstalledModel
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.LanguagePair
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.util.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * SQLDelight-backed implementation of [ModelRepository].
 * Maps between generated SQLDelight row types and domain models.
 * Uses `asFlow().mapToList()` for reactive query observation.
 *
 * @param database The SQLDelight [LexiDatabase] instance.
 */
class SqlDelightModelRepository(
    private val database: LexiDatabase
) : ModelRepository {

    private val modelQueries get() = database.lexiQueries

    override fun getAllInstalled(): Flow<List<InstalledModel>> =
        modelQueries.selectAllModels()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getInstalledById(id: ModelId): InstalledModel? =
        modelQueries.selectModelById(id.id).executeAsOneOrNull()?.toDomain()

    override fun getInstalledByCategory(category: ModelCategory): Flow<List<InstalledModel>> =
        modelQueries.selectModelsByCategory(category.name)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun insert(model: InstalledModel): Unit = withContext(Dispatchers.Default) {
        modelQueries.insertModel(
            id = model.id.id,
            name = model.name,
            category = model.category.name,
            version = model.version,
            size_bytes = model.sizeBytes,
            source_lang = model.languagePair?.source?.code,
            target_lang = model.languagePair?.target?.code,
            engine_type = model.engineType.name,
            file_path = model.filePath,
            sha256_checksum = model.sha256Checksum,
            installed_at = model.installedAt,
            last_used_at = model.lastUsedAt,
            quality_rating = model.qualityRating.toDouble(),
            ram_requirement_mb = model.ramRequirementMb.toLong()
        )
    }

    override suspend fun updateLastUsed(id: ModelId, timestamp: Long): Unit = withContext(Dispatchers.Default) {
        modelQueries.updateLastUsed(last_used_at = timestamp, id = id.id)
    }

    override suspend fun delete(id: ModelId): Unit = withContext(Dispatchers.Default) {
        modelQueries.deleteModel(id.id)
    }

    override fun getActiveDownloads(): Flow<List<DownloadProgress>> =
        modelQueries.selectActiveDownloads()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDownloadProgress() } }

    override suspend fun insertDownload(modelId: ModelId, totalBytes: Long): Unit = withContext(Dispatchers.Default) {
        val now = currentTimeMillis()
        modelQueries.insertDownload(
            model_id = modelId.id,
            state = DownloadState.QUEUED.name,
            bytes_downloaded = 0L,
            total_bytes = totalBytes,
            partial_file_path = null,
            started_at = now,
            updated_at = now,
            error_message = null
        )
    }

    override suspend fun updateDownloadProgress(
        modelId: ModelId,
        bytesDownloaded: Long,
        state: DownloadState
    ): Unit = withContext(Dispatchers.Default) {
        modelQueries.updateDownloadProgress(
            bytes_downloaded = bytesDownloaded,
            state = state.name,
            updated_at = currentTimeMillis(),
            model_id = modelId.id
        )
    }

    override suspend fun deleteDownload(modelId: ModelId): Unit = withContext(Dispatchers.Default) {
        modelQueries.deleteDownload(modelId.id)
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun Installed_model.toDomain(): InstalledModel = InstalledModel(
        id = ModelId(id),
        name = name,
        category = ModelCategory.valueOf(category),
        version = version,
        sizeBytes = size_bytes,
        languagePair = if (source_lang != null && target_lang != null) {
            LanguagePair(LanguageCode(source_lang), LanguageCode(target_lang))
        } else {
            null
        },
        engineType = engine_type?.let { EngineType.valueOf(it) } ?: EngineType.OPUS_MT,
        filePath = file_path,
        sha256Checksum = sha256_checksum,
        installedAt = installed_at,
        lastUsedAt = last_used_at,
        qualityRating = quality_rating?.toFloat() ?: 0f,
        ramRequirementMb = ram_requirement_mb?.toInt() ?: 0
    )

    private fun Model_download.toDownloadProgress(): DownloadProgress = DownloadProgress(
        modelId = ModelId(model_id),
        bytesDownloaded = bytes_downloaded,
        totalBytes = total_bytes,
        speedBytesPerSec = 0L, // Calculated at a higher layer in the download manager
        state = DownloadState.valueOf(state),
        estimatedSecondsRemaining = null
    )
}
