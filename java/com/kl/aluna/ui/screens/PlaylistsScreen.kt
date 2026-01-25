package com.kl.aluna.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val allTracks = viewModel<MusicViewModel>().allTracks

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var showTrackSelection by remember { mutableStateOf(false) }
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }
    var selectedPlaylistName by remember { mutableStateOf("") }
    val tempSelectedTracks = remember { mutableStateListOf<Long>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AlunaColors.Background,
                        AlunaColors.Background.copy(alpha = 0.95f),
                        Color(0xFF0D1230)
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AlunaColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (playlists.isNotEmpty()) {
                        Text(
                            text = "${playlists.size} playlists",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlunaColors.TextSecondary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(AlunaColors.Surface.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AlunaColors.TextPrimary
                    )
                }
            },
            actions = {
                FilledTonalIconButton(
                    onClick = { showCreateDialog = true },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = AlunaColors.Primary.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create playlist",
                        tint = AlunaColors.Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        if (playlists.isEmpty()) {
            EmptyPlaylistsState(
                modifier = Modifier.weight(1f),
                onCreateClick = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        onAddTracks = {
                            selectedPlaylistId = playlist.id
                            selectedPlaylistName = playlist.name
                            tempSelectedTracks.clear()
                            showTrackSelection = true
                        },
                        onDelete = {
                            scope.launch {
                                db.musicDao().deletePlaylist(playlist)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCreateDialog = false
                newPlaylistName = ""
            },
            containerColor = AlunaColors.Surface,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
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
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Create New Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name") },
                    placeholder = { Text("My awesome playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AlunaColors.Primary,
                        unfocusedBorderColor = AlunaColors.Border,
                        focusedLabelColor = AlunaColors.Primary,
                        cursorColor = AlunaColors.Primary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            scope.launch {
                                db.musicDao().insertPlaylist(PlaylistEntity(name = newPlaylistName))
                            }
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    },
                    enabled = newPlaylistName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlunaColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCreateDialog = false
                        newPlaylistName = ""
                    }
                ) {
                    Text("Cancel", color = AlunaColors.TextSecondary)
                }
            }
        )
    }

    if (showTrackSelection) {
        ModalBottomSheet(
            onDismissRequest = { 
                showTrackSelection = false
                tempSelectedTracks.clear()
            },
            containerColor = AlunaColors.Surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AlunaColors.TextSecondary.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add to Playlist",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AlunaColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedPlaylistName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlunaColors.Primary
                        )
                    }
                    
                    if (tempSelectedTracks.isNotEmpty()) {
                        Surface(
                            color = AlunaColors.Primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "${tempSelectedTracks.size} selected",
                                style = MaterialTheme.typography.labelLarge,
                                color = AlunaColors.Primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                HorizontalDivider(
                    color = AlunaColors.Border.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (allTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MusicOff,
                                contentDescription = null,
                                tint = AlunaColors.TextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tracks available",
                                style = MaterialTheme.typography.titleMedium,
                                color = AlunaColors.TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(allTracks, key = { _, track -> track.id }) { index, track ->
                            val isSelected = tempSelectedTracks.contains(track.id)
                            
                            TrackSelectionItem(
                                track = track,
                                index = index,
                                isSelected = isSelected,
                                onToggle = {
                                    if (isSelected) tempSelectedTracks.remove(track.id)
                                    else tempSelectedTracks.add(track.id)
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            showTrackSelection = false
                            tempSelectedTracks.clear()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AlunaColors.TextSecondary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(AlunaColors.Border, AlunaColors.Border)
                            )
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                selectedPlaylistId?.let { pid ->
                                    allTracks.filter { tempSelectedTracks.contains(it.id) }.forEach { track ->
                                        db.musicDao().insertTrackToPlaylist(
                                            PlaylistTrackEntity(
                                                playlistId = pid,
                                                trackId = track.id,
                                                title = track.title,
                                                artist = track.artist,
                                                duration = track.duration,
                                                uri = track.uri.toString()
                                            )
                                        )
                                    }
                                }
                            }
                            showTrackSelection = false
                            tempSelectedTracks.clear()
                        },
                        enabled = tempSelectedTracks.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlunaColors.Primary,
                            disabledContainerColor = AlunaColors.Primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (tempSelectedTracks.isEmpty()) "Add Tracks" else "Add ${tempSelectedTracks.size} Tracks",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: PlaylistEntity,
    onAddTracks: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onAddTracks),
        color = AlunaColors.Surface,
        tonalElevation = 4.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
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
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to add tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = AlunaColors.TextSecondary
                )
            }
            
            IconButton(onClick = onAddTracks) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add tracks",
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = AlunaColors.TextSecondary
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = AlunaColors.SurfaceLight
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = AlunaColors.Danger,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Delete playlist", color = AlunaColors.Danger)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackSelectionItem(
    track: Track,
    index: Int,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle),
        color = if (isSelected) AlunaColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AlunaColors.Primary,
                    uncheckedColor = AlunaColors.TextSecondary.copy(alpha = 0.5f),
                    checkmarkColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isSelected)
                                listOf(AlunaColors.Primary, AlunaColors.Secondary)
                            else
                                listOf(AlunaColors.SurfaceLight, AlunaColors.SurfaceLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AlunaColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) AlunaColors.Primary else AlunaColors.TextPrimary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
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
            
            Text(
                text = formatDuration(track.duration),
                style = MaterialTheme.typography.bodySmall,
                color = AlunaColors.TextSecondary
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val minutes = ms / 1000 / 60
    val seconds = ms / 1000 % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
private fun EmptyPlaylistsState(
    modifier: Modifier = Modifier,
    onCreateClick: () -> Unit
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
                .size(140.dp)
                .clip(RoundedCornerShape(32.dp))
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
                imageVector = Icons.AutoMirrored.Outlined.QueueMusic,
                contentDescription = null,
                tint = AlunaColors.Primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Playlists Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create your first playlist and\nstart organizing your music",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AlunaColors.Primary
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
