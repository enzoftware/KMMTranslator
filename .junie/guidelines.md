# KMMTranslator Project Guidelines

This document provides guidelines for developing and maintaining the KMMTranslator project, a Kotlin Multiplatform Mobile (KMM) application that targets both Android and iOS platforms.

## Build/Configuration Instructions

### Prerequisites
- JDK 8 or higher
- Android Studio Arctic Fox or higher with Kotlin Multiplatform Mobile plugin
- Xcode 13 or higher (for iOS development)
- Cocoapods (for iOS dependencies)

### Setting Up the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. For iOS development:
   ```bash
   cd iosApp
   pod install
   ```
5. Open the `iosApp.xcworkspace` file in Xcode

### Building the Project
- **Android**: Use Android Studio's build system or run:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
- **iOS**: Build through Xcode or run:
  ```bash
  ./gradlew :shared:embedAndSignAppleFrameworkForXcode
  ```
  Then open the Xcode workspace and build the iOS app.

### Known Issues
- There's a mismatch between the `HistoryDataSource` interface and its implementation in `SqlDelightHistoryDataSource`. The interface requires `fun getHistory(coroutineContext: CoroutineContext): Flow<List<HistoryItem>>` but the implementation has `override suspend fun getHistory(): CommonFlow<List<HistoryItem>>`.

## Testing Information

### Test Structure
The project follows a standard KMM testing structure:
- `commonTest`: Tests that run on all platforms
- `androidUnitTest`: Android-specific tests
- `iosTest`: iOS-specific tests

### Running Tests
- **Common Tests**: 
  ```bash
  ./gradlew :shared:testDebugUnitTest
  ```
- **Android Tests**: 
  ```bash
  ./gradlew :androidApp:testDebugUnitTest
  ```
- **iOS Tests**: These are run through Xcode or:
  ```bash
  ./gradlew :shared:iosSimulatorArm64Test
  ```

### Adding New Tests
1. Place common tests in `shared/src/commonTest/kotlin/`
2. Place Android-specific tests in `shared/src/androidUnitTest/kotlin/` or `androidApp/src/test/java/`
3. Place iOS-specific tests in `shared/src/iosTest/kotlin/`

### Test Example
Here's an example of a common test for the `Resource` class:

```kotlin
// File: shared/src/commonTest/kotlin/com/enzoftware/translatorapp/core/util/ResourceTest.kt
// Note: This example assumes the following imports:
// import kotlin.test.Test
// import kotlin.test.assertEquals
// import kotlin.test.assertNull
// import kotlin.test.assertNotNull

class ResourceTest {

    @Test
    fun testSuccessResource() {
        // Given
        val data = "Test data"

        // When
        val resource = Resource.Success(data)

        // Then
        assertEquals(data, resource.data)
        assertNull(resource.throwable)
    }

    @Test
    fun testErrorResource() {
        // Given
        val exception = Exception("Test exception")

        // When
        val resource = Resource.Error<String>(exception)

        // Then
        assertNull(resource.data)
        assertNotNull(resource.throwable)
        assertEquals("Test exception", resource.throwable?.message)
    }
}
```

## Additional Development Information

### Project Architecture
The project follows a clean architecture approach with the following layers:
- **Domain**: Contains business logic and interfaces
- **Data**: Implements the interfaces defined in the domain layer
- **Presentation**: Contains UI-related code and ViewModels

### Key Components
- **Shared Module**: Contains code shared between Android and iOS
  - Uses SQLDelight for database operations
  - Uses Ktor for networking
  - Uses Kotlin Coroutines and Flow for asynchronous operations
- **Android App**: Uses Jetpack Compose for UI
  - Uses Hilt for dependency injection
  - Uses Navigation Compose for navigation
- **iOS App**: Uses SwiftUI for UI

### Code Style
- Follow Kotlin coding conventions
- Use meaningful names for classes, methods, and variables
- Write unit tests for all new features
- Document public APIs

### Common Patterns
- **Resource Pattern**: Used for handling the result of operations that might succeed or fail
- **CommonFlow**: A wrapper around Kotlin Flow that works on both Android and iOS
- **ViewModel Pattern**: Used for UI state management

### Debugging Tips
- For Android, use Android Studio's debugger
- For iOS, use Xcode's debugger
- For shared code, you can debug through either Android Studio or Xcode
- Use logging sparingly and remove debug logs before committing

### Dependency Management
- Dependencies are managed through Gradle version catalogs in `gradle/libs.versions.toml`
- Update dependencies regularly to get the latest features and security fixes
