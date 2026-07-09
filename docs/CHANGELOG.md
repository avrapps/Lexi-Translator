# Changelog

All notable changes to Lexi Translator modules are documented in this file.
This file is automatically maintained by the AIDLC (AI-Driven Lifecycle) documentation workflow.

---

## [2026-07-09] Module: sharedUI

- Added `StorageUsageCard.kt` widget: reusable composable showing storage progress bar, used/available space, per-model size breakdown, and low-storage warning.

## [2026-07-09] Module: sharedUI

- Migrated `ModelStoreScreen.kt` from `TabRow` to `PrimaryTabRow` (Material 3) for category tab navigation.

## [2026-07-09] Module: sharedUI

- Replaced Material Icons chevron (`Icon` + `KeyboardArrowRight`) with text `"›"` in `SettingsMainScreen.kt` to reduce Material Icons dependency usage.

## [2026-07-09] Module: sharedUI

- Refactored `ModelScreenPreviews.kt`: expanded to individual preview functions per device/theme variant; removed Android-only `Devices` and `UI_MODE_NIGHT_YES` imports in favor of platform-neutral `widthDp`/`heightDp` dimensions for multiplatform compatibility.

## [2026-07-09] Module: sharedUI

- Replaced `String.format` with manual decimal formatting in `ModelStoreScreen.kt` star-rating helper for multiplatform compatibility.

## [2026-07-09] Module: sharedUI

- Refactored `ModelScreenPreviews.kt`: consolidated 12 separate preview functions into 4 using multi-preview annotations, switched to `LexiTheme`, added real `ModelStoreContent` state with `MockModelData`, and added per-tab (STT/TTS) previews.

## [2026-07-09] Module: sharedUI

- Renamed `onNavigateToModels` → `onNavigateToModelStore` in `LexiNavGraph.kt` for route naming consistency; added trailing comma.

## [2026-07-09] Module: sharedUI

- Replaced `ModelStoreScreen` placeholder with real implementation: scrollable category tabs, lazy model card list, and download buttons (Material 3).

## [2026-07-09] Module: sharedLogic

- Added `ModelId.kt` with inline value classes (`ModelId`, `LanguageCode`, `VoiceProfileId`) for type-safe domain identifiers.
