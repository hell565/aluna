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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kl.aluna.data.AlunaSettings
import com.kl.aluna.data.AlunaStrings
import com.kl.aluna.ui.theme.AlunaColors
import com.kl.aluna.player.MusicPlayer


@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp, start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = AlunaStrings.get("settings"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = AlunaColors.TextPrimary
                )
            }
        },
        containerColor = AlunaColors.Background
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection(title = AlunaStrings.get("appearance")) {
                    SettingsItem(
                        icon = Icons.Outlined.DarkMode,
                        title = AlunaStrings.get("dark_theme"),
                        subtitle = AlunaStrings.get("dark_theme_sub"),
                        trailing = {
                            Switch(
                                checked = AlunaSettings.isDarkTheme,
                                onCheckedChange = { AlunaSettings.isDarkTheme = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AlunaColors.Primary
                                )
                            )
                        }
                    )
                    HorizontalDivider(color = AlunaColors.Border.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        title = AlunaStrings.get("language"),
                        subtitle = AlunaSettings.getLanguageName(),
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = AlunaColors.Border.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Waves,
                        title = AlunaStrings.get("voice_anim"),
                        subtitle = AlunaStrings.get("voice_anim_sub"),
                        trailing = {
                            Switch(
                                checked = AlunaSettings.isVoiceAnimationEnabled,
                                onCheckedChange = { AlunaSettings.isVoiceAnimationEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AlunaColors.Primary
                                )
                            )
                        }
                    )
                    HorizontalDivider(color = AlunaColors.Border.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Timer,
                        title = AlunaStrings.get("sleep_timer"),
                        subtitle = if (MusicPlayer.sleepTimerMinutesLeft.value > 0) 
                            "${MusicPlayer.sleepTimerMinutesLeft.value} ${AlunaStrings.get("minutes")}" 
                            else AlunaStrings.get("off"),
                        onClick = { showSleepTimerDialog = true }
                    )
                }
            }

            item {
                SettingsSection(title = AlunaStrings.get("music")) {
                    SettingsItem(
                        icon = Icons.Outlined.CompareArrows,
                        title = AlunaStrings.get("gapless"),
                        subtitle = AlunaStrings.get("gapless_sub"),
                        trailing = {
                            Switch(
                                checked = AlunaSettings.isGaplessPlaybackEnabled,
                                onCheckedChange = { AlunaSettings.isGaplessPlaybackEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AlunaColors.Primary
                                )
                            )
                        }
                    )
                    if (AlunaSettings.isGaplessPlaybackEnabled) {
                        HorizontalDivider(color = AlunaColors.Border.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Outlined.Timer,
                            title = AlunaStrings.get("fade_duration"),
                            subtitle = "${AlunaSettings.crossfadeDurationSeconds} ${AlunaStrings.get("seconds")}",
                            trailing = {
                                Slider(
                                    value = AlunaSettings.crossfadeDurationSeconds.toFloat(),
                                    onValueChange = { AlunaSettings.crossfadeDurationSeconds = it.toInt() },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    modifier = Modifier.width(120.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = AlunaColors.Primary,
                                        activeTrackColor = AlunaColors.Primary
                                    )
                                )
                            }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = AlunaStrings.get("about")) {
                    SettingsItem(
                        icon = Icons.Outlined.Info,
                        title = AlunaStrings.get("version"),
                        subtitle = "1.1.0"
                    )
                    HorizontalDivider(color = AlunaColors.Border.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsItem(
                        icon = Icons.Outlined.Shield,
                        title = AlunaStrings.get("privacy"),
                        onClick = { }
                    )
                }
            }

            item {
                Footer()
            }
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { showSleepTimerDialog = false },
            onSelect = { minutes ->
                MusicPlayer.startSleepTimer(minutes)
                showSleepTimerDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onSelect = { lang ->
                AlunaSettings.currentLanguage = lang
                showLanguageDialog = false
            }
        )
    }
}

@Composable
private fun SleepTimerDialog(onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AlunaStrings.get("sleep_timer"), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                val options = listOf(
                    0 to AlunaStrings.get("off"),
                    5 to "5 ${AlunaStrings.get("minutes")}",
                    15 to "15 ${AlunaStrings.get("minutes")}",
                    30 to "30 ${AlunaStrings.get("minutes")}",
                    60 to "60 ${AlunaStrings.get("minutes")}"
                )
                options.forEach { (mins, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mins) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = MusicPlayer.sleepTimerMinutesLeft.value == mins, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(AlunaStrings.get("close")) } },
        containerColor = AlunaColors.Surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun LanguageDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AlunaStrings.get("language"), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                val langs = listOf("en" to "English", "ru" to "Русский", "be" to "Беларуская", "tg" to "Тоҷикӣ")
                langs.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = AlunaSettings.currentLanguage == code, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(AlunaStrings.get("close")) } },
        containerColor = AlunaColors.Surface,
        shape = RoundedCornerShape(28.dp)
    )
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
            style = MaterialTheme.typography.labelSmall,
            color = AlunaColors.Primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AlunaColors.Surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDanger) AlunaColors.Danger.copy(alpha = 0.1f) else AlunaColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDanger) AlunaColors.Danger else AlunaColors.Primary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) AlunaColors.Danger else AlunaColors.TextPrimary
            )
            if (subtitle != null) {
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
                tint = AlunaColors.TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun Footer() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aluna",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = AlunaColors.Primary.copy(alpha = 0.5f)
        )
        Text(
            text = AlunaStrings.get("footer_sub"),
            style = MaterialTheme.typography.bodySmall,
            color = AlunaColors.TextSecondary.copy(alpha = 0.5f)
        )
    }
}
