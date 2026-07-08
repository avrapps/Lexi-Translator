---
inclusion: auto
---

# Coding Guidelines — Lexi Translator

## Naming Conventions

### Packages
- Feature packages: `{feature}/view/`, `{feature}/viewmodel/`
- Data layer: `data/repository/`, `data/dao/`, `data/mapper/`
- Reusable UI: `widgets/` (cross-feature composables)
- Engine interfaces: `engine/{type}/`

### Files
| Type | Pattern | Example |
|------|---------|---------|
| Screen composable | `{Feature}Screen.kt` | `LiveInterpreterScreen.kt` |
| Sub-screen composable | `{Feature}{Detail}Screen.kt` | `ModelDetailScreen.kt` |
| Widget (reusable) | `{Name}.kt` | `AiOrb.kt`, `LanguageSelector.kt` |
| ViewModel | `{Feature}ViewModel.kt` | `TextTranslateViewModel.kt` |
| State | `{Feature}State.kt` | `LiveInterpreterState.kt` |
| Intent | `{Feature}Intent.kt` | `TextTranslateIntent.kt` |
| Use Case | `{Action}UseCase.kt` | `TranslateTextUseCase.kt` |
| Repository interface | `{Domain}Repository.kt` | `LibraryRepository.kt` |
| Repository impl | `SqlDelight{Domain}Repository.kt` | `SqlDelightLibraryRepository.kt` |
| DAO | `{Entity}Dao.kt` | `LibraryEntryDao.kt` |
| Engine interface | `{Type}Engine.kt` | `TranslationEngine.kt` |
| Engine impl | `{Tech}{Type}Engine.kt` | `OnnxTranslationEngine.kt` |
| Domain model | `{Name}.kt` | `LanguagePair.kt` |
| Koin module | `{Scope}Module.kt` | `DomainModule.kt` |

### Classes & Functions
- Classes: PascalCase — `TranslationEngine`, `ModelStoreViewModel`
- Functions: camelCase — `translateText()`, `loadModel()`
- Constants: SCREAMING_SNAKE — `MAX_INPUT_LENGTH`, `SILENCE_THRESHOLD_MS`
- Value classes: PascalCase with `@JvmInline` — `ModelId`, `LanguageCode`
- Sealed interfaces: PascalCase — `SttEvent`, `LexiError`

### Compose
- Composable functions: PascalCase (noun) — `AiOrb()`, `ConversationCard()`
- Composable modifiers: lowercase chained — `Modifier.padding().fillMaxWidth()`
- Preview functions: `{Component}Preview` — `AiOrbPreview`

## Widgets (Reusable Composables)

All cross-feature composables go in `sharedUI/.../widgets/`:

1. Widget MUST be self-contained (no external state dependencies)
2. Widget accepts data via parameters and callbacks via lambdas
3. Widget MUST NOT call ViewModels or repositories directly
4. Widget MUST have Compose Preview annotations (see Preview section below)
5. Widget MUST accept `Modifier` as first parameter after required data

```kotlin
// GOOD: Reusable widget
@Composable
fun ConfidenceIndicator(
    confidence: TranslationConfidence,
    modifier: Modifier = Modifier
) { ... }

// BAD: Feature-coupled, not reusable
@Composable
fun ConfidenceIndicator(viewModel: TextTranslateViewModel) { ... }
```

## Compose Preview Guidelines

### MANDATORY: Multi-Form-Factor Previews

Every composable screen and widget MUST have previews for ALL form factors:

```kotlin
@Preview(name = "Phone", device = Devices.PIXEL_7, showBackground = true)
@Preview(name = "Phone Dark", device = Devices.PIXEL_7, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Tablet", device = Devices.PIXEL_TABLET, showBackground = true)
@Preview(name = "Tablet Dark", device = Devices.PIXEL_TABLET, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Desktop", widthDp = 1280, heightDp = 800, showBackground = true)
@Preview(name = "Desktop Dark", widthDp = 1280, heightDp = 800, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LiveInterpreterScreenPreview() {
    LexiTheme {
        LiveInterpreterScreen(state = previewState())
    }
}
```

### Required Preview Variants

For EVERY screen composable, generate previews across:
- **Locale**: At minimum English + one RTL language (Arabic)
- **Theme**: Light AND Dark
- **Screen size**: Phone (360dp), Tablet (768dp), Desktop (1280dp)

### Preview Helper Convention

```kotlin
// Create preview helper functions for sample state
private fun previewState() = LiveInterpreterState(
    orbState = OrbState.LISTENING,
    conversations = sampleConversations()
)
```

## Strings — ALWAYS Externalized

1. NEVER hardcode user-facing strings in composables or ViewModels
2. ALL strings go through Compose Resources (`Res.string.xxx`)
3. String keys follow: `{feature}_{context}_{description}`
   - `interpreter_orb_state_listening`
   - `translation_action_copy`
   - `settings_battery_saver_title`
4. Plurals use `Res.plurals.xxx`
5. Parameterized strings use `Res.string.xxx` with format arguments
6. Strings are synced to Crowdin for i18n

```kotlin
// GOOD
Text(text = stringResource(Res.string.interpreter_start_session))

// BAD — NEVER DO THIS
Text(text = "Start Session")
```

## License Header — MANDATORY

EVERY `.kt`, `.kts`, `.xml` source file MUST begin with the AGPL license header:

```kotlin
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
```

ktlint is configured to FAIL builds if this header is missing.

### AGPL Copyleft Enforcement Summary
- Using ANY part of this code (even a single file, class, or function) triggers the obligation to release YOUR ENTIRE source code under AGPL-3.0
- This applies to: copying, modifying, linking, merging, distributing, or serving over a network
- "Network use" clause (AGPL Section 13): if users interact with your software over a network, you MUST provide access to the corresponding source
- There is NO exception for "internal use" if the software is accessed by others over a network
- Proprietary/closed-source use of any portion is STRICTLY PROHIBITED
- Commercial use is permitted ONLY if full source code is published under AGPL-3.0

## Metrics & Analytics

### Feature Access Metrics (MANDATORY)

ALL key feature access points MUST be logged:

```kotlin
// Every screen entry logs a view event
analytics.logScreenView(screenName = "live_interpreter")

// Every key action logs an event
analytics.logEvent("translation_completed", mapOf(
    "source_lang" to sourceLang.code,
    "target_lang" to targetLang.code,
    "input_length" to text.length,
    "duration_ms" to durationMs,
    "engine_mode" to mode.name
))
```

### Required Metric Events
- Screen views (all 6 tabs + sub-screens)
- Translation completed (language pair, duration, mode, confidence)
- STT session started/ended (language, duration, utterance count)
- TTS playback started (voice, document type, length)
- Model downloaded/deleted (model ID, size, duration)
- Export completed (format, entry count)
- Settings changed (which setting, old/new value)
- Error occurred (error type, context)

### Analytics Interface

```kotlin
interface LexiAnalytics {
    fun logScreenView(screenName: String)
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun logError(error: LexiError, context: String)
    fun setUserProperty(key: String, value: String)
    fun setAnalyticsEnabled(enabled: Boolean)
}
```

- Analytics MUST be disabled by default (opt-in only)
- When disabled, all log calls are no-ops
- Users can disable via Settings > Privacy > Analytics toggle
- Platform implementations: Firebase Crashlytics + Analytics (Android), custom (Desktop)

## Crashlytics Integration

1. Integrate Firebase Crashlytics for Android
2. Log all unhandled exceptions + LexiError instances
3. Include breadcrumbs for last 5 user actions before crash
4. MUST be disableable from Settings > Privacy
5. When disabled: no crash data sent, no network calls
6. Desktop: custom crash log written to `~/.lexi/crash-reports/`

## Ads Integration

### Rules
1. Ads MUST NOT obstruct any user interaction
2. Ads are ONLY shown as interstitial when user CLOSES the app (onPause/onStop)
3. NO banner ads, NO inline ads, NO reward ads during usage
4. Ads MUST NOT appear during active translation or interpretation
5. Ads use AdMob (Android) — disabled in offline mode

### Remove Ads — In-App Purchase
- "Buy Me a Coffee" — $20 one-time purchase
- Removes ALL ads permanently
- Purchase persists across reinstalls (linked to Play Store account)
- Purchase state stored locally + verified via Play Billing

```kotlin
// Billing state check
if (!billingRepository.hasRemoveAdsPurchase()) {
    showExitInterstitial()
}
```

## Code Quality

### ktlint & detekt
- ktlint enforces formatting (indentation, spacing, imports)
- detekt enforces code smells, complexity, naming
- Pre-commit hook REJECTS non-compliant code
- CI/CD FAILS release builds if ktlint reports violations
- Run `./gradlew ktlintCheck detekt` before committing

### Import Ordering
1. `android.*` / `androidx.*`
2. `com.falconlabs.*`
3. `org.*` / `io.*` / third-party
4. `kotlin.*` / `kotlinx.*`
5. `java.*`

### General Rules
- Max line length: 120 characters
- Max function length: 30 lines (extract if longer)
- Max file length: 400 lines (split into sub-files)
- Max parameters: 7 (use data class if more)
- No wildcard imports (`*`)
- No `println` or `System.out` — use structured logging
- No `TODO` without linked issue number
- All public APIs have KDoc comments
