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

/**
 * Platform-agnostic file picker interface.
 * Implementations open a native file browser dialog and return text content.
 */
expect object PlatformFilePicker {
    /**
     * Opens a file picker dialog for text files and returns the file content.
     * Returns null if cancelled or unsupported.
     */
    suspend fun pickTextFile(): String?
}
