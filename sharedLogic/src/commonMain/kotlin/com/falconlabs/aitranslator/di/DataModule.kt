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

import com.falconlabs.aitranslator.analytics.AnalyticsSettings
import com.falconlabs.aitranslator.data.repository.ModelRepository
import com.falconlabs.aitranslator.data.repository.SqlDelightModelRepository
import com.falconlabs.aitranslator.db.DriverFactory
import com.falconlabs.aitranslator.db.LexiDatabase

import org.koin.dsl.module

/**
 * Koin module providing data-layer dependencies (repositories, DAOs, database).
 * Use [single] for singletons (database, repositories) and [factory] for transient instances.
 *
 * Platform modules must provide [DriverFactory] before this module is loaded.
 */
val dataModule = module {
    single { get<DriverFactory>().createDriver() }
    single { LexiDatabase(get()) }

    // Model repository — CRUD for installed models and download tracking
    single<ModelRepository> { SqlDelightModelRepository(get()) }

    // Analytics settings (disabled by default, wires toggle to LexiAnalytics)
    single { AnalyticsSettings(get()) }
}
