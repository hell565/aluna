package com.kl.aluna.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import com.kl.aluna.MainActivity
import com.kl.aluna.R  // если есть R.string.app_name

class MediaNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "aluna_music"
        const val NOTIFICATION_ID = 1001
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    lateinit var playerNotificationManager: PlayerNotificationManager

    fun createNotificationManager() {
        createChannel()

        playerNotificationManager = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
            .setChannelNameResourceId(R.string.app_name)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return MusicPlayer.currentTrack.value?.title ?: "No track"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(context, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return MusicPlayer.currentTrack.value?.artist
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    // Пока null, позже добавим Coil или BitmapFactory
                    return null
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    notificationManager.notify(notificationId, notification)
                }

                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    notificationManager.cancel(notificationId)
                }
            })
            .build()

        playerNotificationManager.setPlayer(MusicPlayer.exoPlayer)
        playerNotificationManager.setUseRewindAction(false)
        playerNotificationManager.setUseFastForwardAction(false)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}