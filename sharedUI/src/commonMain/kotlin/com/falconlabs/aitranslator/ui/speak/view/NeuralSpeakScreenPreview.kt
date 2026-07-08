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

package com.falconlabs.aitranslator.ui.speak.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun NeuralSpeakScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        NeuralSpeakScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun NeuralSpeakScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        NeuralSpeakScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun NeuralSpeakScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        NeuralSpeakScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun NeuralSpeakScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        NeuralSpeakScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun NeuralSpeakScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        NeuralSpeakScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun NeuralSpeakScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        NeuralSpeakScreen()
    }
}
