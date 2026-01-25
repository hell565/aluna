package com.kl.aluna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    // Автоматическое сканирование при первом входе, если список пустой
    LaunchedEffect(Unit) {
        if (tracks.isEmpty() && !isScanning) {
            viewModel.scan(context)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AlunaColors.Background),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 80.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Верхняя карточка "Сейчас играет"
        item {
            NowPlayingCard()
        }

        // Секция Library
        item {
            SectionHeader(title = "Library")
        }

        item {
            CategoryCard(
                icon = Icons.Outlined.List,
                title = "Playlists",
                subtitle = "Ваши плейлисты",
                onClick = { onCategoryClick("Playlists") }
            )
        }

        item {
            CategoryCard(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Favorites",
                subtitle = "Избранные треки",
                onClick = { onCategoryClick("Favorites") }
            )
        }

        item {
            CategoryCard(
                icon = Icons.Outlined.AccessTime,
                title = "Recent",
                subtitle = "Недавно прослушанное",
                onClick = { onCategoryClick("Recent") }
            )
        }

        // Секция Browse (все треки)
        item {
            SectionHeader(title = "Browse")
        }

        if (isScanning) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AlunaColors.Primary,
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "Сканирование музыки...",
                            color = AlunaColors.TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else if (tracks.isEmpty()) {
            item {
                EmptyMusicState()
            }
        } else {
            itemsIndexed(
                items = tracks,
                key = { _, track -> track.id }
            ) { index, track ->
                BrowseTrackItem(
                    track = track,
                    index = index,
                    onClick = { MusicPlayer.playTrack(track) }
                )
            }
        }
    }
}

// ────────────────────────────────────────────────
// Вспомогательные composable (заглушки + реализация)
// ────────────────────────────────────────────────

@Composable
private fun NowPlayingCard() {
    val currentTrack = MusicPlayer.currentTrack.value
    val isPlaying = MusicPlayer.isPlaying.value

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = AlunaColors.Surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Обложка (заглушка)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AlunaColors.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentTrack?.title ?: "Ничего не играет",
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentTrack?.artist ?: "Выберите трек",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlunaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { MusicPlayer.togglePlayPause() }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Пауза" else "Играть",
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = AlunaColors.TextSecondary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun CategoryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = AlunaColors.Surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AlunaColors.Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.MusicOff,
            contentDescription = null,
            tint = AlunaColors.TextSecondary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Музыка не найдена",
            style = MaterialTheme.typography.titleLarge,
            color = AlunaColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Добавьте треки на устройство или разрешите доступ к хранилищу",
            style = MaterialTheme.typography.bodyLarge,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BrowseTrackItem(
    track: Track,
    index: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = AlunaColors.Surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер трека
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AlunaColors.TextSecondary,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Мини-обложка
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Инфо о треке
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AlunaColors.TextPrimary,
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

            // Длительность
            val durationMin = TimeUnit.MILLISECONDS.toMinutes(track.duration)
            val durationSec = TimeUnit.MILLISECONDS.toSeconds(track.duration) % 60
            Text(
                text = String.format("%02d:%02d", durationMin, durationSec),
                style = MaterialTheme.typography.bodyMedium,
                color = AlunaColors.TextSecondary
            )
        }
    }
}