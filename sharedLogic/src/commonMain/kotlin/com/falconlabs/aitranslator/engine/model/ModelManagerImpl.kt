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

import com.falconlabs.aitranslator.data.repository.ModelRepository
import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.DeleteResult
import com.falconlabs.aitranslator.domain.model.DeviceProfile
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.InstalledModel
import com.falconlabs.aitranslator.domain.model.IntegrityResult
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.domain.model.ModelRecommendation
import com.falconlabs.aitranslator.domain.model.StorageUsage

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Concrete implementation of [ModelManager] that orchestrates model download,
 * storage, integrity verification, and device-aware recommendations.
 *
 * Delegates download lifecycle to [DownloadManager] and persistence to [ModelRepository].
 */
class ModelManagerImpl(
    private val modelRepository: ModelRepository,
    private val downloadManager: DownloadManager,
    private val fileManager: DownloadFileManager,
    private val modelCatalog: ModelCatalogProvider,
    private val storageProvider: StorageInfoProvider,
    private val httpDownloader: HttpDownloader,
    private val scope: kotlinx.coroutines.CoroutineScope
) : ModelManager {

    override fun getAvailableModels(): Flow<List<AiModel>> = modelCatalog.getCatalog()

    override fun getInstalledModels(): Flow<List<InstalledModel>> = modelRepository.getAllInstalled()

    override suspend fun downloadModel(modelId: ModelId): Flow<DownloadProgress> {
        val model = modelCatalog.getModelById(modelId)
            ?: throw IllegalArgumentException("Model not found in catalog: ${modelId.id}")

        val destFilePath = storageProvider.getModelFilePath(modelId)
        val catalog = modelCatalog as? BundledModelCatalog
        val modelFiles = catalog?.getModelFiles(modelId) ?: emptyMap()

        if (modelFiles.size > 1) {
            // Multi-file model — download primary file via DownloadManager (with progress),
            // then download remaining files in background
            val primaryEntry = modelFiles.entries.first()
            val primaryUrl = primaryEntry.value
            val progressFlow = downloadManager.startDownload(model, primaryUrl, destFilePath, scope)

            // Download remaining files after primary completes
            scope.launch {
                val modelDir = java.io.File(destFilePath).parentFile ?: return@launch
                // Wait for primary file to exist
                val primaryFile = java.io.File(destFilePath)
                while (!primaryFile.exists()) {
                    delay(500)
                }

                for ((filename, url) in modelFiles) {
                    if (filename == primaryEntry.key) continue // skip primary
                    val file = java.io.File(modelDir, filename)
                    if (!file.exists()) {
                        try {
                            httpDownloader.download(url, file.absolutePath, 0L) { _, _ -> }
                        } catch (_: Exception) { /* non-fatal supplementary download */ }
                    }
                }
            }
            return progressFlow
        } else if (modelFiles.size == 1) {
            val url = modelFiles.values.first()
            return downloadManager.startDownload(model, url, destFilePath, scope)
        } else {
            // Fallback to getDownloadUrl
            val url = modelCatalog.getDownloadUrl(modelId)
            return downloadManager.startDownload(model, url, destFilePath, scope)
        }
    }

    private suspend fun downloadSupplementaryFile(url: String, destPath: String) {
        httpDownloader.download(url, destPath, 0L) { _, _ -> }
    }

    override suspend fun pauseDownload(modelId: ModelId) {
        downloadManager.pause(modelId)
    }

    override suspend fun resumeDownload(modelId: ModelId) {
        val model = modelCatalog.getModelById(modelId) ?: return
        val downloadUrl = modelCatalog.getDownloadUrl(modelId)
        val destFilePath = storageProvider.getModelFilePath(modelId)
        downloadManager.resume(model, downloadUrl, destFilePath, scope)
    }

    override suspend fun cancelDownload(modelId: ModelId) {
        val destFilePath = storageProvider.getModelFilePath(modelId)
        downloadManager.cancel(modelId, destFilePath)
    }

    override suspend fun deleteModel(modelId: ModelId): DeleteResult {
        return try {
            val installed = modelRepository.getInstalledById(modelId)
                ?: return DeleteResult.Error("Model not installed: ${modelId.id}")
            fileManager.deleteFile(installed.filePath)
            modelRepository.delete(modelId)
            DeleteResult.Success
        } catch (e: Exception) {
            DeleteResult.Error(e.message ?: "Unknown error during deletion")
        }
    }

    override suspend fun verifyIntegrity(modelId: ModelId): IntegrityResult {
        val installed = modelRepository.getInstalledById(modelId)
            ?: return IntegrityResult.Invalid("Model not installed", "")
        return downloadManager.verifyIntegrity(installed.filePath, installed.sha256Checksum)
    }

    override fun getStorageUsage(): Flow<StorageUsage> = modelRepository.getAllInstalled().map { models ->
        val totalUsed = models.sumOf { it.sizeBytes }
        val available = storageProvider.getAvailableStorageBytes()
        val perModel = models.associate { it.id to it.sizeBytes }
        StorageUsage(
            totalUsedBytes = totalUsed,
            availableBytes = available,
            perModelUsage = perModel
        )
    }

    override fun getRecommendations(deviceProfile: DeviceProfile): List<ModelRecommendation> =
        ModelRecommendationEngine.recommend(
            catalog = modelCatalog.getCatalogSnapshot(),
            deviceProfile = deviceProfile,
            limit = 10
        )
}

/**
 * Provides the model catalog — the list of all available models.
 * In production, this fetches from a bundled JSON or remote manifest.
 */
interface ModelCatalogProvider {
    /** Reactive flow of the full model catalog. */
    fun getCatalog(): Flow<List<AiModel>>

    /** Snapshot of the current catalog (non-reactive, for sync operations). */
    fun getCatalogSnapshot(): List<AiModel>

    /** Returns a specific model from the catalog by ID. */
    fun getModelById(modelId: ModelId): AiModel?

    /** Returns the download URL for a specific model. */
    fun getDownloadUrl(modelId: ModelId): String
}

/**
 * Provides platform-specific storage information (free space, model paths).
 */
interface StorageInfoProvider {
    /** Returns available storage in bytes on the models partition. */
    fun getAvailableStorageBytes(): Long

    /** Returns the file path where a model should be stored on this platform. */
    fun getModelFilePath(modelId: ModelId): String
}
