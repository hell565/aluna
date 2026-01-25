package com.kl.aluna.player

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: android.net.Uri,
    val dateAdded: Long,
    val albumArtUri: android.net.Uri? = null
)

object MusicPlayer {
    lateinit var exoPlayer: ExoPlayer
    val playlist = mutableStateListOf<Track>()
    var currentTrackIndex = mutableStateOf(-1)
    var currentTrack = mutableStateOf<Track?>(null)
    var isPlaying = mutableStateOf(false)

    private var positionJob: Job? = null

    fun initialize(player: ExoPlayer) {
        exoPlayer = player

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                MusicPlayer.isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentTrackIndex.value = exoPlayer.currentMediaItemIndex
                currentTrack.value = playlist.getOrNull(currentTrackIndex.value)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    // можно обновить duration
                }
            }
        })

        startPositionUpdater()
    }

    private fun startPositionUpdater() {
        positionJob?.cancel()
        positionJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                // Здесь можно обновлять currentPosition если нужно в UI
                delay(500)
            }
        }
    }

    fun setPlaylistAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        playlist.clear()
        playlist.addAll(tracks)
        if (tracks.isNotEmpty() && startIndex in tracks.indices) {
            val mediaItems = tracks.map { MediaItem.fromUri(it.uri) }
            exoPlayer.setMediaItems(mediaItems)
            exoPlayer.seekTo(startIndex, 0)
            exoPlayer.prepare()
            exoPlayer.play()
            currentTrackIndex.value = startIndex
            currentTrack.value = tracks[startIndex]
        }
    }

    fun playTrack(track: Track) {
        val index = playlist.indexOfFirst { it.id == track.id }
        if (index != -1) {
            exoPlayer.seekTo(index, 0)
            exoPlayer.play()
        } else {
            // Если трек не в плейлисте — добавляем как одиночный
            setPlaylistAndPlay(listOf(track))
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun next() = exoPlayer.seekToNextMediaItem()
    fun previous() = exoPlayer.seekToPreviousMediaItem()
}