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
    val context = LocalContext.current
    val voiceAssistant = remember { VoiceAssistant(context) }
    var isListening by remember { mutableStateOf(false) }
    val messages = remember { mutableStateListOf<Message>() }

    // Listen to voice assistant events
    DisposableEffect(voiceAssistant) {
        voiceAssistant.setListener(object : VoiceAssistant.VoiceAssistantListener {
            override fun onResult(text: String) {
                messages.add(Message(System.currentTimeMillis().toString(), text, true))
            }
            override fun onError(error: Exception) {
                isListening = false
            }
            override fun onListeningStateChanged(listening: Boolean) {
                isListening = listening
            }
        })
        onDispose {
            voiceAssistant.stopListening()
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    
    // Gradient backgrounds for bubbles
    val userBubbleGradient = Brush.linearGradient(
        colors = listOf(AlunaColors.Primary, AlunaColors.Secondary)
    )
    
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseOutExpo),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseOutExpo, delayMillis = 800),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseOutExpo),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )
    
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseOutExpo, delayMillis = 800),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )
    
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AlunaColors.Background,
                        AlunaColors.Surface.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        if (messages.isEmpty()) {
            EmptyAssistantState(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 80.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 100.dp,
                    bottom = 300.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message = message, gradient = userBubbleGradient)
                }
            }
        }

        // Top Glassmorphism Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(AlunaColors.Background.copy(alpha = 0.7f))
                .blur(10.dp)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isListening) {
                Text(
                    text = "Listening...",
                    style = MaterialTheme.typography.titleMedium,
                    color = AlunaColors.Primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                SoundWaveVisualizer(
                    phase = wavePhase,
                    modifier = Modifier
                        .width(240.dp)
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                )
            }
            
            VoiceButtonNew(
                isListening = isListening,
                ring1Scale = if (isListening) ring1Scale else 1f,
                ring2Scale = if (isListening) ring2Scale else 1f,
                ring3Scale = 1f,
                ring1Alpha = if (isListening) ring1Alpha else 0f,
                ring2Alpha = if (isListening) ring2Alpha else 0f,
                ring3Alpha = 0f,
                onClick = { 
                    isListening = !isListening 
                    if (isListening) {
                        voiceAssistant.startListening()
                    } else {
                        voiceAssistant.stopListening()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Surface(
                shape = CircleShape,
                color = AlunaColors.SurfaceLight.copy(alpha = 0.5f),
                modifier = Modifier.clickable { isListening = !isListening }
            ) {
                Text(
                    text = if (isListening) "STOP" else "TAP TO SPEAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isListening) AlunaColors.Primary else AlunaColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }
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
