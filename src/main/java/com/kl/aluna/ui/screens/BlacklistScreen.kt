package com.kl.aluna.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.kl.aluna.data.AlunaStrings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kl.aluna.player.*
import com.kl.aluna.ui.theme.AlunaColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val musicViewModel: MusicViewModel = viewModel()
    val allTracks = musicViewModel.allTracks
    
    LaunchedEffect(Unit) {
        if (allTracks.isEmpty()) {
            musicViewModel.scan(context)
        }
    }
    
    val blacklistedTracks = allTracks.filter { MusicPlayer.blacklist.contains(it.id) }

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
                            text = AlunaStrings.get("blacklist"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = AlunaColors.TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${blacklistedTracks.size} ${AlunaStrings.get("tracks")}",
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, 
                                AlunaStrings.get("back"), 
                                tint = AlunaColors.TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            if (blacklistedTracks.isEmpty()) {
                EmptyBlacklistState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 20.dp, 
                        end = 20.dp, 
                        top = 16.dp, 
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(
                        blacklistedTracks, 
                        key = { _, track -> track.id }
                    ) { index, track ->
                        BlacklistTrackItem(
                            track = track,
                            index = index,
                            onRemoveFromBlacklist = {
                                MusicPlayer.toggleBlacklist(track)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBlacklistState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                AlunaColors.Danger.copy(alpha = 0.2f),
                                AlunaColors.Danger.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    tint = AlunaColors.TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = AlunaStrings.get("no_blacklist"),
                style = MaterialTheme.typography.titleLarge,
                color = AlunaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = AlunaStrings.get("blacklist_hint"),
                style = MaterialTheme.typography.bodyMedium,
                color = AlunaColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BlacklistTrackItem(
    track: Track,
    index: Int,
    onRemoveFromBlacklist: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isRemoving by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "itemScale"
    )
    
    val removeScale by animateFloatAsState(
        targetValue = if (isRemoving) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "removeScale"
    )

    AnimatedVisibility(
        visible = !isRemoving || removeScale > 1f,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(20.dp)),
            color = AlunaColors.Surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    AlunaColors.Danger.copy(alpha = 0.8f),
                                    AlunaColors.Danger.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AlunaColors.TextPrimary,
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
                
                Surface(
                    modifier = Modifier
                        .scale(removeScale)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            isRemoving = true
                            onRemoveFromBlacklist()
                        },
                    color = AlunaColors.Success.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = AlunaColors.Success,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = AlunaStrings.get("restore"),
                            style = MaterialTheme.typography.labelMedium,
                            color = AlunaColors.Success,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
