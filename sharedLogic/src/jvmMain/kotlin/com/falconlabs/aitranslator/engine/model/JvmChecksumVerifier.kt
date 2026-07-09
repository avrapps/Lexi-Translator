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

import com.falconlabs.aitranslator.domain.model.IntegrityResult

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File
import java.security.MessageDigest

/**
 * JVM implementation of [ChecksumVerifier] using java.security.MessageDigest.
 * Processes files in 8KB chunks to avoid loading entire models into memory.
 */
class JvmChecksumVerifier : ChecksumVerifier {

    override suspend fun verify(filePath: String, expectedChecksum: String): IntegrityResult {
        val actual = computeSha256(filePath)
        return if (actual.equals(expectedChecksum, ignoreCase = true)) {
            IntegrityResult.Valid
        } else {
            IntegrityResult.Invalid(expected = expectedChecksum, actual = actual)
        }
    }

    override suspend fun computeSha256(filePath: String): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File does not exist: $filePath")
        }
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }
}
