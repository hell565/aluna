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
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.player.MusicViewModel
import com.kl.aluna.player.Track
import com.kl.aluna.ui.theme.AlunaColors

import com.kl.aluna.data.AlunaStrings
import com.kl.aluna.data.AlunaSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: MusicViewModel = viewModel()
    val allTracks = viewModel.allTracks
    val favoriteIds = MusicPlayer.favorites
    val currentTrack = MusicPlayer.currentTrack.value
    val isPlaying = MusicPlayer.isPlaying.value

    val favoriteTracks = remember(allTracks.size, favoriteIds.size) {
        allTracks.filter { track -> favoriteIds.contains(track.id) }
    }

    LaunchedEffect(favoriteIds.size) {
        if (allTracks.isEmpty()) {
            viewModel.scan(context)
        }
    }

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
                        text = AlunaStrings.get("favorites"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                    if (favoriteTracks.isNotEmpty()) {
                        Text(
                            text = "${favoriteTracks.size} ${AlunaStrings.get("tracks")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlunaColors.Secondary
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
                if (favoriteTracks.isNotEmpty()) {
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
                                    if (favoriteTracks.isNotEmpty()) {
                                        MusicPlayer.setPlaylistAndPlay(favoriteTracks.shuffled())
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
                            color = AlunaColors.Secondary
                        ) {
                            IconButton(
                                onClick = {
                                    if (favoriteTracks.isNotEmpty()) {
                                        MusicPlayer.setPlaylistAndPlay(favoriteTracks)
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

        if (favoriteTracks.isEmpty()) {
            EmptyFavoritesState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(favoriteTracks, key = { _, track -> track.id }) { index, track ->
                    val isCurrentlyPlaying = currentTrack?.id == track.id
                    
                    FavoriteTrackItem(
                        track = track,
                        index = index,
                        isPlaying = isCurrentlyPlaying && isPlaying,
                        isActive = isCurrentlyPlaying,
                        onPlay = { 
                            if (isCurrentlyPlaying) {
                                MusicPlayer.togglePlayPause()
                            } else {
                                MusicPlayer.playTrack(track)
                            }
                        },
                        onRemove = { MusicPlayer.toggleFavorite(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteTrackItem(
    track: Track,
    index: Int,
    isPlaying: Boolean,
    isActive: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isActive) 12.dp else 4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onPlay),
        color = if (isActive) AlunaColors.Secondary.copy(alpha = 0.12f) else AlunaColors.Surface,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, AlunaColors.Secondary.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Surface(
                        shape = CircleShape,
                        color = AlunaColors.Secondary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlunaColors.TextSecondary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AlunaColors.Secondary, Color(0xFFFF8E53))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) AlunaColors.Secondary else AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
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

            Text(
                text = formatDuration(track.duration),
                style = MaterialTheme.typography.labelMedium,
                color = AlunaColors.TextSecondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = AlunaColors.Secondary.copy(alpha = 0.1f)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = AlunaColors.Secondary,
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
private fun EmptyFavoritesState(
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
                            AlunaColors.Secondary.copy(alpha = 0.2f),
                            Color(0xFFFF8E53).copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = AlunaColors.Secondary,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = AlunaStrings.get("no_favorites_yet"),
            style = MaterialTheme.typography.headlineSmall,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = AlunaStrings.get("add_to_favorites_hint"),
            style = MaterialTheme.typography.bodyLarge,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
