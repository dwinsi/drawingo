package com.example.drawingo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset

@Composable
fun AnimationBottomDock(
    currentFrameIndex: Int,
    frameCount: Int,
    isPlaying: Boolean,
    onionSkinEnabled: Boolean,
    activePreset: QuickMagicPreset,
    onFrameSelect: (Int) -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: (Int) -> Unit,
    onTogglePlayback: () -> Unit,
    onToggleOnionSkin: () -> Unit,
    onSelectPreset: (QuickMagicPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Flipbook, 1 = Quick Magic

    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE8E2D9), RoundedCornerShape(26.dp))
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
            AnimationTab("Quick Magic", selectedTab == 1) { selectedTab = 1 }
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
                        .clickable { onTogglePlayback() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isPlaying) "⏸" else "▶️", fontSize = 20.sp)
                }

                // Onion Skin Toggle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (onionSkinEnabled) Color(0xFF69489B) else DockIconBackground)
                        .clickable { onToggleOnionSkin() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧅", fontSize = 16.sp)
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
                                .background(if (isSelected) Color(0xFFE9E2F0) else Color.White)
                                .border(2.dp, if (isSelected) Color(0xFF69489B) else Color(0xFFE8E2D9), RoundedCornerShape(8.dp))
                                .clickable { onFrameSelect(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF69489B) else Color(0xFF343849))
                        }
                    }
                    
                    item {
                        // Add Frame Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DockIconBackground)
                                .clickable { onAddFrame() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕", fontSize = 20.sp)
                        }
                    }
                    
                    item {
                        // Duplicate Frame Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DockIconBackground)
                                .clickable { onDuplicateFrame() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👯", fontSize = 20.sp)
                        }
                    }
                }
            }
        } else {
            // Quick Magic Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PresetButton("None", "🚫", activePreset == QuickMagicPreset.NONE) { onSelectPreset(QuickMagicPreset.NONE) }
                PresetButton("Wiggle", "〰️", activePreset == QuickMagicPreset.WIGGLE) { onSelectPreset(QuickMagicPreset.WIGGLE) }
                PresetButton("Pulse", "💓", activePreset == QuickMagicPreset.PULSE) { onSelectPreset(QuickMagicPreset.PULSE) }
                PresetButton("Draw-On", "✍️", activePreset == QuickMagicPreset.DRAW_ON) { onSelectPreset(QuickMagicPreset.DRAW_ON) }
            }
        }
    }
}

@Composable
private fun AnimationTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) Color(0xFF343849) else Color(0xFF8B8884)
        )
    }
}

@Composable
private fun PresetButton(label: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) ActiveToolHighlight else DockIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Text(
            label,
            fontWeight = FontWeight.Bold,
            color = if (selected) ActiveToolHighlight else Color(0xFF343849),
            fontSize = 12.sp
        )
    }
}
