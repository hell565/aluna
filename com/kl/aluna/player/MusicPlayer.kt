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
        
        // Load data from DB
        scope.launch {
            favorites.clear()
            favorites.addAll(db.musicDao().getFavoriteIds().first())
            
            val recent = db.musicDao().getRecent().first()
            recentTracks.clear()
            recentTracks.addAll(recent.map { 
                Track(it.trackId, it.title, it.artist, it.duration, android.net.Uri.parse(it.uri), 0)
            })
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                MusicPlayer.isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionUpdater()
                    currentTrack.value?.let { addToRecent(it) }
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
            }
        })
    }

    private fun startPositionUpdater() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (true) {
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
        exoPlayer.seekTo(startIndex, 0)
        exoPlayer.prepare()
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
        val nextIndex = (currentTrackIndex.value + 1) % playlist.size
        exoPlayer.seekTo(nextIndex, 0)
        exoPlayer.play()
    }

    fun previous() {
        if (playlist.isEmpty()) return
        val prevIndex = if (currentTrackIndex.value <= 0) playlist.size - 1 else currentTrackIndex.value - 1
        exoPlayer.seekTo(prevIndex, 0)
        exoPlayer.play()
    }

    fun toggleFavorite(track: Track) {
        scope.launch(Dispatchers.IO) {
            if (favorites.contains(track.id)) {
                db.musicDao().deleteFavorite(FavoriteEntity(track.id))
                withContext(Dispatchers.Main) { favorites.remove(track.id) }
            } else {
                db.musicDao().insertFavorite(FavoriteEntity(track.id))
                withContext(Dispatchers.Main) { favorites.add(track.id) }
            }
        }
    }

    private fun addToRecent(track: Track) {
        scope.launch(Dispatchers.IO) {
            db.musicDao().insertRecent(RecentEntity(track.id, track.title, track.artist, track.duration, track.uri.toString()))
            val recent = db.musicDao().getRecent().first()
            withContext(Dispatchers.Main) {
                recentTracks.clear()
                recentTracks.addAll(recent.map { Track(it.trackId, it.title, it.artist, it.duration, android.net.Uri.parse(it.uri), 0) })
            }
        }
    }
}
