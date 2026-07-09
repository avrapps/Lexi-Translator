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

package com.falconlabs.aitranslator.ui.models.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.falconlabs.aitranslator.ui.models.viewmodel.MockModelData
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreState
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreTab
import com.falconlabs.aitranslator.ui.theme.LexiTheme

// -- Model Store Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ModelStorePhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TRANSLATION,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ModelStorePhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TRANSLATION,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun ModelStoreTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TRANSLATION,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun ModelStoreTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TRANSLATION,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun ModelStoreDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TRANSLATION,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun ModelStoreDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TRANSLATION,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

// -- STT Tab Preview --

@Preview(name = "Phone Light — STT", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ModelStoreSttPreview() {
    LexiTheme(darkTheme = false) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.STT,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

// -- TTS Tab Preview --

@Preview(name = "Phone Light — TTS", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ModelStoreTtsPreview() {
    LexiTheme(darkTheme = false) {
        ModelStoreContent(
            state = ModelStoreState(
                selectedTab = ModelStoreTab.TTS,
                translationModels = MockModelData.translationModels,
                sttModels = MockModelData.sttModels,
                ttsModels = MockModelData.ttsModels,
            ),
            onIntent = {},
            onModelClick = {},
        )
    }
}

// -- Model Detail Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ModelDetailPhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        ModelDetailScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ModelDetailPhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        ModelDetailScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun ModelDetailTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        ModelDetailScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun ModelDetailTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        ModelDetailScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun ModelDetailDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        ModelDetailScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun ModelDetailDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        ModelDetailScreen()
    }
}
