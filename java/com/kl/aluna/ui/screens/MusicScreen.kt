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
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.player.MusicViewModel
import com.kl.aluna.player.Track
import com.kl.aluna.ui.theme.AlunaColors

@Composable
fun MusicScreen(
    onCategoryClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: MusicViewModel = viewModel()
    val tracks = viewModel.allTracks
    val isScanning by viewModel.isScanning
    val context = LocalContext.current
    val currentTrack = MusicPlayer.currentTrack.value

    LaunchedEffect(Unit) {
        if (tracks.isEmpty() && !isScanning) {
            viewModel.scan(context)
        }
    }

    val displayTracks = remember(tracks, currentTrack) {
        if (currentTrack != null) {
            val list = tracks.toMutableList()
            val index = list.indexOfFirst { it.id == currentTrack.id }
            if (index != -1) {
                val track = list.removeAt(index)
                list.add(0, track)
            }
            list
        } else tracks
    }

    LazyColumn(
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
            ),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 80.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { NowPlayingCard() }
        item { SectionHeader(title = "Library") }
        item { 
            CategoryCard(
                icon = Icons.AutoMirrored.Outlined.List, 
                title = "Playlists", 
                subtitle = "Your playlists",
                accentColor = AlunaColors.Primary,
                onClick = { onCategoryClick("Playlists") }
            ) 
        }
        item { 
            CategoryCard(
                icon = Icons.Outlined.FavoriteBorder, 
                title = "Favorites", 
                subtitle = "${MusicPlayer.favorites.size} tracks",
                accentColor = AlunaColors.Secondary,
                onClick = { onCategoryClick("Favorites") }
            ) 
        }
        item { 
            CategoryCard(
                icon = Icons.Outlined.AccessTime, 
                title = "Recent", 
                subtitle = "${MusicPlayer.recentTracks.size} tracks",
                accentColor = AlunaColors.Success,
                onClick = { onCategoryClick("Recent") }
            ) 
        }
        item { SectionHeader(title = "Browse") }
        
        if (isScanning) {
            item { 
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp), 
                    contentAlignment = Alignment.Center
                ) { 
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AlunaColors.Primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning for music...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlunaColors.TextSecondary
                        )
                    }
                } 
            }
        } else if (displayTracks.isEmpty()) {
            item { EmptyMusicState() }
        } else {
            itemsIndexed(displayTracks, key = { _, track -> track.id }) { index, track ->
                BrowseTrackItem(
                    track = track, 
                    index = index, 
                    isActive = currentTrack?.id == track.id,
                    onClick = { MusicPlayer.playTrack(track) }
                )
            }
        }
    }
}

@Composable
private fun NowPlayingCard() {
    val currentTrack = MusicPlayer.currentTrack.value
    val isPlaying = MusicPlayer.isPlaying.value
    val currentPos = MusicPlayer.currentPosition.value
    val duration = MusicPlayer.trackDuration.value
    val isFav = currentTrack?.let { MusicPlayer.favorites.contains(it.id) } ?: false

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        color = AlunaColors.Surface,
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentTrack != null) "NOW PLAYING" else "NOT PLAYING",
                        style = MaterialTheme.typography.labelSmall,
                        color = AlunaColors.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTrack?.title ?: "Select a track",
                        style = MaterialTheme.typography.titleLarge,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentTrack?.artist ?: "Browse your library",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlunaColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Column {
                LinearProgressIndicator(
                    progress = { if (duration > 0) (currentPos.toFloat() / duration) else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AlunaColors.Primary,
                    trackColor = AlunaColors.SurfaceLight
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Slider(
                    value = if (duration > 0) currentPos.toFloat() / duration else 0f,
                    onValueChange = { MusicPlayer.seekTo((it * duration).toLong()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = AlunaColors.Primary,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPos),
                    style = MaterialTheme.typography.labelMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "-${formatTime(if (duration > currentPos) duration - currentPos else 0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AlunaColors.TextSecondary
                )
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentTrack?.let { MusicPlayer.toggleFavorite(it) } },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFav) AlunaColors.Secondary else AlunaColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { MusicPlayer.previous() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = null,
                        tint = AlunaColors.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                FilledIconButton(
                    onClick = { MusicPlayer.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AlunaColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(
                    onClick = { MusicPlayer.next() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        tint = AlunaColors.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = null,
                        tint = AlunaColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable 
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = AlunaColors.TextSecondary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable 
private fun CategoryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = AlunaColors.Surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlunaColors.TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AlunaColors.TextSecondary
            )
        }
    }
}

@Composable 
private fun EmptyMusicState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AlunaColors.SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicOff,
                contentDescription = null,
                tint = AlunaColors.TextSecondary,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Music Found",
            style = MaterialTheme.typography.titleMedium,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some music files to your device",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable 
private fun BrowseTrackItem(
    track: Track,
    index: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val isPlaying = MusicPlayer.isPlaying.value && isActive
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isActive) AlunaColors.Primary.copy(alpha = 0.12f) else AlunaColors.Surface,
        tonalElevation = if (isActive) 8.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                        tint = AlunaColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlunaColors.TextSecondary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isActive) 
                                listOf(AlunaColors.Primary, AlunaColors.Secondary)
                            else 
                                listOf(AlunaColors.Primary.copy(alpha = 0.6f), AlunaColors.Secondary.copy(alpha = 0.6f))
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
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) AlunaColors.Primary else AlunaColors.TextPrimary,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
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
                text = formatTime(track.duration),
                style = MaterialTheme.typography.bodySmall,
                color = AlunaColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
