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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.falconlabs.aitranslator.domain.model.DownloadProgress
import com.falconlabs.aitranslator.domain.model.ModelCategory
import com.falconlabs.aitranslator.domain.model.ModelId
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreIntent
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreState
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreTab
import com.falconlabs.aitranslator.ui.models.viewmodel.ModelStoreViewModel
import com.falconlabs.aitranslator.ui.widgets.StorageUsageCard

import aitranslator.sharedui.generated.resources.Res
import aitranslator.sharedui.generated.resources.model_store_action_delete
import aitranslator.sharedui.generated.resources.model_store_action_pause
import aitranslator.sharedui.generated.resources.model_store_action_redownload
import aitranslator.sharedui.generated.resources.model_store_download_button
import aitranslator.sharedui.generated.resources.model_store_engine_label
import aitranslator.sharedui.generated.resources.model_store_filter_all
import aitranslator.sharedui.generated.resources.model_store_filter_from
import aitranslator.sharedui.generated.resources.model_store_filter_language
import aitranslator.sharedui.generated.resources.model_store_filter_to
import aitranslator.sharedui.generated.resources.model_store_multilingual
import aitranslator.sharedui.generated.resources.model_store_progress_speed
import aitranslator.sharedui.generated.resources.model_store_ram_label
import aitranslator.sharedui.generated.resources.model_store_size_label
import aitranslator.sharedui.generated.resources.model_store_status_installed
import aitranslator.sharedui.generated.resources.model_store_tab_stt
import aitranslator.sharedui.generated.resources.model_store_tab_translation
import aitranslator.sharedui.generated.resources.model_store_tab_tts
import aitranslator.sharedui.generated.resources.model_store_title
import org.jetbrains.compose.resources.stringResource

/**
 * Model Store screen displaying downloadable AI models across
 * Translation, STT (Speech), and TTS (Voice) categories.
 */
@Composable
fun ModelStoreScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onModelClick: (ModelId) -> Unit = {},
    viewModel: ModelStoreViewModel =
        viewModel {
            ModelStoreViewModel(
                org.koin.java.KoinJavaComponent.get(com.falconlabs.aitranslator.engine.model.ModelManager::class.java)
            )
        },
) {
    val state by viewModel.state.collectAsState()
    ModelStoreContent(
        state = state,
        onIntent = viewModel::onIntent,
        onModelClick = onModelClick,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModelStoreContent(
    state: ModelStoreState,
    onIntent: (ModelStoreIntent) -> Unit,
    onModelClick: (ModelId) -> Unit,
    onBack: () -> Unit,
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
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
            // Storage usage card — uses real data from ModelManager
            val storageUsage = state.storageUsage
            if (storageUsage != null) {
                StorageUsageCard(
                    totalUsedBytes = storageUsage.totalUsedBytes,
                    availableBytes = storageUsage.availableBytes,
                    perModelUsage = storageUsage.perModelUsage.entries.map { (id, size) ->
                        com.falconlabs.aitranslator.ui.widgets.ModelStorageEntry(id, id.id, size)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            ModelStoreTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { onIntent(ModelStoreIntent.SelectTab(it)) },
            )

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                )
            } else {
                // Language filters for Translation tab
                if (state.selectedTab == ModelStoreTab.TRANSLATION) {
                    TranslationFilters(
                        models = state.translationModels,
                        selectedSource = state.selectedSourceLang,
                        selectedTarget = state.selectedTargetLang,
                        onSourceSelected = { onIntent(ModelStoreIntent.SelectSourceLang(it)) },
                        onTargetSelected = { onIntent(ModelStoreIntent.SelectTargetLang(it)) },
                    )
                } else {
                    LanguageFilter(
                        models = state.currentModels,
                        selectedLang = state.selectedFilterLang,
                        onLangSelected = { onIntent(ModelStoreIntent.SelectFilterLang(it)) },
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(state.filteredModels, key = { it.id.id }) { model ->
                        val isInstalled = state.isInstalled(model.id)
                        val downloadProgress = state.getDownloadProgress(model.id)
                        ModelCard(
                            model = model,
                            isInstalled = isInstalled,
                            downloadProgress = downloadProgress,
                            onCardClick = { onModelClick(model.id) },
                            onDownloadClick = { onIntent(ModelStoreIntent.DownloadModel(model.id)) },
                            onPauseClick = { onIntent(ModelStoreIntent.PauseDownload(model.id)) },
                            onDeleteClick = { onIntent(ModelStoreIntent.DeleteModel(model.id)) },
                            onRedownloadClick = { onIntent(ModelStoreIntent.RedownloadModel(model.id)) },
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
    isInstalled: Boolean,
    downloadProgress: DownloadProgress?,
    onCardClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onPauseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRedownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { onCardClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Name + language pair + status badge
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
                if (isInstalled) {
                    Text(
                        text = "✓ " + stringResource(Res.string.model_store_status_installed),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = formatLanguagePair(model),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Size, quality stars, RAM badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.model_store_size_label, formatSize(model.sizeBytes)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatQualityStars(model.qualityRating),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(Res.string.model_store_ram_label, model.ramRequirementMb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            // Download progress bar
            if (downloadProgress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val fraction = if (downloadProgress.totalBytes > 0L) {
                    (downloadProgress.bytesDownloaded.toFloat() / downloadProgress.totalBytes.toFloat()).coerceIn(
                        0f,
                        1f
                    )
                } else {
                    0f
                }
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${formatSize(
                            downloadProgress.bytesDownloaded
                        )} / ${formatSize(downloadProgress.totalBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val speedMb = (downloadProgress.speedBytesPerSec / 1_000_000).toInt()
                    Text(
                        text = stringResource(Res.string.model_store_progress_speed, speedMb),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Engine type + action button
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
                when {
                    downloadProgress != null -> {
                        OutlinedButton(onClick = onPauseClick) {
                            Text(text = stringResource(Res.string.model_store_action_pause))
                        }
                    }
                    isInstalled -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onRedownloadClick) {
                                Text(text = stringResource(Res.string.model_store_action_redownload))
                            }
                            OutlinedButton(
                                onClick = onDeleteClick,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(text = stringResource(Res.string.model_store_action_delete))
                            }
                        }
                    }
                    else -> {
                        Button(onClick = onDownloadClick) {
                            Text(text = stringResource(Res.string.model_store_download_button))
                        }
                    }
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

@Composable
private fun TranslationFilters(
    models: List<AiModel>,
    selectedSource: String?,
    selectedTarget: String?,
    onSourceSelected: (String?) -> Unit,
    onTargetSelected: (String?) -> Unit,
) {
    val sourceLanguages = models.mapNotNull { it.languagePair?.source?.code }.distinct().sorted()
    val targetLanguages = if (selectedSource != null) {
        models.filter { it.languagePair?.source?.code == selectedSource }
            .mapNotNull { it.languagePair?.target?.code }.distinct().sorted()
    } else {
        models.mapNotNull { it.languagePair?.target?.code }.distinct().sorted()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ModelFilterDropdown(
            label = stringResource(Res.string.model_store_filter_from),
            options = sourceLanguages,
            selected = selectedSource,
            onSelected = onSourceSelected,
            modifier = Modifier.weight(1f),
        )
        ModelFilterDropdown(
            label = stringResource(Res.string.model_store_filter_to),
            options = targetLanguages,
            selected = selectedTarget,
            onSelected = onTargetSelected,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LanguageFilter(
    models: List<AiModel>,
    selectedLang: String?,
    onLangSelected: (String?) -> Unit,
) {
    val languages = models.mapNotNull { model ->
        model.languagePair?.source?.code
            ?: model.languagePair?.target?.code
            ?: extractLangFromName(model.name)
    }.distinct().sorted()

    if (languages.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            ModelFilterDropdown(
                label = stringResource(Res.string.model_store_filter_language),
                options = languages,
                selected = selectedLang,
                onSelected = onLangSelected,
                modifier = Modifier.fillMaxWidth(0.5f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelFilterDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allLabel = stringResource(Res.string.model_store_filter_all)
    val displayValue = selected?.uppercase() ?: allLabel
    val dropdownOptions = listOf(null to allLabel) + options.map { it to it.uppercase() }

    com.falconlabs.aitranslator.ui.widgets.LexiDropdown(
        selectedLabel = displayValue,
        options = dropdownOptions,
        onSelected = { onSelected(it) },
        modifier = modifier,
        label = label,
    )
}

/** Extract language hint from model name like "Kokoro English Female" -> "en" */
private fun extractLangFromName(name: String): String? {
    val langMap = mapOf(
        "english" to "en",
        "german" to "de",
        "french" to "fr",
        "spanish" to "es",
        "japanese" to "ja",
        "hindi" to "hi",
        "chinese" to "zh",
        "multilingual" to "multi"
    )
    val lower = name.lowercase()
    return langMap.entries.firstOrNull { lower.contains(it.key) }?.value
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
