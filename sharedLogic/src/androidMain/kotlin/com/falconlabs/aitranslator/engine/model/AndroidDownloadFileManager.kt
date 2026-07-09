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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File

/**
 * Android implementation of [DownloadFileManager].
 * Provides file system operations for download lifecycle management.
 */
class AndroidDownloadFileManager : DownloadFileManager {

    override suspend fun deleteFile(path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
