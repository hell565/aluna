package com.kl.aluna.player

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,
    val dateAdded: Long,
    val albumArtUri: Uri? = null
)
