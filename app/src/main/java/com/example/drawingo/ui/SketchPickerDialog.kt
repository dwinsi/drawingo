package com.example.drawingo.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drawingo.model.SketchCategory
import com.example.drawingo.model.StockSketch
import com.example.drawingo.net.SketchRepository
import com.example.drawingo.theme.ElectricCyan

@Composable
fun SketchPickerDialog(
    sketches: List<StockSketch>,
    selectedSketch: StockSketch?,
    isLoading: Boolean,
    onSketchSelected: (StockSketch) -> Unit,
    onClearTemplate: () -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(SketchCategory.ALL) }

    val filteredSketches = remember(sketches, selectedCategory) {
        if (selectedCategory == SketchCategory.ALL) {
            sketches
        } else {
            sketches.filter { it.category == selectedCategory }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(32.dp))
                .border(4.dp, Color(0xFFE5E7EB), RoundedCornerShape(32.dp)),
            color = Color(0xFFF9FAFB),
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Coloring Pages",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1F2937)
                            )
                        }
                        Text(
                            text = "Tap a sketch to load outline and color inside!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Refresh / Sync from Cloud button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB))
                                .clickable { onRefresh() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF4FC3F7),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text("☁️", fontSize = 24.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Close Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF6B9E))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✖", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Active Sketch Banner (if any is active)
                if (selectedSketch != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFD166).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFFFF9F1C), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedSketch.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active: ${selectedSketch.title}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "Ready to color with crayons & brushes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFF6B9E))
                                .clickable {
                                    onClearTemplate()
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Clear 📄",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Category Chips Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val categories = listOf(
                        SketchCategory.ALL,
                        SketchCategory.CELESTIAL,
                        SketchCategory.SEA_ANIMALS,
                        SketchCategory.WILD_ANIMALS
                    )

                    categories.forEach { cat ->
                        val isSelected = (selectedCategory == cat)
                        val count = if (cat == SketchCategory.ALL) sketches.size else sketches.count { it.category == cat }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF4FC3F7) else Color(0xFFE5E7EB))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "${cat.emoji} ${cat.displayName} ($count)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else Color(0xFF4B5563)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sketches Grid
                if (filteredSketches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color(0xFF4FC3F7))
                        } else {
                            Text(
                                text = "No sketches found. Tap ☁️ to sync from Cloud!",
                                color = Color(0xFF9CA3AF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        items(filteredSketches, key = { it.id }) { sketch ->
                            val isCurrent = (selectedSketch?.id == sketch.id)
                            SketchCard(
                                sketch = sketch,
                                isCurrent = isCurrent,
                                onClick = {
                                    onSketchSelected(sketch)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SketchCard(
    sketch: StockSketch,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var previewBitmap by remember(sketch.id) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(sketch.id) {
        previewBitmap = SketchRepository.loadSketchBitmap(context, sketch)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .then(
                if (isCurrent) Modifier.border(4.dp, Color(0xFF4FC3F7), RoundedCornerShape(24.dp))
                else Modifier.border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Outline Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = sketch.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(sketch.emoji, fontSize = 48.sp)
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF4FC3F7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Active 🖍️", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${sketch.emoji} ${sketch.title}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = sketch.category.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        }
    }
}
