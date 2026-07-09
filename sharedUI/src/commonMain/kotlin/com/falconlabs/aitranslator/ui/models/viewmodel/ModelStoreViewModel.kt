/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.models.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.DownloadState
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.domain.model.StorageUsage
import com.falconlabs.aitranslator.engine.model.ModelManager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tabs displayed in the Model Store screen. */
enum class ModelStoreTab { TRANSLATION, STT, TTS }

/** UI state for the Model Store screen. */
data class ModelStoreState(
    val selectedTab: ModelStoreTab = ModelStoreTab.TRANSLATION,
    val translationModels: List<AiModel> = emptyList(),
    val sttModels: List<AiModel> = emptyList(),
    val ttsModels: List<AiModel> = emptyList(),
    val installedModelIds: Set<ModelId> = emptySet(),
    val activeDownloads: Map<ModelId, DownloadProgress> = emptyMap(),
    val storageUsage: StorageUsage? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Filters
    val selectedSourceLang: String? = null,
    val selectedTargetLang: String? = null,
    val selectedFilterLang: String? = null,
) {
    /** Returns models for the currently selected tab. */
    val currentModels: List<AiModel>
        get() = when (selectedTab) {
            ModelStoreTab.TRANSLATION -> translationModels
            ModelStoreTab.STT -> sttModels
            ModelStoreTab.TTS -> ttsModels
        }

    /** Returns filtered models based on active filters. */
    val filteredModels: List<AiModel>
        get() = when (selectedTab) {
            ModelStoreTab.TRANSLATION -> {
                var filtered = translationModels
                if (selectedSourceLang != null) {
                    filtered = filtered.filter { it.languagePair?.source?.code == selectedSourceLang }
                }
                if (selectedTargetLang != null) {
                    filtered = filtered.filter { it.languagePair?.target?.code == selectedTargetLang }
                }
                filtered
            }
            ModelStoreTab.STT -> {
                if (selectedFilterLang != null) {
                    sttModels.filter {
                        it.languagePair?.source?.code == selectedFilterLang ||
                            it.languagePair?.target?.code == selectedFilterLang ||
                            it.name.contains(selectedFilterLang, ignoreCase = true)
                    }
                } else {
                    sttModels
                }
            }
            ModelStoreTab.TTS -> {
                if (selectedFilterLang != null) {
                    ttsModels.filter {
                        it.languagePair?.source?.code == selectedFilterLang ||
                            it.languagePair?.target?.code == selectedFilterLang ||
                            it.name.contains(selectedFilterLang, ignoreCase = true)
                    }
                } else {
                    ttsModels
                }
            }
        }

    /** Check if a specific model is currently installed. */
    fun isInstalled(modelId: ModelId): Boolean = modelId in installedModelIds

    /** Get download progress for a model, if actively downloading. */
    fun getDownloadProgress(modelId: ModelId): DownloadProgress? = activeDownloads[modelId]
}

/** Intents (user actions) for the Model Store. */
sealed interface ModelStoreIntent {
    data class SelectTab(val tab: ModelStoreTab) : ModelStoreIntent
    data class DownloadModel(val modelId: ModelId) : ModelStoreIntent
    data class RedownloadModel(val modelId: ModelId) : ModelStoreIntent
    data class PauseDownload(val modelId: ModelId) : ModelStoreIntent
    data class ResumeDownload(val modelId: ModelId) : ModelStoreIntent
    data class CancelDownload(val modelId: ModelId) : ModelStoreIntent
    data class DeleteModel(val modelId: ModelId) : ModelStoreIntent
    data class SelectSourceLang(val lang: String?) : ModelStoreIntent
    data class SelectTargetLang(val lang: String?) : ModelStoreIntent
    data class SelectFilterLang(val lang: String?) : ModelStoreIntent
}

/**
 * MVI ViewModel for the Model Store screen.
 * Connects to [ModelManager] for real model catalog, downloads, and storage tracking.
 */
class ModelStoreViewModel(private val modelManager: ModelManager) : ViewModel() {

    private val _state = MutableStateFlow(ModelStoreState())
    val state: StateFlow<ModelStoreState> = _state.asStateFlow()

    init {
        observeModels()
        observeStorage()
        observeDownloads()
    }

    private fun observeModels() {
        viewModelScope.launch {
            modelManager.getAvailableModels()
                .combine(modelManager.getInstalledModels()) { catalog, installed ->
                    Triple(
                        catalog,
                        installed,
                        installed.map { it.id }.toSet()
                    )
                }
                .collect { (catalog, _, installedIds) ->
                    _state.update { current ->
                        current.copy(
                            translationModels = catalog.filter { it.category == ModelCategory.TRANSLATION },
                            sttModels = catalog.filter { it.category == ModelCategory.STT },
                            ttsModels = catalog.filter { it.category == ModelCategory.TTS },
                            installedModelIds = installedIds,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeStorage() {
        viewModelScope.launch {
            modelManager.getStorageUsage().collect { usage ->
                _state.update { it.copy(storageUsage = usage) }
            }
        }
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            // Observe active downloads from the DownloadManager via ModelManager
            // The DownloadManager's activeDownloads flow updates in real-time
            org.koin.java.KoinJavaComponent.get<com.falconlabs.aitranslator.engine.model.DownloadManager>(
                com.falconlabs.aitranslator.engine.model.DownloadManager::class.java
            ).activeDownloads.collect { downloads ->
                _state.update { it.copy(activeDownloads = downloads) }
            }
        }
    }

    fun onIntent(intent: ModelStoreIntent) {
        when (intent) {
            is ModelStoreIntent.SelectTab -> {
                _state.update {
                    it.copy(
                        selectedTab = intent.tab,
                        selectedSourceLang = null,
                        selectedTargetLang = null,
                        selectedFilterLang = null
                    )
                }
            }
            is ModelStoreIntent.DownloadModel -> startDownload(intent.modelId)
            is ModelStoreIntent.RedownloadModel -> redownload(intent.modelId)
            is ModelStoreIntent.PauseDownload -> pauseDownload(intent.modelId)
            is ModelStoreIntent.ResumeDownload -> resumeDownload(intent.modelId)
            is ModelStoreIntent.CancelDownload -> cancelDownload(intent.modelId)
            is ModelStoreIntent.DeleteModel -> deleteModel(intent.modelId)
            is ModelStoreIntent.SelectSourceLang -> {
                _state.update { it.copy(selectedSourceLang = intent.lang, selectedTargetLang = null) }
            }
            is ModelStoreIntent.SelectTargetLang -> {
                _state.update { it.copy(selectedTargetLang = intent.lang) }
            }
            is ModelStoreIntent.SelectFilterLang -> {
                _state.update { it.copy(selectedFilterLang = intent.lang) }
            }
        }
    }

    private fun startDownload(modelId: ModelId) {
        viewModelScope.launch {
            try {
                modelManager.downloadModel(modelId)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun pauseDownload(modelId: ModelId) {
        viewModelScope.launch {
            modelManager.pauseDownload(modelId)
            _state.update { current ->
                val downloads = current.activeDownloads.toMutableMap()
                downloads[modelId]?.let { progress ->
                    downloads[modelId] = progress.copy(state = DownloadState.PAUSED)
                }
                current.copy(activeDownloads = downloads)
            }
        }
    }

    private fun resumeDownload(modelId: ModelId) {
        viewModelScope.launch {
            modelManager.resumeDownload(modelId)
        }
    }

    private fun cancelDownload(modelId: ModelId) {
        viewModelScope.launch {
            modelManager.cancelDownload(modelId)
            _state.update { current ->
                current.copy(activeDownloads = current.activeDownloads - modelId)
            }
        }
    }

    private fun deleteModel(modelId: ModelId) {
        viewModelScope.launch {
            modelManager.deleteModel(modelId)
        }
    }

    private fun redownload(modelId: ModelId) {
        viewModelScope.launch {
            try {
                modelManager.deleteModel(modelId)
                modelManager.downloadModel(modelId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
