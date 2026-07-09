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

/** Result of a model deletion operation (Requirement 6.10). */
sealed interface DeleteResult {
    data object Success : DeleteResult
    data class Error(val message: String) : DeleteResult
}

/** Result of a SHA-256 integrity check on an installed model (Requirement 6.4). */
sealed interface IntegrityResult {
    data object Valid : IntegrityResult
    data class Invalid(val expected: String, val actual: String) : IntegrityResult
}

/** Result of loading a model or voice profile into memory. */
sealed interface LoadResult {
    data object Success : LoadResult
    data class Error(val message: String) : LoadResult
}
