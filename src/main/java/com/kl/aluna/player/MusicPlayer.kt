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
    val blacklist = mutableStateListOf<Long>()
    val recentTracks = mutableStateListOf<Track>()

    private var positionJob: Job? = null
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isFadingOut = false
    
    private var allAvailableTracks = mutableStateListOf<Track>()

    fun isInitialized(): Boolean = this::exoPlayer.isInitialized

    fun initialize(context: Context, player: ExoPlayer) {
        if (isInitialized()) return
        
        appContext = context.applicationContext
        db = AppDatabase.getInstance(context)
        exoPlayer = player
        
        val sessionId = "AlunaMediaSession_${System.currentTimeMillis()}"
        mediaSession = MediaSession.Builder(appContext, exoPlayer)
            .setId(sessionId)
            .build()
        
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
            db.musicDao().getBlacklistIds().collectLatest { ids ->
                withContext(Dispatchers.Main) {
                    blacklist.clear()
                    blacklist.addAll(ids)
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
                if (exoPlayer.isPlaying) {
                    val pos = exoPlayer.currentPosition
                    val dur = exoPlayer.duration
                    currentPosition.value = pos
                    
                    if (com.kl.aluna.data.AlunaSettings.isGaplessPlaybackEnabled && !isFadingOut && dur > 1000) {
                        val fadeMillis = com.kl.aluna.data.AlunaSettings.crossfadeDurationSeconds * 1000L
                        if (pos >= dur - fadeMillis && pos < dur - 100) {
                            startCrossfadeNext()
                        }
                    }
                }
                delay(100)
            }
        }
    }

    private fun startCrossfadeNext() {
        if (isFadingOut) return
        isFadingOut = true
        scope.launch {
            val fadeMillis = com.kl.aluna.data.AlunaSettings.crossfadeDurationSeconds * 1000L
            val steps = 30
            val stepDelay = fadeMillis / steps
            
            withContext(Dispatchers.Main) {
                for (i in steps downTo 0) {
                    val volume = (i / steps.toFloat()).let { it * it }
                    exoPlayer.volume = volume
                    delay(stepDelay / 2)
                }
                
                next()
                
                for (i in 0..steps) {
                    val volume = (i / steps.toFloat()).let { it * it }
                    exoPlayer.volume = volume
                    delay(stepDelay / 2)
                }
                
                exoPlayer.volume = 1.0f
                isFadingOut = false
            }
        }
    }

    private var sleepTimerJob: Job? = null
    val sleepTimerMinutesLeft = mutableStateOf(0)

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            sleepTimerMinutesLeft.value = 0
            return
        }
        
        sleepTimerMinutesLeft.value = minutes
        sleepTimerJob = scope.launch {
            var remaining = minutes * 60
            while (remaining > 0) {
                delay(1000)
                remaining--
                sleepTimerMinutesLeft.value = (remaining / 60) + 1
            }
            sleepTimerMinutesLeft.value = 0
            if (exoPlayer.isPlaying) {
                for (i in 20 downTo 0) {
                    exoPlayer.volume = i / 20f
                    delay(50)
                }
                exoPlayer.pause()
                exoPlayer.volume = 1.0f
            }
        }
    }

    fun setAllTracks(tracks: List<Track>) {
        allAvailableTracks.clear()
        allAvailableTracks.addAll(tracks)
    }

    fun setPlaylistAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        
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
        exoPlayer.play()
    }

    fun preparePlaylist(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        
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

    fun playTrack(track: Track) {
        val index = playlist.indexOfFirst { it.id == track.id }
        if (index != -1) {
            exoPlayer.seekTo(index, 0)
            exoPlayer.play()
        } else {
            val tracksToPlay = if (allAvailableTracks.isNotEmpty()) {
                val trackIndex = allAvailableTracks.indexOfFirst { it.id == track.id }
                if (trackIndex != -1) {
                    allAvailableTracks.toList()
                } else {
                    listOf(track)
                }
            } else {
                listOf(track)
            }
            val startIdx = tracksToPlay.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            setPlaylistAndPlay(tracksToPlay, startIdx)
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.playbackState == Player.STATE_IDLE) exoPlayer.prepare()
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun next() {
        if (playlist.isEmpty()) return
        val currentIdx = exoPlayer.currentMediaItemIndex
        val nextIdx = if (currentIdx < playlist.size - 1) currentIdx + 1 else 0
        exoPlayer.seekTo(nextIdx, 0)
        if (!exoPlayer.isPlaying) exoPlayer.play()
    }

    fun previous() {
        if (playlist.isEmpty()) return
        val currentIdx = exoPlayer.currentMediaItemIndex
        val prevIdx = if (currentIdx > 0) currentIdx - 1 else playlist.size - 1
        exoPlayer.seekTo(prevIdx, 0)
        if (!exoPlayer.isPlaying) exoPlayer.play()
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

    fun toggleBlacklist(track: Track) {
        scope.launch(Dispatchers.IO) {
            val isBlacklisted = blacklist.contains(track.id)
            if (isBlacklisted) {
                db.musicDao().deleteBlacklist(BlacklistEntity(track.id))
            } else {
                db.musicDao().insertBlacklist(BlacklistEntity(track.id))
                if (currentTrack.value?.id == track.id) {
                    withContext(Dispatchers.Main) {
                        next()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                notifyServiceUpdate()
            }
        }
    }
    
    fun isBlacklisted(trackId: Long): Boolean = blacklist.contains(trackId)

    private fun addToRecent(track: Track) {
        scope.launch(Dispatchers.IO) {
            db.musicDao().insertRecent(RecentEntity(
                track.id, track.title, track.artist, track.duration, track.uri.toString()
            ))
        }
    }
    
    fun shuffleAndPlay() {
        val tracksToShuffle = if (allAvailableTracks.isNotEmpty()) {
            allAvailableTracks.filter { !blacklist.contains(it.id) }
        } else if (playlist.isNotEmpty()) {
            playlist.filter { !blacklist.contains(it.id) }
        } else {
            emptyList()
        }
        
        if (tracksToShuffle.isNotEmpty()) {
            setPlaylistAndPlay(tracksToShuffle.shuffled())
        }
    }

    fun playAll() {
        val tracksToPlay = if (allAvailableTracks.isNotEmpty()) {
            allAvailableTracks.filter { !blacklist.contains(it.id) }
        } else if (playlist.isNotEmpty()) {
            playlist.filter { !blacklist.contains(it.id) }
        } else {
            emptyList()
        }
        
        if (tracksToPlay.isNotEmpty()) {
            setPlaylistAndPlay(tracksToPlay.toList())
        }
    }
    
    fun release() {
        mediaSession?.release()
        mediaSession = null
        exoPlayer.release()
        positionJob?.cancel()
    }
}
