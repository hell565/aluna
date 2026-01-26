package com.kl.aluna

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.kl.aluna.player.MediaNotificationManager
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.ui.navigation.AlunaNavigation
import com.kl.aluna.ui.theme.AlunaTheme
import com.kl.aluna.player.MusicViewModel   // или com.kl.aluna.viewmodel.MusicViewModel — куда ты его положил

class MainActivity : ComponentActivity() {

    private val viewModel = MusicViewModel()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.scan(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val exoPlayer = ExoPlayer.Builder(this).build()
        MusicPlayer.initialize(this, exoPlayer)
        
        viewModel.scan(this)

        val notificationManager = MediaNotificationManager(this)
        notificationManager.createNotificationManager()

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)

        setContent {
            AlunaTheme {
                AlunaNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayer.release()
    }
}