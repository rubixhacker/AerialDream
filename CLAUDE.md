# CLAUDE.md - AerialDream

This document provides essential context for AI assistants working with this codebase.

## Project Overview

**AerialDream** is an Android DreamService (screensaver) application that displays Apple TV-style aerial videos. When activated as the device's screensaver, it streams high-quality aerial footage with a time display and location overlay.

- **Package:** `com.hackedbuce.aerialdream`
- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin 1.9.0

## Repository Structure

```
AerialDream/
├── build.gradle                    # Root build configuration
├── settings.gradle                 # Project settings (includes :mobile)
├── gradle.properties               # Gradle JVM and AndroidX settings
└── mobile/                         # Main app module
    ├── build.gradle                # Module build config with dependencies
    ├── proguard-rules.pro          # R8/ProGuard rules (minimal)
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/hackedbuce/aerialdream/
        │   │   ├── AerialDream.kt          # Main DreamService
        │   │   ├── AerialService.kt        # Retrofit API interface
        │   │   ├── MainApplication.kt      # Application class, DI setup
        │   │   ├── DreamLifecycleOwner.kt  # Lifecycle bridge for DreamService
        │   │   ├── data/
        │   │   │   ├── Asset.kt            # Video asset data class
        │   │   │   ├── Video.kt            # Video collection data class
        │   │   │   └── Result.kt           # Sealed Result class
        │   │   ├── repository/
        │   │   │   └── VideosRepository.kt # API wrapper with error handling
        │   │   └── ui/
        │   │       ├── MainViewModel.kt    # ViewModel for video loading
        │   │       └── ViewModelFactory.kt # Manual DI factory
        │   └── res/
        │       ├── layout/dream_aerial.xml # Main layout with PlayerView
        │       └── values/                 # Colors, strings
        ├── test/                           # Unit tests
        └── androidTest/                    # Instrumentation tests
```

## Architecture

The app follows **MVVM with Repository pattern**:

```
AerialDream (View/DreamService)
    ↓ observes LiveData
MainViewModel (ViewModel)
    ↓ calls
VideosRepository (Repository)
    ↓ calls
AerialService (Retrofit interface)
    ↓ fetches from
Apple Aerial API
```

### Key Components

| Component | Purpose |
|-----------|---------|
| `AerialDream.kt` | DreamService that handles video playback with ExoPlayer, time display, and location overlay |
| `MainViewModel.kt` | Loads videos via repository, randomly selects an asset, exposes LiveData |
| `VideosRepository.kt` | Wraps API calls with error handling, returns `Result<T>` |
| `DreamLifecycleOwner.kt` | Bridges DreamService callbacks to AndroidX Lifecycle for ViewModel compatibility |
| `MainApplication.kt` | Initializes Retrofit client and repository singleton |

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Check for dependency updates
./gradlew dependencies
```

## Testing

### Unit Tests

Located in `mobile/src/test/java/com/hackedbuce/aerialdream/`

- **MainViewModelTest.kt** - Tests ViewModel video loading and error handling
- Uses JUnit 4, Mockito, Mockito-Kotlin
- Uses `StandardTestDispatcher` for coroutine testing
- Uses `InstantTaskExecutorRule` for LiveData testing

Run tests:
```bash
./gradlew testDebugUnitTest
```

### Test Patterns

```kotlin
@Test
fun `descriptive test name with backticks`() = runTest {
    // Given - setup mocks and test data
    whenever(mockRepository.getVideos()).thenReturn(Result.Success(testData))

    // When - perform action
    viewModel.loadVideos()
    advanceUntilIdle()

    // Then - verify results
    assertNotNull(viewModel.video.value)
}
```

## Dependencies

### Core Libraries
- **Kotlin Coroutines:** 1.7.3
- **AndroidX Lifecycle:** 2.7.0 (ViewModel, LiveData)
- **Retrofit:** 2.9.0 with GSON converter
- **ExoPlayer (Media3):** 1.2.1

### Testing Libraries
- **JUnit:** 4.13.2
- **Mockito:** 5.1.1
- **Mockito-Kotlin:** 5.1.0
- **Coroutines Test:** 1.7.3

## Code Conventions

### Naming
- **Classes:** PascalCase (`MainViewModel`, `VideosRepository`)
- **Functions/Properties:** camelCase (`loadVideos`, `videoUrl`)
- **Private LiveData:** Underscore prefix with public accessor (`_video` / `video`)
- **XML Resources:** snake_case (`dream_aerial`, `gradient_background`)
- **Packages:** lowercase, logical grouping (`data`, `repository`, `ui`)

### Kotlin Style
- Use data classes for models
- Use sealed classes for type-safe results (`Result<T>`)
- Use coroutines for async operations (`viewModelScope.launch`)
- Prefer `whenever()` over `when()` in tests (mockito-kotlin)
- Use named parameters for clarity

### Architecture Guidelines
- ViewModels should not reference Android framework classes
- Repository handles all error wrapping
- Use `Result.Success` / `Result.Error` for API responses
- DreamService lifecycle managed via `DreamLifecycleOwner`

## API Integration

**Base URL:** `http://a1.phobos.apple.com/us/r1000/000/Features/atv/`

**Endpoint:** `GET /{season}/videos/entries.json`

Current season: `AutumnResources`

**Response Structure:**
```json
[
  {
    "id": "video-collection-id",
    "assets": [
      {
        "url": "https://video-url.mov",
        "accessibilityLabel": "Location Name",
        "id": "asset-id",
        "timeOfDay": "day"
      }
    ]
  }
]
```

## Common Tasks

### Adding a New Data Field

1. Update data class in `mobile/src/main/java/.../data/`
2. Add `@SerializedName("json_field")` if name differs
3. Update any affected ViewModels
4. Add tests for new functionality

### Adding a New ViewModel

1. Create class in `ui/` package extending `ViewModel`
2. Add factory method to `ViewModelFactory.kt`
3. Use `viewModelScope.launch` for coroutines
4. Expose data via `LiveData` with private `MutableLiveData`
5. Add corresponding unit tests

### Modifying the DreamService

1. `AerialDream.kt` is the main entry point
2. Override `onAttachedToWindow()` for setup
3. Override `onDetachedFromWindow()` for cleanup
4. Use `lifecycleOwner` for lifecycle-aware operations
5. ExoPlayer instance managed in `onAttachedToWindow`/`onDetachedFromWindow`

## Important Notes

- **DreamService Lifecycle:** Unlike Activity, DreamService doesn't natively support AndroidX Lifecycle. `DreamLifecycleOwner` bridges this gap.
- **No ProGuard Rules:** Currently no custom R8/ProGuard rules. Add rules to `mobile/proguard-rules.pro` if obfuscation breaks anything.
- **Single Module:** All code is in the `mobile` module. No multi-module architecture.
- **Manual DI:** Uses `ViewModelFactory` for dependency injection. No Hilt/Dagger.

## Debugging Tips

- DreamService is accessed via: Settings > Display > Screen saver > Aerial
- Use `adb shell am start -a android.intent.action.MAIN -c android.intent.category.DREAM` to trigger
- Check Logcat for `AerialDream` tag for debugging output
- Network issues: Verify internet permission and API endpoint availability
