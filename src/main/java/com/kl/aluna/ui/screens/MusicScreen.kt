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
import com.kl.aluna.data.AlunaStrings
import com.kl.aluna.data.AlunaSettings

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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

    val displayTracks = tracks

    LazyColumn(
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
            ),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { WelcomeHeader() }
        item { NowPlayingCard() }
        item { QuickActionsRow(onCategoryClick = onCategoryClick) }
        item { SectionHeader(title = AlunaStrings.get("library")) }
        item { 
            CategoryCard(
                icon = Icons.AutoMirrored.Outlined.List, 
                title = AlunaStrings.get("playlists"), 
                subtitle = AlunaStrings.get("your_playlists"),
                accentColor = AlunaColors.Primary,
                gradientColors = listOf(Color(0xFF6B4EFF), Color(0xFF9D4EFF)),
                onClick = { onCategoryClick("Playlists") }
            ) 
        }
        item { 
            CategoryCard(
                icon = Icons.Outlined.FavoriteBorder, 
                title = AlunaStrings.get("favorites"), 
                subtitle = "${MusicPlayer.favorites.size} ${AlunaStrings.get("tracks")}",
                accentColor = AlunaColors.Secondary,
                gradientColors = listOf(Color(0xFFFF6B9D), Color(0xFFFF8E53)),
                onClick = { onCategoryClick("Favorites") }
            ) 
        }
        item { 
            CategoryCard(
                icon = Icons.Outlined.AccessTime, 
                title = AlunaStrings.get("recent"), 
                subtitle = "${MusicPlayer.recentTracks.size} ${AlunaStrings.get("tracks")}",
                accentColor = AlunaColors.Success,
                gradientColors = listOf(Color(0xFF4ECDC4), Color(0xFF44B89D)),
                onClick = { onCategoryClick("Recent") }
            ) 
        }
        item { SectionHeader(title = AlunaStrings.get("browse")) }
        
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
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = AlunaStrings.get("scanning"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AlunaColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } 
            }
        } else if (displayTracks.isEmpty()) {
            item { EmptyMusicState() }
        } else {
            itemsIndexed(displayTracks, key = { _, track -> track.id }) { index, track ->
                val isActive = currentTrack?.id == track.id
                val isPlaying = MusicPlayer.isPlaying.value && isActive
                
                BrowseTrackItem(
                    track = track, 
                    index = index, 
                    isActive = isActive,
                    isPlaying = isPlaying,
                    onClick = { 
                        if (isActive) {
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

@Composable
private fun WelcomeHeader() {
    Column(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = AlunaStrings.get("my_music"),
            style = MaterialTheme.typography.headlineLarge,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Black
        )
        Text(
            text = AlunaStrings.get("explore"),
            style = MaterialTheme.typography.bodyLarge,
            color = AlunaColors.TextSecondary
        )
    }
}

@Composable
private fun QuickActionsRow(onCategoryClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionChip(
            icon = Icons.Default.Shuffle,
            label = AlunaStrings.get("shuffle"),
            onClick = { MusicPlayer.shuffleAndPlay() },
            modifier = Modifier.weight(1f)
        )
        QuickActionChip(
            icon = Icons.Default.PlayArrow,
            label = AlunaStrings.get("play_all"),
            onClick = { MusicPlayer.playAll() },
            modifier = Modifier.weight(1f),
            isPrimary = true
        )
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isPrimary) AlunaColors.Primary else AlunaColors.Surface,
        tonalElevation = if (isPrimary) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) Color.White else AlunaColors.Primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isPrimary) Color.White else AlunaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
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
            .shadow(24.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp)),
        color = AlunaColors.Surface
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AlunaColors.Surface,
                            AlunaColors.Surface.copy(alpha = 0.95f),
                            Color(0xFF1A1F3A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(16.dp, RoundedCornerShape(28.dp))
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        AlunaColors.Primary,
                                        AlunaColors.Secondary,
                                        Color(0xFF9D4EFF)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPlaying) AlunaColors.Primary.copy(alpha = 0.15f) else AlunaColors.SurfaceLight.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = if (isPlaying) AlunaStrings.get("now_playing") else if (currentTrack != null) AlunaStrings.get("paused") else "ALUNA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPlaying) AlunaColors.Primary else AlunaColors.TextSecondary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentTrack?.title ?: AlunaStrings.get("no_track"),
                            style = MaterialTheme.typography.titleLarge,
                            color = AlunaColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentTrack?.artist ?: AlunaStrings.get("welcome"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlunaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Column {
                    Slider(
                        value = if (duration > 0) currentPos.toFloat() / duration else 0f,
                        onValueChange = { MusicPlayer.seekTo((it * duration).toLong()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AlunaColors.Primary,
                            activeTrackColor = AlunaColors.Primary,
                            inactiveTrackColor = AlunaColors.SurfaceLight
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
                        fontWeight = FontWeight.Bold
                    )
                    if (duration > 0) {
                        Text(
                            text = "-${formatTime(if (duration > currentPos) duration - currentPos else 0)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AlunaColors.TextSecondary
                        )
                    }
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
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
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFav) AlunaColors.Secondary.copy(alpha = 0.15f) else Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFav) AlunaColors.Secondary else AlunaColors.TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = AlunaColors.SurfaceLight.copy(alpha = 0.5f)
                    ) {
                        IconButton(
                            onClick = { MusicPlayer.previous() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = null,
                                tint = AlunaColors.TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(12.dp, CircleShape),
                        shape = CircleShape,
                        color = AlunaColors.Primary
                    ) {
                        IconButton(
                            onClick = { MusicPlayer.togglePlayPause() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = AlunaColors.SurfaceLight.copy(alpha = 0.5f)
                    ) {
                        IconButton(
                            onClick = { MusicPlayer.next() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                tint = AlunaColors.TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = AlunaColors.TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                    )
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = AlunaColors.TextSecondary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}

@Composable 
private fun CategoryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = AlunaColors.Surface
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(colors = gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlunaColors.TextSecondary
                )
            }
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
                .size(140.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AlunaColors.Primary.copy(alpha = 0.2f),
                            AlunaColors.Secondary.copy(alpha = 0.15f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicOff,
                contentDescription = null,
                tint = AlunaColors.TextSecondary,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = AlunaStrings.get("no_music"),
            style = MaterialTheme.typography.titleLarge,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = AlunaStrings.get("add_music_hint"),
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
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isActive) AlunaColors.Primary.copy(alpha = 0.12f) else AlunaColors.Surface,
        tonalElevation = 2.dp,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, AlunaColors.Primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(if (isActive) 8.dp else 4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isActive) 
                                listOf(AlunaColors.Primary, AlunaColors.Secondary)
                            else 
                                listOf(AlunaColors.Primary.copy(alpha = 0.8f), AlunaColors.Secondary.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && isPlaying) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                } else if (isActive) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) AlunaColors.Primary else AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = AlunaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (isActive && isPlaying) {
                    if (AlunaSettings.isVoiceAnimationEnabled) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = AlunaColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AlunaColors.Primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = AlunaStrings.get("now_playing"),
                                style = MaterialTheme.typography.labelSmall,
                                color = AlunaColors.Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = formatTime(track.duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = AlunaColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
