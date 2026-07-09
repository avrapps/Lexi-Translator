# Implementation Plan: Lexi Translator

## Overview

Progressive delivery plan for the Lexi Translator offline AI translation platform. Built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP), targeting Android, Desktop, and iOS. Each wave produces a testable, deployable increment.

**Store Publishing Targets**: Google Play Store, Apple App Store, Microsoft Windows Store, Snap Store (Linux).

## Tasks

---

## Wave 1: Foundation & Infrastructure

- [x] 1. Project Structure and Build Configuration
  - [x] 1.1 Configure CI/CD GitHub Actions workflows
    - Create `.github/workflows/pr-check.yml`: build Android + Desktop + iOS, run ktlint, run detekt, run unit tests
    - Create `.github/workflows/release.yml`: build AAB/APK, Desktop MSI/EXE + Snap package, iOS IPA on tag push
    - Configure release workflow to FAIL if ktlint check does not pass
    - Configure publishing steps: Google Play (AAB), App Store Connect (IPA), Windows Store (MSIX), Snap Store (snap)
    - Configure matrix strategy for multi-platform builds
    - Set 30-minute timeout for PR workflow
    - _Requirements: 1.4, 1.5_

  - [x] 1.2 Configure ktlint, detekt, and formatting enforcement
    - Add ktlint Gradle plugin with AGPL license header rule
    - Add detekt Gradle plugin with custom rule set (complexity, naming, performance)
    - Configure pre-commit Git hook that runs `ktlint --check` and `detekt` — reject non-compliant code
    - Configure release build to FAIL if ktlint reports any violations (`ktlintCheck` as dependency of `assemble`)
    - Create `.editorconfig` with project-wide formatting rules (120 char line length, 4-space indent)
    - Add import ordering rule: android → com.falconlabs → third-party → kotlin → java
    - _Requirements: 1.10_

  - [x] 1.3 Add AGPL license, CONTRIBUTORS, and project governance
    - Add `LICENSE` file with AGPL-3.0 full text including strict copyleft clause: any derivative work MUST publish source code under AGPL-3.0
    - Add `NOTICE` file with attribution requirements
    - Create `CONTRIBUTORS.md` template with contribution guidelines
    - Add AGPL license header template to ktlint configuration — all `.kt`, `.kts` files MUST include the header
    - Configure ktlint to FAIL builds on missing license headers
    - _Requirements: 1.6_

  - [x] 1.4 Restructure Gradle modules and version catalog
    - Add SQLDelight, Koin, Kotest, ONNX Runtime, ktlint, detekt, Firebase (Crashlytics + Analytics), AdMob, and Play Billing to `libs.versions.toml`
    - Configure `sharedLogic` with SQLDelight plugin and Koin dependencies
    - Configure `sharedUI` with Compose Navigation dependencies
    - Add iOS target configuration to `sharedLogic` and `sharedUI`
    - Configure Desktop packaging for Windows (MSI/EXE via Conveyor or jpackage) and Linux (Snap)
    - _Requirements: 1.1_

  - [x] 1.5 Set up Koin DI module skeleton
    - Create `com.falconlabs.aitranslator.di` package in `sharedLogic/commonMain`
    - Define `domainModule`, `dataModule` with empty factory/single bindings
    - Create `viewModelModule` in `sharedUI/commonMain`
    - Create platform module stubs in `androidApp`, `desktopApp`
    - Wire Koin initialization in each platform's Application/main entry point
    - _Requirements: 1.2_

  - [x] 1.6 Create Navigation Graph skeleton
    - Define `NavGraph.kt` in `sharedUI/commonMain` using Compose Navigation type-safe routes
    - Create sealed route classes for all 6 tabs: LiveInterpreter, TextTranslate, NeuralSpeak, Library, Models, Settings
    - Define nested route classes for sub-screens (ModelDetail, VoiceLibrary, FolderView, etc.)
    - Create `LexiBottomNavBar` composable with tab items
    - Create placeholder `@Composable` screens for each destination (empty Box with screen name text)
    - Generate Compose Previews for each placeholder screen across Phone, Tablet, Desktop in Light + Dark themes
    - _Requirements: 1.3_

  - [x] 1.7 Implement Neural-Minimalist theme system
    - Create `LexiTheme.kt` with Material3 `MaterialTheme` wrapper
    - Define `LexiColorScheme` for Light, Dark, and Dynamic color palettes using Neural-Minimalist design tokens
    - Create `LexiTypography` with scaled text styles (Small/Medium/Large/Huge/Presentation) using Inter + JetBrains Mono
    - Create `LexiShapes` with rounded corner definitions (8px standard, 24px cards, pill for status)
    - Add `AnimationToggle` CompositionLocal for reduced-motion support
    - Wire theme into platform entry points (MainActivity, main.kt)
    - Generate Compose Preview for theme showcase across all form factors and themes
    - _Requirements: 7.4, 9.7_

  - [x] 1.8 Set up SQLDelight + SQLCipher database
    - Create `lexi.sq` schema files in `sharedLogic/commonMain/sqldelight`
    - Define all tables from design: `installed_model`, `model_download`, `library_entry`, `folder`, `tag`, `entry_tag`, `conversation_card`, `session_state`, `user_setting`
    - Create FTS5 virtual table `library_entry_fts`
    - Create all indexes from the design schema
    - Configure platform-specific `SqlDriver` factories (AndroidSqliteDriver, JdbcSqliteDriver)
    - Add SQLCipher dependency and wire AES-256 encryption for the Android driver
    - _Requirements: 5.5_

  - [x] 1.9 Set up Analytics interface and Crashlytics integration
    - Create `LexiAnalytics` interface in `sharedLogic/commonMain` with: `logScreenView()`, `logEvent()`, `logError()`, `setAnalyticsEnabled()`
    - Create `NoOpAnalytics` implementation (default when disabled)
    - Create `FirebaseAnalyticsImpl` in `androidApp` integrating Firebase Crashlytics + Analytics
    - Create `DesktopAnalyticsImpl` in `desktopApp` writing to local `~/.lexi/analytics/` log files
    - Wire analytics toggle to Settings repository (disabled by default)
    - Register `LexiAnalytics` in Koin platform modules
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [x] 1.10 Set up Ads and In-App Purchase infrastructure
    - Add AdMob dependency to `androidApp`
    - Create `AdsManager` interface with `showExitInterstitial()` and `isAdFree(): Boolean`
    - Implement `AdMobAdsManager` in `androidApp` — load interstitial on app start, show ONLY on app close
    - Create `BillingRepository` interface with `hasRemoveAdsPurchase()` and `purchaseRemoveAds()`
    - Implement `PlayBillingRepository` using Google Play Billing Library
    - Configure $20 "Buy Me a Coffee" one-time product in Play Console
    - Wire purchase state to `AdsManager` — skip ads when purchased
    - Ensure ads are NOT shown during offline mode
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 1.11 Write smoke tests for Wave 1
    - Verify Koin DI graph resolves without cycles
    - Verify all navigation destinations are reachable from bottom nav
    - Verify SQLDelight schema compiles and generates correct Kotlin code
    - Verify theme applies in both Light and Dark modes
    - Verify ktlint fails on missing license header
    - Verify ktlint fails on formatting violations in release build
    - Verify analytics calls are no-ops when disabled
    - _Requirements: 1.1, 1.2, 1.3, 1.10, 11.4_

- [x] 2. Wave 1 Checkpoint
  - Ensure all platform targets build without errors (Android, Desktop, iOS)
  - Verify CI/CD workflow runs green on a test PR
  - Verify release workflow produces AAB, Desktop installer, and IPA artifacts
  - Verify ktlint rejects code without AGPL license header
  - Verify ktlint/detekt FAILS release builds on formatting violations
  - Verify pre-commit hook blocks non-compliant commits
  - Verify navigation between all placeholder screens works
  - Verify theme renders correctly in Light/Dark modes with Neural-Minimalist palette
  - Verify database schema compiles and SQLDelight generates Kotlin adapters
  - Verify analytics disabled by default — no network calls
  - Verify interstitial ad loads but only shows on app close
  - Verify Compose Previews render for Phone, Tablet, Desktop in both themes
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 2: CI/CD Automation, Website & Store Publishing

- [ ] 3. CI/CD Automation
  - [ ] 3.1 Implement Crowdin integration
    - Add Crowdin GitHub Action to push new source strings on merge to main
    - Configure auto-pull of approved translations
    - Auto-commit i18n updates with `[i18n]` commit prefix
    - _Requirements: 1.8_

  - [ ] 3.2 Implement AIDLC documentation automation
    - Create GitHub Action that detects modified modules on merge
    - Auto-update corresponding documentation files
    - Append dated changelog entries
    - _Requirements: 1.7_

  - [ ] 3.3 Implement version bump and changelog generation
    - Create release workflow triggered by version tags
    - Parse Conventional Commits to generate changelog grouped by type
    - Generate blog post summary (≤500 words)
    - Generate release notes for GitHub Releases
    - _Requirements: 1.9_

  - [ ] 3.4 Set up Code Review Agent
    - Configure AI code review action on PR open events
    - Target: style violations, null-safety issues, unused imports, performance anti-patterns
    - Post inline review comments within 5 minutes
    - _Requirements: 1.11_

  - [ ] 3.5 Configure store publishing pipelines
    - Configure Fastlane or Gradle Play Publisher for Google Play Store (AAB upload, staged rollout)
    - Configure Fastlane for App Store Connect (IPA upload, TestFlight distribution)
    - Configure Conveyor or jpackage for Windows Store MSIX packaging and submission
    - Configure Snapcraft for Snap Store (snap build, upload, channel management)
    - Add signing key management via GitHub Secrets for all stores
    - Create release checklist GitHub Issue template
    - _Requirements: 1.5_

- [ ] 4. GitHub Pages Website
  - [ ] 4.1 Create GitHub Pages website
    - Create `/docs` or `gh-pages` branch with static site
    - Implement responsive layout (320px, 768px, 1280px breakpoints)
    - Add sections: features, screenshots, download links (Play Store, App Store, Windows Store, Snap Store), contribution guide
    - Add architecture overview and build instructions for contributors
    - Ensure WCAG 2.1 Level AA compliance (semantic HTML, contrast ratios)
    - _Requirements: 13.1, 13.2, 13.4, 13.5_

  - [ ] 4.2 Wire website auto-update on release
    - Add CI step to update version number, release date, and changelog on website
    - Trigger within 15 minutes of release publication
    - Update store badges with latest version
    - _Requirements: 13.3_

- [ ] 5. UI Integration Tests
  - [ ] 5.1 Write platform UI integration tests
    - Create Compose UI tests for critical flows: navigation, translation, model download
    - Test on Android (Compose test rule) and Desktop (JVM Compose test)
    - Verify all screens render without crashes at default and accessibility scales
    - _Requirements: 1.4_

  - [ ]* 5.2 Write performance benchmark tests
    - Translation latency ≤300ms for ≤500 chars with loaded model
    - STT partial transcription ≤500ms
    - TTS audio start ≤500ms
    - Model cold-load ≤3s for ≤500MB model
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 6. Wave 2 Checkpoint
  - Verify Crowdin sync pushes/pulls strings on merge
  - Verify changelog generates correctly from Conventional Commits
  - Verify Code Review Agent posts inline comments on PRs
  - Verify release pipeline produces signed artifacts for all 4 stores
  - Verify GitHub Pages website renders responsively at all breakpoints
  - Verify website auto-updates with new release info and store badges
  - Verify UI integration tests pass on Android and Desktop
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 3: Model Management & Download System

- [ ] 7. Model Data Layer
  - [ ] 7.1 Create model domain models and value classes
    - Implement `ModelId`, `LanguagePair`, `AiModel`, `InstalledModel`, `ModelCategory`, `EngineType`, `CpuRequirement`
    - Implement `DownloadProgress`, `DownloadState`, `StorageUsage`, `DeviceProfile`, `ModelRecommendation`
    - Implement `DeleteResult`, `IntegrityResult`, `LoadResult`
    - Place in `sharedLogic/commonMain/com.falconlabs.aitranslator.domain.model`
    - _Requirements: 6.1, 6.2_

  - [ ] 7.2 Implement ModelManager interface and repository
    - Define `ModelManager` interface in `sharedLogic/commonMain`
    - Define `ModelRepository` interface for CRUD operations on installed models
    - Implement `SqlDelightModelRepository` using generated SQLDelight queries
    - Register in Koin `dataModule`
    - _Requirements: 6.1, 6.6_

  - [ ] 7.3 Implement download manager with pause/resume/verify
    - Create `DownloadManager` class with `downloadModel()`, `pauseDownload()`, `resumeDownload()`, `cancelDownload()`
    - Implement partial file retention using `model_download` table tracking bytes downloaded
    - Implement resume from last byte position via HTTP Range headers (platform expect/actual)
    - Implement SHA-256 checksum verification after download completes
    - Implement download state machine: QUEUED→DOWNLOADING→PAUSED→VERIFYING→COMPLETED/FAILED
    - Emit `DownloadProgress` Flow with speed and ETA calculation
    - _Requirements: 6.3, 6.4, 6.5, 6.9_

  - [ ]* 7.4 Write property tests for download state machine
    - **Property 21: Download State Machine Valid Transitions**
    - **Property 22: SHA-256 Integrity Verification**
    - **Property 25: Download Resume From Last Position**
    - **Validates: Requirements 6.3, 6.4, 6.5, 6.9**

  - [ ] 7.5 Implement model recommendations engine
    - Create `ModelRecommendationEngine` that filters models by device RAM, storage, and CPU
    - Rank models by compatibility score (RAM fit, storage fit, language pair overlap)
    - Limit to 10 recommendations
    - _Requirements: 6.7_

  - [ ]* 7.6 Write property test for model recommendations
    - **Property 24: Recommendations Fit Device Profile**
    - **Validates: Requirements 6.7**

- [ ] 8. Model Store UI
  - [ ] 8.1 Implement Model Store screen
    - Create `ModelStoreViewModel` with MVI pattern (State, Intent, Effect)
    - Create `ModelStoreScreen` composable with category tabs (Translation, Voice, STT)
    - Display model cards with metadata: name, size, quality stars, RAM badge, language badges
    - Add search/filter functionality
    - Show download button with progress indicator for active downloads
    - _Requirements: 6.1, 6.2_

  - [ ] 8.2 Implement Model Detail screen
    - Create `ModelDetailViewModel` with MVI pattern
    - Display full model metadata, compatibility info, and changelog
    - Show download/delete/update actions
    - Display dependent language pairs on delete confirmation
    - Show storage usage per model
    - _Requirements: 6.2, 6.6, 6.10_

  - [ ] 8.3 Implement storage tracking UI
    - Create `StorageUsageCard` composable showing total used / available / per-model breakdown
    - Wire `ModelManager.getStorageUsage()` Flow to ViewModel state
    - Show insufficient storage warning when download exceeds available space
    - _Requirements: 6.6, 10.4_

  - [ ]* 8.4 Write property tests for storage and metadata
    - **Property 20: Model Metadata Completeness**
    - **Property 23: Storage Usage Aggregation**
    - **Property 26: Model Deletion Dependency Listing**
    - **Property 34: Insufficient Storage Suggests Removals**
    - **Validates: Requirements 6.2, 6.6, 6.10, 10.4**

- [ ] 9. Wave 3 Checkpoint
  - Verify Model Store screen displays mock model catalog with categories
  - Verify download flow: initiate → progress → pause → resume → verify → complete
  - Verify SHA-256 integrity check rejects corrupted files
  - Verify storage usage updates after download/delete
  - Verify model recommendations respect device constraints
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 4: Text Translation Screen

- [ ] 10. Translation Engine Layer
  - [ ] 10.1 Define TranslationEngine interface and domain models
    - Create `TranslationEngine` interface in `sharedLogic/commonMain`
    - Create `TranslationRequest`, `TranslationResult`, `TranslationMode`, `TranslationConfidence`
    - Create `LanguageDetectionResult` model
    - Create `DictionaryEntry` model (meaning, usage examples, grammar notes, gender, formality)
    - _Requirements: 3.2, 3.6, 3.9, 3.11_

  - [ ] 10.2 Implement ONNX Runtime translation engine (Android + Desktop)
    - Create `OnnxTranslationEngine` implementing `TranslationEngine`
    - Implement model loading/unloading with ONNX Runtime session management
    - Implement tokenization using MarianTokenizer (SentencePiece)
    - Implement beam search decoding (num_beams=4)
    - Configure execution providers: NNAPI (Android), CPU (Desktop)
    - Implement `detectLanguage()` using language identification model
    - _Requirements: 3.2, 3.3, 8.1, 8.5_

  - [ ] 10.3 Implement input validation and preprocessing
    - Create `InputValidator` with max character limits (10,000 for translation)
    - Create `LanguageDetector` wrapper with confidence threshold
    - Implement word count check for dictionary threshold (≤5 words)
    - Implement script difference detection for transliteration trigger
    - _Requirements: 3.1, 3.3, 3.4, 3.9, 3.10_

  - [ ]* 10.4 Write property tests for translation validation
    - **Property 7: Input Length Validation** (10,000 char limit for translation)
    - **Property 8: Language Swap Involution**
    - **Property 10: Word Count Dictionary Threshold**
    - **Property 11: Script Difference Triggers Transliteration**
    - **Property 12: Low Confidence Triggers Language Prompt**
    - **Validates: Requirements 3.1, 3.4, 3.5, 3.9, 3.10**

- [ ] 11. Text Translation UI
  - [ ] 11.1 Implement Text Translation screen composable
    - Create `TextTranslateViewModel` with MVI (State: input, output, languages, mode, loading, error)
    - Create `TextTranslateScreen` composable with:
      - Source language selector + target language selector + swap button
      - Input TextField (multiline, char counter, max 10,000)
      - Output card (translated text, confidence badge)
      - Engine mode selector (Default/Fast/Accurate/Experimental)
    - Wire keyboard/paste input sources
    - _Requirements: 3.1, 3.5, 3.6, 3.11_

  - [ ] 11.2 Implement Quick Actions bar
    - Create `QuickActionsRow` composable with: Copy, Share, Speak (placeholder), Favorite, Save, Edit, Compare
    - Implement Copy action using platform clipboard API
    - Implement Share action using platform share sheet
    - Implement Favorite action (toggles `is_favorite` in Library)
    - Implement Save action (persists to Library_Store)
    - _Requirements: 3.7_

  - [ ] 11.3 Implement dictionary and transliteration display
    - Create `DictionaryCard` composable showing: meaning, usage examples, grammar, gender, formality
    - Show/hide based on word count (≤5 words triggers dictionary)
    - Create `TransliterationRow` composable showing Latin script transliteration
    - Show/hide based on script difference between source and target
    - _Requirements: 3.9, 3.10_

  - [ ] 11.4 Implement confidence indicator and alternatives
    - Create `ConfidenceIndicator` composable (Low/Medium/High with color coding)
    - Create `AlternativesSection` composable displaying up to 5 alternatives
    - Wire alternatives from `TranslationResult.alternatives`
    - _Requirements: 3.8, 3.11_

  - [ ]* 11.5 Write property test for bounded alternatives
    - **Property 9: Bounded Collection Constraints** (max 5 alternatives)
    - **Validates: Requirements 3.8**

  - [ ]* 11.6 Write unit tests for Text Translation screen
    - Test language swap preserves text
    - Test auto-detect triggers from 3+ characters
    - Test error state when model not installed
    - Test Quick Actions state management
    - _Requirements: 3.1, 3.3, 3.4, 3.5, 3.12_

- [ ] 12. Wave 4 Checkpoint
  - Verify text input accepts keyboard and paste up to 10,000 characters
  - Verify translation produces results with confidence indicator
  - Verify language swap works correctly
  - Verify dictionary shows for ≤5 word inputs
  - Verify transliteration shows for cross-script language pairs
  - Verify Quick Actions (copy, favorite, save) work
  - Verify error handling when model not loaded
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 5: Live Interpreter Screen

- [ ] 13. STT Engine Layer
  - [ ] 13.1 Define SttEngine interface and event models
    - Create `SttEngine` interface in `sharedLogic/commonMain`
    - Create `SttEvent` sealed interface: PartialTranscription, FinalTranscription, AudioLevel, Error, SilenceDetected
    - Create `SttConfig` data class (languages, silenceThresholdMs, sampleRate, enableDualLanguage)
    - Create `SttError` sealed class hierarchy
    - _Requirements: 2.3, 2.4, 2.7_

  - [ ] 13.2 Implement Whisper ONNX STT engine
    - Create `WhisperSttEngine` implementing `SttEngine`
    - Implement ONNX Runtime session for Whisper model
    - Implement audio preprocessing (16kHz, mono, float32 normalization)
    - Implement streaming partial transcription emission
    - Configure execution providers per platform (NNAPI/CPU)
    - _Requirements: 2.3, 8.2_

  - [ ] 13.3 Implement silence detection algorithm
    - Create `SilenceDetector` class with configurable threshold (default 1500ms)
    - Implement amplitude tracking with rolling window
    - Emit `SilenceDetected` event when silence exceeds threshold
    - Prevent false silence during model processing pauses
    - _Requirements: 2.4_

  - [ ]* 13.4 Write property tests for silence detection
    - **Property 1: Silence Detection Threshold**
    - **Validates: Requirements 2.4**

- [ ] 14. AI Orb and Live Interpreter UI
  - [ ] 14.1 Implement AI Orb composable
    - Create `AiOrb` composable with animated states: Idle, Listening, Thinking, Speaking, Downloading, Error, LowBattery
    - Implement waveform visualization reflecting audio amplitude in Listening state
    - Implement state transition animations (respect reduced-motion preference)
    - Create static state indicators as fallback when animations disabled
    - Support AI Orb style variants: Classic, Minimal, Vibrant
    - _Requirements: 2.1, 2.2, 7.4, 9.7_

  - [ ] 14.2 Implement Live Interpreter screen
    - Create `LiveInterpreterViewModel` with MVI (LiveInterpreterState, Intents, Effects)
    - Create `LiveInterpreterScreen` composable with:
      - AI Orb centered display
      - Language pair display with swap control
      - Conversation cards list (scrollable, latest at bottom)
      - Mode toggles: Auto-Speak, Dual Language, Push-to-Talk
    - Implement ConversationCard composable (source text, translated text, language badge, timestamp, confidence)
    - _Requirements: 2.1, 2.7, 2.8, 2.10, 2.11_

  - [ ] 14.3 Implement Dual Language Mode
    - Wire STT to listen for both languages simultaneously
    - Implement language detection to determine which language is being spoken
    - Route translation to opposite language of the pair automatically
    - Handle barge-in: stop TTS when new audio input detected
    - _Requirements: 2.7, 2.8, 2.9_

  - [ ]* 14.4 Write property tests for Dual Language Mode
    - **Property 2: Dual Language Mode Target Selection**
    - **Property 3: ConversationCard Metadata Completeness**
    - **Validates: Requirements 2.8, 2.11**

  - [ ] 14.5 Implement Push-to-Talk mode
    - Create press-and-hold button that enables STT capture only while held
    - Disable continuous listening when Push-to-Talk is active
    - Show visual feedback (button state, orb transition)
    - _Requirements: 2.10_

  - [ ] 14.6 Implement Full Screen and Caption Size
    - Create `FullScreenInterpreter` composable with large caption text (≥32sp)
    - Implement caption size adjustment: Small(14sp), Medium(18sp), Large(24sp), Huge(32sp), Presentation(48sp)
    - Apply high-contrast styling in full screen mode
    - _Requirements: 2.13, 2.14_

  - [ ] 14.7 Wire Auto-Speak and TTS placeholder
    - When Auto-Speak enabled, pass translated text to TTS after translation completes
    - For now, create a TTS placeholder that logs the request (full TTS in Wave 6)
    - Implement barge-in: stop TTS within 200ms when audio input detected
    - _Requirements: 2.6, 2.9_

  - [ ]* 14.8 Write property tests for conversation storage
    - **Property 4: Conversation Storage Cap** (max 10,000 entries)
    - **Property 5: Engine Error State Transitions**
    - **Property 6: Missing Prerequisite Blocks Operation**
    - **Validates: Requirements 2.12, 2.15, 2.16**

- [ ] 15. Wave 5 Checkpoint
  - Verify AI Orb animates through all 7 states correctly
  - Verify STT produces partial transcriptions during speech
  - Verify silence detection finalizes transcription after 1.5s pause
  - Verify Dual Language Mode auto-detects speaker language and translates to opposite
  - Verify Push-to-Talk captures only while held
  - Verify ConversationCards display with complete metadata
  - Verify Full Screen mode renders captions at ≥32sp
  - Verify barge-in stops TTS when new audio detected
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 6: Neural Speak (TTS Studio)

- [ ] 16. TTS Engine Layer
  - [ ] 16.1 Define TtsEngine interface and event models
    - Create `TtsEngine` interface in `sharedLogic/commonMain`
    - Create `TtsEvent` sealed interface: AudioChunk, Progress, Completed, Error
    - Create `TtsRequest` data class (text, voiceProfile, speed, pitch, pause, volume)
    - Create `VoiceProfile`, `VoiceProfileId`, `AudioFormat` models
    - Create `TtsError` sealed class with skip-and-continue semantics
    - _Requirements: 4.2, 4.3, 4.6_

  - [ ] 16.2 Implement multi-engine TTS with ONNX Runtime
    - Create `MultiTtsEngine` implementing `TtsEngine` that delegates to active voice engine
    - Implement `KokoroEngine`, `PiperEngine`, `VitsEngine` stubs with ONNX session management
    - Implement audio chunk streaming via Flow
    - Implement speed/pitch/pause/volume parameter application
    - Implement skip-and-continue for unsupported characters
    - _Requirements: 4.2, 4.9, 10.6_

  - [ ]* 16.3 Write property tests for TTS validation
    - **Property 7: Input Length Validation** (500,000 char limit for TTS)
    - **Property 9: Bounded Collection Constraints** (max 50 queue items)
    - **Property 13: Voice Control Range Validation**
    - **Property 35: TTS Skip-and-Continue Resilience**
    - **Validates: Requirements 4.1, 4.3, 4.5, 10.6**

- [ ] 17. Voice Library and Neural Speak UI
  - [ ] 17.1 Implement Voice Library screen
    - Create `VoiceLibraryViewModel` with MVI pattern
    - Create `VoiceLibraryScreen` composable with:
      - Voice cards (name, language, engine type, quality, size)
      - Preview playback button (short sample)
      - Download/Delete/Update/Favorite actions
      - Filter by language, engine type, quality
    - Wire to `ModelManager` for voice model downloads
    - _Requirements: 4.4_

  - [ ] 17.2 Implement Neural Speak main UI
    - Create `NeuralSpeakViewModel` with MVI (State: text, voice, controls, playback, queue)
    - Create `NeuralSpeakScreen` composable with:
      - Text input area (multiline, char counter, max 500,000)
      - Voice selector (currently downloaded voices)
      - Playback controls: Play, Pause, Resume, Seek, progress bar
      - Queue list (up to 50 items)
    - _Requirements: 4.1, 4.5_

  - [ ] 17.3 Implement voice controls UI
    - Create `VoiceControlsPanel` composable with sliders:
      - Speed: 0.5x to 3.0x (0.1 increments)
      - Pitch: 0.5x to 2.0x (0.1 increments)
      - Pause duration: 0 to 5s (0.5s increments)
      - Volume: 0% to 100%
    - Wire slider values to `TtsRequest` parameters
    - Apply changes in real-time during playback
    - _Requirements: 4.3_

  - [ ] 17.4 Implement document parsing
    - Create `DocumentParser` interface with implementations for: PDF, TXT, EPUB, DOCX
    - Use platform expect/actual for file picker integration
    - Extract plain text from documents with chapter/section awareness
    - Handle parse failures with error messages and alternative suggestions
    - _Requirements: 4.1, 4.10_

  - [ ] 17.5 Implement audio export
    - Create `AudioExporter` that converts TTS output to WAV, MP3, FLAC, OGG
    - Implement export progress tracking
    - Use platform file save dialog for output location
    - _Requirements: 4.6_

  - [ ] 17.6 Implement long document features
    - Implement reading position persistence (resume from last position)
    - Implement bookmark system (add/remove/jump-to bookmarks)
    - Implement sleep timer (5-120 minutes in 5-minute increments)
    - Implement background playback (platform service/foreground notification)
    - _Requirements: 4.7, 4.8_

  - [ ]* 17.7 Write unit tests for Neural Speak
    - Test document parsing for each format (PDF, TXT, EPUB, DOCX)
    - Test voice control range clamping
    - Test queue management (add, remove, reorder, max 50)
    - Test reading position persistence across app restart
    - _Requirements: 4.1, 4.3, 4.5, 4.7_

- [ ] 18. Wave 6 Checkpoint
  - Verify Voice Library shows available voices with preview playback
  - Verify Neural Speak accepts text input and produces audio output
  - Verify voice controls adjust speed, pitch, pause, volume in real-time
  - Verify document import works for at least TXT and PDF
  - Verify export produces valid audio files in all 4 formats
  - Verify reading position resumes after navigation away and back
  - Verify sleep timer stops playback after configured duration
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 7: Library & History

- [ ] 19. Library Data Layer
  - [ ] 19.1 Implement Library repository and use cases
    - Implement `SqlDelightLibraryRepository` with full CRUD operations
    - Implement `LibraryFilter` (by content type, language, date range, engine, voice, favorites)
    - Implement `PaginatedResult` for efficient large dataset loading
    - Implement FTS5 full-text search via `library_entry_fts` table
    - Implement folder CRUD with depth validation (max 3 levels)
    - Implement tag CRUD with per-entry limit (max 20 tags)
    - Register in Koin `dataModule`
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [ ]* 19.2 Write property tests for Library data layer
    - **Property 14: Persistence Round-Trip** (LibraryEntry, ConversationCard)
    - **Property 15: Search Result Correctness**
    - **Property 16: Time-Based Filter Correctness**
    - **Property 17: Structural Constraints** (folder depth ≤3, tags ≤20)
    - **Validates: Requirements 5.1, 5.2, 5.3, 5.4**

- [ ] 20. Library UI
  - [ ] 20.1 Implement Library browser screen
    - Create `LibraryViewModel` with MVI (State: entries, filters, search, folders, selection)
    - Create `LibraryScreen` composable with:
      - Search bar with full-text search
      - Filter chips: content type, language, time range (Today/Week/Month/Favorites)
      - Entry list (cards with source/translated text preview, metadata badges)
      - Sort options (date, language, type)
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ] 20.2 Implement folder and tag management
    - Create `FolderBrowserScreen` with nested folder navigation (breadcrumb)
    - Create folder creation dialog with parent selection (enforce 3-level max)
    - Create `TagManagementSheet` for adding/removing tags on entries
    - Display tag count and enforce 20-tag limit per entry
    - _Requirements: 5.4_

  - [ ] 20.3 Implement bulk operations
    - Add multi-select mode with selection counter
    - Implement bulk delete with confirmation dialog
    - Implement bulk move-to-folder with folder picker
    - Implement bulk apply-tags with tag selection
    - Implement bulk export with format selection
    - _Requirements: 5.8_

  - [ ] 20.4 Implement biometric lock
    - Create `BiometricAuthManager` with platform expect/actual
    - Implement biometric prompt on Library access when lock enabled
    - Implement 3-failure fallback to PIN/password
    - Wire toggle in Settings
    - _Requirements: 5.6_

  - [ ]* 20.5 Write property tests for biometric and bulk operations
    - **Property 18: Biometric Failure Threshold** (3 fails → PIN fallback)
    - **Property 19: Bulk Operations Consistency**
    - **Validates: Requirements 5.6, 5.8**

  - [ ] 20.6 Implement export formats
    - Create `LibraryExporter` with format support: JSON, TXT, CSV, PDF, Audio
    - Implement export for single entries and bulk selections
    - Use platform file save dialog for output
    - Show storage limit warning at 500MB with cleanup suggestions
    - _Requirements: 5.7, 5.9_

- [ ] 21. Wave 7 Checkpoint
  - Verify Library displays all content types with correct metadata
  - Verify full-text search returns matching entries within 500ms
  - Verify time-based filters (Today/Week/Month) work correctly
  - Verify folder creation respects 3-level depth limit
  - Verify tag assignment respects 20-tag limit
  - Verify bulk operations affect all selected items correctly
  - Verify biometric lock gates library access with PIN fallback after 3 failures
  - Verify export produces valid files in all formats
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 8: Settings & Configuration

- [ ] 22. Settings Implementation
  - [ ] 22.1 Implement Settings repository and persistence
    - Implement `DataStoreSettingsRepository` using `user_setting` table
    - Create `AppSettings` data class with all sub-settings (AI, Audio, Privacy, Display, Battery, Download)
    - Implement `getSettings()` Flow for reactive setting changes
    - Implement `updateSetting()` with immediate persistence
    - Implement `resetToDefaults()` and `exportAllData()`
    - _Requirements: 7.9, 7.10_

  - [ ] 22.2 Implement Settings UI screen
    - Create `SettingsViewModel` with MVI pattern
    - Create `SettingsScreen` composable with sectioned layout:
      - AI Settings: engine mode, performance backend, thread count slider
      - Audio Settings: mic sensitivity, noise suppression, echo cancel, speaker volume
      - Privacy Settings: offline mode toggle, analytics/crashlytics toggle, clear history, delete cache, export data
      - Display Settings: theme (Light/Dark/System), dynamic color, font size, caption size, Orb style, animation toggle
      - Battery Settings: profile selector (Battery Saver/Balanced/Max Performance)
      - Download Settings: WiFi-only, auto-update, background download toggles
      - Monetization: "Buy Me a Coffee" purchase button, restore purchases
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 11.3, 12.3_

  - [ ] 22.3 Implement Battery profile enforcement
    - Create `BatteryProfileEnforcer` that observes active profile
    - When Battery Saver active: limit threads to 2, disable GPU, set 16kHz audio
    - When Balanced: adjust based on current battery level
    - When Max Performance: enable all cores + hardware acceleration
    - Wire enforcement into engine configurations
    - _Requirements: 7.7, 8.6_

  - [ ] 22.4 Implement Offline Mode enforcement
    - Create `OfflineModeEnforcer` that blocks all outbound network requests when enabled
    - Hook into download manager to prevent downloads when offline mode active
    - Block ads loading when offline mode active
    - Allow local-only operations to continue unaffected
    - _Requirements: 7.8, 10.1_

  - [ ] 22.5 Implement live settings application
    - Wire all settings changes to apply without app restart
    - Theme changes apply immediately to active composables
    - Audio settings apply to active STT/TTS sessions
    - Battery profile changes take effect immediately on engines
    - Analytics toggle takes effect immediately (no-op when disabled)
    - _Requirements: 7.10_

  - [ ]* 22.6 Write property test for Battery Saver enforcement
    - **Property 27: Battery Saver Enforces Constraints** (threads ≤2, no GPU, 16kHz)
    - **Validates: Requirements 7.7, 8.6**

- [ ] 23. Wave 8 Checkpoint
  - Verify all settings sections render with correct controls
  - Verify settings persist across app restart
  - Verify Battery Saver limits threads to 2, disables GPU, sets 16kHz audio
  - Verify Offline Mode blocks network requests and ads
  - Verify theme changes apply immediately without restart
  - Verify thread count slider respects device core count
  - Verify analytics toggle disables all telemetry when off
  - Verify "Buy Me a Coffee" purchase flow works
  - Ensure all tests pass, ask the user if questions arise.

---

## Wave 9: Performance, Accessibility & Reliability

- [ ] 24. Memory Management and Model Lifecycle
  - [ ] 24.1 Implement model idle timeout and unloading
    - Create `ModelLifecycleManager` tracking last-used timestamps per loaded model
    - Implement idle timeout (configurable, default 5 minutes) that unloads unused models
    - Release ONNX Runtime session and associated memory on unload
    - Notify user when models are unloaded
    - _Requirements: 8.7_

  - [ ] 24.2 Implement low memory LRU eviction
    - Create `MemoryPressureMonitor` with platform expect/actual for system memory checks
    - When available memory < 15% total RAM, evict models in LRU order
    - Continue eviction until memory > 15% or no models remain
    - Show notification indicating which models were unloaded
    - _Requirements: 8.8_

  - [ ]* 24.3 Write property tests for memory management
    - **Property 28: Idle Timeout Model Unloading**
    - **Property 29: Low Memory LRU Eviction**
    - **Validates: Requirements 8.7, 8.8**

- [ ] 25. Auto-Save and Crash Recovery
  - [ ] 25.1 Implement session auto-save system
    - Create `SessionAutoSaver` that saves active state every 5 seconds
    - Implement `SessionRepository` with `saveSessionState()` and `recoverSession()`
    - Save: active screen, conversation history, pending input, last-saved timestamp
    - Use debounce (1s) to avoid excessive writes during rapid state changes
    - _Requirements: 10.3, 10.8_

  - [ ] 25.2 Implement crash recovery on launch
    - On app launch, check `SessionRepository.recoverSession()`
    - If recovery state exists, restore conversation cards and pending input
    - Show "Session recovered" notification to user
    - Clear recovery state after successful restore
    - _Requirements: 10.3_

  - [ ]* 25.3 Write property test for crash recovery
    - **Property 33: Crash Recovery From Auto-Save** (max 5s data loss)
    - **Validates: Requirements 10.3, 10.8**

- [ ] 26. Low Battery Auto-Switch
  - [ ] 26.1 Implement battery level monitoring and auto-switch
    - Create `BatteryMonitor` with platform expect/actual for battery level observation
    - When battery < 15%, auto-switch to Battery Saver profile
    - Show notification to user about the switch
    - Allow user to override back to higher performance mode
    - _Requirements: 10.7_

- [ ] 27. Accessibility Implementation
  - [ ] 27.1 Implement screen reader support
    - Add semantic roles and contentDescription to all interactive elements
    - Announce AI Orb state transitions via `LiveRegion` announcements
    - Ensure all buttons, toggles, and inputs have accessibility labels
    - Test with TalkBack (Android) and VoiceOver conceptual compatibility
    - _Requirements: 9.1_

  - [ ] 27.2 Implement contrast and text scaling
    - Ensure all color pairs meet WCAG 4.5:1 (normal text) and 3:1 (large text) in high contrast
    - Support system text scaling up to 200% without layout overflow
    - Test all screens at max text scale for truncation/overflow
    - _Requirements: 9.3, 9.6_

  - [ ] 27.3 Implement keyboard navigation (Desktop)
    - Add logical tab order to all interactive elements
    - Add visible focus indicators (2px min width, 3:1 contrast ratio)
    - Ensure all actions are keyboard-accessible (Enter/Space for activation)
    - _Requirements: 9.4_

  - [ ] 27.4 Implement reduced motion support
    - Check system `prefers-reduced-motion` preference
    - When enabled: set all animation durations to 0ms
    - Replace AI Orb animations with static state indicators
    - Disable auto-playing transitions
    - _Requirements: 9.7_

  - [ ]* 27.5 Write property tests for accessibility
    - **Property 30: Font Scaling Proportionality** (no overflow at 200%)
    - **Property 31: Contrast Ratio Compliance** (WCAG ratios)
    - **Property 32: Reduced Motion Disables Animations**
    - **Validates: Requirements 9.2, 9.3, 9.6, 9.7**

- [ ] 28. Wave 9 Checkpoint
  - Verify idle models unload after 5-minute timeout
  - Verify low memory triggers LRU eviction with user notification
  - Verify auto-save captures session state every 5 seconds
  - Verify crash recovery restores last session with ≤5s data loss
  - Verify low battery auto-switches to Battery Saver profile
  - Verify screen reader announces Orb state changes
  - Verify text scales to 200% without layout breakage
  - Verify keyboard navigation works on Desktop with visible focus indicators
  - Verify reduced motion disables all animations
  - Ensure all tests pass, ask the user if questions arise.


## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each wave is a deployable increment — commit and test after each checkpoint
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Checkpoints describe what to verify before committing the wave
- All code is Kotlin (KMP + Compose Multiplatform) — no language ambiguity
- Engine implementations use ONNX Runtime for cross-platform AI inference
- Platform-specific code uses expect/actual pattern for audio, file system, biometric APIs
- ktlint FAILS release builds on formatting violations (non-negotiable)
- AGPL license header MUST be present in every source file (enforced by ktlint)
- ALL Compose screens MUST have multi-form-factor previews (Phone, Tablet, Desktop × Light/Dark)
- ALL user-facing strings MUST use Compose Resources (never hardcoded)
- ALL feature access metrics MUST be logged via LexiAnalytics interface
- Ads shown ONLY on app close — never during active usage
- Publishing targets: Google Play Store, Apple App Store, Microsoft Windows Store, Snap Store

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["1.4", "1.5", "1.8"] },
    { "id": 2, "tasks": ["1.6", "1.7"] },
    { "id": 3, "tasks": ["1.9", "1.10"] },
    { "id": 4, "tasks": ["1.11"] },
    { "id": 5, "tasks": ["3.1", "3.2", "3.3", "3.4", "3.5"] },
    { "id": 6, "tasks": ["4.1", "4.2"] },
    { "id": 7, "tasks": ["5.1", "5.2"] },
    { "id": 8, "tasks": ["7.1"] },
    { "id": 9, "tasks": ["7.2", "7.5"] },
    { "id": 10, "tasks": ["7.3", "7.6"] },
    { "id": 11, "tasks": ["7.4", "8.1", "8.3"] },
    { "id": 12, "tasks": ["8.2", "8.4"] },
    { "id": 13, "tasks": ["10.1"] },
    { "id": 14, "tasks": ["10.2", "10.3"] },
    { "id": 15, "tasks": ["10.4", "11.1"] },
    { "id": 16, "tasks": ["11.2", "11.3", "11.4"] },
    { "id": 17, "tasks": ["11.5", "11.6"] },
    { "id": 18, "tasks": ["13.1"] },
    { "id": 19, "tasks": ["13.2", "13.3"] },
    { "id": 20, "tasks": ["13.4", "14.1"] },
    { "id": 21, "tasks": ["14.2", "14.3", "14.5"] },
    { "id": 22, "tasks": ["14.4", "14.6", "14.7"] },
    { "id": 23, "tasks": ["14.8"] },
    { "id": 24, "tasks": ["16.1"] },
    { "id": 25, "tasks": ["16.2"] },
    { "id": 26, "tasks": ["16.3", "17.1"] },
    { "id": 27, "tasks": ["17.2", "17.3", "17.4"] },
    { "id": 28, "tasks": ["17.5", "17.6"] },
    { "id": 29, "tasks": ["17.7"] },
    { "id": 30, "tasks": ["19.1"] },
    { "id": 31, "tasks": ["19.2", "20.1"] },
    { "id": 32, "tasks": ["20.2", "20.3", "20.4"] },
    { "id": 33, "tasks": ["20.5", "20.6"] },
    { "id": 34, "tasks": ["22.1"] },
    { "id": 35, "tasks": ["22.2", "22.3", "22.4"] },
    { "id": 36, "tasks": ["22.5", "22.6"] },
    { "id": 37, "tasks": ["24.1", "24.2", "25.1", "26.1"] },
    { "id": 38, "tasks": ["24.3", "25.2"] },
    { "id": 39, "tasks": ["25.3", "27.1", "27.2", "27.3", "27.4"] },
    { "id": 40, "tasks": ["27.5"] }
  ]
}
```
