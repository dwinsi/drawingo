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

private val DrawingoLightColorScheme = lightColorScheme(
    primary = PrimaryDrawingo,
    onPrimary = CloudWhite,
    primaryContainer = StudioAccentSoft,
    onPrimaryContainer = StudioText,
    secondary = SecondaryDrawingo,
    onSecondary = CloudWhite,
    secondaryContainer = StudioControl,
    onSecondaryContainer = StudioText,
    tertiary = TertiaryDrawingo,
    onTertiary = CloudWhite,
    tertiaryContainer = StudioAccentSoft,
    onTertiaryContainer = StudioText,
    background = StudioSurface,
    onBackground = StudioText,
    surface = StudioSurface,
    onSurface = StudioText,
    surfaceVariant = StudioControl,
    onSurfaceVariant = StudioText
)

private val DrawingoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB99CDC),
    onPrimary = Color(0xFF20152F),
    primaryContainer = Color(0xFF4A3565),
    onPrimaryContainer = Color(0xFFE9E1F2),
    secondary = Color(0xFFC9BED6),
    onSecondary = CloudWhite,
    secondaryContainer = Color(0xFF36303D),
    onSecondaryContainer = Color(0xFFE4DDEB),
    tertiary = Color(0xFFC5B5D9),
    onTertiary = CloudWhite,
    tertiaryContainer = Color(0xFF40384A),
    onTertiaryContainer = Color(0xFFE9E1F2),
    background = ObsidianBlack,
    onBackground = CloudWhite,
    surface = MidnightIndigo,
    onSurface = CloudWhite,
    surfaceVariant = Color(0xFF302B35),
    onSurfaceVariant = Color(0xFFE4DDEB)
)

@Composable
fun DrawingoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
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
