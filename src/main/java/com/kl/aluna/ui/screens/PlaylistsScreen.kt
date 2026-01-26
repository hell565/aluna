package com.kl.aluna.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kl.aluna.data.db.*
import com.kl.aluna.player.*
import com.kl.aluna.ui.theme.AlunaColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val scope = rememberCoroutineScope()
    val playlists by db.musicDao().getAllPlaylists().collectAsState(initial = emptyList())
    val musicViewModel: MusicViewModel = viewModel()
    val allTracks = musicViewModel.allTracks

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showTrackSelection by remember { mutableStateOf(false) }
    val playlistTracksMap = remember { mutableStateMapOf<Long, List<PlaylistTrackEntity>>() }

    // Observe tracks for each playlist
    playlists.forEach { playlist ->
        val tracks by db.musicDao().getTracksForPlaylist(playlist.id).collectAsState(initial = emptyList())
        playlistTracksMap[playlist.id] = tracks
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlunaColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Playlists",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AlunaColors.TextPrimary)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AlunaColors.Primary.copy(alpha = 0.2f),
                            contentColor = AlunaColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("New", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (playlists.isEmpty()) {
                EmptyPlaylistsState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(playlists) { playlist ->
                        val tracks = playlistTracksMap[playlist.id] ?: emptyList()
                        PlaylistGridItem(
                            playlist = playlist,
                            trackCount = tracks.size,
                            onClick = { selectedPlaylist = playlist },
                            onDelete = {
                                scope.launch { db.musicDao().deletePlaylist(playlist) }
                            }
                        )
                    }
                }
            }
        }

        // Playlist Detail View (Overlay)
        AnimatedVisibility(
            visible = selectedPlaylist != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedPlaylist?.let { playlist ->
                val tracks = playlistTracksMap[playlist.id] ?: emptyList()
                PlaylistDetailView(
                    playlist = playlist,
                    tracks = tracks,
                    onClose = { selectedPlaylist = null },
                    onAddTracks = { showTrackSelection = true },
                    onPlayAll = {
                        val mappedTracks = tracks.map { pt ->
                            Track(pt.trackId, pt.title, pt.artist, pt.duration, android.net.Uri.parse(pt.uri), 0L)
                        }
                        MusicPlayer.setPlaylistAndPlay(mappedTracks)
                    },
                    onDeleteTrack = { trackId ->
                        // Add deletion logic to DAO if needed, for now we can filter out or add a specific delete query
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                scope.launch { db.musicDao().insertPlaylist(PlaylistEntity(name = name)) }
                showCreateDialog = false
            }
        )
    }

    if (showTrackSelection && selectedPlaylist != null) {
        val playlist = selectedPlaylist!!
        TrackSelectionBottomSheet(
            allTracks = allTracks,
            onDismiss = { showTrackSelection = false },
            onTracksSelected = { selectedIds ->
                scope.launch {
                    val tracksToAdd = allTracks.filter { selectedIds.contains(it.id) }
                    tracksToAdd.forEach { track ->
                        db.musicDao().insertTrackToPlaylist(
                            PlaylistTrackEntity(
                                playlistId = playlist.id,
                                trackId = track.id,
                                title = track.title,
                                artist = track.artist,
                                duration = track.duration,
                                uri = track.uri.toString()
                            )
                        )
                    }
                    showTrackSelection = false
                }
            }
        )
    }
}

@Composable
fun PlaylistGridItem(
    playlist: PlaylistEntity,
    trackCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AlunaColors.Surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$trackCount tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlunaColors.TextSecondary
                )
            }

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = AlunaColors.SurfaceLight
            ) {
                DropdownMenuItem(
                    text = { Text("Delete", color = AlunaColors.Danger) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = AlunaColors.Danger) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailView(
    playlist: PlaylistEntity,
    tracks: List<PlaylistTrackEntity>,
    onClose: () -> Unit,
    onAddTracks: () -> Unit,
    onPlayAll: () -> Unit,
    onDeleteTrack: (Long) -> Unit
) {
    Scaffold(
        containerColor = AlunaColors.Background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(playlist.name, fontWeight = FontWeight.ExtraBold)
                        Text("${tracks.size} songs", style = MaterialTheme.typography.bodySmall, color = AlunaColors.TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                actions = {
                    IconButton(onClick = onAddTracks) {
                        Icon(Icons.Default.AddCircleOutline, null, tint = AlunaColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (tracks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onPlayAll,
                    containerColor = AlunaColors.Primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play All")
                }
            }
        }
    ) { padding ->
        if (tracks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp), tint = AlunaColors.TextSecondary.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No tracks in this playlist", color = AlunaColors.TextSecondary)
                    TextButton(onClick = onAddTracks) { Text("Add some music") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tracks) { track ->
                    TrackListItem(
                        track = track,
                        onPlay = {
                            val t = Track(track.trackId, track.title, track.artist, track.duration, android.net.Uri.parse(track.uri), 0L)
                            MusicPlayer.playTrack(t)
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TrackListItem(track: PlaylistTrackEntity, onPlay: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onPlay),
        color = AlunaColors.Surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(AlunaColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, null, tint = AlunaColors.Primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(track.artist, style = MaterialTheme.typography.bodySmall, color = AlunaColors.TextSecondary)
            }
            Text(formatDuration(track.duration), style = MaterialTheme.typography.labelSmall, color = AlunaColors.TextSecondary)
        }
    }
}

@Composable
fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onCreate(name) }, enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = AlunaColors.Surface,
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSelectionBottomSheet(
    allTracks: List<Track>,
    onDismiss: () -> Unit,
    onTracksSelected: (Set<Long>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<Long>() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AlunaColors.Surface) {
        Column(Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
            Text("Select Tracks", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(allTracks) { track ->
                    val isSelected = selectedIds.contains(track.id)
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (isSelected) selectedIds.remove(track.id) else selectedIds.add(track.id)
                        }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(track.title, fontWeight = FontWeight.Medium)
                            Text(track.artist, style = MaterialTheme.typography.bodySmall, color = AlunaColors.TextSecondary)
                        }
                    }
                }
            }
            Button(
                onClick = { onTracksSelected(selectedIds.toSet()) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                enabled = selectedIds.isNotEmpty()
            ) {
                Text("Add ${selectedIds.size} tracks")
            }
        }
    }
}

@Composable
private fun EmptyPlaylistsState(onCreateClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.AutoMirrored.Outlined.QueueMusic, null, Modifier.size(100.dp), tint = AlunaColors.TextSecondary.copy(alpha = 0.2f))
        Spacer(Modifier.height(24.dp))
        Text("No playlists yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Create your first collection of songs", color = AlunaColors.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onCreateClick, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = ms / 1000 / 60
    val seconds = ms / 1000 % 60
    return String.format("%d:%02d", minutes, seconds)
}
