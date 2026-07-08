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

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.falconlabs.aitranslator.billing.BillingRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Android implementation of [AdsManager] using Google AdMob.
 *
 * Behavior:
 * - Loads an interstitial ad on initialization (app start).
 * - Shows the interstitial ONLY when [showExitInterstitial] is called (app close).
 * - Does NOT show ads if the user has purchased "Buy Me a Coffee" ([billingRepository]).
 * - Does NOT load or show ads if the device is offline.
 * - Automatically reloads an ad after one is shown (for the next app close).
 *
 * Ad rules enforced:
 * - NO banner ads, NO inline ads, NO reward ads.
 * - Ads are ONLY shown on app exit (onPause/onStop).
 * - Ads are NEVER shown during active translation, interpretation, or TTS playback.
 *
 * @param context Application context for AdMob initialization and connectivity checks.
 * @param billingRepository Used to check if the user has purchased ad removal.
 */
class AdMobAdsManager(
    private val context: Context,
    private val billingRepository: BillingRepository,
) : AdsManager {

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false

    init {
        MobileAds.initialize(context)
        loadInterstitialAd()
    }

    override fun showExitInterstitial() {
        // Do not show ads if user purchased ad removal
        if (billingRepository.hasRemoveAdsPurchase()) return

        // Do not show ads while offline (Requirement 12.5)
        if (!isNetworkAvailable()) return

        val ad = interstitialAd ?: return

        // Resolve the current foreground activity for ad display
        val activity = resolveCurrentActivity() ?: return

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitialAd()
            }
        }

        ad.show(activity)
    }

    override fun isAdFree(): Boolean {
        return billingRepository.hasRemoveAdsPurchase()
    }

    /**
     * Loads an interstitial ad in the background.
     * Does nothing if:
     * - An ad is already loaded or currently loading.
     * - The user has purchased ad removal.
     * - The device is offline.
     */
    private fun loadInterstitialAd() {
        if (interstitialAd != null || isAdLoading) return
        if (billingRepository.hasRemoveAdsPurchase()) return
        if (!isNetworkAvailable()) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isAdLoading = false
                }
            },
        )
    }

    /**
     * Checks if the device has active network connectivity.
     * Used to suppress ad loading/display in offline mode (Requirement 12.5).
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Resolves the current foreground Activity for ad display.
     * If the context is an Activity, uses it directly.
     * Otherwise, attempts to unwrap a ContextWrapper chain.
     */
    private fun resolveCurrentActivity(): Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    companion object {
        /**
         * AdMob interstitial ad unit ID.
         * Replace with the production ad unit ID before release.
         * Current value is the AdMob test ad unit ID for interstitials.
         */
        private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }
}
