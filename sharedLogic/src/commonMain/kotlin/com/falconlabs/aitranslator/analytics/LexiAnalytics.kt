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

/**
 * Analytics interface for tracking screen views, events, errors, and user properties
 * across all Lexi Translator platforms.
 *
 * Analytics is DISABLED by default (opt-in). When disabled, all calls are no-ops.
 * Users can enable via Settings > Privacy > Analytics toggle.
 *
 * Platform implementations:
 * - Android: [FirebaseAnalyticsImpl] — Firebase Analytics + Crashlytics
 * - Desktop: [DesktopAnalyticsImpl] — structured JSON log files in ~/.lexi/analytics/
 *
 * Required metric events:
 * - Screen views (all 6 tabs + sub-screens)
 * - translation_completed (source_lang, target_lang, duration_ms, mode, confidence)
 * - stt_session_started / stt_session_ended (language, duration_ms, utterance_count)
 * - tts_playback_started (voice, document_type, length)
 * - model_downloaded / model_deleted (model_id, size_bytes, duration_ms)
 * - export_completed (format, entry_count)
 * - setting_changed (key, old_value, new_value)
 * - error_occurred (error_type, context)
 */
interface LexiAnalytics {

    /**
     * Logs a screen view event.
     * Should be called on every screen entry.
     *
     * @param screenName Unique name of the screen (e.g., "live_interpreter", "settings_privacy")
     */
    fun logScreenView(screenName: String)

    /**
     * Logs a named event with optional parameters.
     *
     * @param name Event name (e.g., "translation_completed", "model_downloaded")
     * @param params Key-value pairs describing the event (e.g., source_lang, duration_ms)
     */
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())

    /**
     * Logs an error with optional context description.
     * On Android, unhandled exceptions are also forwarded to Crashlytics.
     *
     * @param error The [Throwable] that occurred
     * @param context Human-readable description of where/why the error occurred
     */
    fun logError(error: Throwable, context: String = "")

    /**
     * Sets a user property for segmentation.
     *
     * @param key Property name (e.g., "preferred_engine", "ui_language")
     * @param value Property value
     */
    fun setUserProperty(key: String, value: String)

    /**
     * Enables or disables analytics collection.
     * When [enabled] is false, all subsequent log calls become no-ops and
     * no data is sent to any backend. Defaults to false (opt-in model).
     *
     * @param enabled true to enable, false to disable
     */
    fun setAnalyticsEnabled(enabled: Boolean)
}
