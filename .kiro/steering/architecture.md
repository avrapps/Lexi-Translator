---
inclusion: auto
---

# Architecture Guidelines — Lexi Translator

## Module Structure

```
AITranslator/
├── sharedLogic/          # Pure Kotlin business logic (no UI)
│   └── commonMain/
│       └── com.falconlabs.aitranslator/
│           ├── di/                  # Koin module definitions
│           ├── domain/
│           │   ├── model/           # Domain entities, value classes
│           │   └── usecase/         # Use case classes (one per file)
│           ├── data/
│           │   ├── repository/      # Repository implementations
│           │   ├── dao/             # SQLDelight DAO wrappers
│           │   ├── local/           # Local data source adapters
│           │   └── mapper/          # Entity ↔ Domain mappers
│           ├── engine/
│           │   ├── translation/     # TranslationEngine interface + models
│           │   ├── stt/             # SttEngine interface + models
│           │   ├── tts/             # TtsEngine interface + models
│           │   └── model/           # ModelManager interface + models
│           └── util/                # Shared utilities, extensions
│
├── sharedUI/             # Compose Multiplatform UI
│   └── commonMain/
│       └── com.falconlabs.aitranslator.ui/
│           ├── navigation/          # NavGraph, routes, type-safe args
│           ├── theme/               # LexiTheme, colors, typography, shapes
│           ├── widgets/             # REUSABLE composables (cross-feature)
│           │   ├── AiOrb.kt
│           │   ├── LanguageSelector.kt
│           │   ├── ConfidenceIndicator.kt
│           │   ├── ProgressCard.kt
│           │   ├── QuickActionsRow.kt
│           │   └── ...
│           ├── interpreter/         # Live Interpreter feature
│           │   ├── view/            # Screen composables
│           │   └── viewmodel/       # MVI ViewModel + State + Intent
│           ├── translation/         # Text Translation feature
│           │   ├── view/
│           │   └── viewmodel/
│           ├── speak/               # Neural Speak feature
│           │   ├── view/
│           │   └── viewmodel/
│           ├── library/             # Library feature
│           │   ├── view/
│           │   └── viewmodel/
│           ├── models/              # Model Management feature
│           │   ├── view/
│           │   └── viewmodel/
│           └── settings/            # Settings feature
│               ├── view/
│               └── viewmodel/
│
├── androidApp/           # Android-specific code
│   └── com.falconlabs.aitranslator/
│       ├── engine/       # NNAPI, AudioRecord implementations
│       ├── service/      # Foreground services (TTS background)
│       ├── widget/       # Android home screen widgets
│       ├── ads/          # AdMob integration
│       ├── billing/      # In-app purchase (remove ads)
│       └── analytics/    # Crashlytics + metrics
│
├── desktopApp/           # Desktop-specific code
│   └── com.falconlabs.aitranslator/
│       ├── engine/       # CPU/JVM engine implementations
│       └── audio/        # javax.sound.sampled
│
└── iosApp/               # iOS-specific code (future)
```

## Architecture Pattern: MVI (Model-View-Intent)

Every feature follows strict unidirectional data flow:

```
User Action → Intent → Reducer(State, Intent) → New State → UI recomposition
                                    ↓
                              Side Effects → Use Case → Engine/Repository
                                    ↓
                              Result Intent → Reducer → Updated State
```

### Rules

1. ViewModels expose ONLY `StateFlow<FeatureState>` and accept `Intent` via a single `onIntent(intent)` function
2. State classes are `data class` with all fields needed to render the screen
3. Intents are `sealed interface` with one subclass per user action
4. Side effects are `sealed interface` — never trigger side effects in the reducer
5. Reducers are pure functions: `(State, Intent) -> Pair<State, List<Effect>>`

## Dependency Injection — Koin Rules

1. ALL injection MUST use constructor injection
2. NEVER use `by inject()`, `lateinit var`, or field injection
3. NEVER use `get()` outside of Koin module definitions
4. Every class declares its dependencies as constructor parameters
5. Platform-specific implementations are provided via platform modules
6. Use `single {}` for singletons, `factory {}` for transient instances, `viewModel {}` for ViewModels

## Navigation Rules

1. Single NavGraph defined in `sharedUI/navigation/`
2. All route arguments MUST be type-safe (sealed classes with typed parameters)
3. Bottom navigation has exactly 6 tabs: Interpreter, Translate, Speak, Library, Models, Settings
4. Nested navigation uses separate NavHost per tab for back stack isolation
5. Deep links MUST be declared in route definitions
6. Navigation actions MUST go through ViewModel side effects (never call `navController` directly from composables)

## Data Flow

```
Screen (Composable)
  ↓ collects StateFlow
ViewModel (MVI Store)
  ↓ calls
Use Case (sharedLogic)
  ↓ calls
Repository Interface (sharedLogic)
  ↓ implemented by
Repository Impl (sharedLogic/data)
  ↓ uses
SQLDelight DAO / Engine Interface
  ↓ implemented by
Platform-specific (androidApp/desktopApp/iosApp)
```

## Error Handling

1. All errors use sealed class hierarchies extending `LexiError`
2. Every error MUST include `message: String` and `recoverySuggestions: List<RecoverySuggestion>`
3. Errors propagate via `Result<T>` or sealed state classes (never thrown exceptions in business logic)
4. UI maps errors to user-friendly messages with actionable recovery options
