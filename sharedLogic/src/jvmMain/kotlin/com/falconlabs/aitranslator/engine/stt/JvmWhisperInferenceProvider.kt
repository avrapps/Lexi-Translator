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

import java.io.File

/**
 * JVM implementation of [WhisperInferenceProvider] using [WhisperOnnxInference].
 * Looks for Whisper model files in ~/.lexi/models/whisper-{size}/ directories.
 */
class JvmWhisperInferenceProvider : WhisperInferenceProvider {

    private val modelsBaseDir = File(System.getProperty("user.home"), ".lexi/models")
    private var inference: WhisperOnnxInference? = null
    private var loaded = false

    override suspend fun transcribe(samples: FloatArray, languageCode: String): String? {
        if (!loaded) load()
        return inference?.transcribe(samples, languageCode)
    }

    override fun isReady(): Boolean = loaded && inference != null

    override suspend fun load() {
        if (loaded) return

        // Find any downloaded whisper model directory
        val whisperDir = findWhisperModelDir()
        if (whisperDir != null) {
            val onnx = WhisperOnnxInference(whisperDir.absolutePath)
            onnx.load()
            if (onnx.isReady()) {
                inference = onnx
                loaded = true
            }
        }
    }

    private fun findWhisperModelDir(): File? {
        // Look for whisper-tiny, whisper-small, whisper-medium (prefer smallest)
        val candidates = listOf("whisper-tiny", "whisper-small", "whisper-medium")
        for (name in candidates) {
            val dir = File(modelsBaseDir, name)
            if (dir.exists() && File(dir, "encoder_model_quantized.onnx").exists()) {
                // Also need tokenizer.json — download it if missing
                ensureTokenizer(dir, name)
                return dir
            }
        }
        return null
    }

    private fun ensureTokenizer(dir: File, modelName: String) {
        val tokenizerFile = File(dir, "tokenizer.json")
        if (!tokenizerFile.exists()) {
            try {
                val url = "https://huggingface.co/Xenova/$modelName/resolve/main/tokenizer.json"
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "Lexi-Translator/1.0")
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.connect()

                if (conn.responseCode in listOf(301, 302, 307, 308)) {
                    val redirect = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (redirect != null) {
                        val redirectConn = java.net.URL(redirect).openConnection() as java.net.HttpURLConnection
                        redirectConn.connect()
                        if (redirectConn.responseCode == 200) {
                            redirectConn.inputStream.use { input ->
                                tokenizerFile.outputStream().use { output -> input.copyTo(output) }
                            }
                        }
                        redirectConn.disconnect()
                    }
                } else if (conn.responseCode == 200) {
                    conn.inputStream.use { input ->
                        tokenizerFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {
                // Non-fatal — inference will work without tokenizer but won't decode properly
            }
        }
    }
}
