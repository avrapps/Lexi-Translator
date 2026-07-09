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

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.falconlabs.aitranslator.ui.theme.LexiTheme

// -- Settings Main Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainPhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        SettingsMainScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainPhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        SettingsMainScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun SettingsMainTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        SettingsMainScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun SettingsMainTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        SettingsMainScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        SettingsMainScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingsMainDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        SettingsMainScreen()
    }
}

// -- AI Settings Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsPhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        AiSettingsScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsPhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        AiSettingsScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AiSettingsTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        AiSettingsScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AiSettingsTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        AiSettingsScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        AiSettingsScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AiSettingsDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        AiSettingsScreen()
    }
}

// -- Audio Settings Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsPhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsPhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AudioSettingsTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun AudioSettingsTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        AudioSettingsScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AudioSettingsDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        AudioSettingsScreen()
    }
}

// -- Privacy Settings Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsPhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsPhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun PrivacySettingsTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun PrivacySettingsTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        PrivacySettingsScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PrivacySettingsDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        PrivacySettingsScreen()
    }
}
