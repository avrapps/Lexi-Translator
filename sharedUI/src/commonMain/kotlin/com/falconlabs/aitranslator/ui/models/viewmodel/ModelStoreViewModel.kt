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

import androidx.lifecycle.ViewModel
import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.ModelId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Tabs displayed in the Model Store screen. */
enum class ModelStoreTab { TRANSLATION, STT, TTS }

/** UI state for the Model Store screen. */
data class ModelStoreState(
    val selectedTab: ModelStoreTab = ModelStoreTab.TRANSLATION,
    val translationModels: List<AiModel> = emptyList(),
    val sttModels: List<AiModel> = emptyList(),
    val ttsModels: List<AiModel> = emptyList(),
    val isLoading: Boolean = false,
) {
    /** Returns models for the currently selected tab. */
    val currentModels: List<AiModel>
        get() = when (selectedTab) {
            ModelStoreTab.TRANSLATION -> translationModels
            ModelStoreTab.STT -> sttModels
            ModelStoreTab.TTS -> ttsModels
        }
}

/** Intents (user actions) for the Model Store. */
sealed interface ModelStoreIntent {
    data class SelectTab(val tab: ModelStoreTab) : ModelStoreIntent
    data class DownloadModel(val modelId: ModelId) : ModelStoreIntent
}

/**
 * MVI ViewModel for the Model Store screen.
 * Manages tab selection and exposes hardcoded mock models for now.
 */
class ModelStoreViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        ModelStoreState(
            translationModels = MockModelData.translationModels,
            sttModels = MockModelData.sttModels,
            ttsModels = MockModelData.ttsModels,
        )
    )
    val state: StateFlow<ModelStoreState> = _state.asStateFlow()

    fun onIntent(intent: ModelStoreIntent) {
        when (intent) {
            is ModelStoreIntent.SelectTab -> {
                _state.update { it.copy(selectedTab = intent.tab) }
            }
            is ModelStoreIntent.DownloadModel -> {
                // TODO: Wire to real download manager; for now just log.
                println("Download requested for model: ${intent.modelId.id}")
            }
        }
    }
}
