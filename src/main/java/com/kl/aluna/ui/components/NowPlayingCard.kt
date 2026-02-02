package com.kl.aluna.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kl.aluna.data.AlunaStrings
import com.kl.aluna.player.MusicPlayer
import com.kl.aluna.ui.theme.AlunaColors
import kotlinx.coroutines.delay

@Composable
fun NowPlayingCard(onShown: () -> Unit, skipAnimation: Boolean) {
    val currentTrack = MusicPlayer.currentTrack.value
    val isPlaying = MusicPlayer.isPlaying.value
    val currentPos = MusicPlayer.currentPosition.value
    val duration = MusicPlayer.trackDuration.value
    val sleepTimer = MusicPlayer.sleepTimerMinutesLeft.value
    
    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playScale"
    )
    
    var visible by remember { mutableStateOf(skipAnimation) }
    LaunchedEffect(Unit) {
        if (!skipAnimation) {
            delay(150)
            visible = true
            onShown()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)),
        modifier = Modifier.graphicsLayer {
            // Использование RenderNode на старых Android
            clip = true
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp)),
            color = AlunaColors.Surface,
            tonalElevation = 2.dp // Легче чем тень shadow()
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
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(28.dp)
                                }
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
                                AnimatedContent(
                                    targetState = isPlaying,
                                    label = "statusBadge"
                                ) { playing ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (playing) AlunaColors.Primary.copy(alpha = 0.15f) 
                                               else AlunaColors.SurfaceLight.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = if (playing) AlunaStrings.get("now_playing") 
                                                   else if (currentTrack != null) AlunaStrings.get("paused") 
                                                   else "ALUNA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (playing) AlunaColors.Primary else AlunaColors.TextSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
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
                }
            }
        }
    }
}
