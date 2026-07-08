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

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Android implementation of [LexiAnalytics] integrating Firebase Analytics and Crashlytics.
 *
 * - Analytics is disabled by default (opt-in).
 * - When disabled, no data is sent and no network calls are made.
 * - Crashlytics logs unhandled exceptions and breadcrumbs for the last 5 user actions.
 * - When analytics is disabled, Crashlytics collection is also disabled.
 *
 * @param context Application context used to obtain Firebase instances.
 */
class FirebaseAnalyticsImpl(context: Context) : LexiAnalytics {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    private var enabled: Boolean = false
    private val breadcrumbs: ArrayDeque<String> = ArrayDeque()

    init {
        // Disabled by default — no data collection until user opts in
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        crashlytics.setCrashlyticsCollectionEnabled(false)
    }

    override fun logScreenView(screenName: String) {
        if (!enabled) return

        addBreadcrumb("screen:$screenName")

        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun logEvent(name: String, params: Map<String, Any>) {
        if (!enabled) return

        addBreadcrumb("event:$name")

        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun logError(error: Throwable, context: String) {
        if (!enabled) return

        addBreadcrumb("error:${error::class.simpleName}:$context")

        // Log breadcrumbs as Crashlytics custom keys for context
        crashlytics.setCustomKey("error_context", context)
        crashlytics.setCustomKey("breadcrumbs", breadcrumbs.joinToString(" -> "))

        crashlytics.recordException(error)

        // Also log as analytics event
        val bundle = Bundle().apply {
            putString("error_type", error::class.simpleName ?: "Unknown")
            putString("error_message", error.message ?: "")
            putString("error_context", context)
        }
        firebaseAnalytics.logEvent("error_occurred", bundle)
    }

    override fun setUserProperty(key: String, value: String) {
        if (!enabled) return
        firebaseAnalytics.setUserProperty(key, value)
    }

    override fun setAnalyticsEnabled(enabled: Boolean) {
        this.enabled = enabled
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        crashlytics.setCrashlyticsCollectionEnabled(enabled)

        if (!enabled) {
            breadcrumbs.clear()
        }
    }

    /**
     * Maintains a rolling buffer of the last 5 user actions as breadcrumbs.
     * These are attached to crash reports for debugging context.
     */
    private fun addBreadcrumb(action: String) {
        if (breadcrumbs.size >= MAX_BREADCRUMBS) {
            breadcrumbs.removeFirst()
        }
        breadcrumbs.addLast(action)

        // Also log as Crashlytics breadcrumb
        crashlytics.log(action)
    }

    companion object {
        private const val MAX_BREADCRUMBS = 5
    }
}
