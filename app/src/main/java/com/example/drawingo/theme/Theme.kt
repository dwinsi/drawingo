package com.example.drawingo.theme

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

private val DarkColorScheme =
    darkColorScheme(
        primary = ElectricCyan,
        onPrimary = Color(0xFF00363A),
        primaryContainer = CyanDepth,
        onPrimaryContainer = Color(0xFFB2F8FF),
        secondary = CyberViolet,
        onSecondary = Color(0xFF3B0764),
        secondaryContainer = DeepViolet,
        onSecondaryContainer = Color(0xFFF3E8FF),
        tertiary = NeonRose,
        onTertiary = Color(0xFF500022),
        tertiaryContainer = RoseNight,
        onTertiaryContainer = Color(0xFFFFD6E5),
        background = ObsidianVoid,
        onBackground = Color.White,
        surface = CosmicSlate,
        onSurface = Color.White,
        surfaceVariant = GlassCardSurface,
        onSurfaceVariant = Color(0xFFE2E8F0)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF006970),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFA6F5FF),
        onPrimaryContainer = Color(0xFF002022),
        secondary = Color(0xFF7E22CE),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF3E8FF),
        onSecondaryContainer = Color(0xFF2E004E),
        tertiary = Color(0xFFC2005A),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD9E2),
        onTertiaryContainer = Color(0xFF3E001A),
        background = Color(0xFFF8FAFC),
        onBackground = Color(0xFF0F172A),
        surface = Color.White,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFE2E8F0),
        onSurfaceVariant = Color(0xFF334155)
    )

@Composable
fun DrawingoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.example.drawingo.theme.Typography,
        content = content
    )
}

@Composable
fun KautukTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    DrawingoTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
