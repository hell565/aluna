package com.kl.aluna.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import com.kl.aluna.data.AlunaStrings
import com.kl.aluna.data.AlunaSettings

import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.delay
import kotlin.math.abs

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
    
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (tracks.isEmpty() && !isScanning) {
            viewModel.scan(context)
        }
    }
    
    LaunchedEffect(tracks) {
        if (tracks.isNotEmpty()) {
            val filteredTracks = tracks.filter { !MusicPlayer.blacklist.contains(it.id) }
            MusicPlayer.setAllTracks(filteredTracks)
        }
    }

    val displayTracks = tracks.filter { track ->
        !MusicPlayer.blacklist.contains(track.id) &&
        (searchQuery.isEmpty() || 
         track.title.contains(searchQuery, ignoreCase = true) ||
         track.artist.contains(searchQuery, ignoreCase = true))
    }

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
        item { 
            WelcomeHeader() 
        }
        
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                isActive = isSearchActive,
                onActiveChange = { isSearchActive = it }
            )
        }
        
        if (!isSearchActive || searchQuery.isEmpty()) {
            item { 
                NowPlayingCard() 
            }
            item { 
                QuickActionsRow(onCategoryClick = onCategoryClick) 
            }
            item { 
                SectionHeader(title = AlunaStrings.get("library")) 
            }
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
            item { 
                SectionHeader(title = AlunaStrings.get("browse")) 
            }
        }
        
        if (isScanning) {
            item { 
                ScanningState()
            }
        } else if (displayTracks.isEmpty()) {
            item { 
                if (searchQuery.isNotEmpty()) {
                    NoSearchResults(query = searchQuery)
                } else {
                    EmptyMusicState() 
                }
            }
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
                    },
                    onAddToBlacklist = {
                        MusicPlayer.toggleBlacklist(track)
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "searchScale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp)),
        color = AlunaColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AlunaColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = { 
                    onQueryChange(it)
                    onActiveChange(it.isNotEmpty())
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = AlunaColors.TextPrimary,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(AlunaColors.Primary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = AlunaStrings.get("search_tracks"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AlunaColors.TextSecondary
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { 
                        onQueryChange("")
                        onActiveChange(false)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = AlunaColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoSearchResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = AlunaColors.TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = AlunaStrings.get("no_results"),
            style = MaterialTheme.typography.titleLarge,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "\"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )
    
    Surface(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                isPressed = true
                onClick()
            },
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
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
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
    val isBlacklisted = currentTrack?.let { MusicPlayer.blacklist.contains(it.id) } ?: false
    val sleepTimer = MusicPlayer.sleepTimerMinutesLeft.value
    
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPlaying) AlunaColors.Primary.copy(alpha = 0.15f) 
                                       else AlunaColors.SurfaceLight.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (isPlaying) AlunaStrings.get("now_playing") 
                                           else if (currentTrack != null) AlunaStrings.get("paused") 
                                           else "ALUNA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPlaying) AlunaColors.Primary else AlunaColors.TextSecondary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            
                            if (sleepTimer > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AlunaColors.Success.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = AlunaColors.Success,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${sleepTimer}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AlunaColors.Success,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
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
                    val progress = if (duration > 0) currentPos.toFloat() / duration else 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(100),
                        label = "progress"
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(AlunaColors.SurfaceLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
                                    )
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Slider(
                        value = progress,
                        onValueChange = { MusicPlayer.seekTo((it * duration).toLong()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .alpha(0f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Transparent,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        icon = Icons.Default.Speed,
                        isActive = MusicPlayer.playbackSpeed.value != 1.0f,
                        label = "${MusicPlayer.playbackSpeed.value}x",
                        onClick = { showSpeedDialog = true }
                    )
                    
                    ControlButton(
                        icon = Icons.Default.Bedtime,
                        isActive = sleepTimer > 0,
                        onClick = { showSleepDialog = true }
                    )
                    
                    ControlButton(
                        icon = Icons.Default.Repeat,
                        isActive = false,
                        onClick = { }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FavoriteButton(
                        isFavorite = isFav,
                        onClick = { currentTrack?.let { MusicPlayer.toggleFavorite(it) } }
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    PlayerButton(
                        icon = Icons.Default.SkipPrevious,
                        onClick = { MusicPlayer.previous() },
                        size = 56.dp,
                        iconSize = 32.dp
                    )
                    
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
                    
                    PlayerButton(
                        icon = Icons.Default.SkipNext,
                        onClick = { MusicPlayer.next() },
                        size = 56.dp,
                        iconSize = 32.dp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    BlacklistButton(
                        isBlacklisted = isBlacklisted,
                        onClick = { currentTrack?.let { MusicPlayer.toggleBlacklist(it) } }
                    )
                }
            }
        }
    }
    
    if (showSpeedDialog) {
        SpeedControlDialog(
            currentSpeed = MusicPlayer.playbackSpeed.value,
            onSpeedChange = { MusicPlayer.setSpeed(it) },
            onDismiss = { showSpeedDialog = false }
        )
    }
    
    if (showSleepDialog) {
        SleepTimerDialog(
            currentMinutes = sleepTimer,
            onSetTimer = { MusicPlayer.startSleepTimer(it) },
            onDismiss = { showSleepDialog = false }
        )
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    label: String? = null,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "controlScale"
    )
    
    Surface(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                isPressed = true
                onClick()
            },
        color = if (isActive) AlunaColors.Primary.copy(alpha = 0.15f) else AlunaColors.SurfaceLight.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) AlunaColors.Primary else AlunaColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) AlunaColors.Primary else AlunaColors.TextSecondary,
                    fontWeight = FontWeight.Bold
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

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 1.3f
            isFavorite -> 1.1f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favScale"
    )
    
    IconButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isFavorite) AlunaColors.Secondary.copy(alpha = 0.15f) else Color.Transparent
            )
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) AlunaColors.Secondary else AlunaColors.TextSecondary,
            modifier = Modifier.size(26.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun BlacklistButton(
    isBlacklisted: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "blacklistScale"
    )
    
    IconButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isBlacklisted) AlunaColors.Danger.copy(alpha = 0.15f) else Color.Transparent
            )
    ) {
        Icon(
            imageVector = if (isBlacklisted) Icons.Default.Block else Icons.Outlined.Block,
            contentDescription = null,
            tint = if (isBlacklisted) AlunaColors.Danger else AlunaColors.TextSecondary,
            modifier = Modifier.size(26.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun PlayerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "btnScale"
    )
    
    Surface(
        modifier = Modifier
            .size(size)
            .scale(scale),
        shape = CircleShape,
        color = AlunaColors.SurfaceLight.copy(alpha = 0.5f)
    ) {
        IconButton(
            onClick = {
                isPressed = true
                onClick()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AlunaColors.TextPrimary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun SpeedControlDialog(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AlunaColors.Surface,
        title = {
            Text(
                text = AlunaStrings.get("playback_speed"),
                color = AlunaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                speeds.forEach { speed ->
                    val isSelected = abs(currentSpeed - speed) < 0.01f
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSpeedChange(speed)
                                onDismiss()
                            },
                        color = if (isSelected) AlunaColors.Primary.copy(alpha = 0.15f) 
                               else AlunaColors.SurfaceLight
                    ) {
                        Text(
                            text = "${speed}x",
                            modifier = Modifier.padding(16.dp),
                            color = if (isSelected) AlunaColors.Primary else AlunaColors.TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AlunaStrings.get("cancel"), color = AlunaColors.Primary)
            }
        }
    )
}

@Composable
private fun SleepTimerDialog(
    currentMinutes: Int,
    onSetTimer: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val times = listOf(0, 5, 10, 15, 30, 45, 60, 90)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AlunaColors.Surface,
        title = {
            Text(
                text = AlunaStrings.get("sleep_timer"),
                color = AlunaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                times.forEach { minutes ->
                    val isSelected = currentMinutes == minutes || (currentMinutes > 0 && minutes == currentMinutes)
                    val label = if (minutes == 0) AlunaStrings.get("off") else "${minutes} ${AlunaStrings.get("minutes")}"
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSetTimer(minutes)
                                onDismiss()
                            },
                        color = if (isSelected) AlunaColors.Success.copy(alpha = 0.15f) 
                               else AlunaColors.SurfaceLight
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(16.dp),
                            color = if (isSelected) AlunaColors.Success else AlunaColors.TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AlunaStrings.get("cancel"), color = AlunaColors.Primary)
            }
        }
    )
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
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                isPressed = true
                onClick()
            },
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
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun ScanningState() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp), 
        contentAlignment = Alignment.Center
    ) { 
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AlunaColors.Primary,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 4.dp
                )
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
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
    onClick: () -> Unit,
    onAddToBlacklist: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "itemScale"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (animatedOffset < -30) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AlunaColors.Danger.copy(alpha = 0.2f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = AlunaColors.Danger,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = AlunaStrings.get("blacklist"),
                        style = MaterialTheme.typography.labelMedium,
                        color = AlunaColors.Danger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .graphicsLayer { translationX = animatedOffset }
                .clip(RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -100) onAddToBlacklist()
                            offsetX = 0f
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-150f, 0f)
                        }
                    )
                }
                .clickable {
                    isPressed = true
                    onClick()
                },
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
                        AnimatedEqualizer()
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
                    } else {
                        Text(
                            text = formatTime(track.duration),
                            style = MaterialTheme.typography.labelMedium,
                            color = AlunaColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = AlunaColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = AlunaColors.Surface
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                AlunaStrings.get("add_to_blacklist"), 
                                color = AlunaColors.Danger
                            ) 
                        },
                        onClick = {
                            onAddToBlacklist()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                tint = AlunaColors.Danger
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(
                                if (MusicPlayer.favorites.contains(track.id)) 
                                    AlunaStrings.get("remove_favorite")
                                else 
                                    AlunaStrings.get("add_to_favorites"), 
                                color = AlunaColors.TextPrimary
                            ) 
                        },
                        onClick = {
                            MusicPlayer.toggleFavorite(track)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (MusicPlayer.favorites.contains(track.id)) 
                                    Icons.Default.Favorite 
                                else 
                                    Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = AlunaColors.Secondary
                            )
                        }
                    )
                }
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

@Composable
private fun AnimatedEqualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )
    
    Row(
        modifier = Modifier.height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(bar1, bar2, bar3, bar4).forEach { height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}
