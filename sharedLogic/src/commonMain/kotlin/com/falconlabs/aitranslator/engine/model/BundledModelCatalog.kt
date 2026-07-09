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

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.CpuRequirement
import com.falconlabs.aitranslator.domain.model.EngineType
import com.falconlabs.aitranslator.domain.model.LanguageCode
import com.falconlabs.aitranslator.domain.model.LanguagePair
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Model catalog loaded from bundled `model-catalog.json` resource file.
 * Provides the list of all available AI models with download URLs.
 *
 * The JSON-driven approach allows:
 * - Easy addition of new models without code changes
 * - Future remote catalog updates without app updates
 * - Clear separation of data from logic
 */
class BundledModelCatalog : ModelCatalogProvider {

    private val catalog: List<AiModel>
    private val catalogFlow: MutableStateFlow<List<AiModel>>
    private val downloadFiles: Map<String, Map<String, String>> // modelId → (filename → url)
    private val downloadUrls: Map<String, String> // modelId → primary download URL

    init {
        val jsonText = loadCatalogJson()
        val parsed = parseCatalog(jsonText)
        catalog = parsed.first
        downloadFiles = parsed.second
        downloadUrls = downloadFiles.mapValues { (_, files) -> files.values.first() }
        catalogFlow = MutableStateFlow(catalog)
    }

    override fun getCatalog(): Flow<List<AiModel>> = catalogFlow

    override fun getCatalogSnapshot(): List<AiModel> = catalog

    override fun getModelById(modelId: ModelId): AiModel? = catalog.find { it.id == modelId }

    override fun getDownloadUrl(modelId: ModelId): String = downloadUrls[modelId.id] ?: ""

    /**
     * Returns all files to download for a model.
     * Key = local filename, Value = remote URL.
     */
    fun getModelFiles(modelId: ModelId): Map<String, String> = downloadFiles[modelId.id] ?: emptyMap()

    private fun loadCatalogJson(): String {
        // Load from classpath resource
        val stream = this::class.java.classLoader?.getResourceAsStream("model-catalog.json")
            ?: return "{}"
        return stream.bufferedReader().use { it.readText() }
    }

    private fun parseCatalog(json: String): Pair<List<AiModel>, Map<String, Map<String, String>>> {
        val models = mutableListOf<AiModel>()
        val files = mutableMapOf<String, Map<String, String>>()

        // Parse translation models
        parseCategory(json, "translation", ModelCategory.TRANSLATION, models, files)
        // Parse speech models
        parseCategory(json, "speech", ModelCategory.STT, models, files)
        // Parse voice models
        parseCategory(json, "voice", ModelCategory.TTS, models, files)

        return models to files
    }

    private fun parseCategory(
        json: String,
        categoryKey: String,
        category: ModelCategory,
        models: MutableList<AiModel>,
        files: MutableMap<String, Map<String, String>>
    ) {
        // Find the category block
        val categoryStart = json.indexOf("\"$categoryKey\"")
        if (categoryStart == -1) return
        val blockStart = json.indexOf("{", categoryStart + categoryKey.length + 3)
        if (blockStart == -1) return

        // Find model ID blocks within this category
        val modelIdPattern = "\"(opus-mt-[^\"]+|whisper-[^\"]+|kokoro-[^\"]+|piper-[^\"]+|vits-[^\"]+)\"\\s*:".toRegex()
        val matches = modelIdPattern.findAll(json, blockStart)

        for (match in matches) {
            val modelId = match.groupValues[1]
            val modelBlockStart = json.indexOf("{", match.range.last)
            if (modelBlockStart == -1) continue
            val modelBlockEnd = findMatchingBrace(json, modelBlockStart)
            if (modelBlockEnd == -1) continue
            val modelJson = json.substring(modelBlockStart, modelBlockEnd + 1)

            val model = parseModel(modelId, modelJson, category)
            if (model != null) {
                models.add(model)
                val modelFiles = parseFilesToDownload(modelJson)
                if (modelFiles.isNotEmpty()) {
                    files[modelId] = modelFiles
                }
            }
        }
    }

    private fun parseModel(
        modelId: String,
        json: String,
        category: ModelCategory
    ): AiModel? {
        val name = when (category) {
            ModelCategory.TRANSLATION -> {
                val from = extractString(json, "fromLanguage") ?: return null
                val to = extractString(json, "toLanguage") ?: return null
                "$from → $to"
            }
            ModelCategory.STT -> extractString(json, "language") ?: "Unknown"
            ModelCategory.TTS -> extractString(json, "voiceName") ?: extractString(json, "language") ?: "Unknown"
        }

        val languagePair = if (category == ModelCategory.TRANSLATION) {
            val from = extractString(json, "fromLocale") ?: return null
            val to = extractString(json, "toLocale") ?: return null
            LanguagePair(LanguageCode(from), LanguageCode(to))
        } else {
            null
        }

        return AiModel(
            id = ModelId(modelId),
            name = name,
            category = category,
            version = extractString(json, "version") ?: "1.0.0",
            sizeBytes = extractLong(json, "sizeBytes") ?: 0L,
            languagePair = languagePair,
            engineType = parseEngine(extractString(json, "engine") ?: "OPUS_MT"),
            qualityRating = extractFloat(json, "rating") ?: 3.0f,
            ramRequirementMb = extractInt(json, "ramRequiredMb") ?: 100,
            cpuRequirement = parseCpu(extractString(json, "cpuRequirement") ?: "LOW"),
            license = extractString(json, "license") ?: "Unknown",
            publisher = extractString(json, "publisher") ?: "Unknown",
        )
    }

    private fun parseFilesToDownload(json: String): Map<String, String> {
        val blockKey = "filesToDownload"
        val start = json.indexOf("\"$blockKey\"")
        if (start == -1) return emptyMap()
        val braceStart = json.indexOf("{", start + blockKey.length)
        if (braceStart == -1) return emptyMap()
        val braceEnd = findMatchingBrace(json, braceStart)
        if (braceEnd == -1) return emptyMap()
        val block = json.substring(braceStart + 1, braceEnd)

        val result = mutableMapOf<String, String>()
        val pattern = "\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        pattern.findAll(block).forEach { match ->
            result[match.groupValues[1]] = match.groupValues[2]
        }
        return result
    }

    private fun extractString(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    private fun extractLong(json: String, key: String): Long? {
        val pattern = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toLongOrNull()
    }

    private fun extractFloat(json: String, key: String): Float? {
        val pattern = "\"$key\"\\s*:\\s*([\\d.]+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toFloatOrNull()
    }

    private fun extractInt(json: String, key: String): Int? {
        val pattern = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun parseEngine(value: String): EngineType = when (value.uppercase()) {
        "OPUS_MT" -> EngineType.OPUS_MT
        "WHISPER" -> EngineType.WHISPER
        "KOKORO" -> EngineType.KOKORO
        "PIPER" -> EngineType.PIPER
        "VITS" -> EngineType.VITS
        else -> EngineType.OPUS_MT
    }

    private fun parseCpu(value: String): CpuRequirement = when (value.uppercase()) {
        "LOW" -> CpuRequirement.LOW
        "MEDIUM" -> CpuRequirement.MEDIUM
        "HIGH" -> CpuRequirement.HIGH
        else -> CpuRequirement.LOW
    }

    private fun findMatchingBrace(text: String, openIndex: Int): Int {
        var depth = 0
        for (i in openIndex until text.length) {
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
}
