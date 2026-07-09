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

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.CpuRequirement
import com.falconlabs.aitranslator.domain.model.DeviceProfile
import com.falconlabs.aitranslator.domain.model.ModelRecommendation

/**
 * Computes model compatibility recommendations based on device capabilities.
 * Scores models 0.0–1.0 based on RAM fit, storage fit, and CPU compatibility.
 *
 * Supports Requirement 6.7 (recommendations ranked by compatibility score, limit 10).
 */
object ModelRecommendationEngine {

    /**
     * Returns up to [limit] model recommendations ranked by compatibility score.
     *
     * Scoring formula (weights):
     * - RAM fit: 40% (model RAM requirement vs available RAM)
     * - Storage fit: 30% (model size vs available storage)
     * - CPU match: 30% (model CPU requirement vs device core count)
     */
    fun recommend(
        catalog: List<AiModel>,
        deviceProfile: DeviceProfile,
        limit: Int = 10
    ): List<ModelRecommendation> {
        return catalog
            .map { model -> ModelRecommendation(model, computeScore(model, deviceProfile)) }
            .filter { it.compatibilityScore > 0.2f } // Exclude clearly incompatible
            .sortedByDescending { it.compatibilityScore }
            .take(limit)
    }

    private fun computeScore(model: AiModel, device: DeviceProfile): Float {
        val ramScore = computeRamScore(model.ramRequirementMb, device.availableRamMb)
        val storageScore = computeStorageScore(model.sizeBytes, device.availableStorageBytes)
        val cpuScore = computeCpuScore(model.cpuRequirement, device.cpuCores, device.hasNnapi)

        return (ramScore * 0.4f) + (storageScore * 0.3f) + (cpuScore * 0.3f)
    }

    private fun computeRamScore(requiredMb: Int, availableMb: Int): Float {
        if (availableMb <= 0) return 0f
        val ratio = availableMb.toFloat() / requiredMb.toFloat()
        return when {
            ratio >= 4f -> 1.0f   // Plenty of headroom
            ratio >= 2f -> 0.9f   // Comfortable
            ratio >= 1.5f -> 0.7f // Tight but workable
            ratio >= 1f -> 0.5f   // Barely fits
            else -> 0.1f          // Insufficient RAM
        }
    }

    private fun computeStorageScore(modelBytes: Long, availableBytes: Long): Float {
        if (availableBytes <= 0L) return 0f
        val ratio = availableBytes.toFloat() / modelBytes.toFloat()
        return when {
            ratio >= 10f -> 1.0f
            ratio >= 5f -> 0.9f
            ratio >= 2f -> 0.7f
            ratio >= 1.2f -> 0.5f
            ratio >= 1f -> 0.3f
            else -> 0.0f // Not enough storage
        }
    }

    private fun computeCpuScore(requirement: CpuRequirement, cores: Int, hasNnapi: Boolean): Float {
        val baseScore = when (requirement) {
            CpuRequirement.LOW -> 1.0f
            CpuRequirement.MEDIUM -> if (cores >= 4) 0.9f else 0.5f
            CpuRequirement.HIGH -> if (cores >= 6) 0.8f else 0.3f
        }
        // Bonus for NNAPI availability (hardware acceleration)
        val nnapiBonus = if (hasNnapi && requirement != CpuRequirement.LOW) 0.1f else 0f
        return (baseScore + nnapiBonus).coerceAtMost(1.0f)
    }
}
