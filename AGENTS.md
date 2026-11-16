# AGENTS.md

Guidance for any coding agent working in this repository. `CLAUDE.md` and `GEMINI.md` import this file; edit this one.

## Project

**Parley** is a Kotlin Multiplatform translator. Business logic lives once in `shared/`; each app ships a native UI:

- `shared/` holds domain, data and presentation logic.
  - `commonMain`: domain contracts, Ktor client, Room history (`translate/data/local`), `TranslateViewModel` and `VoiceToTextViewModel`.
  - `androidMain` and `iosMain`: the `actual`s (`UiLanguage`, `DatabaseDriverFactory`, `HttpClientFactory`).
- `androidApp/`: Jetpack Compose + Hilt.
- `iosApp/`: SwiftUI, consuming `shared` through CocoaPods.

Put logic in `shared` whenever it is not platform UI or a platform API.

## Commands

```bash
./gradlew :androidApp:assembleDebug              # Android debug APK
./gradlew :shared:testAndroidHostTest            # shared tests on the JVM
./gradlew :shared:iosSimulatorArm64Test          # shared tests on the iOS simulator
./gradlew :shared:connectedAndroidDeviceTest     # shared tests on an Android device (incl. real Room)
./gradlew :androidApp:testDebugUnitTest          # Android logic + Robolectric Compose screen tests
./gradlew :androidApp:connectedDebugAndroidTest  # Android Hilt E2E suites (needs an emulator/device)
./rebuild_ios_framework.sh                       # after changing shared Kotlin, before building iOS
```

iOS: open `iosApp/iosApp.xcworkspace` (the workspace, not the `.xcodeproj`) and run or test the `iosApp` scheme.

- Swift tests live in `iosAppTests/`. That folder is synced, so new test files are picked up automatically.
- The `iosApp` app target lists its sources explicitly. Every new app Swift file must be added to `project.pbxproj` (through Xcode, or by mirroring an existing entry), or it won't compile into the app.

Versions are in `gradle/libs.versions.toml`.

## Architecture

- **Unidirectional flow:** UIs send sealed `TranslateEvent` / `VoiceToTextEvent` objects to `viewModel.onEvent()`. Each shared ViewModel exposes one `CommonStateFlow` state.
- **Platform wrappers:**
  - iOS wraps the shared ViewModels in `@MainActor ObservableObject`s, subscribing through `DisposableHandle`.
  - Android wraps them in Hilt `ViewModel`s (`AndroidTranslateViewModel`, `AndroidVoiceToTextViewModel`).
- **Async state:** the shared state is `combine(...).stateIn(...)`, so it reaches the UI asynchronously. Never bind a text field straight to it: the Android editor goes through `EditorTextSync`, which ignores late echoes of the user's own keystrokes. Otherwise fast typing gets scrambled.
- **Android DI:**
  - `di/AppModule.kt` wires the HTTP client, the Room history source and `TranslateUseCase`.
  - `voice_to_text/di/VoiceToTextModule.kt` provides the speech parser.
  - `settings/di/SettingsModule.kt` provides the DataStore `SettingsRepository`, holding the user name and Light/Dark theme. Its keys match iOS `SettingsStore`.
- **Persistence (Room 3 KMP):**
  - `TranslatorDatabase` (`parley.db`) lives in `commonMain`. Only the file location is platform code (`TranslatorDatabaseFactory`): Android uses `getDatabasePath`, iOS uses `<Application Support>/databases`.
  - Use the bundled SQLite driver (`buildTranslatorDatabase()`) on every platform, so behaviour and SQLite version match.
  - DAO functions are `suspend` or return `Flow` (Room 3 requires it).
  - Schema changes: bump `version`, add a `Migration`, and commit the regenerated JSON in `shared/schemas/`.
  - `LegacyHistoryImporter` copies history from the pre-Room SQLDelight `translator.db` once, then deletes it. Keep it until no supported install can still have that file.
  - No `iosX64`: Room 3 and `sqlite-bundled` don't ship Intel-simulator binaries.
  - Tables: `historyEntity` (v1) and `savedWordEntity` (v2, "My Words", added by an `AutoMigration`). Both data sources share one database through `TranslatorStorage`.
- **Phrasebook:**
  - The built-in phrases live in `phrasebook/domain/Phrasebook.kt`. Every entry needs a translation for every `Language`; `PhrasebookTest` fails otherwise.
  - Entry `key`s are persisted in saved words, so never rename or delete a key that has shipped. A saved word whose key is gone can't be opened in the translator.
  - `TranslateEvent.ShowPhrasebookEntry` shows a phrase offline and adds no history.
- **Backend:** `KtorTranslateClient` posts JSON to `https://translate.pl-coding.com/translate`. The service is sometimes unavailable; the UI shows an error banner.

## UI parity

Both apps implement one design language, "Warm Paper & Indigo". Any UI change is a **parity** change: update the other platform in the same piece of work, or call out the gap.

- **Tokens:** `iosApp/.../core/presentation/design_system/CozyTheme.swift` is mirrored 1:1 by `androidApp/.../core/designsystem/CozyTheme.kt`. That covers colours, radius, spacing, and springs converted from SwiftUI `response`/`damping`.
- **Building blocks** share names across platforms: `cozyCard`, `cozyFloatingChrome`, `cozyEntrance`, `CozyAmbientBackground`, `CozyCopyChip`, `CozySpeakButton`, `CozyBannerToast`.
  - Android-only: `cozyClickable`, which gives a ripple-free press scale.
  - Android approximations: Liquid Glass becomes a mostly opaque fill, and the iOS mesh gradient becomes radial glows.
- **Fonts:** Android bundles Nunito (rounded) and Source Serif 4 (serif). Their OFL licences ship in `assets/licenses`. Keep Apple fonts out of the Android app; their licence forbids it.
- **Copy:** Android strings live in `res/values/strings.xml` and use the iOS wording.
- **Screenshots:** per-platform images in `docs/screenshots/{ios,android}/` with matching file names, shown side by side in the README. Retake both sets when a screen changes visibly.

## Android conventions

- **Navigation:** type-safe routes live in `core/presentation/Routes.kt`. Wrap every `navigate`/`popBackStack` in `entry.ifResumed { }` (`MainActivity.kt`), so double taps and taps during transitions can't push duplicates or pop the start destination.
- **Theme:** comes from Settings, not the system. The splash screen stays up until `SettingsViewModel.isLoaded`, so a Dark user never sees a Light frame.
- **Microphone permission:** `classifyMicPermissionResult` separates "dismissed" from "blocked", using the permission-rationale flag before and after the request plus a remembered earlier denial. Permission state on the voice screen uses `rememberSaveable`.
- **Text-to-speech:** goes through `TextToSpeechController`, which queues a tap made before the engine is ready and only reports "speaking" once `speak()` succeeds. The manifest declares a `TTS_SERVICE` query, which Android 11+ needs to see the engine.
- **Touch targets:** small `cozyClickable` chips rely on Compose's automatic 48dp touch-target expansion. `TouchTargetTest` guards this.

## Testing

- **Shared:** `commonTest` runs on the JVM, the iOS simulator and Android devices (`androidDeviceTest` joins the `test` source-set tree).
  - Name tests as plain identifiers (`subject_expectation`). Backticked names with spaces break Android dexing below API 30, and commas break Kotlin/Native.
  - `HistoryDatabaseContract` holds the real-Room tests. Its subclasses live in `iosTest` and `androidDeviceTest`. The Android host JVM can't load the Android build of the bundled driver.
  - Never call `dependsOn` by hand in `shared/build.gradle.kts`: it switches off Kotlin's default hierarchy, and the iOS source sets lose their `actual`s.
  - A successful translation emits twice (the result, then the history insert) in no guaranteed order. Wait for the settled state rather than counting `awaitItem()`s.
- **Android JVM tests** (`src/test`): Robolectric is pinned to SDK 35 in `src/test/resources/robolectric.properties`, because the SDK 36 sandbox needs JDK 21. Screen tests use `@Config(qualifiers = "w411dp-h1600dp-xxhdpi")` so lazy and below-the-fold content is composed.
- **Android E2E** (`src/androidTest`): `@UninstallModules(AppModule, VoiceToTextModule, SettingsModule)`, with fakes provided by `di/TestAppModule.kt`. Look nodes up by the `*TestTags` objects or content descriptions, because history stays visible next to a result and the same text appears twice.

## Toolchain

- **AGP 9:**
  - `androidApp` uses AGP's built-in Kotlin, so don't apply `org.jetbrains.kotlin.android`.
  - `shared` uses `com.android.kotlin.multiplatform.library`. Its Android host tests are the `androidHostTest` source set, with Android resources enabled for the flag drawables.
- **SDK:** compileSdk is 37.2, so a build machine needs SDK Platform 37.2 and Build-Tools 37. `targetSdk` deliberately stays at 36.
