package com.kl.aluna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kl.aluna.ui.theme.AlunaColors

data class RecentTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val playedAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val recentTracks = remember { mutableStateListOf<RecentTrack>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AlunaColors.Background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Recently Played",
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
            actions = {
                if (recentTracks.isNotEmpty()) {
                    TextButton(onClick = { recentTracks.clear() }) {
                        Text("Clear", color = AlunaColors.TextSecondary)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AlunaColors.Background
            )
        )

        if (recentTracks.isEmpty()) {
            EmptyRecentState(
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(recentTracks) { index, track ->
                    RecentTrackItem(
                        track = track,
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTrackItem(
    track: RecentTrack,
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
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlunaColors.TextSecondary
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AlunaColors.TextSecondary
                    )
                    Text(
                        text = track.playedAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = AlunaColors.TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = track.duration,
                style = MaterialTheme.typography.bodySmall,
                color = AlunaColors.TextSecondary
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
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
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AlunaColors.SurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = AlunaColors.Primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No recent plays",
            style = MaterialTheme.typography.titleMedium,
            color = AlunaColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start listening to music and your history will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
