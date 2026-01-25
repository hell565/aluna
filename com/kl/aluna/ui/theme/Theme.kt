package com.kl.aluna.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AlunaColors.Primary,
    secondary = AlunaColors.Secondary,
    tertiary = AlunaColors.Success,
    background = AlunaColors.Background,
    surface = AlunaColors.Surface,
    surfaceVariant = AlunaColors.SurfaceLight,
    onPrimary = AlunaColors.White,
    onSecondary = AlunaColors.White,
    onTertiary = AlunaColors.White,
    onBackground = AlunaColors.TextPrimary,
    onSurface = AlunaColors.TextPrimary,
    onSurfaceVariant = AlunaColors.TextSecondary,
    outline = AlunaColors.Border
)

private val LightColorScheme = darkColorScheme(
    primary = AlunaColors.Primary,
    secondary = AlunaColors.Secondary,
    tertiary = AlunaColors.Success,
    background = AlunaColors.Background,
    surface = AlunaColors.Surface,
    surfaceVariant = AlunaColors.SurfaceLight,
    onPrimary = AlunaColors.White,
    onSecondary = AlunaColors.White,
    onTertiary = AlunaColors.White,
    onBackground = AlunaColors.TextPrimary,
    onSurface = AlunaColors.TextPrimary,
    onSurfaceVariant = AlunaColors.TextSecondary,
    outline = AlunaColors.Border
)

@Composable
fun AlunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AlunaColors.Background.toArgb()
            window.navigationBarColor = AlunaColors.Surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AlunaTypography,
        content = content
    )
}
