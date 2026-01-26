package com.kl.aluna.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AlunaSettings {
    var isDarkTheme by mutableStateOf(true)
    var isVoiceAnimationEnabled by mutableStateOf(true)
    var currentLanguage by mutableStateOf("en")
    var isGaplessPlaybackEnabled by mutableStateOf(true)

    fun getLanguageName(): String = when (currentLanguage) {
        "en" -> "English"
        "ru" -> "Русский"
        "be" -> "Беларуская"
        "tg" -> "Тоҷикӣ"
        else -> "English"
    }
}

object AlunaStrings {

    private val strings = mapOf(
        "en" to mapOf(
            "settings" to "Settings",
            "appearance" to "Appearance",
            "dark_theme" to "Dark Theme",
            "dark_theme_sub" to "Enable dark mode",
            "voice_anim" to "Voice Animation",
            "voice_anim_sub" to "Pulsing effect when listening",
            "voice" to "Voice",
            "language" to "Language",
            "music" to "Music",
            "gapless" to "Gapless Playback",
            "gapless_sub" to "Seamless transitions",
            "about" to "About",
            "version" to "Version",
            "privacy" to "Privacy Policy",
            "footer_sub" to "Your cosmic voice assistant",
            "library" to "Library",
            "playlists" to "Playlists",
            "favorites" to "Favorites",
            "recent" to "Recent",
            "browse" to "Browse",
            "tracks" to "tracks",
            "your_playlists" to "Your playlists",
            "now_playing" to "PLAYING",
            "paused" to "PAUSED",
            "no_track" to "No Track",
            "welcome" to "Welcome",
            "select_mood" to "Select your mood",
            "new" to "New",
            "new_playlist" to "New Playlist",
            "create" to "Create",
            "cancel" to "Cancel",
            "name" to "Name",
            "delete" to "Delete",
            "play_all" to "Play All",
            "no_tracks_playlist" to "No tracks in this playlist",
            "add_some_music" to "Add some music",
            "remove_from_playlist" to "Remove from Playlist",
            "select_tracks" to "Select Tracks",
            "selected" to "selected",
            "add_tracks_to_playlist" to "Add Tracks to Playlist",
            "no_playlists_yet" to "No playlists yet",
            "create_first_collection" to "Create your first collection of songs",
            "back" to "Back"
        ),

        "ru" to mapOf(
            "settings" to "Настройки",
            "appearance" to "Внешний вид",
            "dark_theme" to "Темная тема",
            "dark_theme_sub" to "Включить ночной режим",
            "voice_anim" to "Анимация голоса",
            "voice_anim_sub" to "Пульсация при прослушивании",
            "voice" to "Голос",
            "language" to "Язык",
            "music" to "Музыка",
            "gapless" to "Бесшовное видео",
            "gapless_sub" to "Плавные переходы",
            "about" to "О приложении",
            "version" to "Версия",
            "privacy" to "Политика конфиденциальности",
            "footer_sub" to "Ваш космический голосовой помощник",
            "library" to "Библиотека",
            "playlists" to "Плейлисты",
            "favorites" to "Избранное",
            "recent" to "Недавние",
            "browse" to "Обзор",
            "tracks" to "треков",
            "your_playlists" to "Ваши списки",
            "now_playing" to "ИГРАЕТ",
            "paused" to "ПАУЗА",
            "no_track" to "Нет трека",
            "welcome" to "Добро пожаловать",
            "select_mood" to "Выберите настроение",
            "new" to "Новый",
            "new_playlist" to "Новый плейлист",
            "create" to "Создать",
            "cancel" to "Отмена",
            "name" to "Название",
            "delete" to "Удалить",
            "play_all" to "Играть все",
            "no_tracks_playlist" to "В этом плейлисте нет треков",
            "add_some_music" to "Добавить музыку",
            "remove_from_playlist" to "Удалить из плейлиста",
            "select_tracks" to "Выбрать треки",
            "selected" to "выбрано",
            "add_tracks_to_playlist" to "Добавить треки в плейлист",
            "no_playlists_yet" to "Плейлистов пока нет",
            "create_first_collection" to "Создайте свою первую коллекцию песен",
            "back" to "Назад"
        ),

        "be" to mapOf(
            "settings" to "Налады",
            "appearance" to "Знешні выгляд",
            "dark_theme" to "Цёмная тэма",
            "dark_theme_sub" to "Уключыць начны рэжым",
            "voice_anim" to "Анімацыя голасу",
            "voice_anim_sub" to "Пульсацыя пры праслухоўванні",
            "voice" to "Голас",
            "language" to "Мова",
            "music" to "Музыка",
            "gapless" to "Бясшвоўнае прайграванне",
            "gapless_sub" to "Плыўныя пераходы",
            "about" to "Аб дадатку",
            "version" to "Версія",
            "privacy" to "Палітыка прыватнасці",
            "footer_sub" to "Ваш касмічны галасавы памочнік",
            "back" to "Назад"
        ),

        "tg" to mapOf(
            "settings" to "Танзимот",
            "appearance" to "Намуди зоҳирӣ",
            "dark_theme" to "Мавзӯи торик",
            "dark_theme_sub" to "Фаъол кардани ҳолати шабона",
            "voice_anim" to "Аниматсияи овоз",
            "voice_anim_sub" to "Тапиш ҳангоми гӯш кардан",
            "voice" to "Овоз",
            "language" to "Забон",
            "music" to "Мусиқӣ",
            "gapless" to "Пайвастаи бефосила",
            "gapless_sub" to "Гузаришҳои ҳамвор",
            "about" to "Дар бораи барнома",
            "version" to "Версия",
            "privacy" to "Сиёсати махфият",
            "footer_sub" to "Ёрдамчии овозии кайҳонии шумо",
            "back" to "Бозгашт"
        )
    )

    fun get(key: String): String {
        return strings[AlunaSettings.currentLanguage]?.get(key)
            ?: strings["en"]?.get(key)
            ?: key
    }
}
