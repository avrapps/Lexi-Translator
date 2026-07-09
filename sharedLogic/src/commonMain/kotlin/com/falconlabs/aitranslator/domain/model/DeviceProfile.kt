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

package com.falconlabs.aitranslator.domain.model

/**
 * Hardware capabilities of the current device.
 * Used to compute model compatibility recommendations (Requirement 6.7).
 */
data class DeviceProfile(
    val availableRamMb: Int,
    val availableStorageBytes: Long,
    val cpuCores: Int,
    val hasNnapi: Boolean,
    val hasGpu: Boolean
)

/**
 * A model paired with its compatibility score for the current device.
 * Score ranges from 0.0 (incompatible) to 1.0 (fully compatible).
 */
data class ModelRecommendation(
    val model: AiModel,
    val compatibilityScore: Float
)
