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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// -- Settings Main Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        SettingsMainScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        SettingsMainScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun SettingsMainScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        SettingsMainScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun SettingsMainScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        SettingsMainScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        SettingsMainScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        SettingsMainScreen()
    }
}

// -- AI Settings Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        AiSettingsScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AiSettingsScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AiSettingsScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        AiSettingsScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AiSettingsScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AiSettingsScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        AiSettingsScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AiSettingsScreen()
    }
}

// -- Audio Settings Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AudioSettingsScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AudioSettingsScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        AudioSettingsScreen()
    }
}

// -- Privacy Settings Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun PrivacySettingsScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun PrivacySettingsScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PrivacySettingsScreen()
    }
}
