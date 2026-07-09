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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Whisper-based STT engine that captures audio, accumulates chunks,
 * runs silence detection, and performs ONNX inference for transcription.
 *
 * Pipeline:
 * 1. Capture audio via AudioCaptureProvider
 * 2. Track amplitude for orb visualization
 * 3. Detect silence (1500ms threshold)
 * 4. On silence: run Whisper ONNX inference on accumulated audio buffer
 * 5. Emit FinalTranscription with the transcribed text
 */
class WhisperSttEngine(
    private val audioCaptureProvider: AudioCaptureProvider,
    private val whisperInference: WhisperInferenceProvider,
) : SttEngine {

    private val silenceDetector = SilenceDetector()
    private var _isListening = false

    override val isListening: Boolean get() = _isListening

    override fun startListening(config: SttConfig): Flow<SttEvent> = flow {
        if (!audioCaptureProvider.isAvailable()) {
            emit(SttEvent.Error(SttError.AudioCaptureError("Microphone not available")))
            return@flow
        }

        _isListening = true
        silenceDetector.reset()

        val audioBuffer = mutableListOf<Float>()
        var speechStartTime = currentTimeMillis()
        var hasSpeech = false

        try {
            audioCaptureProvider.startCapture(config.sampleRate).collect { samples ->
                if (!_isListening) return@collect

                // Calculate RMS amplitude for this chunk
                val rms = calculateRms(samples)
                emit(SttEvent.AudioLevel(rms))

                // Track speech activity
                if (rms > 0.02f) {
                    if (!hasSpeech) {
                        hasSpeech = true
                        speechStartTime = currentTimeMillis()
                        audioBuffer.clear()
                    }
                    audioBuffer.addAll(samples.toList())

                    // Emit partial transcription showing recording status
                    val durationSec = (currentTimeMillis() - speechStartTime) / 1000.0
                    val durationStr = "${(durationSec * 10).toInt() / 10.0}"
                    emit(
                        SttEvent.PartialTranscription(
                            text = "Recording... ${durationStr}s",
                            detectedLanguage = config.primaryLanguage,
                        )
                    )
                }

                // Check for silence
                val silenceDuration = silenceDetector.processAmplitude(rms)
                if (silenceDuration != null && hasSpeech && audioBuffer.isNotEmpty()) {
                    // Require at least 1.5 seconds of audio for meaningful transcription
                    val audioDurationMs = (audioBuffer.size * 1000L) / config.sampleRate
                    if (audioDurationMs < 1500) {
                        // Too short — ignore and keep listening
                        println("[WhisperSTT] Audio too short (${audioDurationMs}ms), continuing...")
                        silenceDetector.reset()
                    } else {
                        // Silence detected after sufficient speech — run Whisper inference
                        emit(SttEvent.SilenceDetected(silenceDuration))

                        val duration = currentTimeMillis() - speechStartTime
                        val audioSamples = audioBuffer.toFloatArray()

                        // Run Whisper ONNX inference
                        println(
                            "[WhisperSTT] Running inference on ${audioSamples.size} samples (${audioSamples.size / 16000.0}s)"
                        )
                        val transcription = runWhisperInference(audioSamples, config.primaryLanguage.code)
                        println("[WhisperSTT] Inference result: '$transcription'")

                        if (transcription != null && transcription.isNotBlank()) {
                            emit(
                                SttEvent.FinalTranscription(
                                    text = transcription,
                                    detectedLanguage = config.primaryLanguage,
                                    confidence = 0.85f,
                                    durationMs = duration,
                                )
                            )
                        } else {
                            emit(
                                SttEvent.FinalTranscription(
                                    text = "(Could not transcribe audio)",
                                    detectedLanguage = config.primaryLanguage,
                                    confidence = 0f,
                                    durationMs = duration,
                                )
                            )
                        }

                        // Reset for next segment
                        audioBuffer.clear()
                        hasSpeech = false
                        silenceDetector.reset()
                    }
                }
            }
        } finally {
            _isListening = false
        }
    }

    override suspend fun stopListening() {
        _isListening = false
        audioCaptureProvider.stopCapture()
    }

    /**
     * Runs Whisper ONNX inference on the audio buffer.
     */
    private suspend fun runWhisperInference(samples: FloatArray, languageCode: String): String? {
        if (!whisperInference.isReady()) {
            whisperInference.load()
        }
        return whisperInference.transcribe(samples, languageCode)
    }

    private fun calculateRms(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        var sum = 0f
        for (sample in samples) {
            sum += sample * sample
        }
        return kotlin.math.sqrt(sum / samples.size)
    }
}
