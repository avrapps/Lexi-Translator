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

package com.falconlabs.aitranslator.ads

/**
 * Platform-agnostic interface for managing in-app advertisements.
 *
 * Ad display rules (Requirement 12):
 * - Interstitials are shown ONLY when the user closes the application (onPause/onStop).
 * - NO banner, inline, or reward ads are shown at any time during usage.
 * - Ads are NEVER shown during active translation, interpretation, or TTS playback.
 * - Ads are NEVER loaded or shown while Offline Mode is active.
 * - When the user purchases "Buy Me a Coffee", [isAdFree] returns true and all ads are suppressed.
 *
 * Platform implementations:
 * - Android: [AdMobAdsManager] — AdMob interstitial, loaded on app start, shown on app close.
 * - Desktop / iOS: No-op implementation (no ads).
 */
interface AdsManager {

    /**
     * Attempts to show an interstitial ad.
     *
     * This MUST only be called from an onPause/onStop lifecycle callback triggered
     * by the user navigating away from or closing the application. It MUST NOT be
     * called during any active feature interaction.
     *
     * The implementation is responsible for:
     * - Doing nothing if [isAdFree] returns true.
     * - Doing nothing if the device is offline.
     * - Doing nothing if no ad has been loaded yet.
     */
    fun showExitInterstitial()

    /**
     * Returns true if the user has purchased "Buy Me a Coffee" (remove ads).
     *
     * When this returns true, [showExitInterstitial] is a no-op and no ad requests
     * are made to the ad network.
     *
     * @return true if the user is entitled to an ad-free experience.
     */
    fun isAdFree(): Boolean
}

/**
 * No-op [AdsManager] for platforms that do not serve ads (Desktop, iOS).
 * All calls are safe, side-effect-free no-ops.
 */
class NoOpAdsManager : AdsManager {

    override fun showExitInterstitial() {
        // No ads on this platform
    }

    override fun isAdFree(): Boolean = true
}
