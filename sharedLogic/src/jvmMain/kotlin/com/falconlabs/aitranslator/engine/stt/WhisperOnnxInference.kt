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

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer

/**
 * Whisper ONNX inference engine for speech-to-text.
 *
 * Pipeline:
 * 1. Audio samples (16kHz float32) → Mel spectrogram (80 channels × 3000 frames)
 * 2. Mel features → Whisper Encoder → hidden states [1, 1500, 384]
 * 3. Hidden states + decoder tokens → Whisper Decoder → logits [1, seq, 51865]
 * 4. Greedy decode until <|endoftext|> → text tokens → string
 */
class WhisperOnnxInference(private val modelDir: String) {

    private val env = OrtEnvironment.getEnvironment()
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null
    private var vocab: Map<Int, String> = emptyMap()
    private var isLoaded = false

    // Whisper special token IDs
    private val EOT = 50257L // <|endoftext|>
    private val SOT = 50258L // <|startoftranscript|>
    private val TRANSCRIBE = 50359L // <|transcribe|>
    private val NO_TIMESTAMPS = 50363L // <|notimestamps|>

    // Mel spectrogram parameters (Whisper uses 80 mel bins, 25ms window, 10ms hop)
    private val N_MELS = 80
    private val N_FFT = 400 // 25ms at 16kHz
    private val HOP_LENGTH = 160 // 10ms at 16kHz
    private val MAX_FRAMES = 3000 // 30 seconds
    private val SAMPLE_RATE = 16000

    suspend fun load() = withContext(Dispatchers.IO) {
        val encoderFile = File(modelDir, "encoder_model_quantized.onnx")
        val decoderFile = File(modelDir, "decoder_model_quantized.onnx")

        if (!encoderFile.exists() || !decoderFile.exists()) return@withContext

        val opts = OrtSession.SessionOptions()
        opts.setIntraOpNumThreads(4)
        encoderSession = env.createSession(encoderFile.absolutePath, opts)
        decoderSession = env.createSession(decoderFile.absolutePath, opts)

        // Load vocabulary from tokenizer.json
        loadVocabulary()
        isLoaded = true
    }

    /**
     * Transcribes audio samples to text.
     * @param samples 16kHz mono float32 audio samples.
     * @param languageCode Language hint (e.g., "en" for English).
     * @return Transcribed text, or null if model not loaded.
     */
    suspend fun transcribe(samples: FloatArray, languageCode: String = "en"): String? =
        withContext(Dispatchers.Default) {
            if (!isLoaded) return@withContext null
            if (samples.isEmpty()) return@withContext null

            // 1. Compute mel spectrogram
            val melFeatures = computeMelSpectrogram(samples)

            // 2. Run encoder
            val encoderOutput = runEncoder(melFeatures)

            // 3. Greedy decode
            val langToken = getLanguageToken(languageCode)
            val tokenIds = greedyDecode(encoderOutput, langToken)

            // 4. Decode tokens to text
            encoderOutput.close()
            decodeTokens(tokenIds)
        }

    fun isReady(): Boolean = isLoaded

    /**
     * Computes a proper log-mel spectrogram from audio samples using FFT.
     * Whisper expects 80 mel channels, 400-sample window, 160-sample hop.
     * Output shape: [80 * MAX_FRAMES] (row-major: mel_channel × frame)
     */
    private fun computeMelSpectrogram(samples: FloatArray): FloatArray {
        val melOutput = FloatArray(N_MELS * MAX_FRAMES) // zero-padded

        // Pad audio to at least 30 seconds (Whisper expects this)
        val paddedSamples = if (samples.size < SAMPLE_RATE * 30) {
            FloatArray(SAMPLE_RATE * 30).also { samples.copyInto(it) }
        } else {
            samples.copyOf(SAMPLE_RATE * 30)
        }

        val numFrames = min((paddedSamples.size - N_FFT) / HOP_LENGTH + 1, MAX_FRAMES)

        // Hann window
        val window = FloatArray(N_FFT) { i ->
            (0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / N_FFT))).toFloat()
        }

        // Mel filterbank (simplified triangular filters spanning 0-8000 Hz)
        val melFilters = createMelFilterbank(N_MELS, N_FFT / 2 + 1, SAMPLE_RATE)

        for (frame in 0 until numFrames) {
            val start = frame * HOP_LENGTH

            // Apply window and compute power spectrum via DFT (simplified — real FFT)
            val powerSpectrum = computePowerSpectrum(paddedSamples, start, window)

            // Apply mel filterbank
            for (mel in 0 until N_MELS) {
                var energy = 0f
                for (k in powerSpectrum.indices) {
                    energy += melFilters[mel * powerSpectrum.size + k] * powerSpectrum[k]
                }
                // Log scale with floor
                melOutput[mel * MAX_FRAMES + frame] = ln(max(energy, 1e-10f))
            }
        }

        // Normalize (Whisper expects specific scaling)
        val maxVal = melOutput.maxOrNull() ?: 0f
        if (maxVal > 0f) {
            for (i in melOutput.indices) {
                melOutput[i] = max(melOutput[i], maxVal - 8f) // clamp at max - 8
                melOutput[i] = (melOutput[i] / 4f) + 1f // scale to ~[0, 1] range
            }
        }

        return melOutput
    }

    /**
     * Computes power spectrum of a windowed frame using a simple DFT.
     * Returns N_FFT/2+1 frequency bins.
     */
    private fun computePowerSpectrum(
        samples: FloatArray,
        offset: Int,
        window: FloatArray
    ): FloatArray {
        val fftSize = N_FFT / 2 + 1
        val power = FloatArray(fftSize)

        // DFT (real input → half-spectrum)
        for (k in 0 until fftSize) {
            var real = 0f
            var imag = 0f
            val freq = 2.0 * Math.PI * k / N_FFT
            for (n in 0 until N_FFT) {
                val idx = offset + n
                val sample = if (idx < samples.size) samples[idx] * window[n] else 0f
                real += sample * Math.cos(freq * n).toFloat()
                imag -= sample * Math.sin(freq * n).toFloat()
            }
            power[k] = real * real + imag * imag
        }
        return power
    }

    /**
     * Creates a triangular mel filterbank matrix.
     * Returns a flat array of shape [N_MELS × numFreqBins].
     */
    private fun createMelFilterbank(
        numMels: Int,
        numFreqBins: Int,
        sampleRate: Int
    ): FloatArray {
        val filters = FloatArray(numMels * numFreqBins)
        val maxFreq = sampleRate / 2.0

        // Mel scale conversion
        fun hzToMel(hz: Double) = 2595.0 * Math.log10(1.0 + hz / 700.0)
        fun melToHz(mel: Double) = 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0)

        val melMin = hzToMel(0.0)
        val melMax = hzToMel(maxFreq)
        val melPoints = DoubleArray(numMels + 2) { i ->
            melToHz(melMin + (melMax - melMin) * i / (numMels + 1))
        }

        // Convert Hz points to FFT bin indices
        val binPoints = IntArray(numMels + 2) { i ->
            ((melPoints[i] * N_FFT) / sampleRate).toInt().coerceIn(0, numFreqBins - 1)
        }

        for (m in 0 until numMels) {
            val left = binPoints[m]
            val center = binPoints[m + 1]
            val right = binPoints[m + 2]

            for (k in left until center) {
                if (center > left) {
                    filters[m * numFreqBins + k] = (k - left).toFloat() / (center - left).toFloat()
                }
            }
            for (k in center until right) {
                if (right > center) {
                    filters[m * numFreqBins + k] = (right - k).toFloat() / (right - center).toFloat()
                }
            }
        }
        return filters
    }

    private fun runEncoder(melFeatures: FloatArray): OnnxTensor {
        val session = encoderSession!!
        val shape = longArrayOf(1, N_MELS.toLong(), MAX_FRAMES.toLong())
        val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(melFeatures), shape)

        val inputs = mapOf("input_features" to inputTensor)
        val result = session.run(inputs)

        // Get first output (last_hidden_state)
        val output = result.iterator().next().value as OnnxTensor
        inputTensor.close()
        return output
    }

    private fun greedyDecode(encoderOutput: OnnxTensor, langToken: Long): List<Long> {
        val session = decoderSession!!
        val maxTokens = 224 // Whisper max generation length
        val generated = mutableListOf(SOT, langToken, TRANSCRIBE, NO_TIMESTAMPS)

        for (step in 0 until maxTokens) {
            val inputShape = longArrayOf(1, generated.size.toLong())
            val inputIds = OnnxTensor.createTensor(
                env,
                LongBuffer.wrap(generated.toLongArray()),
                inputShape
            )

            val inputs = mapOf(
                "input_ids" to inputIds,
                "encoder_hidden_states" to encoderOutput,
            )

            try {
                val result = session.run(inputs)
                val logits = result.iterator().next().value as OnnxTensor
                val logitsBuffer = logits.floatBuffer

                // Get logits for last position
                val vocabSize = 51865
                val offset = (generated.size - 1) * vocabSize
                var maxIdx = 0L
                var maxVal = Float.MIN_VALUE
                for (i in 0 until vocabSize) {
                    val v = logitsBuffer.get(offset + i)
                    if (v > maxVal) {
                        maxVal = v
                        maxIdx = i.toLong()
                    }
                }

                inputIds.close()
                logits.close()

                if (maxIdx == EOT) break
                // Skip timestamp tokens (>= 50364)
                if (maxIdx >= 50364L) continue
                generated.add(maxIdx)
            } catch (e: Exception) {
                inputIds.close()
                break
            }
        }

        // Return only content tokens (skip prefix tokens)
        return generated.drop(4) // drop SOT, lang, transcribe, notimestamps
    }

    private fun decodeTokens(tokenIds: List<Long>): String {
        if (tokenIds.isEmpty()) return ""
        return tokenIds
            .filter { it < 50257 } // filter out special tokens
            .mapNotNull { vocab[it.toInt()] }
            .joinToString("")
            .replace("Ġ", " ") // GPT-2 BPE space marker
            .replace("Ã¤", "ä").replace("Ã¶", "ö").replace("Ã¼", "ü") // common UTF-8 fixes
            .trim()
    }

    private fun getLanguageToken(code: String): Long = when (code) {
        "en" -> 50259L
        "zh" -> 50260L
        "de" -> 50261L
        "es" -> 50262L
        "ru" -> 50263L
        "ko" -> 50264L
        "fr" -> 50265L
        "ja" -> 50266L
        "pt" -> 50267L
        "hi" -> 50276L
        else -> 50259L // default English
    }

    private fun loadVocabulary() {
        val tokenizerFile = File(modelDir, "tokenizer.json")
        if (!tokenizerFile.exists()) return

        val content = tokenizerFile.readText()
        val vocabMap = mutableMapOf<Int, String>()

        // Parse BPE vocab from tokenizer.json model.vocab
        val vocabStart = content.indexOf("\"vocab\"")
        if (vocabStart == -1) return
        val braceStart = content.indexOf("{", vocabStart)
        if (braceStart == -1) return
        val braceEnd = findMatchingBrace(content, braceStart)
        if (braceEnd == -1) return
        val vocabSection = content.substring(braceStart + 1, braceEnd)

        val pattern = "\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*(\\d+)".toRegex()
        pattern.findAll(vocabSection).forEach { match ->
            val token = match.groupValues[1]
                .replace("\\u0120", "Ġ")
                .replace("\\\\", "\\")
                .replace("\\\"", "\"")
            val id = match.groupValues[2].toIntOrNull() ?: return@forEach
            vocabMap[id] = token
        }

        // Also load added_tokens
        val addedStart = content.indexOf("\"added_tokens\"")
        if (addedStart != -1) {
            val addedPattern = "\"id\"\\s*:\\s*(\\d+).*?\"content\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            addedPattern.findAll(content).forEach { match ->
                val id = match.groupValues[1].toIntOrNull() ?: return@forEach
                vocabMap[id] = match.groupValues[2]
            }
        }

        vocab = vocabMap
    }

    private fun findMatchingBrace(text: String, openIdx: Int): Int {
        var depth = 0
        for (i in openIdx until text.length) {
            when (text[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    fun close() {
        encoderSession?.close()
        decoderSession?.close()
        isLoaded = false
    }
}
