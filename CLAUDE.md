# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build entire project
./gradlew build

# Run all tests
./gradlew test

# Run shared tests on the JVM (Android host) and on the iOS simulator
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test

# Run a single shared test class
./gradlew :shared:testAndroidHostTest --tests "com.enzoftware.translatorapp.translate.presentation.TranslateViewModelTest"

# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Build shared module only
./gradlew :shared:build

# Clean build
./gradlew clean build
```

**iOS**: Open `iosApp/iosApp.xcworkspace` in Xcode (not `.xcodeproj`). iOS dependencies are managed via CocoaPods.

## Architecture

This is a **Kotlin Multiplatform Mobile (KMM)** project with Clean Architecture + MVVM.

### Module Structure

- **`shared/`** — All business logic, shared across Android and iOS
  - `src/commonMain/` — Platform-independent Kotlin code
  - `src/androidMain/` — Android-specific implementations (`DatabaseDriverFactory`, `HttpClientFactory`, `UiLanguage`)
  - `src/iosMain/` — iOS-specific implementations (same interfaces as androidMain)
  - `src/commonTest/` — Tests using kotlin-test, assertK, and Turbine
- **`androidApp/`** — Android UI (Jetpack Compose + Hilt)
- **`iosApp/`** — iOS UI (SwiftUI)

### Shared Module Layers

```
translate/domain/     → Interfaces: TranslateClient, HistoryDataSource
translate/data/       → Implementations: KtorTranslateClient, SqlDelightHistoryDataSource
translate/presentation/ → TranslateViewModel (shared), TranslateState, TranslateEvent
core/                 → UiLanguage (expect/actual), Language enum, Resource sealed class
```

### Key Patterns

**Expect/Actual**: `UiLanguage`, `DatabaseDriverFactory`, and `HttpClientFactory` have platform-specific `actual` implementations in `androidMain`/`iosMain`.

**State management**: `TranslateViewModel` holds a `MutableStateFlow<TranslateState>` combined with history from `HistoryDataSource`. The combined flow is exposed as `CommonStateFlow` for iOS compatibility.

**Event-driven UI**: All user interactions are `TranslateEvent` sealed class instances dispatched through `viewModel.onEvent()`.

**Resource pattern**: `sealed class Resource<T>` wraps async results as `Success(data)` or `Error(throwable)`.

**Android DI**: Hilt via `di/AppModule.kt` wires `HttpClient → KtorTranslateClient`, `SqlDriver → SqlDelightHistoryDataSource`, and `TranslateUseCase`. `settings/di/SettingsModule.kt` provides a DataStore-backed `SettingsRepository` (user name + Light/Dark theme).

**Android UI ("Parley")**: Mirrors the iOS SwiftUI design. Tokens and building blocks live in `androidApp/.../core/designsystem/` (`CozyTheme` colors/spacing/motion/fonts, `cozyCard`, `cozyFloatingChrome`, `cozyClickable`, `cozyEntrance`, `CozyAmbientBackground`). When changing a screen on one platform, keep the other in sync. Fonts are bundled open fonts (Nunito, Source Serif 4; OFL licences in `assets/licenses`). Navigation uses type-safe routes in `core/presentation/Routes.kt`.

**Android tests**: `./gradlew :androidApp:testDebugUnitTest` runs logic + Robolectric Compose UI tests on the JVM (sandbox pinned to SDK 35 in `src/test/resources/robolectric.properties`); `./gradlew :androidApp:connectedDebugAndroidTest` runs the Hilt E2E suites on a device/emulator with fakes from `androidTest/.../di/TestAppModule.kt`.

### Translation API

The remote translate endpoint is `https://translate.pl-coding.com/translate` (POST, JSON). Implemented in `KtorTranslateClient.kt`.

### Local Database

SQLDelight generates type-safe Kotlin from `translate.sq`. The database is `TranslatorDatabase` with a single `historyEntity` table storing translation history.

### Supported Languages

English, Arabic, Azerbaijani, Chinese, French, German, Italian, Japanese, Korean, Spanish — defined as `Language` enum in `core/domain/language/Language.kt`.

## Key Dependencies (from `gradle/libs.versions.toml`)

- Gradle `9.9.1`, Kotlin: `2.4.21`, Ktor: `3.6.1`, SQLDelight: `2.4.1`, Coroutines `1.11.0`
- Android: AGP `9.5.1` (built-in Kotlin; `shared` uses `com.android.kotlin.multiplatform.library`), compileSdk 37.2, Compose BOM `2026.09.00`, Hilt `2.60.1`, KSP `2.3.14`, Coil `3.6.3`
- Testing: kotlin-test, assertK, Turbine
