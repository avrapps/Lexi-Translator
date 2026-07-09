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

import com.falconlabs.aitranslator.data.repository.ModelRepository
import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.DownloadState
import com.falconlabs.aitranslator.domain.model.IntegrityResult
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.util.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

/**
 * Manages the download lifecycle for AI models including pause, resume,
 * cancel, and SHA-256 integrity verification after completion.
 *
 * State machine: QUEUED → DOWNLOADING → PAUSED → VERIFYING → COMPLETED/FAILED
 *
 * Supports Requirement 6.3 (download progress with pause/resume/cancel)
 * and Requirement 6.4 (integrity verification).
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
     * Initiates a new model download.
     * Creates a download record in the repository, transitions to QUEUED then DOWNLOADING,
     * and begins streaming bytes via [HttpDownloader].
     *
     * @param model The model metadata to download.
     * @param downloadUrl The remote URL to fetch the model file from.
     * @param destFilePath The local file path to write downloaded content to.
     * @return Flow emitting [DownloadProgress] updates until completion or failure.
     */
    suspend fun startDownload(
        model: AiModel,
        downloadUrl: String,
        destFilePath: String
    ): Flow<DownloadProgress> {
        val modelId = model.id

        // Insert download record with QUEUED state
        modelRepository.insertDownload(modelId, model.sizeBytes)
        updateProgress(modelId, 0L, model.sizeBytes, DownloadState.QUEUED)

        // Transition to DOWNLOADING
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
                // Persist progress periodically (the repository handles throttling)
            }

            // Download complete — verify integrity
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
            // If not a deliberate pause/cancel, mark as FAILED
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

        return _activeDownloads.mapNotNull { it[modelId] }
    }

    /**
     * Pauses an active download. Retains the partial file on disk and
     * persists the bytes-downloaded count for later resume.
     */
    suspend fun pause(modelId: ModelId) {
        httpDownloader.cancel()
        val current = _activeDownloads.value[modelId] ?: return
        updateProgress(modelId, current.bytesDownloaded, current.totalBytes, DownloadState.PAUSED)
        modelRepository.updateDownloadProgress(modelId, current.bytesDownloaded, DownloadState.PAUSED)
    }

    /**
     * Resumes a previously paused download from the last persisted byte position.
     *
     * @param model The model metadata.
     * @param downloadUrl The remote URL to resume downloading from.
     * @param destFilePath The local file path (partial file must exist).
     */
    suspend fun resume(
        model: AiModel,
        downloadUrl: String,
        destFilePath: String
    ) {
        val modelId = model.id
        val current = _activeDownloads.value[modelId] ?: return
        val startByte = current.bytesDownloaded

        updateProgress(modelId, startByte, current.totalBytes, DownloadState.DOWNLOADING)
        modelRepository.updateDownloadProgress(modelId, startByte, DownloadState.DOWNLOADING)

        downloadStartTimeMs = currentTimeMillis()

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

    /**
     * Cancels an active or paused download. Deletes the partial file from disk
     * and removes the download record from the repository.
     */
    suspend fun cancel(modelId: ModelId, destFilePath: String) {
        httpDownloader.cancel()
        fileManager.deleteFile(destFilePath)
        modelRepository.deleteDownload(modelId)
        _activeDownloads.update { it - modelId }
    }

    /**
     * Verifies the SHA-256 integrity of a downloaded or installed model file.
     *
     * @param filePath The path to the model file.
     * @param expectedChecksum The expected SHA-256 hex string.
     * @return [IntegrityResult] indicating valid or invalid checksum.
     */
    suspend fun verifyIntegrity(filePath: String, expectedChecksum: String): IntegrityResult {
        return checksumVerifier.verify(filePath, expectedChecksum)
    }

    private fun updateProgress(
        modelId: ModelId,
        bytesDownloaded: Long,
        totalBytes: Long,
        state: DownloadState
    ) {
        val speed = if (state == DownloadState.DOWNLOADING) {
            calculateSpeed(bytesDownloaded)
        } else {
            0L
        }
        val eta = if (state == DownloadState.DOWNLOADING) {
            calculateEta(bytesDownloaded, totalBytes, speed)
        } else {
            null
        }
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

    private fun calculateEta(bytesDownloaded: Long, totalBytes: Long, speedBytesPerSec: Long): Long? {
        if (speedBytesPerSec <= 0L) return null
        val remaining = totalBytes - bytesDownloaded
        if (remaining <= 0L) return 0L
        return remaining / speedBytesPerSec
    }
}

/**
 * Abstraction for file system operations needed by [DownloadManager].
 * Platform-specific implementations handle actual file deletion.
 */
interface DownloadFileManager {
    /** Deletes the file at the given [path]. No-op if the file does not exist. */
    suspend fun deleteFile(path: String)
}


