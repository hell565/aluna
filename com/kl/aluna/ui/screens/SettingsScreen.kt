package com.kl.aluna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kl.aluna.ui.theme.AlunaColors

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var darkTheme by remember { mutableStateOf(true) }
    var voiceAnimation by remember { mutableStateOf(true) }
    var autoPlay by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AlunaColors.Background),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 80.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Enable dark mode",
                    trailing = {
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { darkTheme = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AlunaColors.White,
                                checkedTrackColor = AlunaColors.Primary,
                                uncheckedThumbColor = AlunaColors.White,
                                uncheckedTrackColor = AlunaColors.Border
                            )
                        )
                    }
                )
                HorizontalDivider(color = AlunaColors.Border, thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Outlined.Waves,
                    title = "Voice Animation",
                    subtitle = "Pulsing effect when listening",
                    trailing = {
                        Switch(
                            checked = voiceAnimation,
                            onCheckedChange = { voiceAnimation = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AlunaColors.White,
                                checkedTrackColor = AlunaColors.Primary,
                                uncheckedThumbColor = AlunaColors.White,
                                uncheckedTrackColor = AlunaColors.Border
                            )
                        )
                    }
                )
            }
        }
        
        item {
            SettingsSection(title = "Voice") {
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = "Language",
                    subtitle = "English (US)",
                    onClick = { }
                )
            }
        }
        
        item {
            SettingsSection(title = "Music") {
                SettingsItem(
                    icon = Icons.Outlined.PlayCircle,
                    title = "Auto-play",
                    subtitle = "Play next track automatically",
                    trailing = {
                        Switch(
                            checked = autoPlay,
                            onCheckedChange = { autoPlay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AlunaColors.White,
                                checkedTrackColor = AlunaColors.Primary,
                                uncheckedThumbColor = AlunaColors.White,
                                uncheckedTrackColor = AlunaColors.Border
                            )
                        )
                    }
                )
            }
        }
        
        item {
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.0"
                )
                HorizontalDivider(color = AlunaColors.Border, thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Outlined.Shield,
                    title = "Privacy Policy",
                    onClick = { }
                )
            }
        }
        
        item {
            Footer()
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = AlunaColors.TextSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AlunaColors.Surface
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isDanger: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDanger) {
                        AlunaColors.Danger.copy(alpha = 0.15f)
                    } else {
                        AlunaColors.SurfaceLight
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDanger) AlunaColors.Danger else AlunaColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDanger) AlunaColors.Danger else AlunaColors.TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlunaColors.TextSecondary
                )
            }
        }
        
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AlunaColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun Footer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aluna v1.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = AlunaColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your cosmic voice assistant",
            style = MaterialTheme.typography.bodySmall,
            color = AlunaColors.TextSecondary.copy(alpha = 0.7f)
        )
    }
}
