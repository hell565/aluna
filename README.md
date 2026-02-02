Aluna - Android Music Player
Overview
Aluna is an Android music player application built with Kotlin and Jetpack Compose. It features a modern Material 3 design, voice assistant integration, and comprehensive music management capabilities.

Important: This is an Android application that requires Android Studio and an Android device/emulator to run. It cannot run as a web application in Replit.

Project Architecture
Technology Stack
Language: Kotlin
UI Framework: Jetpack Compose with Material 3
Architecture Pattern: MVVM (Model-View-ViewModel)
Database: Room (SQLite)
Media Playback: Media3 ExoPlayer
Voice Recognition: Vosk
Image Loading: Coil
Project Structure
src/main/java/com/kl/aluna/
├── MainActivity.kt              # Main entry point
├── data/
│   ├── AlunaSettings.kt        # App settings
│   └── db/AppDatabase.kt       # Room database
├── player/
│   ├── MediaNotificationManager.kt
│   ├── MusicPlayer.kt
│   ├── MusicRepository.kt
│   ├── MusicService.kt
│   ├── MusicViewModel.kt
│   └── Track.kt
├── ui/
│   ├── components/             # Reusable UI components
│   ├── navigation/             # Navigation setup
│   ├── screens/                # App screens
│   └── theme/                  # Material 3 theme
└── voice/
    └── VoiceAssistant.kt       # Voice control
Key Features
Music playback with Media3 ExoPlayer
Voice assistant integration (Vosk offline speech recognition)
Favorites and playlists management
Media notifications with playback controls
Modern Material 3 design with dark/light themes
Category-based music browsing
Build Configuration
Compile SDK: 36
Min SDK: 29 (Android 10)
Target SDK: 36
Java Version: 17
Kotlin Compiler Extension: 1.5.14
How to Build
Clone the repository
Open in Android Studio
Sync Gradle dependencies
Run on Android device or emulator (API 29+)
Recent Changes
Initial import to Replit (Feb 2026)
Added project documentation
