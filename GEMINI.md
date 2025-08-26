## Project Overview

This is a Kotlin Multiplatform Mobile (KMM) project designed for building a translation application that runs on both Android and iOS. The project follows a standard KMM structure, with a shared module for business logic and platform-specific modules for the UI.

### Key Technologies

- **Kotlin Multiplatform:** The core technology for sharing code between Android and iOS.
- **Gradle:** The build system used for the project. The main build configuration is in `build.gradle.kts`, with dependency versions managed in `gradle/libs.versions.toml`.
- **Ktor:** Used for networking. Ktor client is configured in the `shared` module.
- **SQLDelight:** Used for local database storage. SQL queries are in the `shared/src/commonMain/sqldelight` directory, and the generated Kotlin code is used in the shared module.
- **Kotlinx Coroutines:** For managing asynchronous operations across the application.
- **SwiftUI & Jetpack Compose:** The `iosApp` uses SwiftUI for its UI, and the `androidApp` uses Jetpack Compose.

### Project Structure

- **`shared` module:** Contains the majority of the application's logic, including networking, data persistence, and view models. This code is compiled for both Android and iOS.
  - `src/commonMain`: Code and resources shared across all platforms.
  - `src/androidMain`: Platform-specific implementations for Android.
  - `src/iosMain`: Platform-specific implementations for iOS.
- **`androidApp` module:** The Android application. It depends on the `shared` module and implements the UI using Jetpack Compose.
- **`iosApp` module:** The iOS application. It uses the `shared` module as a dependency (via CocoaPods) and implements the UI using SwiftUI.

### Development Workflow

- **Shared Logic:** All business logic, data handling, and networking should be implemented in the `shared` module whenever possible.
- **UI:** The UI is implemented natively for each platform in the `androidApp` and `iosApp` modules.
- **Dependencies:** Project-wide dependencies are managed in the `gradle/libs.versions.toml` file. Module-specific dependencies are declared in their respective `build.gradle.kts` files.
- **Building:** The project is built using Gradle. You can run `./gradlew build` to build the entire project.
- **Running on Android:** Use Android Studio to open the project and run the `androidApp` configuration.
- **Running on iOS:** Open `iosApp/iosApp.xcworkspace` in Xcode and run the project from there.