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

import com.falconlabs.aitranslator.domain.model.LanguageCode

import java.io.File

/**
 * JVM (Desktop) implementation of [TranslationInferenceProvider].
 * Uses ONNX Runtime to run OPUS-MT encoder-decoder models with a MarianTokenizer.
 *
 * On first translation, lazily downloads supplementary files (decoder, vocab)
 * if they're not already present in the model directory.
 */
class JvmTranslationInferenceProvider : TranslationInferenceProvider {

    private val modelsBaseDir = File(System.getProperty("user.home"), ".lexi/models")
    private val sessions = mutableMapOf<String, Pair<OnnxTranslationSession, MarianTokenizer>>()

    private companion object {
        const val HF_BASE = "https://huggingface.co"
    }

    override suspend fun runInference(
        text: String,
        source: LanguageCode,
        target: LanguageCode
    ): String? {
        val modelId = "opus-mt-${source.code}-${target.code}"
        val modelDir = File(modelsBaseDir, modelId)

        // Migrate old flat file to new directory structure if needed
        val flatFile = File(modelsBaseDir, "$modelId.onnx")
        if (flatFile.exists() && !modelDir.exists()) {
            modelDir.mkdirs()
            flatFile.renameTo(File(modelDir, "encoder_model_quantized.onnx"))
        }

        if (!modelDir.exists()) return null

        // Ensure supplementary files are present (decoder + vocab)
        ensureSupplementaryFiles(modelId, modelDir)

        val (session, tokenizer) = getOrLoadSession(modelId, modelDir.absolutePath)

        // Tokenize → Encode → Decode → Detokenize
        val inputIds = tokenizer.encode(text)
        val outputIds = session.translate(inputIds)
        return tokenizer.decode(outputIds)
    }

    override fun hasModel(source: LanguageCode, target: LanguageCode): Boolean {
        val modelId = "opus-mt-${source.code}-${target.code}"
        val modelDir = File(modelsBaseDir, modelId)
        val encoderInDir = File(modelDir, "encoder_model_quantized.onnx")
        val flatFile = File(modelsBaseDir, "$modelId.onnx")
        // Support both new directory layout and old flat file layout
        return encoderInDir.exists() || flatFile.exists()
    }

    private suspend fun ensureSupplementaryFiles(modelId: String, modelDir: File) =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val decoderFile = File(modelDir, "decoder_model_quantized.onnx")
            val vocabFile = File(modelDir, "vocab.json")
            val tokenizerFile = File(modelDir, "tokenizer.json")
            val hfModelPath = "Xenova/$modelId"

            if (!decoderFile.exists()) {
                downloadFile(
                    "$HF_BASE/$hfModelPath/resolve/main/onnx/decoder_model_quantized.onnx",
                    decoderFile
                )
            }
            if (!tokenizerFile.exists()) {
                downloadFile(
                    "$HF_BASE/$hfModelPath/resolve/main/tokenizer.json",
                    tokenizerFile
                )
            }
            if (!vocabFile.exists()) {
                downloadFile(
                    "$HF_BASE/$hfModelPath/resolve/main/vocab.json",
                    vocabFile
                )
            }
        }

    private fun downloadFile(url: String, destFile: File) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Lexi-Translator/1.0")
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.connect()

            if (connection.responseCode in listOf(301, 302, 307, 308)) {
                val redirectUrl = connection.getHeaderField("Location")
                connection.disconnect()
                if (redirectUrl != null) {
                    downloadFile(redirectUrl, destFile)
                    return
                }
            }

            if (connection.responseCode == 200) {
                destFile.parentFile?.mkdirs()
                connection.inputStream.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            // Supplementary download failure is non-fatal — inference will fallback
            e.printStackTrace()
        }
    }

    private suspend fun getOrLoadSession(
        modelId: String,
        modelDir: String
    ): Pair<OnnxTranslationSession, MarianTokenizer> {
        sessions[modelId]?.let { return it }

        val session = OnnxTranslationSession(modelDir)
        session.loadModel()

        val tokenizer = MarianTokenizer(modelDir)
        tokenizer.load()

        val pair = session to tokenizer
        sessions[modelId] = pair
        return pair
    }
}
