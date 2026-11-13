package com.example.drawingo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CenterFocusWeak
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingo.theme.StudioAccentSoft
import com.example.drawingo.theme.StudioAccentUi
import com.example.drawingo.theme.StudioControl
import com.example.drawingo.theme.StudioOutline
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText

@Composable
fun SideFeatureRail(
    isMagicMotionVisible: Boolean,
    onToggleMagicMotion: () -> Unit,
    isAnimationDockVisible: Boolean,
    onToggleAnimationDock: () -> Unit,
    onPaperStyleClick: () -> Unit,
    onSymmetryClick: () -> Unit,
    symmetryMode: com.example.drawingo.model.SymmetryMode,
    onLayersClick: () -> Unit,
    onZenModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = StudioSurface,
        border = BorderStroke(1.dp, StudioOutline)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RailIconButton(
                icon = Icons.Rounded.AutoAwesome,
                label = "Magic",
                selected = isMagicMotionVisible,
                onClick = onToggleMagicMotion
            )
            RailIconButton(
                icon = Icons.Rounded.Movie,
                label = "Movie",
                selected = isAnimationDockVisible,
                onClick = onToggleAnimationDock
            )
            RailIconButton(
                icon = Icons.Rounded.GridOn,
                label = "Paper",
                onClick = onPaperStyleClick
            )
            RailIconButton(
                icon = Icons.Rounded.Flip,
                label = "Mirror",
                selected = symmetryMode != com.example.drawingo.model.SymmetryMode.NONE,
                onClick = onSymmetryClick
            )
            RailIconButton(
                icon = Icons.Rounded.Layers,
                label = "Layers",
                onClick = onLayersClick
            )
            RailIconButton(
                icon = Icons.Rounded.CenterFocusWeak,
                label = "Focus",
                onClick = onZenModeClick
            )
        }
    }
}

@Composable
fun RailIconButton(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 46.dp, height = 46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) StudioAccentSoft else StudioControl)
            .border(1.dp, if (selected) StudioAccentUi else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) StudioAccentUi else StudioText,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) StudioAccentUi else StudioText,
            maxLines = 1
        )
    }
}
