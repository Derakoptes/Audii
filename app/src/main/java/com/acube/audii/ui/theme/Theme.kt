package com.acube.audii.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB0B0B0), // Light grey
    secondary = Color(0xFF8A8A8A), // Medium grey
    tertiary = Color(0xFF5A5A5A), // Dark grey
    background = Color(0xFF121212), // Almost black
    surface = Color(0xFF1E1E1E), // Dark grey surface
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)


private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF8A8A8A), // Medium grey
    secondary = Color(0xFFB0B0B0), // Light grey
    tertiary = Color(0xFFD6D6D6), // Very light grey
    background = Color(0xFFFFFFFF), // White
    surface = Color(0xFFF5F5F5), // Light grey surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)


@Composable
fun AudiiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color, A12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}