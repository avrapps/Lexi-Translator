# Requirements Document

## Introduction

Lexi Translator is a privacy-first, offline AI translation platform built with Kotlin Multiplatform (KMP) and Compose Multiplatform (CMP). The application performs Translation, Speech Recognition (STT), and Speech Synthesis (TTS) entirely on-device across Android, Desktop, and iOS platforms. No internet connection, cloud processing, or user data collection is required for core functionality.

The platform provides six primary navigation areas: Live Interpreter, Text Translate, Neural Speak, Library, Models, and Settings. The flagship feature is a real-time dual-language interpreter with animated AI Orb visualization.

## Glossary

- **Lexi**: The application system performing translation and speech operations
- **AI_Orb**: The animated visual element indicating system state during Live Interpreter sessions
- **Translation_Engine**: The on-device neural network model performing text-to-text translation
- **STT_Engine**: The Speech-to-Text engine converting audio input to text on-device
- **TTS_Engine**: The Text-to-Speech engine synthesizing spoken audio from text on-device
- **Model_Manager**: The subsystem responsible for downloading, storing, verifying, and managing AI models
- **Library_Store**: The encrypted local database storing user history, favorites, and collections
- **Conversation_Card**: A UI element displaying a single utterance with source text, translated text, and metadata
- **Voice_Profile**: A downloadable TTS voice with associated metadata (language, quality, engine type)
- **Language_Pair**: A source language and target language combination for translation
- **Silence_Detection**: The algorithm determining when a speaker has finished an utterance
- **Koin_DI**: The Koin dependency injection framework used for service resolution across all platforms
- **Navigation_Graph**: The Compose Multiplatform navigation structure defining screen routing
- **AIDLC**: AI-Driven Lifecycle documentation approach where code updates docs and docs drive features
- **Crowdin**: The localization platform used for managing translated UI strings

## Requirements

### Requirement 1: Core Architecture and Build System

**User Story:** As a developer, I want a well-structured Kotlin Multiplatform project with CI/CD automation, so that I can build, test, and deploy Lexi across Android, Desktop, and iOS from a single codebase.

#### Acceptance Criteria

1. THE Lexi SHALL build Compose Multiplatform targets for Android, Desktop, and iOS from the shared modules (sharedLogic, sharedUI), producing a compilable artifact for each platform with zero compilation errors
2. THE Lexi SHALL use Koin_DI for all dependency injection using only constructor injection across all platform targets, prohibiting field injection, lateinit-injected properties, and service locator patterns outside of the Koin module definitions
3. THE Lexi SHALL use a single Navigation_Graph defined in sharedUI for all screen routing with type-safe arguments, where each navigation destination declares its required arguments as typed parameters at compile time
4. WHEN a pull request is submitted, THE Lexi CI/CD pipeline SHALL execute GitHub Actions workflows to build all platform targets, run ktlint and detekt checks, and execute UI integration tests, completing within 30 minutes and reporting pass/fail status on the pull request
5. WHEN a release tag is created, THE Lexi CI/CD pipeline SHALL generate an Android AAB/APK, a Desktop distributable package, and an iOS IPA, and publish the Android artifact to Google Play and the iOS artifact to App Store Connect
6. THE Lexi SHALL enforce AGPL license headers in all source files and maintain a CONTRIBUTORS file tracking all contributors
7. WHEN source code changes are merged to the main branch, THE AIDLC documentation system SHALL update documentation files that correspond to the modified modules and append a dated entry to the changelog
8. THE Lexi SHALL integrate Crowdin for all user-facing strings with automated sync triggered on each merge to the main branch, pulling approved translations and pushing new source strings
9. WHEN a version bump is triggered, THE Lexi SHALL generate a changelog from Conventional Commits history, a blog post summary of up to 500 words, and release notes grouped by commit type (feat, fix, chore)
10. THE Lexi SHALL enforce code formatting via ktlint and detekt with pre-commit hooks rejecting non-compliant code before the commit is created
11. WHEN a pull request is opened, THE Code_Review_Agent SHALL perform automated issue detection covering code style violations, potential null-safety issues, unused imports, and performance anti-patterns, and provide inline review comments on the pull request within 5 minutes of submission

---

### Requirement 2: Live Interpreter

**User Story:** As a traveler, I want real-time two-way interpretation between languages using on-device AI, so that I can have natural conversations with people who speak different languages without internet access.

#### Acceptance Criteria

1. THE AI_Orb SHALL display animated visual states representing: Idle, Listening, Thinking, Speaking, Downloading, Error, and Low_Battery
2. THE AI_Orb SHALL render a live waveform visualization reflecting audio input amplitude during the Listening state
3. WHEN audio input is detected, THE STT_Engine SHALL produce partial transcription results within 500 milliseconds of audio onset
4. WHEN the Silence_Detection algorithm identifies a pause of 1.5 seconds or more after speech, THE STT_Engine SHALL finalize the transcription and pass it to the Translation_Engine
5. WHEN finalized text is received, THE Translation_Engine SHALL produce a translated result within 300 milliseconds
6. WHEN Auto_Speak mode is enabled and translation is complete, THE TTS_Engine SHALL begin speaking the translated text within 500 milliseconds
7. WHILE Dual_Language_Mode is active, THE STT_Engine SHALL listen for both configured languages simultaneously and auto-detect which language is being spoken
8. WHILE Dual_Language_Mode is active, THE Lexi SHALL translate detected speech to the opposite language of the Language_Pair without requiring button press
9. WHEN audio input is detected during TTS playback, THE TTS_Engine SHALL stop playback within 200 milliseconds and THE STT_Engine SHALL resume listening
10. WHERE Push_To_Talk mode is enabled, THE STT_Engine SHALL only capture audio while the talk button is held
11. WHEN a translation is completed, THE Lexi SHALL display a Conversation_Card with source text, translated text, detected language, timestamp, and confidence score
12. THE Library_Store SHALL persist all Conversation_Cards locally up to a maximum of 10,000 entries with options to export, delete, and pin conversations
13. THE Lexi SHALL provide Caption_Size adjustment with options: Small (14sp), Medium (18sp), Large (24sp), Huge (32sp), and Presentation (48sp)
14. WHERE Full_Screen_Mode is enabled, THE Lexi SHALL display captions at minimum 32sp font size with high-contrast styling against the background
15. IF the STT_Engine or Translation_Engine fails to produce output during a live session, THEN THE Lexi SHALL transition the AI_Orb to the Error state, display an error message indicating the failure reason, and allow the user to retry
16. IF the required STT or Translation model for a language in the Language_Pair is not installed, THEN THE Lexi SHALL prevent session start and prompt the user to download the missing model

---

### Requirement 3: Text Translation

**User Story:** As a user, I want to translate text from multiple input sources with high accuracy and rich linguistic context, so that I can understand and communicate in foreign languages offline.

#### Acceptance Criteria

1. THE Lexi SHALL accept text input via: keyboard typing, clipboard paste, system share menu, document import, and voice dictation, up to a maximum of 10,000 characters per translation request
2. WHEN text input is provided and is 10,000 characters or fewer, THE Translation_Engine SHALL produce a translated result within 300 milliseconds on the reference device
3. WHEN source language is not specified, THE Lexi SHALL auto-detect the input language using the offline Language_Detection model from a minimum of 3 characters of input
4. IF the Language_Detection model cannot determine the source language with sufficient confidence, THEN THE Lexi SHALL prompt the user to manually select the source language before proceeding with translation
5. THE Lexi SHALL provide a language swap control that reverses the source and target languages while preserving current text
6. THE Lexi SHALL offer Translation_Engine selection with modes: Default, Fast, Accurate, and Experimental
7. WHEN translation is complete, THE Lexi SHALL display Quick_Actions: Copy, Share, Speak, Favorite, Save, Edit, and Compare
8. WHERE alternate translations are available, THE Lexi SHALL display up to 5 alternative translation options for the input text
9. WHEN a single word or phrase of 5 words or fewer is translated, THE Lexi SHALL display dictionary information including: meaning, usage examples, grammar notes, gender, and formality register (formal/informal)
10. WHERE the target language uses a different script, THE Lexi SHALL display transliteration in Latin characters alongside the translation
11. THE Lexi SHALL display a Translation_Confidence indicator categorized as Low, Medium, or High for each translation result
12. IF the Translation_Engine fails to produce a result for the given input, THEN THE Lexi SHALL display an error message indicating the failure reason and suggest corrective actions (rephrase input, select a different Translation_Engine mode, or verify the language pair model is installed)

---

### Requirement 4: Neural Speak (Offline TTS Studio)

**User Story:** As a user, I want to convert text and documents into natural-sounding speech using offline AI voices, so that I can listen to content in any supported language without internet.

#### Acceptance Criteria

1. THE Lexi SHALL accept TTS input from: keyboard typing, clipboard paste, PDF files, TXT files, EPUB files, DOCX files, and OCR capture with a maximum input length of 500,000 characters per session
2. THE TTS_Engine SHALL support multiple voice engines: Kokoro, Piper, and VITS with extensible architecture for future engines
3. THE Lexi SHALL provide voice controls with the following ranges: Speed (0.5x to 3.0x in 0.1 increments), Pitch (0.5x to 2.0x in 0.1 increments), Pause duration between sentences (0 to 5 seconds in 0.5-second increments), and Volume (0% to 100%)
4. THE Lexi SHALL provide a Voice_Library interface to Preview, Download, Delete, Update, and Favorite available Voice_Profiles
5. THE Lexi SHALL provide playback controls: Play, Pause, Resume, Seek position, and Queue management supporting up to 50 queued items
6. WHEN export is requested, THE TTS_Engine SHALL produce audio files in formats: WAV, MP3, FLAC, and OGG
7. WHILE reading a document, THE Lexi SHALL track and persist reading position so that playback resumes from the last position after app restart or navigation away
8. WHILE reading a document, THE Lexi SHALL support Bookmarks, Sleep Timer (configurable from 5 minutes to 120 minutes in 5-minute increments), and Background Playback
9. WHEN a TTS playback is initiated, THE TTS_Engine SHALL begin audio output within 500 milliseconds of request
10. IF a document file cannot be parsed due to corruption, DRM protection, or unsupported encoding, THEN THE Lexi SHALL display an error message indicating the failure reason and suggest alternative input methods
11. IF no Voice_Profile is downloaded for the selected language, THEN THE Lexi SHALL prompt the user to download a compatible Voice_Profile from the Voice_Library before initiating playback

---

### Requirement 5: Library and History

**User Story:** As a user, I want all my translation history, speech outputs, and documents organized and searchable in an encrypted local library, so that I can find and reuse past translations securely.

#### Acceptance Criteria

1. THE Library_Store SHALL persist history entries for all content types: Translation, Speech, Voice, Documents, and Conversation, storing metadata including timestamp, source language, target language, engine used, and content type
2. THE Library_Store SHALL support full-text search by keyword across all stored content, and filtering by language, date range, engine used, and voice used, returning results within 500 milliseconds for up to 50,000 entries
3. THE Library_Store SHALL provide time-based filters: Today, This Week, This Month, and Favorites
4. THE Library_Store SHALL support organizational structures: Folders (nested up to 3 levels), Collections, and Tags (up to 20 tags per entry)
5. THE Library_Store SHALL encrypt all stored data using SQLCipher with AES-256 encryption
6. WHERE biometric lock is enabled, THE Library_Store SHALL require biometric authentication before granting access to library contents; IF biometric authentication fails 3 times consecutively, THEN THE Library_Store SHALL fall back to device PIN/password authentication
7. WHEN export is requested, THE Library_Store SHALL export data in formats: JSON, TXT, CSV, PDF, and Audio
8. THE Library_Store SHALL support bulk operations: multi-select delete, move to folder, apply tags, and export selection
9. IF the Library_Store reaches a storage limit of 500 MB, THEN THE Lexi SHALL notify the user and suggest removing old entries or exporting data to free space

---

### Requirement 6: Model Management

**User Story:** As a user, I want to browse, download, and manage AI models for translation, voice, speech recognition, and OCR, so that I can customize my offline capabilities based on my language needs and device capacity.

#### Acceptance Criteria

1. THE Model_Manager SHALL display a Model Store interface categorized by: Translation, Voice, and Speech Recognition models
2. THE Model_Manager SHALL display model metadata: Name, Version, Size, Quality rating (1 to 5 stars), RAM requirements, CPU requirements, supported Languages, License, and Publisher
3. WHEN a model download is initiated, THE Model_Manager SHALL display download progress (percentage complete, download speed, and estimated time remaining) and support Pause, Resume, and Cancel operations during download
4. WHEN a model download completes, THE Model_Manager SHALL verify the file integrity via SHA-256 checksum before making the model available
5. IF a model's SHA-256 checksum verification fails after download, THEN THE Model_Manager SHALL discard the corrupted file, display an error message indicating the integrity check failed, and offer the user the option to retry the download
6. THE Model_Manager SHALL track and display storage usage: total used space, available free space, and per-model disk consumption
7. THE Model_Manager SHALL provide recommendations for models based on: installed language pairs, device available RAM, and CPU capability, displaying up to 10 recommended models ranked by compatibility score
8. WHEN a model update is available, THE Model_Manager SHALL notify the user and provide one-tap update capability
9. IF a model download fails due to network loss or timeout, THEN THE Model_Manager SHALL retain the partial download data, inform the user of the failure reason, and allow resuming the download from the last completed byte position
10. WHEN the user requests deletion of an installed model, THE Model_Manager SHALL display a confirmation prompt indicating the model name, size to be freed, and any active language pairs that depend on the model, and upon confirmation SHALL delete the model and recalculate available storage within 2 seconds

---

### Requirement 7: Settings and Configuration

**User Story:** As a user, I want granular control over AI performance, audio configuration, privacy, display, and battery behavior, so that I can optimize Lexi for my specific device and usage patterns.

#### Acceptance Criteria

1. THE Lexi SHALL provide AI settings: default Translation_Engine selection, quality mode (Default, Fast, Accurate, Experimental), performance backend (NNAPI/CPU/GPU), and thread count configurable from 1 to the device's available processor core count
2. THE Lexi SHALL provide Audio settings: microphone sensitivity (0% to 100%), noise suppression toggle, echo cancellation toggle, and speaker volume (0% to 100%)
3. THE Lexi SHALL provide Privacy settings: Offline Mode enforcement, analytics disabled by default, clear history, delete cache, and export all data
4. THE Lexi SHALL provide Display settings: theme selection (Light/Dark/System), dynamic color support, font size (Small/Medium/Large/Huge), caption size (Small/Medium/Large/Huge/Presentation), AI_Orb style (Classic/Minimal/Vibrant), and animation toggle
5. THE Lexi SHALL provide Battery profiles: Battery Saver (maximum 2 threads, no GPU, 16kHz audio sample rate), Balanced (thread count and sample rate adjusted based on current battery level), and Maximum Performance (all available cores and hardware acceleration enabled)
6. THE Lexi SHALL provide Download settings: WiFi-only downloads toggle, auto-update models toggle, and background download toggle
7. WHEN Battery Saver profile is active, THE Lexi SHALL limit processing to a maximum of 2 threads, disable GPU acceleration, and set audio sample rate to 16kHz
8. WHEN Offline Mode is enforced, THE Lexi SHALL block all outbound network requests from the application process
9. THE Lexi SHALL persist all user-configured settings to local storage and restore them on application launch
10. WHEN a setting value is changed, THE Lexi SHALL apply the new value without requiring application restart

---

### Requirement 8: Performance

**User Story:** As a user, I want translations and speech operations to feel instant and responsive, so that conversations flow naturally without awkward pauses.

#### Acceptance Criteria

1. WHEN text of up to 500 characters is submitted for translation with the model already loaded, THE Translation_Engine SHALL return results within 300 milliseconds on a device with at least 4 GB RAM and a mid-range processor
2. WHEN audio input is being processed, THE STT_Engine SHALL produce partial transcription results within 200 to 500 milliseconds of each detected speech segment
3. WHEN TTS playback is requested with the voice model already loaded, THE TTS_Engine SHALL begin audio output within 500 milliseconds
4. WHEN a model of up to 500 MB is loaded from cold state, THE Lexi SHALL complete model initialization within 3 seconds
5. THE Lexi SHALL support configurable hardware acceleration: NNAPI (Android), GPU compute, and CPU-only fallback
6. WHILE Battery Saver mode is active, THE Lexi SHALL reduce thread count to a maximum of 2 threads and disable hardware acceleration to extend battery life
7. WHEN a loaded model has been unused for the configured idle timeout period (default: 5 minutes), THE Lexi SHALL release that model's memory
8. IF available system memory drops below 15 percent of total device RAM, THEN THE Lexi SHALL unload least-recently-used models and notify the user that models were unloaded to free memory

---

### Requirement 9: Accessibility

**User Story:** As a user with accessibility needs, I want Lexi to work seamlessly with assistive technologies and provide configurable display options, so that I can use all translation features regardless of visual, motor, or cognitive ability.

#### Acceptance Criteria

1. THE Lexi SHALL provide screen reader compatibility by assigning semantic roles and descriptive labels to all interactive elements, and SHALL announce AI_Orb state transitions to assistive technologies within 1 second of the state change
2. THE Lexi SHALL support adjustable font sizes from system accessibility settings through to Presentation-scale captions, applying the selected scale consistently across all text-displaying screens
3. WHERE high contrast mode is enabled, THE Lexi SHALL render all text at a minimum contrast ratio of 4.5:1 for normal text and 3:1 for large text against their backgrounds, and all interactive element borders at a minimum contrast ratio of 3:1 against adjacent colors
4. WHILE the Desktop platform is in use, THE Lexi SHALL support full keyboard navigation with a logical tab order across all interactive elements, and SHALL display focus indicators with a minimum width of 2px and a contrast ratio of at least 3:1 against adjacent colors
5. WHERE haptic feedback is available on the device, THE Lexi SHALL provide haptic responses for AI_Orb state changes, confirmations, and errors, with a user setting to enable or disable haptic feedback per event category
6. THE Lexi SHALL support system-level text scaling up to 200% without layout overflow or content truncation on any screen
7. WHEN the operating system reduced-motion preference is enabled, THE Lexi SHALL disable AI_Orb animations, transition animations, and any auto-playing motion, replacing them with static state indicators

---

### Requirement 10: Reliability and Error Handling

**User Story:** As a user, I want Lexi to work reliably in all conditions including low battery, limited storage, and unexpected errors, so that I can depend on it during travel and critical communication scenarios.

#### Acceptance Criteria

1. THE Lexi SHALL perform all core operations (translation, STT, TTS, Library access) without any network connectivity, requiring network only for model downloads and model update checks
2. IF an AI model fails to produce output within 10 seconds of receiving input, THEN THE Lexi SHALL display an error message indicating the failure reason and offer corrective actions (re-record audio, retry with different input, or select a different model)
3. IF the application crashes during an active session, THEN THE Lexi SHALL recover the conversation state and unsaved translations from the most recent auto-save point on next launch, with a maximum data loss of 5 seconds of activity prior to the crash
4. IF device storage is insufficient for a requested model download, THEN THE Model_Manager SHALL inform the user of required space in megabytes and suggest installed models that can be removed to free sufficient space
5. WHILE an operation exceeding 2 seconds in duration is in progress (model download, document processing, model loading), THE Lexi SHALL display a progress indicator showing completion percentage and allow the user to cancel the operation
6. IF the TTS_Engine encounters an unsupported character or text segment, THEN THE TTS_Engine SHALL skip the problematic segment, continue playback of remaining text, and display a notification to the user indicating which segment was skipped
7. IF device battery level falls below 15%, THEN THE Lexi SHALL notify the user and automatically switch to Battery Saver profile to preserve remaining battery for critical translation operations
8. THE Lexi SHALL auto-save active session state (conversation history, pending translations, and current input) at intervals no greater than 5 seconds during active use

---

### Requirement 11: Metrics, Crashlytics, and Analytics

**User Story:** As a product owner, I want visibility into feature usage, crash rates, and performance metrics, so that I can improve the app based on real usage data while respecting user privacy choices.

#### Acceptance Criteria

1. THE Lexi SHALL log feature access metrics for all key user actions: screen views, translations completed, STT sessions, TTS playback, model downloads, exports, and settings changes
2. THE Lexi SHALL integrate Firebase Crashlytics for crash reporting on Android, logging unhandled exceptions with breadcrumbs of the last 5 user actions before the crash
3. THE Lexi SHALL provide a Privacy toggle in Settings to enable or disable analytics and crash reporting, with analytics disabled by default requiring explicit opt-in
4. WHEN analytics is disabled, THE Lexi SHALL make zero network calls for telemetry and all logging calls SHALL be no-ops
5. WHEN the user disables analytics after previously enabling it, THE Lexi SHALL cease all data collection immediately and delete any locally buffered telemetry data
6. THE Lexi SHALL log translation performance metrics including: source/target language, input character count, processing duration, engine mode, and confidence level
7. THE Lexi SHALL log error events with error type, context screen, and recovery action taken by the user

---

### Requirement 12: Ads and Monetization

**User Story:** As a user, I want a non-intrusive ad experience with the option to permanently remove ads via a one-time purchase, so that the app remains free while offering a premium ad-free experience.

#### Acceptance Criteria

1. THE Lexi SHALL display an interstitial ad ONLY when the user closes the application (onPause/onStop transition), and SHALL NOT display ads during active usage, translation, interpretation, or any feature interaction
2. THE Lexi SHALL NOT display banner ads, inline ads, or reward ads at any point during application usage
3. THE Lexi SHALL offer a "Buy Me a Coffee" in-app purchase at $20.00 USD (one-time) that permanently removes all ads from the application
4. WHEN the user completes the "Buy Me a Coffee" purchase, THE Lexi SHALL permanently disable all ad displays and persist the purchase state across reinstalls via Play Store account linking
5. WHILE Offline Mode is enforced, THE Lexi SHALL NOT attempt to load or display any advertisements
6. THE Lexi SHALL NOT display ads during active Live Interpreter sessions, active TTS playback, or while the user is typing in a translation input field

---

### Requirement 13: GitHub Pages Website

**User Story:** As a potential user or contributor, I want a modern, informative project website, so that I can learn about Lexi features, download the app, and contribute to the project.

#### Acceptance Criteria

1. THE Lexi project SHALL maintain a GitHub Pages website with responsive design that renders without horizontal scrolling or content overlap at viewport widths of 320px, 768px, and 1280px
2. THE website SHALL display feature descriptions, platform download links for Android, Desktop, and iOS, application screenshots for at least 2 platforms, and contribution guidelines
3. WHEN a new release is published, THE CI/CD pipeline SHALL update the website with the current version number, release date, and changelog within 15 minutes of release publication
4. THE website SHALL include documentation for contributors including architecture overview, build instructions, and coding guidelines
5. THE website SHALL meet WCAG 2.1 Level AA contrast ratios for all text content and provide navigable structure using semantic HTML headings
