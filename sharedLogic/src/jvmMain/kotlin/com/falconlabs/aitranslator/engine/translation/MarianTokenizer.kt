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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * SentencePiece Unigram tokenizer for OPUS-MT models.
 * Implements the Viterbi algorithm for optimal subword segmentation.
 *
 * Loads vocabulary from `tokenizer.json` (Hugging Face format) which contains
 * a Unigram model with (token, log_probability) pairs.
 */
class MarianTokenizer(private val modelDir: String) {

    private var vocab: List<Pair<String, Float>> = emptyList()
    private var tokenToId: Map<String, Int> = emptyMap()
    private var idToToken: Map<Int, String> = emptyMap()
    private var isLoaded = false

    private val eosTokenId = 0 // </s>
    private val unkTokenId = 1 // <unk> (but unk_id is 2 in some models)

    /** Maximum token length to consider during Viterbi search. */
    private var maxTokenLength = 0

    /**
     * Loads the Unigram vocabulary from tokenizer.json.
     */
    suspend fun load() = withContext(Dispatchers.IO) {
        val tokenizerFile = File(modelDir, "tokenizer.json")
        if (tokenizerFile.exists()) {
            loadFromTokenizerJson(tokenizerFile)
        } else {
            // Fallback: load from vocab.json (token→id only, no scores)
            val vocabFile = File(modelDir, "vocab.json")
            if (vocabFile.exists()) loadFromVocabJson(vocabFile)
        }
        isLoaded = true
    }

    /**
     * Tokenizes input text using the Unigram Viterbi algorithm.
     * Returns token IDs with EOS appended.
     */
    fun encode(text: String): LongArray {
        if (!isLoaded) return longArrayOf(unkTokenId.toLong(), eosTokenId.toLong())

        // SentencePiece preprocessing: replace spaces with ▁ (word boundary marker)
        val normalized = "\u2581" + text.replace(" ", "\u2581")
        println("[MarianTokenizer] Encoding: '${text.take(40)}...' (${normalized.length} chars)")

        val tokens = viterbiSegment(normalized)
        println("[MarianTokenizer] Segmented into ${tokens.size} tokens")
        val ids = tokens.map { token ->
            tokenToId[token]?.toLong() ?: unkTokenId.toLong()
        }
        return (ids + eosTokenId.toLong()).toLongArray()
    }

    /**
     * Decodes token IDs back to text.
     */
    fun decode(ids: LongArray): String {
        if (!isLoaded) return ""

        val tokens = ids
            .filter { it.toInt() != eosTokenId && it.toInt() != unkTokenId }
            .map { id -> idToToken[id.toInt()] ?: "" }

        return tokens.joinToString("")
            .replace("\u2581", " ")
            .trim()
    }

    /**
     * Viterbi algorithm for Unigram segmentation.
     * Finds the segmentation that maximizes the sum of log probabilities.
     */
    private fun viterbiSegment(text: String): List<String> {
        val n = text.length
        if (n == 0) return emptyList()

        // best[i] = (bestScore, bestTokenEnd) for text[0..i)
        val bestScore = FloatArray(n + 1) { if (it == 0) 0f else Float.NEGATIVE_INFINITY }
        val bestToken = IntArray(n + 1) { 0 } // length of the best last token ending at position i

        for (end in 1..n) {
            val maxStart = maxOf(0, end - maxTokenLength)
            for (start in maxStart until end) {
                val subword = text.substring(start, end)
                val tokenIdx = tokenToId[subword] ?: continue
                val score = vocab[tokenIdx].second
                val totalScore = bestScore[start] + score
                if (totalScore > bestScore[end]) {
                    bestScore[end] = totalScore
                    bestToken[end] = end - start
                }
            }
            // If no token found, try single character as UNK fallback
            if (bestScore[end] == Float.NEGATIVE_INFINITY) {
                bestScore[end] = bestScore[end - 1] + -100f // High penalty for UNK
                bestToken[end] = 1
            }
        }

        // Backtrack to find the best segmentation
        val tokens = mutableListOf<String>()
        var pos = n
        while (pos > 0) {
            val tokenLen = bestToken[pos]
            tokens.add(text.substring(pos - tokenLen, pos))
            pos -= tokenLen
        }
        tokens.reverse()
        return tokens
    }

    private fun loadFromTokenizerJson(file: File) {
        val content = file.readText()
        println("[MarianTokenizer] Parsing tokenizer.json (${content.length} chars)...")

        val vocabList = mutableListOf<Pair<String, Float>>()
        val toId = mutableMapOf<String, Int>()
        val toToken = mutableMapOf<Int, String>()

        // Find the vocab array: handle "vocab" : [ or "vocab":[ with whitespace
        val vocabKeyIdx = content.indexOf("\"vocab\"")
        if (vocabKeyIdx == -1) { println("[MarianTokenizer] ERROR: vocab key not found"); return }
        // Skip past "vocab" and any whitespace/colon to find the first [
        var scanPos = vocabKeyIdx + 7 // skip past "vocab"
        while (scanPos < content.length && content[scanPos] in " \t\n\r:") scanPos++
        if (scanPos >= content.length || content[scanPos] != '[') {
            println("[MarianTokenizer] ERROR: vocab array not found at pos $scanPos")
            return
        }
        // This is the outer [ of the array. Find the first inner [
        var pos = content.indexOf('[', scanPos + 1)
        var idx = 0

        while (pos != -1 && pos < content.length) {
            // Find opening quote of token
            val quoteStart = content.indexOf('"', pos + 1)
            if (quoteStart == -1) break

            // Find closing quote (handle escapes)
            var quoteEnd = quoteStart + 1
            while (quoteEnd < content.length) {
                if (content[quoteEnd] == '\\') { quoteEnd += 2; continue }
                if (content[quoteEnd] == '"') break
                quoteEnd++
            }
            if (quoteEnd >= content.length) break

            val rawToken = content.substring(quoteStart + 1, quoteEnd)
            val token = decodeJsonUnicode(rawToken)

            // Find the score number after the comma
            val commaPos = content.indexOf(',', quoteEnd + 1)
            if (commaPos == -1) break
            val bracketEnd = content.indexOf(']', commaPos)
            if (bracketEnd == -1) break
            val scoreStr = content.substring(commaPos + 1, bracketEnd).trim()
            val score = scoreStr.toFloatOrNull() ?: 0f

            vocabList.add(token to score)
            toId[token] = idx
            toToken[idx] = token
            idx++

            // Find next entry: next '[' after ']'
            pos = content.indexOf('[', bracketEnd + 1)
            // Stop if we hit the end of the vocab array (next ']' before next '[')
            val nextClose = content.indexOf(']', bracketEnd + 1)
            if (nextClose != -1 && nextClose < pos) break
        }

        vocab = vocabList
        tokenToId = toId
        idToToken = toToken
        maxTokenLength = if (vocabList.isNotEmpty()) vocabList.maxOf { it.first.length } else 20
        println("[MarianTokenizer] Loaded ${vocabList.size} tokens, maxLen=$maxTokenLength")
    }

    private fun loadFromVocabJson(file: File) {
        val content = file.readText()
        val toId = mutableMapOf<String, Int>()
        val toToken = mutableMapOf<Int, String>()
        val vocabList = mutableListOf<Pair<String, Float>>()

        val pattern = "\"((?:[^\"\\\\]|\\\\.)*)\"\\s*:\\s*(\\d+)".toRegex()
        pattern.findAll(content).forEach { match ->
            val token = decodeJsonUnicode(match.groupValues[1])
            val id = match.groupValues[2].toIntOrNull() ?: return@forEach
            toId[token] = id
            toToken[id] = token
            // Assign uniform score for vocab.json fallback
            while (vocabList.size <= id) vocabList.add("" to -100f)
            vocabList[id] = token to -5f
        }

        vocab = vocabList
        tokenToId = toId
        idToToken = toToken
        maxTokenLength = toId.keys.maxOfOrNull { it.length } ?: 20
    }

    private fun decodeJsonUnicode(raw: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < raw.length) {
            if (raw[i] == '\\' && i + 1 < raw.length) {
                when (raw[i + 1]) {
                    'u' -> {
                        if (i + 5 < raw.length) {
                            val hex = raw.substring(i + 2, i + 6)
                            val cp = hex.toIntOrNull(16)
                            if (cp != null) { sb.append(cp.toChar()); i += 6; continue }
                        }
                        sb.append(raw[i]); i++
                    }
                    '\\' -> { sb.append('\\'); i += 2 }
                    '"' -> { sb.append('"'); i += 2 }
                    'n' -> { sb.append('\n'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    '/' -> { sb.append('/'); i += 2 }
                    else -> { sb.append(raw[i]); i++ }
                }
            } else {
                sb.append(raw[i]); i++
            }
        }
        return sb.toString()
    }
}
