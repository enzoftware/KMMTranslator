# KMM Translator

KMM Translator is a Kotlin Multiplatform Mobile (KMM) project that provides a simple translation application for both Android and iOS platforms. This project serves as a template for building cross-platform applications using modern technologies.

## Features

*   **Cross-Platform:** Shared business logic between Android and iOS.
*   **Native UI:** Each platform has its own native user interface.
*   **Dependency Injection:** Utilizes Koin for managing dependencies.
*   **Networking:** Uses Ktor for making network requests.
*   **Database:** Implements a local database using SQLDelight.
*   **Asynchronous Programming:** Leverages Kotlin Coroutines for managing background threads.

## Technologies Used

*   **Kotlin Multiplatform Mobile (KMM):** A framework for sharing code between mobile platforms.
*   **Gradle:** A build automation tool for multi-language software development.
*   **Ktor:** A framework for building asynchronous servers and clients in connected systems.
*   **SQLDelight:** A library that generates typesafe Kotlin APIs from SQL.
*   **Kotlinx Coroutines:** A library for asynchronous programming and more.
*   **Koin:** A pragmatic and lightweight dependency injection framework for Kotlin.

## Project Structure

The project is divided into three main modules:

*   `shared`: A Kotlin module that contains the common business logic for both Android and iOS applications. This includes networking, database, and other shared functionalities.
*   `androidApp`: An Android application module that implements the user interface and platform-specific features for the Android version of the app.
*   `iosApp`: An iOS application module that implements the user interface and platform-specific features for the iOS version of the app.

## Getting Started

To get started with this project, you will need the following:

*   **Android Studio:** For running the Android application.
*   **Xcode:** For running the iOS application.
*   **Kotlin Multiplatform Mobile plugin:** For KMM development.

### Building and Running the Application

#### Android

1.  Open the project in Android Studio.
2.  Select the `androidApp` run configuration.
3.  Choose an Android emulator or a connected device.
4.  Click the "Run" button.

#### iOS

1.  Open the `iosApp/iosApp.xcworkspace` file in Xcode.
2.  Select a simulator or a connected iOS device.
3.  Click the "Run" button.
