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

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.nav_tab_interpreter
import aitranslator.sharedui.generated.resources.nav_tab_library
import aitranslator.sharedui.generated.resources.nav_tab_settings
import aitranslator.sharedui.generated.resources.nav_tab_speak
import aitranslator.sharedui.generated.resources.nav_tab_translate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom navigation tab definition.
 * Labels use Compose Resources for i18n/Crowdin support.
 */
enum class LexiTab(val labelRes: StringResource, val iconLabel: String, val route: Any) {
    INTERPRETER(
        labelRes = Res.string.nav_tab_interpreter,
        iconLabel = "\uD83C\uDF10",
        route = InterpreterGraph
    ),
    TRANSLATE(
        labelRes = Res.string.nav_tab_translate,
        iconLabel = "\uD83D\uDCDD",
        route = TranslateGraph
    ),
    SPEAK(
        labelRes = Res.string.nav_tab_speak,
        iconLabel = "\uD83D\uDD0A",
        route = SpeakGraph
    ),
    LIBRARY(
        labelRes = Res.string.nav_tab_library,
        iconLabel = "\uD83D\uDCDA",
        route = LibraryGraph
    ),
    SETTINGS(
        labelRes = Res.string.nav_tab_settings,
        iconLabel = "\uD83D\uDD27",
        route = SettingsGraph
    )
}

/**
 * Lexi bottom navigation bar with 5 tabs.
 */
@Composable
fun LexiBottomNavBar(
    selectedTab: LexiTab,
    onTabSelected: (LexiTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        LexiTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = { Text(text = tab.iconLabel) },
                label = { Text(text = stringResource(tab.labelRes)) }
            )
        }
    }
}
