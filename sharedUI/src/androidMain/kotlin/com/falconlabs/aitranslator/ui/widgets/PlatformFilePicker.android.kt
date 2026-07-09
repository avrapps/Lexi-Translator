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

actual object PlatformFilePicker {
    // Android file picking requires Activity context and result handling.
    // This will be wired via ActivityResultLauncher in MainActivity in a future task.
    actual suspend fun pickTextFile(): String? = null
}
