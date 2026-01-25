package com.kl.aluna.player  // или com.kl.aluna.viewmodel — как у тебя лежит

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kl.aluna.player.Track
import kotlinx.coroutines.launch
import com.kl.aluna.player.MusicRepository
import android.content.Context

class MusicViewModel : ViewModel() {

    // Список всех найденных треков (реактивный, Compose увидит изменения)
    val allTracks = mutableStateListOf<Track>()

    // Состояние: идёт ли сканирование (можно показать прогресс-бар на экране)
    val isScanning = mutableStateOf(false)

    // Основной метод сканирования — вызывается после получения разрешений
    fun scan(context: Context) {
        if (isScanning.value) return  // не запускаем повторно

        isScanning.value = true

        viewModelScope.launch {
            try {
                val scannedTracks = MusicRepository.scanTracks(context)
                allTracks.clear()
                allTracks.addAll(scannedTracks)
            } catch (e: Exception) {
                // Можно добавить обработку ошибок, например лог или сообщение пользователю
                e.printStackTrace()
                // allTracks остаётся пустым → покажет "No music found"
            } finally {
                isScanning.value = false
            }
        }
    }

    // Опционально: очистка списка (например, при смене настроек или ошибке)
    fun clearTracks() {
        allTracks.clear()
    }

    // Опционально: если позже добавишь favorites/recent в этот VM
    // val favorites = mutableStateListOf<Track>()
    // fun addToFavorites(track: Track) { ... }
}