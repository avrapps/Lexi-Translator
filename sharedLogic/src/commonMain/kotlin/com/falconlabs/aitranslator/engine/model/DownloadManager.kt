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
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.DownloadState
import com.falconlabs.aitranslator.domain.model.IntegrityResult
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.util.currentTimeMillis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages the download lifecycle for AI models including pause, resume,
 * cancel, and SHA-256 integrity verification after completion.
 *
 * State machine: QUEUED -> DOWNLOADING -> PAUSED -> VERIFYING -> COMPLETED/FAILED
 */
class DownloadManager(
    private val modelRepository: ModelRepository,
    private val httpDownloader: HttpDownloader,
    private val checksumVerifier: ChecksumVerifier,
    private val fileManager: DownloadFileManager
) {
    private val _activeDownloads = MutableStateFlow<Map<ModelId, DownloadProgress>>(emptyMap())

    /** Emits a map of all active downloads keyed by model ID. */
    val activeDownloads: Flow<Map<ModelId, DownloadProgress>> = _activeDownloads.asStateFlow()

    private var downloadStartTimeMs: Long = 0L

    /**
     * Initiates a model download in the given [scope].
     * Returns immediately with a Flow that emits progress updates for this model.
     * The actual HTTP download runs in a background coroutine.
     */
    suspend fun startDownload(
        model: AiModel,
        downloadUrl: String,
        destFilePath: String,
        scope: CoroutineScope
    ): Flow<DownloadProgress> {
        val modelId = model.id

        // Insert download record (delete any existing stale record first)
        try {
            modelRepository.deleteDownload(modelId)
        } catch (_: Exception) {}
        modelRepository.insertDownload(modelId, model.sizeBytes)
        updateProgress(modelId, 0L, model.sizeBytes, DownloadState.QUEUED)

        // Launch download in background so we can return the flow immediately
        scope.launch {
            updateProgress(modelId, 0L, model.sizeBytes, DownloadState.DOWNLOADING)
            modelRepository.updateDownloadProgress(modelId, 0L, DownloadState.DOWNLOADING)
            downloadStartTimeMs = currentTimeMillis()

            try {
                httpDownloader.download(
                    url = downloadUrl,
                    destFile = destFilePath,
                    startByte = 0L
                ) { bytesDownloaded, totalBytes ->
                    val speed = calculateSpeed(bytesDownloaded)
                    val eta = calculateEta(bytesDownloaded, totalBytes, speed)
                    val progress = DownloadProgress(
                        modelId = modelId,
                        bytesDownloaded = bytesDownloaded,
                        totalBytes = totalBytes,
                        speedBytesPerSec = speed,
                        state = DownloadState.DOWNLOADING,
                        estimatedSecondsRemaining = eta
                    )
                    _activeDownloads.update { it + (modelId to progress) }
                }

                // Download complete - mark as installed (skip checksum for HF models without known hash)
                updateProgress(modelId, model.sizeBytes, model.sizeBytes, DownloadState.VERIFYING)
                modelRepository.updateDownloadProgress(modelId, model.sizeBytes, DownloadState.VERIFYING)

                // Compute actual checksum of the downloaded file
                val actualChecksum = checksumVerifier.computeSha256(destFilePath)

                // Mark completed
                updateProgress(modelId, model.sizeBytes, model.sizeBytes, DownloadState.COMPLETED)
                modelRepository.updateDownloadProgress(modelId, model.sizeBytes, DownloadState.COMPLETED)

                // Insert into installed_model table
                val installedModel = com.falconlabs.aitranslator.domain.model.InstalledModel(
                    id = modelId,
                    name = model.name,
                    category = model.category,
                    version = model.version,
                    sizeBytes = model.sizeBytes,
                    languagePair = model.languagePair,
                    engineType = model.engineType,
                    filePath = destFilePath,
                    sha256Checksum = actualChecksum,
                    installedAt = currentTimeMillis(),
                    lastUsedAt = null,
                    qualityRating = model.qualityRating,
                    ramRequirementMb = model.ramRequirementMb
                )
                modelRepository.insert(installedModel)

                // Clean up download record and active state
                modelRepository.deleteDownload(modelId)
                _activeDownloads.update { it - modelId }
            } catch (e: Exception) {
                val currentState = _activeDownloads.value[modelId]?.state
                if (currentState != DownloadState.PAUSED) {
                    updateProgress(
                        modelId,
                        _activeDownloads.value[modelId]?.bytesDownloaded ?: 0L,
                        model.sizeBytes,
                        DownloadState.FAILED
                    )
                    modelRepository.updateDownloadProgress(
                        modelId,
                        _activeDownloads.value[modelId]?.bytesDownloaded ?: 0L,
                        DownloadState.FAILED
                    )
                }
            }
        }

        // Return a flow that emits progress for this specific model
        return _activeDownloads.map { it[modelId] }.map {
            it ?: DownloadProgress(
                modelId = modelId,
                bytesDownloaded = 0L,
                totalBytes = model.sizeBytes,
                speedBytesPerSec = 0L,
                state = DownloadState.QUEUED,
                estimatedSecondsRemaining = null
            )
        }
    }

    /** Pauses an active download. Retains partial file for resume. */
    suspend fun pause(modelId: ModelId) {
        httpDownloader.cancel()
        val current = _activeDownloads.value[modelId] ?: return
        updateProgress(modelId, current.bytesDownloaded, current.totalBytes, DownloadState.PAUSED)
        modelRepository.updateDownloadProgress(modelId, current.bytesDownloaded, DownloadState.PAUSED)
    }

    /** Resumes a paused download from the last byte position. */
    suspend fun resume(
        model: AiModel,
        downloadUrl: String,
        destFilePath: String,
        scope: CoroutineScope
    ) {
        val modelId = model.id
        val current = _activeDownloads.value[modelId] ?: return
        val startByte = current.bytesDownloaded

        updateProgress(modelId, startByte, current.totalBytes, DownloadState.DOWNLOADING)
        modelRepository.updateDownloadProgress(modelId, startByte, DownloadState.DOWNLOADING)
        downloadStartTimeMs = currentTimeMillis()

        scope.launch {
            try {
                httpDownloader.download(
                    url = downloadUrl,
                    destFile = destFilePath,
                    startByte = startByte
                ) { bytesDownloaded, totalBytes ->
                    val speed = calculateSpeed(bytesDownloaded - startByte)
                    val eta = calculateEta(bytesDownloaded, totalBytes, speed)
                    val progress = DownloadProgress(
                        modelId = modelId,
                        bytesDownloaded = bytesDownloaded,
                        totalBytes = totalBytes,
                        speedBytesPerSec = speed,
                        state = DownloadState.DOWNLOADING,
                        estimatedSecondsRemaining = eta
                    )
                    _activeDownloads.update { it + (modelId to progress) }
                }

                // Verify after resume completes
                updateProgress(modelId, model.sizeBytes, model.sizeBytes, DownloadState.VERIFYING)
                modelRepository.updateDownloadProgress(modelId, model.sizeBytes, DownloadState.VERIFYING)

                val result = checksumVerifier.verify(destFilePath, model.id.id)
                when (result) {
                    is IntegrityResult.Valid -> {
                        updateProgress(modelId, model.sizeBytes, model.sizeBytes, DownloadState.COMPLETED)
                        modelRepository.updateDownloadProgress(modelId, model.sizeBytes, DownloadState.COMPLETED)
                    }
                    is IntegrityResult.Invalid -> {
                        updateProgress(modelId, model.sizeBytes, model.sizeBytes, DownloadState.FAILED)
                        modelRepository.updateDownloadProgress(modelId, model.sizeBytes, DownloadState.FAILED)
                    }
                }
            } catch (e: Exception) {
                val currentState = _activeDownloads.value[modelId]?.state
                if (currentState != DownloadState.PAUSED) {
                    updateProgress(
                        modelId,
                        _activeDownloads.value[modelId]?.bytesDownloaded ?: startByte,
                        model.sizeBytes,
                        DownloadState.FAILED
                    )
                    modelRepository.updateDownloadProgress(
                        modelId,
                        _activeDownloads.value[modelId]?.bytesDownloaded ?: startByte,
                        DownloadState.FAILED
                    )
                }
            }
        }
    }

    /** Cancels an active or paused download. Deletes partial file. */
    suspend fun cancel(modelId: ModelId, destFilePath: String) {
        httpDownloader.cancel()
        fileManager.deleteFile(destFilePath)
        modelRepository.deleteDownload(modelId)
        _activeDownloads.update { it - modelId }
    }

    /** Verifies SHA-256 integrity of a model file. */
    suspend fun verifyIntegrity(filePath: String, expectedChecksum: String): IntegrityResult =
        checksumVerifier.verify(filePath, expectedChecksum)

    private fun updateProgress(
        modelId: ModelId,
        bytesDownloaded: Long,
        totalBytes: Long,
        state: DownloadState
    ) {
        val speed = if (state == DownloadState.DOWNLOADING) calculateSpeed(bytesDownloaded) else 0L
        val eta = if (state == DownloadState.DOWNLOADING) calculateEta(bytesDownloaded, totalBytes, speed) else null
        val progress = DownloadProgress(
            modelId = modelId,
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes,
            speedBytesPerSec = speed,
            state = state,
            estimatedSecondsRemaining = eta
        )
        _activeDownloads.update { it + (modelId to progress) }
    }

    private fun calculateSpeed(bytesDownloadedSinceStart: Long): Long {
        val elapsedMs = currentTimeMillis() - downloadStartTimeMs
        if (elapsedMs <= 0L) return 0L
        return (bytesDownloadedSinceStart * 1000L) / elapsedMs
    }

    private fun calculateEta(
        bytesDownloaded: Long,
        totalBytes: Long,
        speedBytesPerSec: Long
    ): Long? {
        if (speedBytesPerSec <= 0L) return null
        val remaining = totalBytes - bytesDownloaded
        if (remaining <= 0L) return 0L
        return remaining / speedBytesPerSec
    }
}

/** Abstraction for file system operations needed by [DownloadManager]. */
interface DownloadFileManager {
    suspend fun deleteFile(path: String)
}
