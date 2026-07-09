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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.falconlabs.aitranslator.domain.model.AiModel
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreIntent
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreState
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreTab
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreViewModel
import com.falconlabs.aitranslator.ui.widgets.MockStorageData
import com.falconlabs.aitranslator.ui.widgets.StorageUsageCard
import org.jetbrains.compose.resources.stringResource
import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.model_store_title
import aitranslator.sharedui.generated.resources.model_store_tab_translation
import aitranslator.sharedui.generated.resources.model_store_tab_stt
import aitranslator.sharedui.generated.resources.model_store_tab_tts
import aitranslator.sharedui.generated.resources.model_store_size_label
import aitranslator.sharedui.generated.resources.model_store_ram_label
import aitranslator.sharedui.generated.resources.model_store_engine_label
import aitranslator.sharedui.generated.resources.model_store_download_button
import aitranslator.sharedui.generated.resources.model_store_multilingual

/**
 * Model Store screen displaying downloadable AI models across
 * Translation, STT (Speech), and TTS (Voice) categories.
 */
@Composable
fun ModelStoreScreen(
    modifier: Modifier = Modifier,
    onModelClick: (ModelId) -> Unit = { modelId ->
        // Default: log navigation intent until navController is wired
        println("Navigate to ModelDetail for: ${modelId.id}")
    },
    viewModel: ModelStoreViewModel = viewModel { ModelStoreViewModel() },
) {
    val state by viewModel.state.collectAsState()
    ModelStoreContent(
        state = state,
        onIntent = viewModel::onIntent,
        onModelClick = onModelClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelStoreContent(
    state: ModelStoreState,
    onIntent: (ModelStoreIntent) -> Unit,
    onModelClick: (ModelId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.model_store_title),
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
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Storage usage card at the top
            StorageUsageCard(
                totalUsedBytes = MockStorageData.totalUsedBytes,
                availableBytes = MockStorageData.availableBytes,
                perModelUsage = MockStorageData.perModelUsage,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ModelStoreTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { onIntent(ModelStoreIntent.SelectTab(it)) },
            )

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(state.currentModels, key = { it.id.id }) { model ->
                        ModelCard(
                            model = model,
                            onCardClick = { onModelClick(model.id) },
                            onDownloadClick = { onIntent(ModelStoreIntent.DownloadModel(model.id)) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ModelStoreTabRow(
    selectedTab: ModelStoreTab,
    onTabSelected: (ModelStoreTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = ModelStoreTab.entries
    val tabLabels = listOf(
        stringResource(Res.string.model_store_tab_translation),
        stringResource(Res.string.model_store_tab_stt),
        stringResource(Res.string.model_store_tab_tts),
    )

    PrimaryTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tabLabels[index],
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun ModelCard(
    model: AiModel,
    onCardClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onCardClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Name + language pair
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatLanguagePair(model),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Size, quality stars, RAM badge, engine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Size
                Text(
                    text = stringResource(Res.string.model_store_size_label, formatSize(model.sizeBytes)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Quality stars
                Text(
                    text = formatQualityStars(model.qualityRating),
                    style = MaterialTheme.typography.bodySmall,
                )
                // RAM badge
                Text(
                    text = stringResource(Res.string.model_store_ram_label, model.ramRequirementMb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Engine type + Download button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.model_store_engine_label, model.engineType.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Button(onClick = onDownloadClick) {
                    Text(text = stringResource(Res.string.model_store_download_button))
                }
            }
        }
    }
}

/** Format bytes to a human-readable MB string. */
private fun formatSize(bytes: Long): String {
    val mb = bytes / 1_000_000
    return "$mb MB"
}

/** Format language pair display (e.g., "en → de") or "Multilingual" for STT. */
@Composable
private fun formatLanguagePair(model: AiModel): String {
    val pair = model.languagePair
    return if (pair != null) {
        "${pair.source.code} → ${pair.target.code}"
    } else if (model.category == ModelCategory.STT) {
        stringResource(Res.string.model_store_multilingual)
    } else {
        ""
    }
}

/** Render quality rating as star glyphs (e.g., "★★★★☆ 4.2"). */
private fun formatQualityStars(rating: Float): String {
    val fullStars = rating.toInt()
    val emptyStars = 5 - fullStars
    val stars = "★".repeat(fullStars) + "☆".repeat(emptyStars)
    // Manual single-decimal formatting for multiplatform compatibility
    val whole = rating.toInt()
    val decimal = ((rating - whole) * 10).toInt()
    return "$stars $whole.$decimal"
}
