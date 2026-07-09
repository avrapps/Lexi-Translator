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

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

/**
 * JVM (Desktop) audio capture using javax.sound.sampled.TargetDataLine.
 * Captures mono 16kHz audio from the system default microphone.
 */
class JvmAudioCaptureProvider : AudioCaptureProvider {

    @Volatile
    private var targetLine: TargetDataLine? = null

    @Volatile
    private var isCapturing = false

    override fun startCapture(sampleRate: Int): Flow<FloatArray> = flow {
        val format = AudioFormat(
            sampleRate.toFloat(), // sample rate
            16, // sample size in bits
            1, // mono
            true, // signed
            false // little-endian
        )

        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            throw RuntimeException("No microphone available on this system")
        }

        val line = AudioSystem.getLine(info) as TargetDataLine
        line.open(format)
        line.start()
        targetLine = line
        isCapturing = true

        // Buffer: ~100ms of audio at 16kHz, 16-bit = 3200 bytes
        val bufferSize = (sampleRate * 2 * 100) / 1000 // 100ms chunks
        val buffer = ByteArray(bufferSize)

        try {
            while (coroutineContext.isActive && isCapturing) {
                val bytesRead = line.read(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    // Convert 16-bit PCM to float32 normalized
                    val samples = FloatArray(bytesRead / 2)
                    for (i in samples.indices) {
                        val lo = buffer[i * 2].toInt() and 0xFF
                        val hi = buffer[i * 2 + 1].toInt()
                        val sample = (hi shl 8) or lo
                        samples[i] = sample / 32768f
                    }
                    emit(samples)
                }
            }
        } finally {
            line.stop()
            line.close()
            targetLine = null
            isCapturing = false
        }
    }.flowOn(Dispatchers.IO)

    override fun stopCapture() {
        isCapturing = false
        targetLine?.stop()
    }

    override fun isAvailable(): Boolean = try {
        val format = AudioFormat(16000f, 16, 1, true, false)
        val info = DataLine.Info(TargetDataLine::class.java, format)
        AudioSystem.isLineSupported(info)
    } catch (_: Exception) {
        false
    }
}
