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
    
    LaunchedEffect(Unit) {
        if (allTracks.isEmpty()) {
            musicViewModel.scan(context)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showTrackSelection by remember { mutableStateOf(false) }
    val playlistTracksMap = remember { mutableStateMapOf<Long, List<PlaylistTrackEntity>>() }

    playlists.forEach { playlist ->
        val tracks by db.musicDao().getTracksForPlaylist(playlist.id).collectAsState(initial = emptyList())
        playlistTracksMap[playlist.id] = tracks
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AlunaColors.Background,
                        Color(0xFF0D1230),
                        Color(0xFF080B1A)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = AlunaStrings.get("playlists"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = AlunaColors.TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${playlists.size} ${AlunaStrings.get("your_playlists").lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlunaColors.TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(44.dp),
                        shape = CircleShape,
                        color = AlunaColors.Surface.copy(alpha = 0.8f)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, AlunaStrings.get("back"), tint = AlunaColors.TextPrimary)
                        }
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = AlunaColors.Primary
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showCreateDialog = true }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(AlunaStrings.get("new"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (playlists.isEmpty()) {
                EmptyPlaylistsState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        val tracks by remember(playlist.id) {
                            derivedStateOf { playlistTracksMap[playlist.id] ?: emptyList() }
                        }
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
                    onShufflePlay = {
                        val mappedTracks = tracks.map { pt ->
                            Track(pt.trackId, pt.title, pt.artist, pt.duration, android.net.Uri.parse(pt.uri), 0L)
                        }
                        MusicPlayer.setPlaylistAndPlay(mappedTracks.shuffled())
                    },
                    onDeleteTrack = { id ->
                        scope.launch {
                            db.musicDao().removeTrackFromPlaylistById(id)
                        }
                    },
                    onAddToFavorites = { track ->
                        val t = Track(track.trackId, track.title, track.artist, track.duration, android.net.Uri.parse(track.uri), 0L)
                        MusicPlayer.toggleFavorite(t)
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150, easing = EaseOutQuart), // Замена spring на быстрый tween
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale)
            .graphicsLayer {
                // Оптимизация отрисовки карточки
                clip = true
                shape = RoundedCornerShape(32.dp)
                shadowElevation = 20.dp.toPx()
            }
            .clickable(
                onClick = { 
                    isPressed = true
                    onClick() 
                }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = AlunaColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6),
                                    Color(0xFFD946EF),
                                    Color(0xFFF43F5E)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "$trackCount ${AlunaStrings.get("tracks")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlunaColors.TextSecondary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = AlunaColors.SurfaceLight,
                shape = RoundedCornerShape(20.dp)
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
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
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
    onShufflePlay: () -> Unit,
    onDeleteTrack: (Long) -> Unit,
    onAddToFavorites: (PlaylistTrackEntity) -> Unit
) {
    Scaffold(
        containerColor = AlunaColors.Background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6B4EFF).copy(alpha = 0.3f),
                                AlunaColors.Background
                            )
                        )
                    )
                    .padding(top = 48.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AlunaColors.Surface.copy(alpha = 0.8f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(onClick = onClose) {
                                Icon(Icons.Default.Close, null, tint = AlunaColors.TextPrimary)
                            }
                        }
                        
                        Surface(
                            shape = CircleShape,
                            color = AlunaColors.Primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(onClick = onAddTracks) {
                                Icon(Icons.Default.Add, null, tint = AlunaColors.Primary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(24.dp, RoundedCornerShape(32.dp))
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF8B5CF6),
                                            Color(0xFFD946EF),
                                            Color(0xFFF43F5E)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Column {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = AlunaColors.TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AlunaColors.Primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${tracks.size} ${AlunaStrings.get("tracks")}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AlunaColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    if (tracks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(12.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(onClick = onShufflePlay),
                                color = AlunaColors.Surface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = null,
                                        tint = AlunaColors.Primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = AlunaStrings.get("shuffle"),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = AlunaColors.TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(12.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(onClick = onPlayAll),
                                color = AlunaColors.Primary
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = AlunaStrings.get("play_all"),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (tracks.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        AlunaColors.Primary.copy(alpha = 0.15f),
                                        AlunaColors.Secondary.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote, 
                            null, 
                            modifier = Modifier.size(56.dp), 
                            tint = AlunaColors.TextSecondary.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(
                        AlunaStrings.get("no_tracks_playlist"), 
                        color = AlunaColors.TextSecondary, 
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(20.dp))
                    Surface(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onAddTracks),
                        color = AlunaColors.Primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                AlunaStrings.get("add_some_music"), 
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tracks) { track ->
                    val currentTrack = MusicPlayer.currentTrack.value
                    val isActive = currentTrack?.id == track.trackId
                    val isPlaying = MusicPlayer.isPlaying.value && isActive
                    val isFavorite = MusicPlayer.favorites.contains(track.trackId)
                    
                    TrackListItem(
                        track = track,
                        isActive = isActive,
                        isPlaying = isPlaying,
                        isFavorite = isFavorite,
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
                        },
                        onToggleFavorite = {
                            onAddToFavorites(track)
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
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isActive) 16.dp else 4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onPlay),
        color = if (isActive) AlunaColors.Primary.copy(alpha = 0.15f) else AlunaColors.Surface,
        border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, AlunaColors.Primary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isActive) 
                            Brush.linearGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))
                        else 
                            Brush.linearGradient(colors = listOf(AlunaColors.SurfaceLight, AlunaColors.SurfaceLight.copy(alpha = 0.7f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && isPlaying) {
                    Icon(Icons.Default.Pause, null, tint = Color.White, modifier = Modifier.size(30.dp))
                } else if (isActive) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(30.dp))
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = AlunaColors.TextSecondary, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isActive) AlunaColors.Primary else AlunaColors.TextPrimary,
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
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatDuration(track.duration), 
                    style = MaterialTheme.typography.labelMedium, 
                    color = AlunaColors.TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) AlunaColors.Secondary else AlunaColors.TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                var showItemMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showItemMenu = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = AlunaColors.TextSecondary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showItemMenu,
                        onDismissRequest = { showItemMenu = false },
                        containerColor = AlunaColors.SurfaceLight,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(AlunaStrings.get("play_next"), fontWeight = FontWeight.Medium) },
                            onClick = { showItemMenu = false },
                            leadingIcon = { Icon(Icons.Default.PlaylistAdd, null, tint = AlunaColors.Primary) }
                        )
                        DropdownMenuItem(
                            text = { Text(AlunaStrings.get("add_to_queue"), fontWeight = FontWeight.Medium) },
                            onClick = { showItemMenu = false },
                            leadingIcon = { Icon(Icons.Default.Queue, null, tint = AlunaColors.Primary) }
                        )
                        HorizontalDivider(color = AlunaColors.Border.copy(alpha = 0.3f))
                        DropdownMenuItem(
                            text = { Text(AlunaStrings.get("remove_from_playlist"), fontWeight = FontWeight.Medium, color = AlunaColors.Danger) },
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
        title = { 
            Text(
                AlunaStrings.get("new_playlist"), 
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(AlunaStrings.get("name")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AlunaColors.Primary,
                    cursorColor = AlunaColors.Primary
                )
            )
        },
        confirmButton = {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = name.isNotBlank()) { onCreate(name) },
                color = if (name.isNotBlank()) AlunaColors.Primary else AlunaColors.SurfaceLight
            ) {
                Text(
                    AlunaStrings.get("create"),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Bold,
                    color = if (name.isNotBlank()) Color.White else AlunaColors.TextSecondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text(AlunaStrings.get("cancel"), fontWeight = FontWeight.Medium) 
            }
        },
        containerColor = AlunaColors.Surface,
        shape = RoundedCornerShape(28.dp)
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
                    AlunaStrings.get("select_tracks"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = AlunaColors.TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlunaColors.Primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        "${selectedIds.size} ${AlunaStrings.get("selected")}",
                        style = MaterialTheme.typography.labelLarge,
                        color = AlunaColors.Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
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
                        color = if (isSelected) AlunaColors.Primary.copy(alpha = 0.12f) else AlunaColors.SurfaceLight.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AlunaColors.Primary.copy(alpha = 0.5f)) else null
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) AlunaColors.Primary else AlunaColors.SurfaceLight,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check, 
                                            null, 
                                            tint = Color.White, 
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    track.title, 
                                    style = MaterialTheme.typography.bodyLarge, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AlunaColors.Primary else AlunaColors.TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    track.artist, 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    color = AlunaColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                formatDuration(track.duration), 
                                style = MaterialTheme.typography.labelMedium, 
                                color = AlunaColors.TextSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = selectedIds.isNotEmpty()) { onTracksSelected(selectedIds.toSet()) },
                color = if (selectedIds.isNotEmpty()) AlunaColors.Primary else AlunaColors.SurfaceLight
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "${AlunaStrings.get("add_tracks_to_playlist")} (${selectedIds.size})",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedIds.isNotEmpty()) Color.White else AlunaColors.TextSecondary
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmptyPlaylistsState(onCreateClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
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
                    imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                    contentDescription = null,
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            Text(
                AlunaStrings.get("no_playlists_yet"),
                style = MaterialTheme.typography.headlineSmall,
                color = AlunaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                AlunaStrings.get("create_first_collection"),
                style = MaterialTheme.typography.bodyMedium,
                color = AlunaColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            Surface(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onCreateClick),
                color = AlunaColors.Primary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        AlunaStrings.get("new_playlist"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = ms / 1000 / 60
    val seconds = ms / 1000 % 60
    return String.format("%d:%02d", minutes, seconds)
}
