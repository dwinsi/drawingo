package com.example.drawingo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset
import com.example.drawingo.theme.StudioAccentUi
import com.example.drawingo.theme.StudioAccentSoft
import com.example.drawingo.theme.StudioMutedText
import com.example.drawingo.theme.StudioOutline
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText

@Composable
fun AnimationBottomDock(
    currentFrameIndex: Int,
    frameCount: Int,
    isPlaying: Boolean,
    playbackFps: Int,
    onionSkinEnabled: Boolean,
    activePresets: Set<QuickMagicPreset>,
    canReplay: Boolean,
    isReplayPlaying: Boolean,
    replayProgress: Float,
    onFrameSelect: (Int) -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: (Int) -> Unit,
    onTogglePlayback: () -> Unit,
    onPlaybackFpsChange: (Int) -> Unit,
    onToggleOnionSkin: () -> Unit,
    onTogglePreset: (QuickMagicPreset) -> Unit,
    onClearPresets: () -> Unit,
    onToggleReplay: () -> Unit,
    onReplaySeek: (Float) -> Unit,
    onResetReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(1) } // 0 = Flipbook, 1 = Replay, 2 = Motion

    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(1.dp, StudioOutline, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(ToolDockBackgroundColor)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DockIconBackground, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AnimationTab("Flipbook", selectedTab == 0) { selectedTab = 0 }
            AnimationTab("Replay", selectedTab == 1) { selectedTab = 1 }
            AnimationTab("Motion", selectedTab == 2) { selectedTab = 2 }
        }

        if (selectedTab == 0) {
            // Flipbook Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play/Pause button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) ActiveToolHighlight else DockIconBackground)
                        .clickable { onTogglePlayback() }
                        .semantics { contentDescription = if (isPlaying) "Pause flipbook" else "Play flipbook" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = StudioAccentUi,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Onion Skin Toggle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (onionSkinEnabled) StudioAccentSoft else DockIconBackground)
                        .clickable { onToggleOnionSkin() }
                        .semantics { contentDescription = if (onionSkinEnabled) "Turn onion skin off" else "Turn onion skin on" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = if (onionSkinEnabled) StudioAccentUi else StudioMutedText,
                        modifier = Modifier.size(21.dp)
                    )
                }
                
                // Timeline
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(frameCount) { index ->
                        val isSelected = index == currentFrameIndex
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioAccentSoft else StudioSurface)
                                .border(2.dp, if (isSelected) StudioAccentUi else StudioOutline, RoundedCornerShape(8.dp))
                                .clickable { onFrameSelect(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontWeight = FontWeight.Bold, color = if (isSelected) StudioAccentUi else StudioText)
                        }
                    }
                    
                    item {
                        // Add Frame Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DockIconBackground)
                                .clickable { onAddFrame() }
                                .semantics { contentDescription = "Add frame" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = StudioText)
                        }
                    }
                    
                    item {
                        // Duplicate Frame Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DockIconBackground)
                                .clickable { onDuplicateFrame() }
                                .semantics { contentDescription = "Duplicate current frame" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = StudioText)
                        }
                    }

                    item {
                        val canDeleteFrame = frameCount > 1
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DockIconBackground)
                                .clickable(enabled = canDeleteFrame) { onDeleteFrame(currentFrameIndex) }
                                .semantics { contentDescription = "Delete current frame" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = if (canDeleteFrame) StudioMutedText else StudioMutedText.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Speed", color = StudioText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Slider(
                    value = playbackFps.toFloat(),
                    onValueChange = { onPlaybackFpsChange(it.roundToInt()) },
                    valueRange = 1f..24f,
                    steps = 22,
                    enabled = frameCount > 1,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Flipbook speed, $playbackFps frames per second" }
                )
                Text("${playbackFps} fps", color = StudioText, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        } else if (selectedTab == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isReplayPlaying) ActiveToolHighlight else DockIconBackground)
                        .clickable(enabled = canReplay) { onToggleReplay() }
                        .semantics {
                            contentDescription = when {
                                !canReplay -> "Draw something before replaying"
                                isReplayPlaying -> "Pause stroke replay"
                                else -> "Play stroke replay"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReplayPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = StudioAccentUi,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (canReplay) "Drawing replay" else "Draw something to replay",
                        fontWeight = FontWeight.Bold,
                        color = StudioText,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = replayProgress,
                        onValueChange = onReplaySeek,
                        valueRange = 0f..1f,
                        enabled = canReplay,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DockIconBackground)
                        .clickable { onResetReplay() }
                        .semantics { contentDescription = "Restart stroke replay" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Replay, contentDescription = null, tint = StudioText)
                }
            }
        } else {
            // Locally rendered motion effects
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                PresetButton("Clear", Icons.Rounded.Block, activePresets.isEmpty()) { onClearPresets() }
                QuickMagicPreset.entries.forEach { preset ->
                    PresetButton(preset.label, preset.icon, activePresets.contains(preset)) { onTogglePreset(preset) }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun AnimationTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) StudioSurface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) StudioText else StudioMutedText
        )
    }
}

@Composable
private fun PresetButton(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .semantics { contentDescription = if (selected) "$label effect enabled" else "$label effect" }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) StudioAccentSoft else DockIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) StudioAccentUi else StudioText, modifier = Modifier.size(24.dp))
        }
        Text(
            label,
            fontWeight = FontWeight.Bold,
            color = if (selected) StudioAccentUi else StudioText,
            fontSize = 12.sp
        )
    }
}

private val QuickMagicPreset.icon: ImageVector
    get() = when (this) {
        QuickMagicPreset.WIGGLE -> Icons.Rounded.Waves
        QuickMagicPreset.PULSE -> Icons.Rounded.FavoriteBorder
        QuickMagicPreset.FLOAT -> Icons.Rounded.Cloud
        QuickMagicPreset.SHIMMER -> Icons.Rounded.Flare
        QuickMagicPreset.SPIN -> Icons.Rounded.Refresh
        QuickMagicPreset.WANDER -> Icons.Rounded.Explore
        QuickMagicPreset.FLIP -> Icons.Rounded.Flip
        QuickMagicPreset.TRANSLATE -> Icons.Rounded.OpenWith
        QuickMagicPreset.RANDOM -> Icons.Rounded.Casino
        QuickMagicPreset.BOUNCE -> Icons.Rounded.SportsBasketball
        QuickMagicPreset.WAVE -> Icons.Rounded.Water
        QuickMagicPreset.SWING -> Icons.Rounded.Sync
        QuickMagicPreset.JITTER -> Icons.Rounded.Bolt
        QuickMagicPreset.HEARTBEAT -> Icons.Rounded.Favorite
        QuickMagicPreset.SPIRAL -> Icons.Rounded.Loop
        QuickMagicPreset.COLOR_CYCLE -> Icons.Rounded.Palette
    }
