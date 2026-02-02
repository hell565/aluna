
Aluna - Android Music Player
An Android music player application built with Kotlin and Jetpack Compose.

Project Overview
Platform: Android (minSdk 29, targetSdk 36)
Language: Kotlin
UI Framework: Jetpack Compose with Material 3
Architecture: MVVM with ViewModel and Room database
Features
Music playback with Media3 ExoPlayer
Voice assistant integration with Vosk
Favorites and playlists management
Media notifications
Modern Material 3 design
Project Structure
src/main/java/com/kl/aluna/
├── MainActivity.kt          # Main activity
├── data/                    # Data layer (settings, database)
├── player/                  # Music player components
├── ui/
│   ├── components/          # Reusable UI components
│   ├── navigation/          # Navigation setup
│   ├── screens/             # App screens
│   └── theme/               # Theme configuration
└── voice/                   # Voice assistant
Development Note
This is an Android application that requires Android Studio and the Android SDK to build and run. The app cannot be run directly in a web browser environment - it must be installed on an Android device or emulator.

To build locally:
Open the project in Android Studio
Sync Gradle dependencies
Run on an Android device or emulator (API 29+)
Dependencies
AndroidX Core, Lifecycle, Activity Compose
Jetpack Compose with Material 3
Navigation Compose
Room Database
Media3 ExoPlayer
Coil for image loading
Vosk for voice recognition
