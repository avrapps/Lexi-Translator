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

package com.falconlabs.aitranslator.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Theme showcase composable demonstrating all Neural-Minimalist design tokens.
 * Intended for preview rendering across form factors and themes.
 */
@Composable
internal fun LexiThemeShowcase(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TypographySection()
            ColorPaletteSection()
            ShapesSection()
            CaptionSizesSection()
        }
    }
}

@Composable
private fun TypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Typography",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Display Large (40sp)",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Display Medium (32sp)",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Headline Large (24sp)",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Body Large (18sp)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Body Medium (16sp)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "LABEL CAPS (12SP)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "mono_status: 13sp",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun ColorPaletteSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Color Palette",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        ColorRow("Primary", MaterialTheme.colorScheme.primary)
        ColorRow("Secondary", MaterialTheme.colorScheme.secondary)
        ColorRow("Tertiary", MaterialTheme.colorScheme.tertiary)
        ColorRow("Surface", MaterialTheme.colorScheme.surface)
        ColorRow("Error", MaterialTheme.colorScheme.error)
        ColorRow("Outline", MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ColorRow(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ShapesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Shapes",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapeBox("8dp", MaterialTheme.shapes.small)
            ShapeBox("12dp", MaterialTheme.shapes.medium)
            ShapeBox("16dp", MaterialTheme.shapes.large)
            ShapeBox("24dp", MaterialTheme.shapes.extraLarge)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(32.dp)
                .clip(LexiPillShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Pill Shape",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ShapeBox(label: String, shape: androidx.compose.ui.graphics.Shape,) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun CaptionSizesSection() {
    val captionStyles = LocalCaptionTextStyles.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Caption Sizes",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Small (14sp)",
            style = captionStyles.small,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Medium (18sp)",
            style = captionStyles.medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Large (24sp)",
            style = captionStyles.large,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Huge (32sp)",
            style = captionStyles.huge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Presentation (48sp)",
            style = captionStyles.presentation,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// -- Multi-form-factor Theme Previews --

@Preview(name = "Phone Light", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LexiThemeShowcasePhoneLightPreview() {
    LexiTheme(darkTheme = false) {
        LexiThemeShowcase()
    }
}

@Preview(name = "Phone Dark", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun LexiThemeShowcasePhoneDarkPreview() {
    LexiTheme(darkTheme = true) {
        LexiThemeShowcase()
    }
}

@Preview(name = "Tablet Light", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun LexiThemeShowcaseTabletLightPreview() {
    LexiTheme(darkTheme = false) {
        LexiThemeShowcase()
    }
}

@Preview(name = "Tablet Dark", widthDp = 768, heightDp = 1024, showBackground = true)
@Composable
private fun LexiThemeShowcaseTabletDarkPreview() {
    LexiTheme(darkTheme = true) {
        LexiThemeShowcase()
    }
}

@Preview(name = "Desktop Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun LexiThemeShowcaseDesktopLightPreview() {
    LexiTheme(darkTheme = false) {
        LexiThemeShowcase()
    }
}

@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun LexiThemeShowcaseDesktopDarkPreview() {
    LexiTheme(darkTheme = true) {
        LexiThemeShowcase()
    }
}
