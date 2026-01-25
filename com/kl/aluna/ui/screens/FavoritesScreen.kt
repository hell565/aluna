package com.kl.aluna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kl.aluna.ui.theme.AlunaColors
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kl.aluna.player.MusicViewModel

data class FavoriteTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: MusicViewModel = viewModel()   // ← вот так правильно
    val tracks = viewModel.allTracks
    val isScanning = viewModel.isScanning.value

    val favorites = remember { mutableStateListOf<FavoriteTrack>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AlunaColors.Background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.titleLarge,
                    color = AlunaColors.TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AlunaColors.TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AlunaColors.Background
            )
        )

        if (favorites.isEmpty()) {
            EmptyFavoritesState(
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(favorites) { index, track ->
                    FavoriteTrackItem(
                        track = track,
                        index = index,
                        onRemove = { favorites.remove(track) },
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteTrackItem(
    track: FavoriteTrack,
    index: Int,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AlunaColors.Surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodyMedium,
                color = AlunaColors.TextSecondary,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AlunaColors.Secondary, AlunaColors.Primary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AlunaColors.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AlunaColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlunaColors.TextSecondary
                )
            }

            Text(
                text = track.duration,
                style = MaterialTheme.typography.bodySmall,
                color = AlunaColors.TextSecondary
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onRemove) {
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
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AlunaColors.SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = AlunaColors.Secondary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No favorites yet",
            style = MaterialTheme.typography.titleMedium,
            color = AlunaColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap the heart icon on any song to add it to your favorites",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
