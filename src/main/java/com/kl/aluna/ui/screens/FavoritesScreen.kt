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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current   // <-- ВАЖНО
    val viewModel: MusicViewModel = viewModel()
    val allTracks = viewModel.allTracks
    val favoriteIds = MusicPlayer.favorites
    val currentTrack = MusicPlayer.currentTrack.value
    val isPlaying = MusicPlayer.isPlaying.value

    val favoriteTracks = remember(allTracks.size, favoriteIds.size) {
        // Берем все треки из основного списка, чьи ID есть в списке фаворитов
        // Это гарантирует, что если трек есть в базе, он появится
        allTracks.filter { track -> favoriteIds.contains(track.id) }
    }

    // Если основной список пуст, пробуем восстановить из Recent или других источников? 
    // Нет, лучше убедиться что scan() прошел.
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
                        AlunaColors.Background.copy(alpha = 0.95f),
                        Color(0xFF0D1230)
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (favoriteTracks.isNotEmpty()) {
                        Text(
                            text = "${favoriteTracks.size} loved tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlunaColors.Secondary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(AlunaColors.Surface.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AlunaColors.TextPrimary
                    )
                }
            },
            actions = {
                if (favoriteTracks.isNotEmpty()) {
                    FilledTonalIconButton(
                        onClick = {
                            if (favoriteTracks.isNotEmpty()) {
                                MusicPlayer.setPlaylistAndPlay(favoriteTracks)
                            }
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = AlunaColors.Secondary.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play all",
                            tint = AlunaColors.Secondary
                        )
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
            if (favoriteTracks.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { MusicPlayer.setPlaylistAndPlay(favoriteTracks) },
                    color = AlunaColors.Secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = AlunaColors.Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Shuffle Play All",
                            style = MaterialTheme.typography.titleMedium,
                            color = AlunaColors.Secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(favoriteTracks, key = { _, track -> track.id }) { index, track ->
                    val isCurrentlyPlaying = currentTrack?.id == track.id
                    
                    FavoriteTrackItem(
                        track = track,
                        index = index,
                        isPlaying = isCurrentlyPlaying && isPlaying,
                        isActive = isCurrentlyPlaying,
                        onPlay = { MusicPlayer.playTrack(track) },
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
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPlay),
        color = if (isActive) AlunaColors.Secondary.copy(alpha = 0.12f) else AlunaColors.Surface,
        tonalElevation = if (isActive) 8.dp else 2.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = AlunaColors.Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlunaColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AlunaColors.Secondary, AlunaColors.Primary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) AlunaColors.Secondary else AlunaColors.TextPrimary,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
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
                style = MaterialTheme.typography.bodySmall,
                color = AlunaColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = AlunaColors.Secondary,
                    modifier = Modifier.size(22.dp)
                )
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
                .size(140.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AlunaColors.Secondary.copy(alpha = 0.15f),
                            AlunaColors.Primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = AlunaColors.Secondary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Favorites Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tap the heart icon on any song\nto add it to your favorites",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
