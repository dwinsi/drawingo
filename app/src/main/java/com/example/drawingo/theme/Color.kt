package com.example.drawingo.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Base colors used by the studio's dark Material theme.
val ObsidianBlack = Color(0xFF0A0A0C)
val MidnightIndigo = Color(0xFF16182B)

val CloudWhite = Color(0xFFFFFFFF)

// Shared studio tokens follow the active Material theme. The drawing surface itself
// remains controlled by the selected paper style.
@get:Composable
val StudioSurface: Color get() = MaterialTheme.colorScheme.surface
@get:Composable
val StudioOutline: Color get() = MaterialTheme.colorScheme.outlineVariant
@get:Composable
val StudioText: Color get() = MaterialTheme.colorScheme.onSurface
@get:Composable
val StudioMutedText: Color get() = MaterialTheme.colorScheme.onSurfaceVariant
@get:Composable
val StudioControl: Color get() = MaterialTheme.colorScheme.surfaceVariant
val StudioAccent = Color(0xFF69489B)
@get:Composable
val StudioAccentUi: Color get() = MaterialTheme.colorScheme.primary
@get:Composable
val StudioAccentSoft: Color get() = MaterialTheme.colorScheme.primaryContainer

// Material theme aliases for the shared studio palette.
val PrimaryDrawingo = StudioAccent
val SecondaryDrawingo = Color(0xFF343849)
val TertiaryDrawingo = Color(0xFF72747B)
