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

import com.falconlabs.aitranslator.domain.model.ModelId
import java.io.File

/**
 * JVM (Desktop) implementation of [StorageInfoProvider].
 * Stores models in ~/.lexi/models/ directory.
 */
class JvmStorageInfoProvider : StorageInfoProvider {

    private val modelsDir: File by lazy {
        val dir = File(System.getProperty("user.home"), ".lexi/models")
        dir.mkdirs()
        dir
    }

    override fun getAvailableStorageBytes(): Long {
        return modelsDir.usableSpace
    }

    override fun getModelFilePath(modelId: ModelId): String {
        return File(modelsDir, "${modelId.id}.onnx").absolutePath
    }
}
