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

package com.falconlabs.aitranslator.ui.settings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.settings_title
import aitranslator.sharedui.generated.resources.settings_models
import aitranslator.sharedui.generated.resources.settings_ai
import aitranslator.sharedui.generated.resources.settings_audio
import aitranslator.sharedui.generated.resources.settings_privacy
import aitranslator.sharedui.generated.resources.settings_display
import aitranslator.sharedui.generated.resources.settings_battery
import aitranslator.sharedui.generated.resources.settings_download

/** Identifiers for each settings menu row. */
internal enum class SettingsItem {
    MODELS, AI, AUDIO, PRIVACY, DISPLAY, BATTERY, DOWNLOAD
}

/**
 * Main Settings screen with a list of configuration categories.
 * The "Models" row navigates to the Model Store.
 *
 * @param onNavigateToModelStore Callback invoked when the user taps the Models row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onNavigateToModelStore: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(SettingsItem.entries.toList()) { item ->
                SettingsRow(
                    label = settingsItemLabel(item),
                    onClick = {
                        when (item) {
                            SettingsItem.MODELS -> onNavigateToModelStore()
                            else -> { /* Other sections not yet wired */ }
                        }
                    },
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun settingsItemLabel(item: SettingsItem): String = when (item) {
    SettingsItem.MODELS -> stringResource(Res.string.settings_models)
    SettingsItem.AI -> stringResource(Res.string.settings_ai)
    SettingsItem.AUDIO -> stringResource(Res.string.settings_audio)
    SettingsItem.PRIVACY -> stringResource(Res.string.settings_privacy)
    SettingsItem.DISPLAY -> stringResource(Res.string.settings_display)
    SettingsItem.BATTERY -> stringResource(Res.string.settings_battery)
    SettingsItem.DOWNLOAD -> stringResource(Res.string.settings_download)
}
