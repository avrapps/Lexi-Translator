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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.CpuRequirement
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.ui.models.viewmodel.MockModelData
import com.falconlabs.aitranslator.ui.widgets.ModelStorageEntry
import com.falconlabs.aitranslator.ui.widgets.StorageUsageCard

/**
 * Model Detail screen showing full metadata, action buttons, and storage
 * usage for a specific AI model.
 *
 * Currently uses hardcoded mock data. The modelId parameter will be wired
 * once the download manager is functional.
 */
@Composable
fun ModelDetailScreen(modifier: Modifier = Modifier) {
    // Use a hardcoded model for now (first translation model)
    val model = MockModelData.translationModels.first()
    // Simulate installed state for demonstration (toggle to see both states)
    val isInstalled = true

    ModelDetailContent(
        model = model,
        isInstalled = isInstalled,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelDetailContent(
    model: AiModel,
    isInstalled: Boolean,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Model Details",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Model name and version header
            ModelHeaderSection(model = model)

            // Metadata card
            ModelMetadataCard(model = model)

            // Action buttons
            ModelActionButtons(
                model = model,
                isInstalled = isInstalled,
                onDeleteClick = { showDeleteDialog = true },
                onDownloadClick = {
                    println("Download requested for model: ${model.id.id}")
                },
                onUpdateClick = {
                    println("Update requested for model: ${model.id.id}")
                },
            )

            // Storage usage for this model
            StorageUsageCard(
                totalUsedBytes = model.sizeBytes,
                availableBytes = 8_000_000_000L,
                perModelUsage = listOf(
                    ModelStorageEntry(
                        modelId = model.id,
                        name = model.name,
                        sizeBytes = model.sizeBytes,
                    )
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteModelDialog(
            modelName = model.name,
            dependentPairs = getDependentLanguagePairs(model),
            onConfirm = {
                showDeleteDialog = false
                println("Delete confirmed for model: ${model.id.id}")
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun ModelHeaderSection(model: AiModel) {
    Column {
        Text(
            text = model.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "v${model.version}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formatQualityStars(model.qualityRating),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ModelMetadataCard(model: AiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            MetadataRow(label = "Category", value = formatCategory(model.category))
            MetadataRow(label = "Size", value = formatSize(model.sizeBytes))
            MetadataRow(label = "Engine", value = model.engineType.name)
            MetadataRow(label = "Languages", value = formatLanguages(model))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            MetadataRow(label = "RAM Required", value = "${model.ramRequirementMb} MB")
            MetadataRow(label = "CPU Demand", value = formatCpuRequirement(model.cpuRequirement))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            MetadataRow(label = "License", value = model.license)
            MetadataRow(label = "Publisher", value = model.publisher)
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModelActionButtons(
    model: AiModel,
    isInstalled: Boolean,
    onDeleteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isInstalled) {
            // Delete button
            OutlinedButton(
                onClick = onDeleteClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Delete")
            }
            // Update button
            Button(
                onClick = onUpdateClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Update")
            }
        } else {
            // Download button with size info
            Button(
                onClick = onDownloadClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Download (${formatSize(model.sizeBytes)})")
            }
        }
    }
}

@Composable
private fun DeleteModelDialog(
    modelName: String,
    dependentPairs: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Model?",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete \"$modelName\"?",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (dependentPairs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "The following language pairs depend on this model:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    dependentPairs.forEach { pair ->
                        Text(
                            text = "• $pair",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/** Get dependent language pairs for a model (mock data). */
private fun getDependentLanguagePairs(model: AiModel): List<String> {
    val pair = model.languagePair ?: return emptyList()
    return listOf("${pair.source.code.uppercase()} → ${pair.target.code.uppercase()}")
}

/** Format bytes to human-readable MB string. */
private fun formatSize(bytes: Long): String {
    val mb = bytes / 1_000_000
    return "$mb MB"
}

/** Format quality rating as star glyphs. */
private fun formatQualityStars(rating: Float): String {
    val fullStars = rating.toInt()
    val emptyStars = 5 - fullStars
    val stars = "★".repeat(fullStars) + "☆".repeat(emptyStars)
    val whole = rating.toInt()
    val decimal = ((rating - whole) * 10).toInt()
    return "$stars $whole.$decimal"
}

/** Format category enum to display string. */
private fun formatCategory(category: ModelCategory): String = when (category) {
    ModelCategory.TRANSLATION -> "Translation"
    ModelCategory.STT -> "Speech-to-Text"
    ModelCategory.TTS -> "Text-to-Speech"
}

/** Format languages display. */
private fun formatLanguages(model: AiModel): String {
    val pair = model.languagePair
    return if (pair != null) {
        "${pair.source.code.uppercase()} → ${pair.target.code.uppercase()}"
    } else {
        "Multilingual"
    }
}

/** Format CPU requirement to display string. */
private fun formatCpuRequirement(cpu: CpuRequirement): String = when (cpu) {
    CpuRequirement.LOW -> "Low"
    CpuRequirement.MEDIUM -> "Medium"
    CpuRequirement.HIGH -> "High"
}
