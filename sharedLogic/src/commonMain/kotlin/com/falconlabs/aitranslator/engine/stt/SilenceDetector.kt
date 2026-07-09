/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.engine.stt

import com.falconlabs.aitranslator.util.currentTimeMillis

/**
 * Detects silence in an audio stream by tracking amplitude over time.
 * Emits a silence event when the amplitude stays below threshold for
 * the configured duration (default 1500ms).
 *
 * Supports Requirement 2.4 (silence detection after 1.5s pause).
 */
class SilenceDetector(private val thresholdMs: Long = 1500L, private val amplitudeThreshold: Float = 0.05f,) {
    private var silenceStartMs: Long = 0L
    private var isSilent: Boolean = false
    private var hasFiredForCurrentSilence: Boolean = false

    /**
     * Processes an audio amplitude sample.
     * @param amplitude Normalized amplitude (0.0 to 1.0).
     * @return The duration of silence in ms if silence threshold exceeded, null otherwise.
     */
    fun processAmplitude(amplitude: Float): Long? {
        val now = currentTimeMillis()

        if (amplitude < amplitudeThreshold) {
            if (!isSilent) {
                // Silence just started
                isSilent = true
                silenceStartMs = now
                hasFiredForCurrentSilence = false
            } else if (!hasFiredForCurrentSilence) {
                val silenceDuration = now - silenceStartMs
                if (silenceDuration >= thresholdMs) {
                    hasFiredForCurrentSilence = true
                    return silenceDuration
                }
            }
        } else {
            // Speech detected — reset silence tracking
            isSilent = false
            hasFiredForCurrentSilence = false
        }

        return null
    }

    /** Resets the detector state. */
    fun reset() {
        isSilent = false
        silenceStartMs = 0L
        hasFiredForCurrentSilence = false
    }
}
