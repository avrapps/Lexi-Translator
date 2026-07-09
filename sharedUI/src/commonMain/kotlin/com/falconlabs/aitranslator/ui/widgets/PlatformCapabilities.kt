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
 * Platform capabilities for conditional UI rendering.
 * Used to show/hide features like voice input based on platform support.
 */
object PlatformCapabilities {
    /**
     * Returns true if the current platform supports voice-to-text input.
     * Android and iOS support it; Desktop (JVM) and Web do not.
     */
    val supportsVoiceInput: Boolean
        get() {
            val os = System.getProperty("os.name")?.lowercase() ?: ""
            // Android has voice input via Intent; Desktop does not have system-level STT
            return os.contains("android")
        }

    /**
     * Returns true if the platform supports system share sheet.
     * Android and iOS support it; Desktop does not.
     */
    val supportsShareSheet: Boolean
        get() {
            val os = System.getProperty("os.name")?.lowercase() ?: ""
            return os.contains("android")
        }

    /**
     * Returns true if we're running on a desktop platform (wider layout).
     */
    val isDesktop: Boolean
        get() {
            val os = System.getProperty("os.name")?.lowercase() ?: ""
            return os.contains("mac") || os.contains("windows") || os.contains("linux")
        }
}
