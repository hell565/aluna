package com.kl.aluna.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.player.Track
import com.kl.aluna.ui.theme.AlunaColors
import com.kl.aluna.data.AlunaStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val recentTracks = MusicPlayer.recentTracks
    val currentTrack = MusicPlayer.currentTrack.value
    val isPlaying = MusicPlayer.isPlaying.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AlunaColors.Background,
                        Color(0xFF0D1230),
                        Color(0xFF080B1A)
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = AlunaStrings.get("recently_played"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                    if (recentTracks.isNotEmpty()) {
                        Text(
                            text = "${recentTracks.size} ${AlunaStrings.get("tracks")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlunaColors.Success
                        )
                    }
                }
            },
            navigationIcon = {
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(44.dp),
                    shape = CircleShape,
                    color = AlunaColors.Surface.copy(alpha = 0.8f)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = AlunaStrings.get("back"),
                            tint = AlunaColors.TextPrimary
                        )
                    }
                }
            },
            actions = {
                if (recentTracks.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = AlunaColors.Surface.copy(alpha = 0.8f)
                        ) {
                            IconButton(
                                onClick = {
                                    if (recentTracks.isNotEmpty()) {
                                        MusicPlayer.setPlaylistAndPlay(recentTracks.shuffled())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = AlunaStrings.get("shuffle"),
                                    tint = AlunaColors.TextPrimary
                                )
                            }
                        }
                        
                        Surface(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = AlunaColors.Success
                        ) {
                            IconButton(
                                onClick = {
                                    if (recentTracks.isNotEmpty()) {
                                        MusicPlayer.setPlaylistAndPlay(recentTracks)
                                    }
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = AlunaStrings.get("play_all"),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        if (recentTracks.isEmpty()) {
            EmptyRecentState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(recentTracks, key = { _, track -> track.id }) { index, track ->
                    val isCurrentlyPlaying = currentTrack?.id == track.id
                    
                    RecentTrackItem(
                        track = track,
                        index = index,
                        isPlaying = isCurrentlyPlaying && isPlaying,
                        isActive = isCurrentlyPlaying,
                        onClick = { 
                            if (isCurrentlyPlaying) {
                                MusicPlayer.togglePlayPause()
                            } else {
                                MusicPlayer.playTrack(track)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTrackItem(
    track: Track,
    index: Int,
    isPlaying: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isActive) 10.dp else 3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = if (isActive) AlunaColors.Success.copy(alpha = 0.12f) else AlunaColors.Surface,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, AlunaColors.Success.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = AlunaColors.Success,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AlunaColors.TextSecondary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isActive) 
                                listOf(AlunaColors.Success, Color(0xFF44B89D))
                            else 
                                listOf(AlunaColors.Success.copy(alpha = 0.7f), Color(0xFF44B89D).copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) AlunaColors.Success else AlunaColors.TextPrimary,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlunaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatDuration(track.duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = AlunaColors.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = AlunaColors.TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (isActive) AlunaColors.Success else AlunaColors.Success.copy(alpha = 0.1f)
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = AlunaStrings.get("play_all"),
                        tint = if (isActive) Color.White else AlunaColors.Success,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = ms / 1000 / 60
    val seconds = ms / 1000 % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
private fun EmptyRecentState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(20.dp, RoundedCornerShape(40.dp))
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AlunaColors.Success.copy(alpha = 0.2f),
                            Color(0xFF44B89D).copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = AlunaColors.Success,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = AlunaStrings.get("no_recent_plays"),
            style = MaterialTheme.typography.headlineSmall,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = AlunaStrings.get("recent_hint"),
            style = MaterialTheme.typography.bodyLarge,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
