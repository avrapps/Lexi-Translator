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
import com.falconlabs.aitranslator.domain.model.DeviceProfile
import com.falconlabs.aitranslator.domain.model.ModelRecommendation

/**
 * Computes device-aware model recommendations by filtering models that exceed
 * hardware capabilities and ranking the rest by a composite compatibility score.
 *
 * Score computation (0.0 to 1.0):
 * - RAM fit (40%): lower ratio of model RAM requirement to device RAM → higher score
 * - Storage fit (30%): lower ratio of model size to available storage → higher score
 * - Quality boost (30%): higher quality rating → higher score
 *
 * Supports Requirement 6.7 (device-aware model recommendations).
 */
class ModelRecommendationEngine {

    companion object {
        private const val MAX_RECOMMENDATIONS = 10
        private const val RAM_WEIGHT = 0.4f
        private const val STORAGE_WEIGHT = 0.3f
        private const val QUALITY_WEIGHT = 0.3f
        private const val MAX_QUALITY_RATING = 5.0f
    }

    /**
     * Filters and ranks available models against the current device profile.
     *
     * Models are excluded if they require more RAM than the device has available
     * or if their file size exceeds available storage.
     *
     * @param availableModels Full catalog of models from the Model Store.
     * @param deviceProfile Current device hardware capabilities.
     * @return Up to [MAX_RECOMMENDATIONS] models ranked by compatibility score descending.
     */
    fun recommend(
        availableModels: List<AiModel>,
        deviceProfile: DeviceProfile
    ): List<ModelRecommendation> {
        return availableModels
            .filter { model -> isCompatible(model, deviceProfile) }
            .map { model -> ModelRecommendation(model, computeScore(model, deviceProfile)) }
            .sortedByDescending { it.compatibilityScore }
            .take(MAX_RECOMMENDATIONS)
    }

    /**
     * Checks whether a model can physically run on the device.
     * A model is incompatible if it requires more RAM than available
     * or its download size exceeds available storage.
     */
    private fun isCompatible(model: AiModel, device: DeviceProfile): Boolean {
        if (model.ramRequirementMb > device.availableRamMb) return false
        if (model.sizeBytes > device.availableStorageBytes) return false
        return true
    }

    /**
     * Computes a composite compatibility score from 0.0 (poor fit) to 1.0 (ideal fit).
     *
     * Components:
     * - RAM fit: 1.0 - (modelRam / deviceRam). Lower usage ratio → higher score.
     * - Storage fit: 1.0 - (modelSize / deviceStorage). Lower usage ratio → higher score.
     * - Quality boost: modelQuality / maxQuality. Higher quality → higher score.
     */
    private fun computeScore(model: AiModel, device: DeviceProfile): Float {
        val ramRatio = model.ramRequirementMb.toFloat() / device.availableRamMb.toFloat()
        val ramScore = (1.0f - ramRatio).coerceIn(0.0f, 1.0f)

        val storageRatio = model.sizeBytes.toFloat() / device.availableStorageBytes.toFloat()
        val storageScore = (1.0f - storageRatio).coerceIn(0.0f, 1.0f)

        val qualityScore = (model.qualityRating / MAX_QUALITY_RATING).coerceIn(0.0f, 1.0f)

        val compositeScore = (ramScore * RAM_WEIGHT) +
            (storageScore * STORAGE_WEIGHT) +
            (qualityScore * QUALITY_WEIGHT)

        return compositeScore.coerceIn(0.0f, 1.0f)
    }
}
