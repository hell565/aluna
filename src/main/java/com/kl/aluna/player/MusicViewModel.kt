package com.kl.aluna.player

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {

    val allTracks = mutableStateListOf<Track>()
    val isScanning = mutableStateOf(false)

    fun scan(context: Context) {
        if (isScanning.value) return

        MusicRepository.setupObserver(context) {
            scan(context)
        }

        isScanning.value = true

        viewModelScope.launch {
            try {
                val syncedTracks = MusicRepository.syncWithMediaStore(context)
                allTracks.clear()
                allTracks.addAll(syncedTracks)
                
                // Всегда обновляем плейлист плеера, чтобы порядок был актуальным (новые сверху)
                MusicPlayer.playlist.clear()
                MusicPlayer.playlist.addAll(syncedTracks)
                
                // Если ничего не играет, подготавливаем первый трек (самый новый)
                if (MusicPlayer.currentTrack.value == null && syncedTracks.isNotEmpty()) {
                    MusicPlayer.preparePlaylist(syncedTracks, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isScanning.value = false
            }
        }
    }

    fun clearTracks() {
        allTracks.clear()
    }
}
