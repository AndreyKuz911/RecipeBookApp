package com.example.recipebookapp.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB3471B),
    onPrimary = Color.White,
    secondary = Color(0xFF4A6A57),
    tertiary = Color(0xFF8E4B55),
    background = Color(0xFFFAF5F0),
    surface = Color(0xFFFFFBF8),
    surfaceVariant = Color(0xFFF2E8DF),
    onSurface = Color(0xFF241A14),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB689),
    secondary = Color(0xFFB5D0BC),
    tertiary = Color(0xFFF0B8C2),
    background = Color(0xFF17120F),
    surface = Color(0xFF201915),
    surfaceVariant = Color(0xFF3A2F29),
)

@Composable
fun RecipeBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
