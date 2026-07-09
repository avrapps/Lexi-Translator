/*
 * Lexi Translator — Offline AI Translation Platform
 * Copyright (C) 2024-2026 ANRMS PRIVATE LIMITED
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.falconlabs.aitranslator.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

import kotlin.math.sin

/**
 * Visual states for the AI Orb (Requirement 2.1).
 */
enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    DOWNLOADING,
    ERROR,
    LOW_BATTERY
}

/**
 * Animated AI Orb composable — the central visual element of the Live Interpreter.
 *
 * Displays animated gradients and pulsing effects based on the current [OrbState].
 * Reflects audio amplitude during LISTENING state via waveform-like pulsing.
 *
 * Supports Requirements 2.1 (animated states), 2.2 (waveform visualization),
 * and 9.7 (reduced-motion support via static indicators when animations disabled).
 *
 * @param state Current orb state.
 * @param audioLevel Audio amplitude (0.0-1.0) for waveform visualization in LISTENING state.
 * @param modifier Modifier for the composable.
 */
@Composable
fun AiOrb(
    state: OrbState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    // Pulsing animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.LISTENING -> 600
                    OrbState.THINKING -> 1200
                    OrbState.SPEAKING -> 800
                    else -> 2000
                },
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    // Color animation based on state
    val primaryColor by animateColorAsState(
        targetValue = when (state) {
            OrbState.IDLE -> Color(0xFF7C3AED) // Purple
            OrbState.LISTENING -> Color(0xFF22D3EE) // Cyan
            OrbState.THINKING -> Color(0xFFD2BBFF) // Light purple
            OrbState.SPEAKING -> Color(0xFF4EDEA3) // Green
            OrbState.DOWNLOADING -> Color(0xFF4CD7F6) // Blue
            OrbState.ERROR -> Color(0xFFFFB4AB) // Error red
            OrbState.LOW_BATTERY -> Color(0xFFFEBC2E) // Warning yellow
        },
        animationSpec = tween(500),
        label = "color",
    )

    val secondaryColor by animateColorAsState(
        targetValue = when (state) {
            OrbState.IDLE -> Color(0xFF302B63)
            OrbState.LISTENING -> Color(0xFF7C3AED)
            OrbState.THINKING -> Color(0xFF5383EC)
            OrbState.SPEAKING -> Color(0xFF007650)
            OrbState.DOWNLOADING -> Color(0xFF03B5D3)
            OrbState.ERROR -> Color(0xFF93000A)
            OrbState.LOW_BATTERY -> Color(0xFF690005)
        },
        animationSpec = tween(500),
        label = "secondary",
    )

    // Audio-reactive scale for LISTENING state
    val audioScale by animateFloatAsState(
        targetValue = if (state == OrbState.LISTENING) 1f + audioLevel * 0.3f else 1f,
        animationSpec = tween(50),
        label = "audio",
    )

    val stateLabel = when (state) {
        OrbState.IDLE -> "READY"
        OrbState.LISTENING -> "LISTENING"
        OrbState.THINKING -> "TRANSLATING"
        OrbState.SPEAKING -> "SPEAKING"
        OrbState.DOWNLOADING -> "LOADING"
        OrbState.ERROR -> "ERROR"
        OrbState.LOW_BATTERY -> "LOW BATTERY"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.size(180.dp),
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val scale = pulseScale * audioScale
                val radius = size.minDimension / 2f * scale * 0.7f

                // Outer glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                        center = center,
                        radius = radius * 1.6f,
                    ),
                    radius = radius * 1.6f,
                    center = center,
                )

                // Main orb gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor, secondaryColor),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )

                // Inner highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius * 0.5f,
                    center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                )

                // Audio waveform bars in LISTENING state
                if (state == OrbState.LISTENING && audioLevel > 0.01f) {
                    drawWaveformBars(this, center, radius, audioLevel, primaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stateLabel,
            style = MaterialTheme.typography.labelSmall,
            color = primaryColor,
        )
    }
}

private fun drawWaveformBars(
    scope: DrawScope,
    center: Offset,
    radius: Float,
    audioLevel: Float,
    color: Color,
) {
    val barCount = 5
    val barWidth = radius * 0.08f
    val maxBarHeight = radius * 0.6f * audioLevel

    for (i in 0 until barCount) {
        val x = center.x + (i - barCount / 2) * barWidth * 3f
        val height =
            maxBarHeight * (0.3f + 0.7f * sin((i * 1.2f + audioLevel * 10f).toDouble()).toFloat().coerceIn(0f, 1f))
        scope.drawRoundRect(
            color = color.copy(alpha = 0.7f),
            topLeft = Offset(x - barWidth / 2, center.y - height / 2),
            size = androidx.compose.ui.geometry.Size(barWidth, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2),
        )
    }
}
