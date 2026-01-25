package com.kl.aluna.ui.screens

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
import java.util.concurrent.TimeUnit

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
        modifier = modifier.fillMaxSize().background(AlunaColors.Background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 80.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { NowPlayingCard() }
        item { SectionHeader(title = "Library") }
        item { CategoryCard(Icons.AutoMirrored.Outlined.List, "Playlists", "Ваши плейлисты", { onCategoryClick("Playlists") }) }
        item { CategoryCard(Icons.Outlined.FavoriteBorder, "Favorites", "${MusicPlayer.favorites.size} tracks", { onCategoryClick("Favorites") }) }
        item { CategoryCard(Icons.Outlined.AccessTime, "Recent", "${MusicPlayer.recentTracks.size} tracks", { onCategoryClick("Recent") }) }
        item { SectionHeader(title = "Browse") }
        
        if (isScanning) {
            item { Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) { CircularProgressIndicator(color = AlunaColors.Primary) } }
        } else if (displayTracks.isEmpty()) {
            item { EmptyMusicState() }
        } else {
            itemsIndexed(displayTracks, key = { _, track -> track.id }) { index, track ->
                BrowseTrackItem(track, index, currentTrack?.id == track.id) { MusicPlayer.playTrack(track) }
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

    Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)), color = AlunaColors.Surface, tonalElevation = 8.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(AlunaColors.Primary, AlunaColors.Secondary))), Alignment.Center) {
                    Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(currentTrack?.title ?: "Ничего не играет", style = MaterialTheme.typography.titleLarge, color = AlunaColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currentTrack?.artist ?: "Выберите трек", style = MaterialTheme.typography.bodyMedium, color = AlunaColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(20.dp))
            Slider(value = if (duration > 0) currentPos.toFloat() / duration else 0f, onValueChange = { MusicPlayer.seekTo((it * duration).toLong()) }, colors = SliderDefaults.colors(thumbColor = AlunaColors.Primary, activeTrackColor = AlunaColors.Primary))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(formatTime(currentPos), style = MaterialTheme.typography.labelSmall, color = AlunaColors.TextSecondary)
                Text(formatTime(duration), style = MaterialTheme.typography.labelSmall, color = AlunaColors.TextSecondary)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                IconButton(onClick = { currentTrack?.let { MusicPlayer.toggleFavorite(it) } }) {
                    Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = AlunaColors.Primary)
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = { MusicPlayer.previous() }, Modifier.size(48.dp)) { Icon(Icons.Default.SkipPrevious, null, tint = AlunaColors.TextPrimary, modifier = Modifier.size(32.dp)) }
                Spacer(Modifier.width(24.dp))
                FilledIconButton(onClick = { MusicPlayer.togglePlayPause() }, Modifier.size(64.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = AlunaColors.Primary)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(24.dp))
                IconButton(onClick = { MusicPlayer.next() }, Modifier.size(48.dp)) { Icon(Icons.Default.SkipNext, null, tint = AlunaColors.TextPrimary, modifier = Modifier.size(32.dp)) }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = { /* TODO: Quick add to playlist */ }) { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, tint = AlunaColors.Primary) }
            }
        }
    }
}

private fun formatTime(ms: Long): String = String.format("%02d:%02d", ms/1000/60, ms/1000%60)

@Composable private fun SectionHeader(title: String) = Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = AlunaColors.TextSecondary, modifier = Modifier.padding(vertical = 8.dp))

@Composable private fun CategoryCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick), color = AlunaColors.Surface, tonalElevation = 2.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(AlunaColors.Primary.copy(0.12f)), Alignment.Center) { Icon(icon, null, tint = AlunaColors.Primary, modifier = Modifier.size(28.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = AlunaColors.TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AlunaColors.TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = AlunaColors.TextSecondary)
        }
    }
}

@Composable private fun EmptyMusicState() = Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.MusicOff, null, tint = AlunaColors.TextSecondary, modifier = Modifier.size(80.dp)); Text("Музыка не найдена") }

@Composable private fun BrowseTrackItem(track: Track, index: Int, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick), 
        color = if (isActive) AlunaColors.Primary.copy(0.1f) else AlunaColors.Surface, 
        tonalElevation = if (isActive) 4.dp else 1.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isActive) Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = AlunaColors.Primary, modifier = Modifier.width(32.dp))
            else Text("${index + 1}", Modifier.width(32.dp), textAlign = TextAlign.Center)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(AlunaColors.Primary, AlunaColors.Secondary))), Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Color.White) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.bodyLarge, color = if (isActive) AlunaColors.Primary else AlunaColors.TextPrimary, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
                Text(track.artist, style = MaterialTheme.typography.bodyMedium, color = AlunaColors.TextSecondary)
            }
            Text(formatTime(track.duration))
        }
    }
}
