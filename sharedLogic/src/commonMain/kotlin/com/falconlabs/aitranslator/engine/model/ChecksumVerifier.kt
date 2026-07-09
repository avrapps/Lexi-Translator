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

import com.falconlabs.aitranslator.domain.model.IntegrityResult

/**
 * Computes SHA-256 checksums for downloaded model files and verifies them
 * against expected values from the model catalog.
 *
 * Supports Requirement 6.4 (model integrity verification).
 */
interface ChecksumVerifier {
    /**
     * Computes the SHA-256 hash of the file at [filePath] and compares it
     * against the [expectedChecksum].
     *
     * @param filePath Absolute path to the file to verify.
     * @param expectedChecksum Expected SHA-256 hex string (lowercase).
     * @return [IntegrityResult.Valid] if checksums match, [IntegrityResult.Invalid] otherwise.
     */
    suspend fun verify(filePath: String, expectedChecksum: String): IntegrityResult

    /**
     * Computes the SHA-256 hash of the file at [filePath].
     *
     * @param filePath Absolute path to the file to hash.
     * @return Lowercase hex-encoded SHA-256 digest string.
     */
    suspend fun computeSha256(filePath: String): String
}
