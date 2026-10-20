package com.example.drawingo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.drawingo.model.DrawingLayer
import com.example.drawingo.theme.StudioAccentUi
import com.example.drawingo.theme.StudioAccentSoft
import com.example.drawingo.theme.StudioMutedText
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayersBottomSheet(
    layers: List<DrawingLayer>,
    selectedLayerId: Long?,
    onDismiss: () -> Unit,
    onAddLayer: () -> Unit,
    onSelectLayer: (Long) -> Unit,
    onToggleVisibility: (Long) -> Unit,
    onDeleteLayer: (Long) -> Unit,
    onOpacityChange: (Long, Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Layers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudioText
                )
                IconButton(onClick = onAddLayer, modifier = Modifier.semantics { contentDescription = "Add layer" }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = StudioAccentUi)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(layers) { index, layer ->
                    val isSelected = layer.id == selectedLayerId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) StudioAccentSoft else StudioSurface)
                            .clickable { onSelectLayer(layer.id) }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onToggleVisibility(layer.id) },
                                        modifier = Modifier.semantics {
                                            contentDescription = if (layer.isVisible) "Hide ${layer.name}" else "Show ${layer.name}"
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = null,
                                            tint = if (layer.isVisible) StudioText else StudioMutedText
                                        )
                                    }
                                    Text(
                                        text = layer.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = StudioText
                                    )
                                }
                                if (layers.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteLayer(layer.id) },
                                        modifier = Modifier.semantics { contentDescription = "Delete ${layer.name}" }
                                    ) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = StudioMutedText)
                                    }
                                }
                            }
                            
                            if (isSelected) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text("Opacity", style = MaterialTheme.typography.bodySmall, color = StudioMutedText)
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Slider(
                                        value = layer.opacity,
                                        onValueChange = { onOpacityChange(layer.id, it) },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = StudioAccentUi,
                                            activeTrackColor = StudioAccentUi
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
