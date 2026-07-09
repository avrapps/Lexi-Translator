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

import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific audio capture interface.
 * Provides a stream of raw audio samples from the device microphone.
 *
 * Implementations:
 * - Android: AudioRecord (requires RECORD_AUDIO permission)
 * - Desktop/JVM: javax.sound.sampled.TargetDataLine
 */
interface AudioCaptureProvider {
    /**
     * Starts capturing audio and emits raw PCM samples as Float arrays.
     * Audio format: 16kHz, mono, float32 normalized (-1.0 to 1.0).
     *
     * @param sampleRate Sample rate in Hz (default 16000 for Whisper).
     * @return Flow of audio chunks (each chunk ~100ms of audio).
     */
    fun startCapture(sampleRate: Int = 16000): Flow<FloatArray>

    /** Stops audio capture and releases resources. */
    fun stopCapture()

    /** Returns true if microphone permission is granted and capture is possible. */
    fun isAvailable(): Boolean
}
