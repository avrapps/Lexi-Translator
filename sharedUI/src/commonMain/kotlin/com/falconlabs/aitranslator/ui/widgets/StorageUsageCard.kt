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

package com.falconlabs.aitranslator.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.falconlabs.aitranslator.domain.model.ModelId

/**
 * Data class representing per-model storage usage for display.
 */
data class ModelStorageEntry(val modelId: ModelId, val name: String, val sizeBytes: Long,)

/**
 * Mock storage data for UI development.
 * Total 2 GB used, 8 GB available, with 5 models.
 */
object MockStorageData {
    private const val MB = 1_000_000L
    private const val GB = 1_000_000_000L

    val totalUsedBytes: Long = 2 * GB
    val availableBytes: Long = 8 * GB

    val perModelUsage: List<ModelStorageEntry> = listOf(
        ModelStorageEntry(ModelId("whisper-medium"), "Whisper Medium", 750 * MB),
        ModelStorageEntry(ModelId("whisper-small"), "Whisper Small", 240 * MB),
        ModelStorageEntry(ModelId("vits-ja-female"), "VITS Japanese Female", 52 * MB),
        ModelStorageEntry(ModelId("kokoro-en-female"), "Kokoro English Female", 45 * MB),
        ModelStorageEntry(ModelId("opus-mt-en-jap"), "Xenova/opus-mt-en-jap", 38 * MB),
    )
}

/**
 * Reusable storage usage card showing a visual progress bar,
 * used/available space, per-model breakdown, and low storage warning.
 *
 * Can be used on both the Model Store overview and Model Detail screens.
 */
@Composable
fun StorageUsageCard(
    totalUsedBytes: Long,
    availableBytes: Long,
    perModelUsage: List<ModelStorageEntry>,
    modifier: Modifier = Modifier,
) {
    val totalCapacity = totalUsedBytes + availableBytes
    val usageFraction = if (totalCapacity > 0) {
        (totalUsedBytes.toFloat() / totalCapacity).coerceIn(0f, 1f)
    } else {
        0f
    }
    val isLowStorage = availableBytes < 500_000_000L // < 500 MB

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Storage Usage",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { usageFraction },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (isLowStorage) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Used / Available text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Used: ${formatBytes(totalUsedBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Available: ${formatBytes(availableBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Low storage warning
            if (isLowStorage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Storage is running low. Consider deleting unused models.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // Per-model breakdown (top 5)
            if (perModelUsage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Models by size",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))

                perModelUsage.take(5).forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = formatBytes(entry.sizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}

/** Format bytes to human-readable string (MB or GB). */
private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000_000L -> {
        val whole = (bytes / 100_000_000).toInt()
        "${whole / 10}.${whole % 10} GB"
    }
    else -> {
        val mb = bytes / 1_000_000
        "$mb MB"
    }
}
