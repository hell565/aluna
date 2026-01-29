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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kl.aluna.ui.theme.AlunaColors
import com.kl.aluna.voice.VoiceAssistant
import kotlin.math.PI

import androidx.compose.material.icons.filled.MicOff
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
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MicOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AlunaColors.Primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "voice_disabled",
                style = MaterialTheme.typography.titleMedium,
                color = AlunaColors.TextPrimary.copy(alpha = 0.6f)
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
        
        val barCount = 30
        val barWidth = width / (barCount * 1.8f)
        val gap = barWidth * 0.8f
        
        for (i in 0 until barCount) {
            val x = i * (barWidth + gap)
            val normalizedX = i.toFloat() / barCount
            
            val amplitude = (sin(phase + normalizedX * 6 * PI.toFloat()) * 0.5f + 0.5f)
            val barHeight = height * 0.1f + height * 0.8f * amplitude
            
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
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = AlunaColors.Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = AlunaColors.Primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "How can I help you?",
            style = MaterialTheme.typography.headlineMedium,
            color = AlunaColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Try saying \"Play music\" or \"Next track\"",
            style = MaterialTheme.typography.bodyLarge,
            color = AlunaColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 20.dp
            ),
            color = Color.Transparent,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .then(
                    if (message.isUser) Modifier.background(gradient) 
                    else Modifier.background(AlunaColors.Surface)
                )
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (message.isUser) AlunaColors.White else AlunaColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
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
                    .size(ringSize * ring2Scale.coerceAtMost(2.2f))
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
                    .size(ringSize * ring1Scale.coerceAtMost(2.2f))
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
