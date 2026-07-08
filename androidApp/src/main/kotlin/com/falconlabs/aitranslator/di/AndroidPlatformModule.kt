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

package com.falconlabs.aitranslator.di

import com.falconlabs.aitranslator.ads.AdMobAdsManager
import com.falconlabs.aitranslator.ads.AdsManager
import com.falconlabs.aitranslator.analytics.FirebaseAnalyticsImpl
import com.falconlabs.aitranslator.analytics.LexiAnalytics
import com.falconlabs.aitranslator.billing.BillingRepository
import com.falconlabs.aitranslator.billing.PlayBillingRepository
import org.koin.dsl.module

/**
 * Android platform-specific Koin module.
 * Provides platform implementations: SqlDriver, engine implementations,
 * analytics, ads, and billing services.
 */
val androidPlatformModule = module {
    // Analytics: Firebase Analytics + Crashlytics (disabled by default)
    single<LexiAnalytics> { FirebaseAnalyticsImpl(get()) }

    // Billing: Google Play Billing for "Buy Me a Coffee" in-app purchase
    single<BillingRepository> { PlayBillingRepository(get()) }

    // Ads: AdMob interstitial — shown ONLY on app close, disabled when purchased or offline
    single<AdsManager> { AdMobAdsManager(get(), get()) }

    // Platform-specific bindings for Android
    // Example: single { AndroidSqliteDriver(LexiDatabase.Schema, get(), "lexi.db") }
}
