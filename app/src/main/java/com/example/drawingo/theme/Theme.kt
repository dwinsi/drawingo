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
    onPrimary = ObsidianBlack,
    primaryContainer = SoftGold,
    onPrimaryContainer = ObsidianBlack,
    secondary = SecondaryDrawingo,
    onSecondary = CloudWhite,
    secondaryContainer = IndigoGloss,
    onSecondaryContainer = CloudWhite,
    tertiary = TertiaryDrawingo,
    onTertiary = CloudWhite,
    tertiaryContainer = SoftIndigo,
    onTertiaryContainer = CloudWhite,
    background = CloudWhite,
    onBackground = ObsidianBlack,
    surface = CloudWhite,
    onSurface = ObsidianBlack,
    surfaceVariant = Color(0xFFF3F3F5),
    onSurfaceVariant = MidnightIndigo
)

private val DrawingoDarkColorScheme = darkColorScheme(
    primary = PrimaryDrawingo,
    onPrimary = ObsidianBlack,
    primaryContainer = GoldVariant,
    onPrimaryContainer = ObsidianBlack,
    secondary = SecondaryDrawingo,
    onSecondary = CloudWhite,
    secondaryContainer = IndigoGloss,
    onSecondaryContainer = CloudWhite,
    tertiary = TertiaryDrawingo,
    onTertiary = CloudWhite,
    tertiaryContainer = SoftIndigo,
    onTertiaryContainer = CloudWhite,
    background = ObsidianBlack,
    onBackground = CloudWhite,
    surface = MidnightIndigo,
    onSurface = CloudWhite,
    surfaceVariant = IndigoGloss,
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
