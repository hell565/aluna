package com.kl.aluna.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kl.aluna.MainActivity
import com.kl.aluna.R
import kotlinx.coroutines.*

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "aluna_music_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_PLAY_PAUSE = "com.kl.aluna.PLAY_PAUSE"
        const val ACTION_NEXT = "com.kl.aluna.NEXT"
        const val ACTION_PREVIOUS = "com.kl.aluna.PREVIOUS"
        const val ACTION_FAVORITE = "com.kl.aluna.FAVORITE"
        const val ACTION_STOP = "com.kl.aluna.STOP"
        
        fun startService(context: Context) {
            val intent = Intent(context, MusicService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, MusicService::class.java)
            context.stopService(intent)
        }
    }

    private val binder = MusicBinder()
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var updateJob: Job? = null

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY_PAUSE -> MusicPlayer.togglePlayPause()
                ACTION_NEXT -> MusicPlayer.next()
                ACTION_PREVIOUS -> MusicPlayer.previous()
                ACTION_FAVORITE -> {
                    MusicPlayer.currentTrack.value?.let { track ->
                        MusicPlayer.toggleFavorite(track)
                        updateNotification()
                    }
                }
                ACTION_STOP -> {
                    MusicPlayer.exoPlayer.stop()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        
        exoPlayer = MusicPlayer.exoPlayer
        
        mediaSession = MediaSessionCompat(this, "AlunaMediaSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    MusicPlayer.togglePlayPause()
                }
                
                override fun onPause() {
                    MusicPlayer.togglePlayPause()
                }
                
                override fun onSkipToNext() {
                    MusicPlayer.next()
                }
                
                override fun onSkipToPrevious() {
                    MusicPlayer.previous()
                }
                
                override fun onSeekTo(pos: Long) {
                    MusicPlayer.seekTo(pos)
                }
                
                override fun onStop() {
                    MusicPlayer.exoPlayer.stop()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
        
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
            addAction(ACTION_FAVORITE)
            addAction(ACTION_STOP)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(notificationReceiver, filter)
        }
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
                updateMediaSession()
                
                if (isPlaying) {
                    startProgressUpdater()
                } else {
                    updateJob?.cancel()
                }
            }
            
            override fun onMediaItemTransition(
                mediaItem: androidx.media3.common.MediaItem?,
                reason: Int
            ) {
                updateNotification()
                updateMediaSession()
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateNotification()
                updateMediaSession()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
        mediaSession.release()
        try {
            unregisterReceiver(notificationReceiver)
        } catch (e: Exception) { }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aluna Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startProgressUpdater() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                updateMediaSession()
                delay(1000)
            }
        }
    }

    private fun updateMediaSession() {
        val track = MusicPlayer.currentTrack.value
        val isPlaying = MusicPlayer.isPlaying.value
        val position = MusicPlayer.currentPosition.value
        val duration = MusicPlayer.trackDuration.value

        val state = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                position,
                1f
            )
            .build()
        
        mediaSession.setPlaybackState(state)
        
        if (track != null) {
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, createAlbumArt())
                .build()
            
            mediaSession.setMetadata(metadata)
        }
    }

    fun updateNotification() {
        val notification = buildNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val track = MusicPlayer.currentTrack.value
        val isPlaying = MusicPlayer.isPlaying.value
        val isFavorite = track?.let { MusicPlayer.favorites.contains(it.id) } ?: false

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = PendingIntent.getBroadcast(
            this, 1,
            Intent(ACTION_PREVIOUS).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getBroadcast(
            this, 2,
            Intent(ACTION_PLAY_PAUSE).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getBroadcast(
            this, 3,
            Intent(ACTION_NEXT).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val favoriteIntent = PendingIntent.getBroadcast(
            this, 4,
            Intent(ACTION_FAVORITE).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getBroadcast(
            this, 5,
            Intent(ACTION_STOP).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(track?.title ?: "Aluna")
            .setContentText(track?.artist ?: "Music Player")
            .setSubText(if (isPlaying) "Playing" else "Paused")
            .setSmallIcon(R.drawable.ic_music_note)
            .setLargeIcon(createAlbumArt())
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setStyle(style)
            .addAction(
                R.drawable.ic_skip_previous,
                "Previous",
                previousIntent
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(
                R.drawable.ic_skip_next,
                "Next",
                nextIntent
            )
            .addAction(
                if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border,
                "Favorite",
                favoriteIntent
            )
            .build()
    }

    private fun createAlbumArt(): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val gradient = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            intArrayOf(0xFF6B4EFF.toInt(), 0xFFFF6B9D.toInt(), 0xFF9D4EFF.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        
        val backgroundPaint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }
        
        canvas.drawRoundRect(
            RectF(0f, 0f, size.toFloat(), size.toFloat()),
            48f, 48f,
            backgroundPaint
        )
        
        val iconPaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            isAntiAlias = true
            textSize = 200f
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText("♪", size / 2f, size / 2f + 70f, iconPaint)
        
        return bitmap
    }
}
