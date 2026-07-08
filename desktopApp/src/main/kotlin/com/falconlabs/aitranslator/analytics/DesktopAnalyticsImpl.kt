/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * COPYLEFT: Using any part of this code requires you to publish your
 * ENTIRE source code under AGPL-3.0. No exceptions. No closed-source use.
 */

package com.falconlabs.aitranslator.analytics

import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Desktop implementation of [LexiAnalytics] that writes structured JSON log files
 * to `~/.lexi/analytics/`.
 *
 * - Analytics is disabled by default (opt-in).
 * - When disabled, no data is written to disk.
 * - Log files are organized by date: `~/.lexi/analytics/YYYY-MM-DD.jsonl`
 * - Each line is a JSON object representing one event.
 *
 * JSON format:
 * ```json
 * {"timestamp":"2024-01-15T10:30:00Z","type":"screen_view","data":{"screen_name":"live_interpreter"}}
 * ```
 */
class DesktopAnalyticsImpl : LexiAnalytics {

    private val analyticsDir: File = File(System.getProperty("user.home"), ".lexi/analytics")
    private var enabled: Boolean = false
    private val breadcrumbs: ArrayDeque<String> = ArrayDeque()

    override fun logScreenView(screenName: String) {
        if (!enabled) return

        addBreadcrumb("screen:$screenName")
        writeEntry("screen_view", mapOf("screen_name" to screenName))
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        if (!enabled) return

        addBreadcrumb("event:$name")
        writeEntry("event", mapOf("name" to name, "params" to params))
    }

    override fun logError(error: Throwable, context: String) {
        if (!enabled) return

        addBreadcrumb("error:${error::class.simpleName}:$context")

        val errorData = mapOf(
            "error_type" to (error::class.simpleName ?: "Unknown"),
            "error_message" to (error.message ?: ""),
            "context" to context,
            "breadcrumbs" to breadcrumbs.toList(),
            "stacktrace" to error.stackTraceToString().take(MAX_STACKTRACE_LENGTH),
        )
        writeEntry("error", errorData)
    }

    override fun setUserProperty(key: String, value: String) {
        if (!enabled) return
        writeEntry("user_property", mapOf("key" to key, "value" to value))
    }

    override fun setAnalyticsEnabled(enabled: Boolean) {
        this.enabled = enabled

        if (!enabled) {
            breadcrumbs.clear()
        } else {
            // Ensure the analytics directory exists when enabling
            analyticsDir.mkdirs()
        }
    }

    /**
     * Maintains a rolling buffer of the last 5 user actions as breadcrumbs.
     */
    private fun addBreadcrumb(action: String) {
        if (breadcrumbs.size >= MAX_BREADCRUMBS) {
            breadcrumbs.removeFirst()
        }
        breadcrumbs.addLast(action)
    }

    /**
     * Writes a single JSON line entry to the daily log file.
     * File naming: `~/.lexi/analytics/YYYY-MM-DD.jsonl`
     */
    private fun writeEntry(type: String, data: Map<String, Any>) {
        try {
            analyticsDir.mkdirs()
            val fileName = "${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}.jsonl"
            val logFile = File(analyticsDir, fileName)
            val jsonLine = buildJsonLine(type, data)
            logFile.appendText("$jsonLine\n")
        } catch (_: Exception) {
            // Silently ignore write failures — analytics should never crash the app
        }
    }

    /**
     * Builds a JSON line string from the event type and data.
     * Uses manual JSON construction to avoid adding a serialization dependency
     * solely for analytics logging.
     */
    private fun buildJsonLine(type: String, data: Map<String, Any>): String {
        val timestamp = Instant.now().toString()
        val dataJson = toJsonObject(data)
        return """{"timestamp":"$timestamp","type":"$type","data":$dataJson}"""
    }

    /**
     * Converts a map to a JSON object string.
     * Handles nested maps and lists for structured logging.
     */
    private fun toJsonObject(map: Map<String, Any>): String {
        val entries = map.entries.joinToString(",") { (key, value) ->
            "\"${escapeJson(key)}\":${toJsonValue(value)}"
        }
        return "{$entries}"
    }

    @Suppress("UNCHECKED_CAST")
    private fun toJsonValue(value: Any): String = when (value) {
        is String -> "\"${escapeJson(value)}\""
        is Number -> value.toString()
        is Boolean -> value.toString()
        is Map<*, *> -> toJsonObject(value as Map<String, Any>)
        is List<*> -> "[${value.joinToString(",") { toJsonValue(it ?: "null") }}]"
        else -> "\"${escapeJson(value.toString())}\""
    }

    private fun escapeJson(text: String): String =
        text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

    companion object {
        private const val MAX_BREADCRUMBS = 5
        private const val MAX_STACKTRACE_LENGTH = 2000
    }
}
