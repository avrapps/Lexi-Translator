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

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Neural-Minimalist dark color palette derived from DESIGN.md tokens.
 * Optimized for low-light environments and high-focus translation scenarios.
 */
val LexiDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD2BBFF),
    onPrimary = Color(0xFF3F008E),
    primaryContainer = Color(0xFF7C3AED),
    onPrimaryContainer = Color(0xFFEDE0FF),
    inversePrimary = Color(0xFF732EE4),
    secondary = Color(0xFF4CD7F6),
    onSecondary = Color(0xFF003640),
    secondaryContainer = Color(0xFF03B5D3),
    onSecondaryContainer = Color(0xFF00424E),
    tertiary = Color(0xFF4EDEA3),
    onTertiary = Color(0xFF003824),
    tertiaryContainer = Color(0xFF007650),
    onTertiaryContainer = Color(0xFF76FFC2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B1326),
    onBackground = Color(0xFFDAE2FD),
    surface = Color(0xFF0B1326),
    onSurface = Color(0xFFDAE2FD),
    surfaceVariant = Color(0xFF2D3449),
    onSurfaceVariant = Color(0xFFCCC3D8),
    inverseSurface = Color(0xFFDAE2FD),
    inverseOnSurface = Color(0xFF283044),
    outline = Color(0xFF958DA1),
    outlineVariant = Color(0xFF4A4455),
    surfaceTint = Color(0xFFD2BBFF),
)

/**
 * Neural-Minimalist light color palette — inverted variant with
 * white/light backgrounds and violet/cyan accents.
 */
val LexiLightColorScheme = lightColorScheme(
    primary = Color(0xFF732EE4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE0FF),
    onPrimaryContainer = Color(0xFF25005A),
    inversePrimary = Color(0xFFD2BBFF),
    secondary = Color(0xFF006878),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFACEDFF),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = Color(0xFF006C48),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF6FFBBE),
    onTertiaryContainer = Color(0xFF002113),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFCFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFCFCFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFF732EE4),
)
