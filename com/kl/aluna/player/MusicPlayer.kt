package com.kl.aluna.player

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kl.aluna.data.db.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

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
    var currentPosition = mutableStateOf(0L)
    var trackDuration = mutableStateOf(0L)
    
    val favorites = mutableStateListOf<Long>()
    val recentTracks = mutableStateListOf<Track>()

    private var positionJob: Job? = null
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize(context: Context, player: ExoPlayer) {
        db = AppDatabase.getInstance(context)
        exoPlayer = player
        
        // Repeat mode
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.shuffleModeEnabled = false // Default to false for predictable next/prev

        // Load data from DB using collect to ensure UI state is always in sync
        scope.launch {
            db.musicDao().getFavoriteIds().collect { ids ->
                withContext(Dispatchers.Main) {
                    favorites.clear()
                    favorites.addAll(ids)
                }
            }
        }
        
        scope.launch {
            db.musicDao().getRecent().collect { recent ->
                val tracks = recent.map { 
                    Track(it.trackId, it.title, it.artist, it.duration, android.net.Uri.parse(it.uri), it.timestamp)
                }
                withContext(Dispatchers.Main) {
                    recentTracks.clear()
                    recentTracks.addAll(tracks)
                }
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                MusicPlayer.isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdater()
                } else {
                    positionJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    trackDuration.value = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentTrackIndex.value = exoPlayer.currentMediaItemIndex
                currentTrack.value = playlist.getOrNull(currentTrackIndex.value)
                if (exoPlayer.duration > 0) {
                    trackDuration.value = exoPlayer.duration
                }
                // Only add to recent when a new track starts playing
                currentTrack.value?.let { addToRecent(it) }
            }
        })
    }

    private fun startPositionUpdater() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                currentPosition.value = exoPlayer.currentPosition
                delay(1000)
            }
        }
    }

    fun setPlaylistAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        playlist.clear()
        playlist.addAll(tracks)
        val mediaItems = tracks.map { MediaItem.fromUri(it.uri) }
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        exoPlayer.seekTo(startIndex, 0)
        exoPlayer.play()
    }

    fun playTrack(track: Track) {
        val index = playlist.indexOfFirst { it.id == track.id }
        if (index != -1) {
            exoPlayer.seekTo(index, 0)
            exoPlayer.play()
        } else {
            setPlaylistAndPlay(listOf(track))
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.prepare()
        }
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }
    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    fun next() {
        if (playlist.isEmpty()) return
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNext()
        } else {
            // Loop to start
            exoPlayer.seekTo(0, 0)
        }
        exoPlayer.play()
    }

    fun previous() {
        if (playlist.isEmpty()) return
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPrevious()
        } else {
            // Loop to end
            exoPlayer.seekTo(playlist.size - 1, 0)
        }
        exoPlayer.play()
    }

    fun toggleFavorite(track: Track) {
        scope.launch(Dispatchers.IO) {
            val isFavorite = favorites.contains(track.id)
            if (isFavorite) {
                db.musicDao().deleteFavorite(FavoriteEntity(track.id))
            } else {
                db.musicDao().insertFavorite(FavoriteEntity(track.id))
            }
            // The Flow in initialize() will catch this change and update the 'favorites' list
        }
    }

    private fun addToRecent(track: Track) {
        scope.launch(Dispatchers.IO) {
            db.musicDao().insertRecent(RecentEntity(track.id, track.title, track.artist, track.duration, track.uri.toString()))
            // Update will come via Flow collect in initialize()
        }
    }
}
