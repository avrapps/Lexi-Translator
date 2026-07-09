/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.engine.model

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

/**
 * JVM (Desktop) implementation of [HttpDownloader] using java.net.HttpURLConnection.
 * Supports HTTP Range headers for resumable downloads.
 */
class JvmHttpDownloader : HttpDownloader {

    @Volatile
    private var isCancelled = false

    override suspend fun download(
        url: String,
        destFile: String,
        startByte: Long,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit
    ) {
        isCancelled = false
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = true
                if (startByte > 0L) {
                    connection.setRequestProperty("Range", "bytes=$startByte-")
                }
                connection.setRequestProperty("User-Agent", "Lexi-Translator/1.0")
                connection.connectTimeout = 30_000
                connection.readTimeout = 30_000
                connection.connect()

                val responseCode = connection.responseCode
                // Follow manual redirects (some servers use 301/307)
                if (responseCode in listOf(301, 302, 307, 308)) {
                    val redirectUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (redirectUrl != null) {
                        download(redirectUrl, destFile, startByte, onProgress)
                        return@withContext
                    }
                }

                if (responseCode !in listOf(200, 206)) {
                    throw RuntimeException("HTTP $responseCode: ${connection.responseMessage}")
                }

                val contentLength = connection.contentLengthLong
                val totalBytes = if (contentLength > 0L) {
                    if (startByte > 0L) startByte + contentLength else contentLength
                } else {
                    Long.MAX_VALUE
                }

                val file = File(destFile)
                file.parentFile?.mkdirs()

                val raf = RandomAccessFile(file, "rw")
                raf.seek(startByte)

                val buffer = ByteArray(8192)
                var bytesDownloaded = startByte
                val inputStream = connection.inputStream

                var lastProgressUpdate = 0L

                while (coroutineContext.isActive && !isCancelled) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    raf.write(buffer, 0, read)
                    bytesDownloaded += read

                    val now = System.currentTimeMillis()
                    if (now - lastProgressUpdate >= 50L || bytesDownloaded >= totalBytes) {
                        onProgress(
                            bytesDownloaded,
                            if (totalBytes ==
                                Long.MAX_VALUE
                            ) {
                                bytesDownloaded + 1
                            } else {
                                totalBytes
                            }
                        )
                        lastProgressUpdate = now
                    }
                }

                raf.close()
                inputStream.close()

                if (isCancelled) {
                    throw RuntimeException("Download cancelled")
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    override fun cancel() {
        isCancelled = true
    }
}
