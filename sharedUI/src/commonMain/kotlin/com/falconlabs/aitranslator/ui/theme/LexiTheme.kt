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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal controlling whether animations are enabled.
 * Defaults to `true`. Set to `false` when the system reduced-motion
 * preference is detected, disabling AI Orb animations, transitions,
 * and auto-playing motion.
 */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/**
 * CompositionLocal providing caption text styles for Live Interpreter
 * display sizes (Small/Medium/Large/Huge/Presentation).
 */
val LocalCaptionTextStyles = staticCompositionLocalOf { CaptionTextStyles() }

/**
 * Root theme composable for Lexi Translator.
 * Wraps Material3 [MaterialTheme] with Neural-Minimalist design tokens.
 *
 * @param darkTheme Whether to use the dark color scheme. Defaults to system preference.
 * @param dynamicColor Whether to use platform dynamic colors (e.g., Material You on Android).
 *   When true and platform supports it, dynamic colors override the static palette.
 *   Defaults to false to preserve the curated Neural-Minimalist palette.
 * @param animationsEnabled Whether animations should be enabled. Set to false when
 *   the OS prefers-reduced-motion setting is active.
 * @param content The composable content to theme.
 */
@Composable
fun LexiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        dynamicColor -> {
            // Dynamic color is handled per-platform via expect/actual or
            // platform-specific overrides. Fall through to static palette
            // in common code.
            if (darkTheme) LexiDarkColorScheme else LexiLightColorScheme
        }
        darkTheme -> LexiDarkColorScheme
        else -> LexiLightColorScheme
    }

    CompositionLocalProvider(
        LocalAnimationsEnabled provides animationsEnabled,
        LocalCaptionTextStyles provides CaptionTextStyles(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LexiTypography,
            shapes = LexiShapes,
            content = content,
        )
    }
}

/**
 * Convenience accessor for caption text styles within the current theme.
 */
object LexiThemeExtras {
    val captionStyles: CaptionTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalCaptionTextStyles.current

    val animationsEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAnimationsEnabled.current
}
