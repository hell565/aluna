package com.kl.aluna.player

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.kl.aluna.data.db.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

object MusicPlayer {
    lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null
    private lateinit var appContext: Context
    
    val playlist = mutableStateListOf<Track>()
    val currentTrackIndex = mutableStateOf(-1)
    val currentTrack = mutableStateOf<Track?>(null)
    val isPlaying = mutableStateOf(false)
    val currentPosition = mutableStateOf(0L)
    val trackDuration = mutableStateOf(0L)
    val playbackSpeed = mutableStateOf(1.0f)
    
    val favorites = mutableStateListOf<Long>()
    val recentTracks = mutableStateListOf<Track>()

    private var positionJob: Job? = null
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize(context: Context, player: ExoPlayer) {
        appContext = context.applicationContext
        db = AppDatabase.getInstance(context)
        exoPlayer = player
        
        mediaSession = MediaSession.Builder(context, exoPlayer).build()
        
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.shuffleModeEnabled = false

        scope.launch {
            db.musicDao().getFavoriteIds().collectLatest { ids ->
                withContext(Dispatchers.Main) {
                    favorites.clear()
                    favorites.addAll(ids)
                }
            }
        }
        
        scope.launch {
            db.musicDao().getRecent().collectLatest { recent ->
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
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying.value = playing
                if (playing) {
                    startPositionUpdater()
                    notifyServiceUpdate()
                } else {
                    positionJob?.cancel()
                    notifyServiceUpdate()
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when(state) {
                    Player.STATE_READY -> trackDuration.value = exoPlayer.duration.coerceAtLeast(0L)
                    Player.STATE_ENDED -> {
                        if (exoPlayer.repeatMode == Player.REPEAT_MODE_OFF) {
                            next()
                        }
                    }
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                if (index in playlist.indices) {
                    currentTrackIndex.value = index
                    currentTrack.value = playlist[index]
                    trackDuration.value = exoPlayer.duration.coerceAtLeast(0L)
                    currentTrack.value?.let { addToRecent(it) }
                    notifyServiceUpdate()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                next()
            }
        })
    }

    private fun notifyServiceUpdate() {
        try {
            val intent = Intent("com.kl.aluna.UPDATE_NOTIFICATION")
            intent.setPackage(appContext.packageName)
            appContext.sendBroadcast(intent)
        } catch (e: Exception) { }
    }

    private fun startPositionUpdater() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                currentPosition.value = exoPlayer.currentPosition
                delay(500)
            }
        }
    }

    fun setPlaylistAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        preparePlaylist(tracks, startIndex)
        exoPlayer.play()
    }

    fun preparePlaylist(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        
        scope.launch(Dispatchers.Main) {
            playlist.clear()
            playlist.addAll(tracks)
            
            val mediaItems = tracks.map { track ->
                MediaItem.Builder()
                    .setUri(track.uri)
                    .setMediaId(track.id.toString())
                    .build()
            }
            
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItems(mediaItems)
            exoPlayer.prepare()
            exoPlayer.seekTo(startIndex, 0)
        }
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
        if (exoPlayer.playbackState == Player.STATE_IDLE) exoPlayer.prepare()
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun next() {
        if (playlist.isEmpty()) return
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else {
            exoPlayer.seekTo(0, 0)
        }
        exoPlayer.play()
    }

    fun previous() {
        if (playlist.isEmpty()) return
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            exoPlayer.seekTo(playlist.size - 1, 0)
        }
        exoPlayer.play()
    }

    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    fun setSpeed(speed: Float) {
        playbackSpeed.value = speed
        exoPlayer.setPlaybackSpeed(speed)
    }

    fun toggleFavorite(track: Track) {
        scope.launch(Dispatchers.IO) {
            val isFavorite = favorites.contains(track.id)
            if (isFavorite) {
                db.musicDao().deleteFavorite(FavoriteEntity(track.id))
            } else {
                db.musicDao().insertFavorite(FavoriteEntity(track.id))
            }
            withContext(Dispatchers.Main) {
                notifyServiceUpdate()
            }
        }
    }

    private fun addToRecent(track: Track) {
        scope.launch(Dispatchers.IO) {
            db.musicDao().insertRecent(RecentEntity(
                track.id, track.title, track.artist, track.duration, track.uri.toString()
            ))
        }
    }
    
    fun shuffleAndPlay() {
        if (playlist.isNotEmpty()) {
            setPlaylistAndPlay(playlist.shuffled())
        }
    }

    fun playAll() {
        if (playlist.isNotEmpty()) {
            setPlaylistAndPlay(playlist.toList())
        }
    }
    
    fun release() {
        mediaSession?.release()
        mediaSession = null
        exoPlayer.release()
        positionJob?.cancel()
    }
}
