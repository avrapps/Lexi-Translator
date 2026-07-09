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

import com.falconlabs.aitranslator.analytics.DesktopAnalyticsImpl
import com.falconlabs.aitranslator.analytics.LexiAnalytics
import com.falconlabs.aitranslator.engine.model.ChecksumVerifier
import com.falconlabs.aitranslator.engine.model.DownloadFileManager
import com.falconlabs.aitranslator.engine.model.HttpDownloader
import com.falconlabs.aitranslator.engine.model.JvmChecksumVerifier
import com.falconlabs.aitranslator.engine.model.JvmDownloadFileManager
import com.falconlabs.aitranslator.engine.model.JvmHttpDownloader
import com.falconlabs.aitranslator.engine.model.JvmStorageInfoProvider
import com.falconlabs.aitranslator.engine.model.StorageInfoProvider
import com.falconlabs.aitranslator.engine.translation.JvmTranslationInferenceProvider
import com.falconlabs.aitranslator.engine.translation.TranslationInferenceProvider

import org.koin.dsl.module

/**
 * Desktop platform-specific Koin module.
 * Provides platform implementations: SqlDriver, engine implementations, and analytics.
 */
val desktopPlatformModule = module {
    // Analytics: structured JSON log files in ~/.lexi/analytics/ (disabled by default)
    single<LexiAnalytics> { DesktopAnalyticsImpl() }

    // Database driver — plain SQLite (no encryption on desktop for now)
    single { com.falconlabs.aitranslator.db.DriverFactory() }

    // Model download infrastructure — JVM implementations
    single<HttpDownloader> { JvmHttpDownloader() }
    single<ChecksumVerifier> { JvmChecksumVerifier() }
    single<DownloadFileManager> { JvmDownloadFileManager() }
    single<StorageInfoProvider> { JvmStorageInfoProvider() }

    // Translation inference — ONNX Runtime on JVM
    single<TranslationInferenceProvider> { JvmTranslationInferenceProvider() }
}
