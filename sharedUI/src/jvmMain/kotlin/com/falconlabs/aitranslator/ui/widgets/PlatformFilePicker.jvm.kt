/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.widgets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual object PlatformFilePicker {
    actual suspend fun pickTextFile(): String? = withContext(Dispatchers.IO) {
        var result: String? = null
        val latch = java.util.concurrent.CountDownLatch(1)

        // Use java.awt.FileDialog for native macOS/Windows/Linux file picker
        javax.swing.SwingUtilities.invokeLater {
            try {
                val dialog = FileDialog(null as Frame?, "Select text file", FileDialog.LOAD)
                dialog.setFilenameFilter { _, name ->
                    name.endsWith(".txt") ||
                        name.endsWith(".md") ||
                        name.endsWith(".csv") ||
                        name.endsWith(".text")
                }
                dialog.isVisible = true

                val dir = dialog.directory
                val file = dialog.file
                if (dir != null && file != null) {
                    result = try {
                        File(dir, file).readText(Charsets.UTF_8)
                    } catch (_: Exception) {
                        null
                    }
                }
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        result
    }
}
