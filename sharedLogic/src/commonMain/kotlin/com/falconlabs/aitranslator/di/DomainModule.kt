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

import com.falconlabs.aitranslator.engine.translation.OfflineTranslationEngine
import com.falconlabs.aitranslator.engine.translation.TranslationEngine
import org.koin.dsl.module

/**
 * Koin module providing domain-layer dependencies (use cases, engines).
 * Use [factory] for transient use case instances and [single] for engine singletons.
 */
val domainModule = module {
    // Translation engine — offline ONNX-backed (singleton for model session reuse)
    single<TranslationEngine> { OfflineTranslationEngine(get(), get()) }
}
