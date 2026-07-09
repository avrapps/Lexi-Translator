/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.engine.translation

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File
import java.nio.LongBuffer

/**
 * Manages an ONNX Runtime session for OPUS-MT translation models.
 * Loads the encoder and decoder ONNX files and runs sequence-to-sequence inference.
 *
 * OPUS-MT models use the Marian architecture:
 * - Encoder: processes source tokens → hidden states
 * - Decoder: autoregressively generates target tokens from hidden states
 *
 * This implementation uses greedy decoding (argmax at each step).
 */
class OnnxTranslationSession(private val modelDir: String,) {
    private val env = OrtEnvironment.getEnvironment()
    private var encoderSession: OrtSession? = null
    private var decoderSession: OrtSession? = null
    private var isLoaded = false

    // Marian special tokens
    private val padTokenId = 58100L
    private val eosTokenId = 0L
    private val decoderStartTokenId = 58100L

    /**
     * Loads the ONNX encoder and decoder models from disk.
     * Must be called before [translate].
     */
    suspend fun loadModel() = withContext(Dispatchers.IO) {
        val encoderFile = File(modelDir, "encoder_model_quantized.onnx")
        val decoderFile = File(modelDir, "decoder_model_quantized.onnx")

        if (!encoderFile.exists()) {
            throw TranslationException.ModelNotInstalled("encoder" to modelDir)
        }

        val opts = OrtSession.SessionOptions()
        opts.setIntraOpNumThreads(4)

        encoderSession = env.createSession(encoderFile.absolutePath, opts)

        if (decoderFile.exists()) {
            decoderSession = env.createSession(decoderFile.absolutePath, opts)
        }

        isLoaded = true
    }

    /**
     * Translates tokenized input IDs to output token IDs using greedy decoding.
     *
     * @param inputIds Source token IDs from the tokenizer.
     * @param maxLength Maximum number of tokens to generate.
     * @return Generated output token IDs.
     */
    suspend fun translate(inputIds: LongArray, maxLength: Int = 200): LongArray = withContext(Dispatchers.Default) {
        if (!isLoaded || encoderSession == null) {
            throw TranslationException.InferenceFailed("Model not loaded")
        }

        // If we only have encoder (no decoder), return input as-is (fallback)
        if (decoderSession == null) {
            return@withContext runEncoderOnly(inputIds)
        }

        // Run encoder
        val encoderOutput = runEncoder(inputIds)

        // Greedy decode
        val outputIds = greedyDecode(encoderOutput, inputIds, maxLength)

        encoderOutput.close()
        outputIds
    }

    private fun runEncoder(inputIds: LongArray): OnnxTensor {
        val session = encoderSession!!
        val shape = longArrayOf(1, inputIds.size.toLong())
        val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape)
        val attentionMask = OnnxTensor.createTensor(
            env,
            LongBuffer.wrap(LongArray(inputIds.size) { 1L }),
            shape
        )

        val inputs = mapOf(
            "input_ids" to inputTensor,
            "attention_mask" to attentionMask,
        )

        val result = session.run(inputs)
        // encoder output is typically "last_hidden_state"
        val output = result.iterator().next().value as OnnxTensor
        inputTensor.close()
        attentionMask.close()
        return output
    }

    private fun runEncoderOnly(inputIds: LongArray): LongArray {
        // Without decoder, we can't translate — return input IDs as fallback
        return inputIds
    }

    private fun greedyDecode(
        encoderHiddenStates: OnnxTensor,
        inputIds: LongArray,
        maxLength: Int,
    ): LongArray {
        val session = decoderSession ?: return inputIds
        val generated = mutableListOf(decoderStartTokenId)
        val inputShape = longArrayOf(1, inputIds.size.toLong())
        val attentionMask = OnnxTensor.createTensor(
            env,
            LongBuffer.wrap(LongArray(inputIds.size) { 1L }),
            inputShape
        )

        // Track recent tokens for repetition penalty
        val recentTokens = ArrayDeque<Long>(6)

        for (step in 0 until maxLength) {
            val decoderInputShape = longArrayOf(1, generated.size.toLong())
            val decoderInputIds = OnnxTensor.createTensor(
                env,
                LongBuffer.wrap(generated.toLongArray()),
                decoderInputShape
            )

            val inputs = mapOf(
                "input_ids" to decoderInputIds,
                "encoder_hidden_states" to encoderHiddenStates,
                "encoder_attention_mask" to attentionMask,
            )

            try {
                val result = session.run(inputs)
                val logits = result.iterator().next().value as OnnxTensor

                val logitsData = logits.floatBuffer
                val vocabSize = logitsData.remaining() / generated.size
                val offset = (generated.size - 1) * vocabSize

                // Copy logits for this position
                val scores = FloatArray(vocabSize) { logitsData.get(offset + it) }

                // Apply repetition penalty (1.3) to recently generated tokens
                for (recentToken in recentTokens) {
                    val idx = recentToken.toInt()
                    if (idx in scores.indices) {
                        scores[idx] = if (scores[idx] > 0) scores[idx] / 1.3f else scores[idx] * 1.3f
                    }
                }

                // Pick argmax
                var maxIdx = 0L
                var maxVal = Float.MIN_VALUE
                for (i in scores.indices) {
                    if (scores[i] > maxVal) {
                        maxVal = scores[i]
                        maxIdx = i.toLong()
                    }
                }

                decoderInputIds.close()
                logits.close()

                if (maxIdx == eosTokenId) break
                generated.add(maxIdx)

                // Update recent window
                recentTokens.addLast(maxIdx)
                if (recentTokens.size > 6) recentTokens.removeFirst()
            } catch (e: Exception) {
                decoderInputIds.close()
                break
            }
        }

        attentionMask.close()
        return generated.drop(1).toLongArray()
    }

    /** Releases ONNX Runtime resources. */
    fun close() {
        encoderSession?.close()
        decoderSession?.close()
        encoderSession = null
        decoderSession = null
        isLoaded = false
    }
}
