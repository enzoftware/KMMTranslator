# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build entire project
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew :shared:test --tests "com.enzoftware.translatorapp.CommonGreetingTest"

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

**Android DI**: Hilt via `di/AppModule.kt` wires `HttpClient → KtorTranslateClient`, `SqlDriver → SqlDelightHistoryDataSource`, and `TranslateUseCase`.

### Translation API

The remote translate endpoint is `https://translate.pl-coding.com/translate` (POST, JSON). Implemented in `KtorTranslateClient.kt`.

### Local Database

SQLDelight generates type-safe Kotlin from `translate.sq`. The database is `TranslatorDatabase` with a single `historyEntity` table storing translation history.

### Supported Languages

English, Arabic, Azerbaijani, Chinese, French, German, Italian, Japanese, Korean, Spanish — defined as `Language` enum in `core/domain/language/Language.kt`.

## Key Dependencies (from `gradle/libs.versions.toml`)

- Kotlin: `2.1.21`, Ktor: `3.1.1`, SQLDelight: `2.0.2`
- Android: AGP `8.12.0`, Compose BOM `2025.06.01`, Hilt `2.51.1`, KSP `2.1.20-1.0.32`
- Testing: kotlin-test, assertK, Turbine
