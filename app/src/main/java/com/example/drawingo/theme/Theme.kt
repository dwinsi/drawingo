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

private val KidLightColorScheme = lightColorScheme(
    primary = PrimaryKid,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF4FA),
    onPrimaryContainer = DarkNavy,
    secondary = SecondaryKid,
    onSecondary = DarkNavy,
    secondaryContainer = Color(0xFFFFD8E4),
    onSecondaryContainer = DarkNavy,
    tertiary = TertiaryKid,
    onTertiary = DarkNavy,
    tertiaryContainer = SunnyYellow,
    onTertiaryContainer = DarkNavy,
    background = SoftSand,
    onBackground = DarkNavy,
    surface = CloudWhite,
    onSurface = DarkNavy,
    surfaceVariant = MilkyGlass,
    onSurfaceVariant = DarkNavy
)

private val KidDarkColorScheme = darkColorScheme(
    primary = PrimaryKid,
    onPrimary = DarkNavy,
    primaryContainer = SkyBlue.copy(alpha = 0.5f),
    onPrimaryContainer = Color.White,
    secondary = SecondaryKid,
    onSecondary = DarkNavy,
    secondaryContainer = BubblegumPink.copy(alpha = 0.5f),
    onSecondaryContainer = Color.White,
    tertiary = TertiaryKid,
    onTertiary = DarkNavy,
    tertiaryContainer = SunnyYellow.copy(alpha = 0.5f),
    onTertiaryContainer = Color.White,
    background = DarkNavy,
    onBackground = CloudWhite,
    surface = Color(0xFF1A1C29),
    onSurface = CloudWhite,
    surfaceVariant = Color(0xFF2A2D42),
    onSurfaceVariant = CloudWhite
)

@Composable
fun DrawingoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to enforce our kid-friendly vibrant theme
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> KidDarkColorScheme
            else -> KidLightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.example.drawingo.theme.Typography,
        content = content
    )
}
