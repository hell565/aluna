package com.kl.aluna.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kl.aluna.ui.theme.AlunaColors
import kotlin.math.PI
import kotlin.math.sin

data class Message(
    val id: String,
    val text: String,
    val isUser: Boolean
)

@Composable
fun AssistantScreen(
    modifier: Modifier = Modifier
) {
    var isListening by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Message>() }
    
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    
    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3"
    )
    
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )
    
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )
    
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOut, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Alpha"
    )
    
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AlunaColors.Background)
    ) {
        if (messages.isEmpty()) {
            EmptyAssistantState(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 80.dp,
                    bottom = 280.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isListening) {
                SoundWaveVisualizer(
                    phase = wavePhase,
                    modifier = Modifier
                        .width(200.dp)
                        .height(60.dp)
                        .padding(bottom = 16.dp)
                )
            }
            
            VoiceButtonNew(
                isListening = isListening,
                ring1Scale = if (isListening) ring1Scale else 1f,
                ring2Scale = if (isListening) ring2Scale else 1f,
                ring3Scale = if (isListening) ring3Scale else 1f,
                ring1Alpha = if (isListening) ring1Alpha else 0f,
                ring2Alpha = if (isListening) ring2Alpha else 0f,
                ring3Alpha = if (isListening) ring3Alpha else 0f,
                onClick = { isListening = !isListening }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isListening) "Tap to stop" else "Tap to speak",
                style = MaterialTheme.typography.bodyMedium,
                color = AlunaColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SoundWaveVisualizer(
    phase: Float,
    modifier: Modifier = Modifier
) {
    val primary = AlunaColors.Primary
    val secondary = AlunaColors.Secondary
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val barCount = 20
        val barWidth = width / (barCount * 2)
        val gap = barWidth
        
        for (i in 0 until barCount) {
            val x = i * (barWidth + gap) + barWidth / 2
            val normalizedX = i.toFloat() / barCount
            
            val amplitude = (sin(phase + normalizedX * 4 * PI.toFloat()) * 0.5f + 0.5f)
            val barHeight = height * 0.2f + height * 0.6f * amplitude
            
            val gradient = Brush.verticalGradient(
                colors = listOf(primary, secondary),
                startY = centerY - barHeight / 2,
                endY = centerY + barHeight / 2
            )
            
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, centerY - barHeight / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
            )
        }
    }
}

@Composable
private fun EmptyAssistantState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello!",
            style = MaterialTheme.typography.displaySmall,
            color = AlunaColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "I'm Aluna, your voice assistant",
            style = MaterialTheme.typography.bodyLarge,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        

    }
}

@Composable
private fun SuggestionChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AlunaColors.SurfaceLight
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) AlunaColors.Primary else AlunaColors.Surface,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = AlunaColors.TextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun VoiceButtonNew(
    isListening: Boolean,
    ring1Scale: Float,
    ring2Scale: Float,
    ring3Scale: Float,
    ring1Alpha: Float,
    ring2Alpha: Float,
    ring3Alpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSize = 88.dp
    val ringSize = 88.dp
    
    Box(
        modifier = modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(ringSize * ring3Scale.coerceAtMost(1.6f))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AlunaColors.Primary.copy(alpha = ring3Alpha),
                                AlunaColors.Secondary.copy(alpha = ring3Alpha)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(ringSize * ring2Scale.coerceAtMost(1.6f))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AlunaColors.Primary.copy(alpha = ring2Alpha),
                                AlunaColors.Secondary.copy(alpha = ring2Alpha)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(ringSize * ring1Scale.coerceAtMost(1.6f))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AlunaColors.Primary.copy(alpha = ring1Alpha),
                                AlunaColors.Secondary.copy(alpha = ring1Alpha)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isListening) {
                            listOf(AlunaColors.Secondary, AlunaColors.Primary)
                        } else {
                            listOf(AlunaColors.Primary, AlunaColors.Primary.copy(alpha = 0.8f))
                        }
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop" else "Start",
                tint = AlunaColors.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
