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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.translate_input_placeholder
import aitranslator.sharedui.generated.resources.translate_char_count

/**
 * Action for the input/output box toolbar.
 */
data class InputAction(
    val icon: String,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/**
 * Reusable text input/output box with fixed height, internal scroll,
 * and a bottom action toolbar.
 *
 * Use for: Translation input, Translation output, TTS input, etc.
 *
 * @param text Current text content.
 * @param onTextChange Callback on change (null = read-only mode).
 * @param actions Toolbar actions at the bottom.
 * @param maxChars Character limit for counter display.
 * @param fixedHeight Fixed height in dp for the text area (scrolls internally).
 * @param placeholder Placeholder text when empty.
 * @param label Optional label above the text.
 * @param readOnly If true, text is not editable (for output display).
 * @param modifier Modifier for outer container.
 */
@Composable
fun TranslationInputBox(
    text: String,
    onTextChange: ((String) -> Unit)?,
    actions: List<InputAction>,
    maxChars: Int,
    modifier: Modifier = Modifier,
    fixedHeight: Int = 180,
    placeholder: String? = null,
    label: String? = null,
    readOnly: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Optional label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 14.dp, top = 10.dp),
            )
        }

        // Fixed-height scrollable text area with visible scrollbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fixedHeight.dp)
                .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
        ) {
            val textScrollState = rememberScrollState()
            if (text.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 10.dp),
                )
            }
            if (readOnly) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp)
                        .verticalScroll(textScrollState),
                )
            } else {
                BasicTextField(
                    value = text,
                    onValueChange = { newText ->
                        if (newText.length <= maxChars) onTextChange?.invoke(newText)
                    },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fixedHeight.dp)
                        .padding(end = 10.dp)
                        .verticalScroll(textScrollState),
                )
            }
            // Visible scrollbar on the right edge
            androidx.compose.foundation.VerticalScrollbar(
                adapter = androidx.compose.foundation.rememberScrollbarAdapter(textScrollState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(fixedHeight.dp),
            )
        }

        // Character counter
        Text(
            text = stringResource(Res.string.translate_char_count, text.length, maxChars),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End).padding(end = 14.dp, bottom = 4.dp),
        )

        // Divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp,
        )

        // Action toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                androidx.compose.material3.TextButton(
                    onClick = action.onClick,
                    enabled = action.enabled,
                ) {
                    Text(
                        text = action.icon,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
