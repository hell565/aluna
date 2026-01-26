Aluna - Android Music Player
Project Overview
This is a native Android application built with Kotlin and Jetpack Compose. It's a music player app featuring:

Music playback with ExoPlayer (Media3)
Room database for local data storage
Material 3 design with Compose UI
Navigation between screens (Music, Favorites, Playlists, Recent, Settings)
Multi-language support (English, Russian, Belarusian, Tajik)
Technical Stack
Language: Kotlin
Build System: Gradle with Kotlin DSL
UI Framework: Jetpack Compose
Min SDK: 29 (Android 10)
Target SDK: 36
Project Structure
src/main/
├── java/com/kl/aluna/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── AlunaSettings.kt       # App settings and translations
│   │   └── db/AppDatabase.kt      # Room database
│   ├── player/                    # Music player logic
│   │   ├── MusicPlayer.kt
│   │   ├── MusicViewModel.kt
│   │   ├── MusicRepository.kt
│   │   └── Track.kt
│   └── ui/
│       ├── navigation/AlunaNavigation.kt
│       ├── screens/
│       │   ├── AssistantScreen.kt
│       │   ├── FavoritesScreen.kt
│       │   ├── MusicScreen.kt
│       │   ├── PlaylistsScreen.kt
│       │   ├── RecentScreen.kt
│       │   └── SettingsScreen.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
└── res/                           # Android resources
Recent Changes (January 2026)
