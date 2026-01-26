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
import com.kl.aluna.data.AlunaStrings
import com.kl.aluna.data.AlunaSettings

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
    
    // ВАЖНО: Принудительное сканирование если список пуст
    LaunchedEffect(Unit) {
        if (allTracks.isEmpty()) {
            musicViewModel.scan(context)
        }
    }

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
                        text = AlunaStrings.get("playlists"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, AlunaStrings.get("back"), tint = AlunaColors.TextPrimary)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AlunaColors.Primary.copy(alpha = 0.15f),
                            contentColor = AlunaColors.Primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(AlunaStrings.get("new"), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (playlists.isEmpty()) {
                EmptyPlaylistsState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
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
                    onDeleteTrack = { id ->
                        scope.launch {
                            db.musicDao().removeTrackFromPlaylistById(id)
                        }
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
            .aspectRatio(0.85f)
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AlunaColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
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
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$trackCount ${AlunaStrings.get("tracks")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlunaColors.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
            ) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = AlunaColors.SurfaceLight,
                shape = RoundedCornerShape(16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(AlunaStrings.get("delete"), color = AlunaColors.Danger, fontWeight = FontWeight.Bold) },
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
                        Text("${tracks.size} ${AlunaStrings.get("tracks")}", style = MaterialTheme.typography.bodySmall, color = AlunaColors.TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null, tint = AlunaColors.TextPrimary)
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
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(AlunaStrings.get("play_all"), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        if (tracks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(80.dp), tint = AlunaColors.TextSecondary.copy(alpha = 0.2f))
                    Spacer(Modifier.height(24.dp))
                    Text(AlunaStrings.get("no_tracks_playlist"), color = AlunaColors.TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onAddTracks,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AlunaColors.Primary.copy(alpha = 0.1f), contentColor = AlunaColors.Primary)
                    ) { 
                        Text(AlunaStrings.get("add_some_music"), fontWeight = FontWeight.Bold) 
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tracks) { track ->
                    val currentTrack = MusicPlayer.currentTrack.value
                    val isActive = currentTrack?.id == track.trackId
                    val isPlaying = MusicPlayer.isPlaying.value && isActive
                    
                    TrackListItem(
                        track = track,
                        isActive = isActive,
                        isPlaying = isPlaying,
                        onPlay = {
                            val t = Track(track.trackId, track.title, track.artist, track.duration, android.net.Uri.parse(track.uri), 0L)
                            if (isActive) {
                                MusicPlayer.togglePlayPause()
                            } else {
                                MusicPlayer.playTrack(t)
                            }
                        },
                        onRemove = {
                            onDeleteTrack(track.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackListItem(
    track: PlaylistTrackEntity, 
    isActive: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onPlay),
        color = if (isActive) AlunaColors.Primary.copy(alpha = 0.12f) else AlunaColors.Surface,
        tonalElevation = if (isActive) 12.dp else 2.dp,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, AlunaColors.Primary.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isActive) 
                            Brush.linearGradient(colors = listOf(AlunaColors.Primary, AlunaColors.Secondary))
                        else 
                            Brush.linearGradient(colors = listOf(AlunaColors.SurfaceLight, AlunaColors.SurfaceLight.copy(alpha = 0.8f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && isPlaying) {
                    Icon(Icons.Default.Pause, null, tint = Color.White, modifier = Modifier.size(32.dp))
                } else if (isActive) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = AlunaColors.TextSecondary, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(Modifier.width(18.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isActive) AlunaColors.Primary else AlunaColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = AlunaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDuration(track.duration), 
                    style = MaterialTheme.typography.labelMedium, 
                    color = AlunaColors.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.width(8.dp))
                
                var showItemMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showItemMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = AlunaColors.TextSecondary.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                    }
                    DropdownMenu(
                        expanded = showItemMenu,
                        onDismissRequest = { showItemMenu = false },
                        containerColor = AlunaColors.SurfaceLight,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(AlunaStrings.get("remove_from_playlist"), fontWeight = FontWeight.Medium) },
                            onClick = { 
                                showItemMenu = false
                                onRemove() 
                            },
                            leadingIcon = { Icon(Icons.Default.PlaylistRemove, null, tint = AlunaColors.Danger) }
                        )
                    }
                }
            }
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AlunaColors.Surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AlunaColors.TextSecondary.copy(alpha = 0.5f)) }
    ) {
        Column(
            Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Tracks",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AlunaColors.TextPrimary
                )
                Text(
                    "${selectedIds.size} selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(20.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allTracks) { track ->
                    val isSelected = selectedIds.contains(track.id)
                    Surface(
                        onClick = {
                            if (isSelected) selectedIds.remove(track.id) else selectedIds.add(track.id)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AlunaColors.Primary.copy(alpha = 0.12f) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AlunaColors.Primary.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) AlunaColors.Primary 
                                        else AlunaColors.SurfaceLight
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else AlunaColors.TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(Modifier.weight(1f)) {
                                Text(
                                    track.title, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AlunaColors.Primary else AlunaColors.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    track.artist, 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = AlunaColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AlunaColors.Primary,
                                    uncheckedColor = AlunaColors.TextSecondary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = { onTracksSelected(selectedIds.toSet()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .height(56.dp),
                enabled = selectedIds.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlunaColors.Primary,
                    disabledContainerColor = AlunaColors.SurfaceLight
                )
            ) {
                Text(
                    "Add ${selectedIds.size} Tracks to Playlist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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
