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
    onPrimary = Color.White,
    primaryContainer = SoftIndigo.copy(alpha = 0.2f),
    onPrimaryContainer = DarkNavy,
    secondary = SecondaryDrawingo,
    onSecondary = Color.White,
    secondaryContainer = SageGreen.copy(alpha = 0.2f),
    onSecondaryContainer = DarkNavy,
    tertiary = TertiaryDrawingo,
    onTertiary = Color.White,
    tertiaryContainer = SlateGray.copy(alpha = 0.2f),
    onTertiaryContainer = DarkNavy,
    background = SoftSand,
    onBackground = DarkNavy,
    surface = CloudWhite,
    onSurface = DarkNavy,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = DarkNavy
)

private val DrawingoDarkColorScheme = darkColorScheme(
    primary = PrimaryDrawingo,
    onPrimary = Color.White,
    primaryContainer = SoftIndigo.copy(alpha = 0.4f),
    onPrimaryContainer = Color.White,
    secondary = SecondaryDrawingo,
    onSecondary = Color.White,
    secondaryContainer = SageGreen.copy(alpha = 0.4f),
    onSecondaryContainer = Color.White,
    tertiary = TertiaryDrawingo,
    onTertiary = Color.White,
    tertiaryContainer = SlateGray.copy(alpha = 0.4f),
    onTertiaryContainer = Color.White,
    background = CosmicSlate,
    onBackground = CloudWhite,
    surface = DarkNavy,
    onSurface = CloudWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = CloudWhite
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
