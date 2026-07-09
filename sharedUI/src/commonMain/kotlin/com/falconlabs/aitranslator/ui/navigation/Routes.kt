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

package com.falconlabs.aitranslator.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Lexi Translator app.
 * All routes use kotlinx.serialization for Compose Navigation type-safe arguments.
 */

// -- Top-level tab graph routes (5 tabs) --

@Serializable
object InterpreterGraph

@Serializable
object TranslateGraph

@Serializable
object SpeakGraph

@Serializable
object LibraryGraph

@Serializable
object SettingsGraph

// -- Live Interpreter routes --

@Serializable
object LiveInterpreterRoute

@Serializable
object ConversationHistoryRoute

// -- Text Translation routes --

@Serializable
object TextTranslateRoute

@Serializable
object DictionaryDetailRoute

// -- Neural Speak routes --

@Serializable
object NeuralSpeakRoute

@Serializable
object VoiceLibraryRoute

@Serializable
object DocumentPlayerRoute

// -- Library routes --

@Serializable
object LibraryBrowserRoute

@Serializable
data class FolderViewRoute(val folderId: String)

@Serializable
data class EntryDetailRoute(val entryId: String)

// -- Settings routes (includes Models as sub-screen) --

@Serializable
object SettingsMainRoute

@Serializable
object AiSettingsRoute

@Serializable
object AudioSettingsRoute

@Serializable
object PrivacySettingsRoute

@Serializable
object ModelStoreRoute

@Serializable
data class ModelDetailRoute(val modelId: String)
