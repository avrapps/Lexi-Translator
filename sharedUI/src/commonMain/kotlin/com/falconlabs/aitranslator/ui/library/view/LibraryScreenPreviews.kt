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

package com.falconlabs.aitranslator.ui.library.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// -- Library Browser Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LibraryBrowserScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        LibraryBrowserScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LibraryBrowserScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LibraryBrowserScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun LibraryBrowserScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        LibraryBrowserScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun LibraryBrowserScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LibraryBrowserScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun LibraryBrowserScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        LibraryBrowserScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun LibraryBrowserScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LibraryBrowserScreen()
    }
}

// -- Folder View Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun FolderViewScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        FolderViewScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun FolderViewScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        FolderViewScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun FolderViewScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        FolderViewScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun FolderViewScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        FolderViewScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun FolderViewScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        FolderViewScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun FolderViewScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        FolderViewScreen()
    }
}

// -- Entry Detail Screen Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun EntryDetailScreenPhoneLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        EntryDetailScreen()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun EntryDetailScreenPhoneDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        EntryDetailScreen()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun EntryDetailScreenTabletLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        EntryDetailScreen()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun EntryDetailScreenTabletDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        EntryDetailScreen()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun EntryDetailScreenDesktopLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        EntryDetailScreen()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun EntryDetailScreenDesktopDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        EntryDetailScreen()
    }
}
