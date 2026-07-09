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

package com.falconlabs.aitranslator.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

import com.falconlabs.aitranslator.ui.navigation.LexiBottomNavBar
import com.falconlabs.aitranslator.ui.navigation.LexiNavGraph
import com.falconlabs.aitranslator.ui.navigation.LexiTab
import com.falconlabs.aitranslator.ui.theme.LexiTheme

/**
 * Root composable for the Lexi Translator application.
 * Wires together the navigation graph and bottom navigation bar
 * under the Neural-Minimalist [LexiTheme].
 */
@Composable
fun LexiApp(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    animationsEnabled: Boolean = true,
) {
    LexiTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        animationsEnabled = animationsEnabled,
    ) {
        val navController = rememberNavController()
        var selectedTab by rememberSaveable { mutableStateOf(LexiTab.INTERPRETER) }

        Scaffold(
            bottomBar = {
                LexiBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            LexiNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
