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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation

import com.falconlabs.aitranslator.ui.interpreter.view.ConversationHistoryScreen
import com.falconlabs.aitranslator.ui.interpreter.view.LiveInterpreterScreen
import com.falconlabs.aitranslator.ui.library.view.EntryDetailScreen
import com.falconlabs.aitranslator.ui.library.view.FolderViewScreen
import com.falconlabs.aitranslator.ui.library.view.LibraryBrowserScreen
import com.falconlabs.aitranslator.ui.models.view.ModelDetailScreen
import com.falconlabs.aitranslator.ui.models.view.ModelStoreScreen
import com.falconlabs.aitranslator.ui.settings.view.AiSettingsScreen
import com.falconlabs.aitranslator.ui.settings.view.AudioSettingsScreen
import com.falconlabs.aitranslator.ui.settings.view.PrivacySettingsScreen
import com.falconlabs.aitranslator.ui.settings.view.SettingsMainScreen
import com.falconlabs.aitranslator.ui.speak.view.DocumentPlayerScreen
import com.falconlabs.aitranslator.ui.speak.view.NeuralSpeakScreen
import com.falconlabs.aitranslator.ui.speak.view.VoiceLibraryScreen
import com.falconlabs.aitranslator.ui.translation.view.DictionaryDetailScreen
import com.falconlabs.aitranslator.ui.translation.view.TextTranslateScreen

/**
 * Root navigation graph for the Lexi Translator app.
 * Uses nested navigation graphs per tab for back stack isolation.
 */
@Composable
fun LexiNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = InterpreterGraph,
        modifier = modifier
    ) {
        // -- Interpreter Tab --
        navigation<InterpreterGraph>(startDestination = LiveInterpreterRoute) {
            composable<LiveInterpreterRoute> {
                LiveInterpreterScreen(
                    onNavigateToModels = { navController.navigate(ModelStoreRoute) },
                )
            }
            composable<ConversationHistoryRoute> { ConversationHistoryScreen() }
        }

        // -- Translate Tab --
        navigation<TranslateGraph>(startDestination = TextTranslateRoute) {
            composable<TextTranslateRoute> {
                TextTranslateScreen(
                    onNavigateToModels = { source, target ->
                        navController.navigate(ModelStoreRoute)
                    },
                )
            }
            composable<DictionaryDetailRoute> { DictionaryDetailScreen() }
        }

        // -- Speak Tab --
        navigation<SpeakGraph>(startDestination = NeuralSpeakRoute) {
            composable<NeuralSpeakRoute> { NeuralSpeakScreen() }
            composable<VoiceLibraryRoute> { VoiceLibraryScreen() }
            composable<DocumentPlayerRoute> { DocumentPlayerScreen() }
        }

        // -- Library Tab --
        navigation<LibraryGraph>(startDestination = LibraryBrowserRoute) {
            composable<LibraryBrowserRoute> { LibraryBrowserScreen() }
            composable<FolderViewRoute> { FolderViewScreen() }
            composable<EntryDetailRoute> { EntryDetailScreen() }
        }

        // -- Settings Tab (includes Models as sub-screens) --
        navigation<SettingsGraph>(startDestination = SettingsMainRoute) {
            composable<SettingsMainRoute> {
                SettingsMainScreen(
                    onNavigateToModelStore = {
                        navController.navigate(ModelStoreRoute)
                    },
                )
            }
            composable<AiSettingsRoute> { AiSettingsScreen() }
            composable<AudioSettingsRoute> { AudioSettingsScreen() }
            composable<PrivacySettingsRoute> { PrivacySettingsScreen() }
            composable<ModelStoreRoute> {
                ModelStoreScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable<ModelDetailRoute> { ModelDetailScreen() }
        }
    }
}
