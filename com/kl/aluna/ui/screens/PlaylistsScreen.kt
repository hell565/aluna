package com.kl.aluna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kl.aluna.ui.theme.AlunaColors

data class Playlist(
    val id: String,
    val name: String,
    val trackCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playlists = remember { mutableStateListOf<Playlist>() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AlunaColors.Background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Playlists",
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
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create playlist",
                        tint = AlunaColors.Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AlunaColors.Background
            )
        )

        if (playlists.isEmpty()) {
            EmptyPlaylistsState(
                onCreateClick = { showCreateDialog = true },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Create Playlist",
                    color = AlunaColors.TextPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AlunaColors.Primary,
                        unfocusedBorderColor = AlunaColors.Border,
                        focusedLabelColor = AlunaColors.Primary,
                        unfocusedLabelColor = AlunaColors.TextSecondary,
                        cursorColor = AlunaColors.Primary,
                        focusedTextColor = AlunaColors.TextPrimary,
                        unfocusedTextColor = AlunaColors.TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            playlists.add(
                                Playlist(
                                    id = System.currentTimeMillis().toString(),
                                    name = newPlaylistName,
                                    trackCount = 0
                                )
                            )
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create", color = AlunaColors.Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = AlunaColors.TextSecondary)
                }
            },
            containerColor = AlunaColors.Surface
        )
    }
}

@Composable
private fun PlaylistItem(
    playlist: Playlist,
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QueueMusic,
                    contentDescription = null,
                    tint = AlunaColors.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${playlist.trackCount} tracks",
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
private fun EmptyPlaylistsState(
    onCreateClick: () -> Unit,
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
                imageVector = Icons.Outlined.QueueMusic,
                contentDescription = null,
                tint = AlunaColors.Primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No playlists yet",
            style = MaterialTheme.typography.titleMedium,
            color = AlunaColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first playlist to organize your music",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AlunaColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}
