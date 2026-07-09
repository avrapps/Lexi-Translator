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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.model_required_dismiss
import aitranslator.sharedui.generated.resources.model_required_download
import aitranslator.sharedui.generated.resources.model_required_message
import aitranslator.sharedui.generated.resources.model_required_title
import org.jetbrains.compose.resources.stringResource

/**
 * Reusable prompt card shown when a required AI model is not installed.
 * Used across Translation, STT (Live Interpreter), and TTS (Neural Speak) screens.
 *
 * @param modelType Human-readable model type ("Translation", "Speech Recognition", "Voice").
 * @param languageInfo Additional context ("English → German", "Whisper Small", etc.).
 * @param onDownload Callback to navigate to Model Store.
 * @param onDismiss Callback to dismiss the prompt.
 * @param modifier Modifier for the card.
 */
@Composable
fun ModelRequiredPrompt(
    modelType: String,
    languageInfo: String,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.model_required_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.model_required_message, modelType, languageInfo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.model_required_dismiss))
                }
                Button(onClick = onDownload) {
                    Text(stringResource(Res.string.model_required_download))
                }
            }
        }
    }
}
