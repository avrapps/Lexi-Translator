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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the analytics enabled/disabled state.
 * This acts as the single source of truth for the analytics toggle.
 *
 * Analytics is DISABLED by default (opt-in model).
 * When the full [SettingsRepository] is implemented, this class will delegate
 * persistence to the `user_setting` table with key "privacy.analytics_enabled".
 *
 * @param analytics The [LexiAnalytics] platform implementation to toggle.
 */
class AnalyticsSettings(private val analytics: LexiAnalytics) {

    private val _isEnabled = MutableStateFlow(DEFAULT_ENABLED)

    /**
     * Observable state of analytics enabled status.
     * UI can collect this to render the toggle state.
     */
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    /**
     * Toggles analytics collection on or off.
     * Propagates the state to the underlying [LexiAnalytics] implementation.
     *
     * @param enabled true to opt-in to analytics, false to disable all collection.
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        analytics.setAnalyticsEnabled(enabled)
    }

    companion object {
        /** Analytics is disabled by default — user must explicitly opt in. */
        const val DEFAULT_ENABLED = false

        /** Settings key for persisting analytics preference. */
        const val SETTING_KEY = "privacy.analytics_enabled"
    }
}
