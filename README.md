Aluna - Android Music Player
Project Overview
This is an Android native application built with:

Kotlin
Jetpack Compose (UI framework)
Android Room Database
Media3/ExoPlayer for audio playback
Vosk for voice recognition
Recent Changes (January 2026)
Enhanced Music Screen with Animations
Added smooth entry animations for all UI elements (fade, slide, scale)
Animated album art rotation during playback
Pulsating play button with glow effect when playing
Animated equalizer bars for currently playing tracks
Spring-based button press animations
Swipe gestures on tracks (left for favorites, right to play)
Animated status badges with smooth transitions
Custom animated progress bar with pulsing indicator
New Features
Speed Control Dialog - Change playback speed (0.5x - 2.0x)
Sleep Timer Dialog - Set timer to stop playback (5-90 minutes)
Quick Control Buttons - Speed, sleep timer, repeat mode in player
Animated Favorites - Heart button with bounce animation
Track Swipe Actions - Swipe left/right for quick actions
Animated Scanning State - Rotating music note during scan
Localization
Added "playback_speed" string in all languages (EN, RU, BE, TG)
Project Structure
src/main/
├── java/com/kl/aluna/
│   ├── MainActivity.kt          # Main entry point
│   ├── data/                     # Data layer
│   │   ├── AlunaSettings.kt     # App settings
│   │   └── db/AppDatabase.kt    # Room database
│   ├── player/                   # Music player components
│   │   ├── MusicPlayer.kt
│   │   ├── MusicService.kt
│   │   ├── MusicRepository.kt
│   │   └── MusicViewModel.kt
│   ├── ui/                       # UI components
│   │   ├── navigation/
│   │   ├── screens/
│   │   └── theme/
│   └── voice/                    # Voice assistant
│       └── VoiceAssistant.kt
├── res/                          # Android resources
└── AndroidManifest.xml
Development Requirements
This Android project requires:

Android Studio (Arctic Fox or later)
Android SDK (API Level 29+, target 36)
JDK 17
Gradle with Android plugin
Building
To build this project, use Android Studio or the Android SDK build tools:

# With gradle wrapper (not included)
./gradlew assembleDebug
# Or use Android Studio's build menu
Note
This is a native Android application and cannot run directly in web-based environments like Replit. To develop and test this app:

Open the project in Android Studio
Sync Gradle dependencies
Run on an Android emulator or physical device
