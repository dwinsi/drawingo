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

enum class DrawingoThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

private val DrawingoLightColorScheme = lightColorScheme(
    primary = PrimaryDrawingo,
    onPrimary = CloudWhite,
    primaryContainer = Color(0xFFE9E1F2),
    onPrimaryContainer = Color(0xFF343849),
    secondary = SecondaryDrawingo,
    onSecondary = CloudWhite,
    secondaryContainer = Color(0xFFF1F0ED),
    onSecondaryContainer = Color(0xFF343849),
    tertiary = TertiaryDrawingo,
    onTertiary = CloudWhite,
    tertiaryContainer = Color(0xFFE9E1F2),
    onTertiaryContainer = Color(0xFF343849),
    background = Color(0xFFFCFBF8),
    onBackground = Color(0xFF343849),
    surface = Color(0xFFFCFBF8),
    onSurface = Color(0xFF343849),
    surfaceVariant = Color(0xFFF1F0ED),
    onSurfaceVariant = Color(0xFF72747B),
    outline = Color(0xFFC6BFB4),
    outlineVariant = Color(0xFFE8E2D9)
)

private val DrawingoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB99CDC),
    onPrimary = Color(0xFF20152F),
    primaryContainer = Color(0xFF4A3565),
    onPrimaryContainer = Color(0xFFE9E1F2),
    secondary = Color(0xFFC9BED6),
    onSecondary = Color(0xFF20152F),
    secondaryContainer = Color(0xFF36303D),
    onSecondaryContainer = Color(0xFFE4DDEB),
    tertiary = Color(0xFFC5B5D9),
    onTertiary = Color(0xFF20152F),
    tertiaryContainer = Color(0xFF40384A),
    onTertiaryContainer = Color(0xFFE9E1F2),
    background = ObsidianBlack,
    onBackground = CloudWhite,
    surface = MidnightIndigo,
    onSurface = CloudWhite,
    surfaceVariant = Color(0xFF302B35),
    onSurfaceVariant = Color(0xFFCEC5D6),
    outline = Color(0xFF8F8499),
    outlineVariant = Color(0xFF4B4552)
)

@Composable
fun DrawingoTheme(
    themeMode: DrawingoThemeMode = DrawingoThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        DrawingoThemeMode.SYSTEM -> isSystemInDarkTheme()
        DrawingoThemeMode.LIGHT -> false
        DrawingoThemeMode.DARK -> true
    }
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DrawingoDarkColorScheme
            else -> DrawingoLightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.example.drawingo.theme.Typography,
        content = content
    )
}
