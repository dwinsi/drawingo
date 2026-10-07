package com.example.drawingo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawingo.R
import com.example.drawingo.animation.CanvasAnimationRenderer
import com.example.drawingo.AppPreferences
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.theme.ElectricCyan
import com.example.drawingo.util.CanvasBitmapUtils

val ToolDockBackgroundColor = Color(0xFFFFFEFA)
val DockIconBackground = Color(0xFFF3EFE7)
val ActiveToolHighlight = Color(0xFFC53C65)
val DockShadow = Color(0x33000000)

@Composable
fun DrawingCanvas(
    viewModel: DrawingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedTool by viewModel.selectedTool.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedStrokeWidth by viewModel.selectedStrokeWidth.collectAsState()
    val selectedEraserWidth by viewModel.selectedEraserWidth.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val canvasScale by viewModel.canvasScale.collectAsState()
    val canvasOffsetX by viewModel.canvasOffsetX.collectAsState()
    val canvasOffsetY by viewModel.canvasOffsetY.collectAsState()

    val completedStrokes by viewModel.completedStrokes.collectAsState()
    val activeStrokes by viewModel.activeStrokes.collectAsState()

    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()
    val showGeminiDialog by viewModel.showGeminiDialog.collectAsState()
    val animationStatus by viewModel.animationStatus.collectAsState()
    val animationSubject by viewModel.animationSubject.collectAsState()

    // Animation States
    val isAnimationActive by viewModel.isAnimationActive.collectAsState()
    val activeAnimationScene by viewModel.activeAnimationScene.collectAsState()
    val animatedEntity by viewModel.animatedEntity.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val animationProgress by viewModel.animationProgress.collectAsState()

    var showAppSettings by remember { mutableStateOf(false) }

    var frameTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                frameTimeMs = System.currentTimeMillis()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
        .background(Color.White)
    ) {
        // Main Interactive Canvas & Animation Scene
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (!isAnimationActive && (zoom != 1f || pan != Offset.Zero)) {
                            viewModel.onPanAndZoom(zoom, pan)
                        }
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            if (event.changes.size == 1) {
                                for (change in event.changes) {
                                    val pointerId = change.id.value
                                    val position = change.position

                                    if (change.changedToDown()) {
                                        viewModel.onPointerDown(pointerId, position)
                                        change.consume()
                                    } else if (change.pressed && change.positionChanged()) {
                                        viewModel.onPointerMove(pointerId, position)
                                        change.consume()
                                    } else if (change.changedToUp()) {
                                        viewModel.onPointerUp(pointerId)
                                        change.consume()
                                    }
                                }
                            } else if (event.changes.size >= 2) {
                                for (change in event.changes) {
                                    viewModel.onPointerCancel(change.id.value)
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (isAnimationActive && activeAnimationScene != null) {
                    // Render Active Drawing-to-Animation Scene!
                    CanvasAnimationRenderer.renderScene(
                        drawScope = this,
                        sceneType = activeAnimationScene!!,
                        entity = animatedEntity,
                        particles = particles,
                        progress = animationProgress,
                        currentTimeMs = frameTimeMs
                    )
                } else {
                    // Render Standard Static Canvas
                    withTransform({
                        translate(canvasOffsetX, canvasOffsetY)
                        scale(canvasScale, canvasScale, pivot = Offset.Zero)
                    }) {
                        drawAllCanvasContent(
                            completedStrokes = completedStrokes,
                            activeStrokes = activeStrokes.values.toList()
                        )
                    }
                }

            }
        }

        // Top Action Bar
        TopKeepBar(
            canUndo = canUndo,
            canRedo = canRedo,
            hasArtwork = completedStrokes.isNotEmpty(),
            isAnimationActive = isAnimationActive,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onClear = { viewModel.clearCanvas() },
            onAnimateDrawingClick = {
                val bitmap = CanvasBitmapUtils.createBitmapFromStrokes(completedStrokes)
                viewModel.triggerDrawingAnimation(context, bitmap, AppPreferences.isCloudAiAllowed(context))
            },
            onStopAnimationClick = { viewModel.stopAnimation() },
            onSettingsClick = { showAppSettings = true },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        )

        if (showGeminiDialog) {
            Dialog(onDismissRequest = { viewModel.closeGeminiDialog() }) {
                Surface(
                    modifier = Modifier.width(340.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFFFEFA),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Animation preview",
                            color = Color(0xFF343849),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            if (animationStatus == null) "Subject: $animationSubject" else animationStatus.orEmpty(),
                            color = Color(0xFF555866),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.closeGeminiDialog() },
                            modifier = Modifier.align(Alignment.End),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD94F79),
                                contentColor = Color.White
                            )
                        ) { Text("Done", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        // Bottom Tool Dock
        if (!isAnimationActive) {
            BottomDrawingoDock(
                selectedTool = selectedTool,
                selectedColor = selectedColor,
                selectedStrokeWidth = if (selectedTool == DrawingTool.ERASER) selectedEraserWidth else selectedStrokeWidth,
                onToolClick = { tool ->
                    viewModel.setTool(tool)
                },
                onColorSelected = { color ->
                    viewModel.setColor(color)
                },
                onWidthSelected = {
                    if (selectedTool == DrawingTool.ERASER) viewModel.setEraserWidth(it)
                    else viewModel.setStrokeWidth(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .align(Alignment.BottomCenter)
            )
        }

        // Loading overlay while cloud drawing analysis runs.
        if (isGeminiLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2333)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ElectricCyan,
                        strokeWidth = 4.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text("✨", fontSize = 32.sp)
                }
            }
        }

        if (showAppSettings) {
            AppSettingsDialog(
                cloudAiEnabled = AppPreferences.isCloudAiAllowed(context),
                backendUrl = AppPreferences.getBackendUrl(context),
                onCloudAiEnabledChanged = { AppPreferences.setCloudAiAllowed(context, it) },
                onBackendUrlSaved = { AppPreferences.setBackendUrl(context, it) },
                onDismiss = { showAppSettings = false }
            )
        }
    }
}

@Composable
fun TopKeepBar(
    canUndo: Boolean,
    canRedo: Boolean,
    hasArtwork: Boolean,
    isAnimationActive: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onAnimateDrawingClick: () -> Unit,
    onStopAnimationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(ToolDockBackgroundColor)
            .border(1.dp, Color(0xFFE8E2D9), RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeSelector(
            animeEnabled = hasArtwork && !isAnimationActive,
            onAnimeClick = onAnimateDrawingClick
        )
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactAction("↶", "Undo", enabled = canUndo, size = 40.dp, onClick = onUndo)
            CompactAction("↷", "Redo", enabled = canRedo, size = 40.dp, onClick = onRedo)
            CompactAction("🗑", "Clear drawing", size = 40.dp, onClick = { showClearConfirmation = true })
            Box {
                CompactAction("⋯", "More actions", size = 40.dp, onClick = { actionsExpanded = true })
                DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                    if (isAnimationActive) {
                        DropdownMenuItem(
                            text = { Text("⏹  Stop animation") },
                            onClick = { actionsExpanded = false; onStopAnimationClick() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("⚙️  Settings") },
                        onClick = { actionsExpanded = false; onSettingsClick() }
                    )
                }
            }
        }
    }

    if (showClearConfirmation) {
        Dialog(onDismissRequest = { showClearConfirmation = false }) {
            Surface(
                modifier = Modifier.width(320.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFFEFA),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Start a fresh picture?",
                        color = Color(0xFF343849),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Your drawing will be cleared from the canvas.",
                        color = Color(0xFF555866),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showClearConfirmation = false },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEAE6F0),
                                contentColor = Color(0xFF343849)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { Text("Keep drawing", fontWeight = FontWeight.Bold) }
                        Button(
                            onClick = {
                                showClearConfirmation = false
                                onClear()
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD94F79),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) { Text("Clear drawing", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    animeEnabled: Boolean,
    onAnimeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DockIconBackground)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (animeEnabled) Color(0xFF69489B) else Color(0xFFE9E2F0))
                .clickable(enabled = animeEnabled, onClick = onAnimeClick)
                .semantics { contentDescription = "Anime drawing" },
            contentAlignment = Alignment.Center
        ) {
            Text("🌠", fontSize = 24.sp)
        }
    }
}

@Composable
private fun CompactAction(
    icon: String,
    label: String,
    enabled: Boolean = true,
    selected: Boolean = false,
    size: Dp = 44.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(when { selected -> ActiveToolHighlight; enabled -> DockIconBackground; else -> Color(0xFFF7F5F1) })
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Text(
            icon,
            color = when { selected -> Color.White; enabled -> Color(0xFF343849); else -> Color(0xFFB5B2AD) },
            fontSize = if (size <= 36.dp) 21.sp else 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomDrawingoDock(
    selectedTool: DrawingTool,
    selectedColor: Color,
    selectedStrokeWidth: Float,
    onToolClick: (DrawingTool) -> Unit,
    onColorSelected: (Color) -> Unit,
    onWidthSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var drawerExpanded by remember { mutableStateOf(false) }
    var sizesExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE8E2D9), RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(ToolDockBackgroundColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        DrawerHandle(drawerExpanded) { drawerExpanded = it }
        PrimaryColorRow(selectedColor, onColorSelected)

        AnimatedVisibility(
            visible = drawerExpanded,
            enter = expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DrawingoPalette.grid.drop(1).forEachIndexed { index, colors ->
                    ColorSwatchRow(colors, selectedColor, onColorSelected, firstColorIndex = index * 8 + 8)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        val tools = listOf(
                            Triple(DrawingTool.PEN, "✏️", "Pen"),
                            Triple(DrawingTool.HIGHLIGHTER, "🖍️", "Marker"),
                            Triple(DrawingTool.BRUSH, "🖌️", "Brush"),
                            Triple(DrawingTool.ERASER, "🧹", "Eraser"),
                            Triple(DrawingTool.LASSO, "🪄", "Wand")
                        )
                        tools.forEach { (tool, icon, label) ->
                            val isSelected = selectedTool == tool
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) ActiveToolHighlight else DockIconBackground)
                                    .clickable { onToolClick(tool) }
                                    .semantics { contentDescription = "$label tool" }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                Text(icon, fontSize = 24.sp, lineHeight = 28.sp)
                                Text(label, color = if (isSelected) Color.White else Color(0xFF4F5260), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box {
                            CompactAction(
                                "📏",
                                if (selectedTool == DrawingTool.ERASER) "Choose eraser size" else "Choose brush size",
                                size = 40.dp
                            ) { sizesExpanded = true }
                            DropdownMenu(
                                expanded = sizesExpanded,
                                onDismissRequest = { sizesExpanded = false },
                                modifier = Modifier.background(Color(0xFFFFFEFA))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        if (selectedTool == DrawingTool.ERASER) "Eraser size" else "Brush size",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF343849)
                                    )
                                    StrokeSizeRow(selectedStrokeWidth) { width -> onWidthSelected(width); sizesExpanded = false }
                                }
                            }
                        }
                }
        }
    }
}
}

@Composable
private fun DrawerHandle(expanded: Boolean, onExpandedChange: (Boolean) -> Unit) {
    var dragDistance by remember { mutableStateOf(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onExpandedChange(!expanded) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        if (dragDistance < -24f) onExpandedChange(true)
                        if (dragDistance > 24f) onExpandedChange(false)
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f }
                )
            }
            .semantics { contentDescription = if (expanded) "Collapse colors and tools drawer" else "Expand colors and tools drawer" }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFC8C3BA))
        )
    }
}

@Composable
private fun PrimaryColorRow(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val colorCellSize = maxWidth / 8
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DrawingoPalette.grid.first().forEachIndexed { index, color ->
                ColorSwatch(
                    color = color,
                    selected = color == selectedColor,
                    colorName = ALL_COLOR_NAMES[index],
                    onClick = { onColorSelected(color) },
                    touchSize = colorCellSize
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    selected: Boolean,
    colorName: String,
    onClick: () -> Unit,
    touchSize: Dp = 44.dp
) {
    Box(
        modifier = Modifier
            .size(touchSize)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$colorName color" },
        contentAlignment = Alignment.Center
    ) {
        val swatchSize = if (selected) {
            (touchSize * 0.88f).coerceAtMost(40.dp)
        } else {
            (touchSize * 0.74f).coerceAtMost(34.dp)
        }
        Box(
            modifier = Modifier
                .size(swatchSize)
                .clip(CircleShape)
                .background(color)
                .border(if (selected) 3.dp else 1.dp, if (selected) Color(0xFF25283A) else Color(0xFFCEC8BF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Text("✓", color = if (color == Color.White) Color.Black else Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun ColorSwatchRow(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    firstColorIndex: Int
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellSize = maxWidth / 8
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, color ->
                ColorSwatch(
                    color = color,
                    selected = color == selectedColor,
                    colorName = ALL_COLOR_NAMES.getOrElse(firstColorIndex + index) { "Custom" },
                    onClick = { onColorSelected(color) },
                    touchSize = cellSize
                )
            }
        }
    }
}

private val ALL_COLOR_NAMES = listOf(
    "Black", "Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink",
    "Charcoal", "Deep crimson", "Deep burnt orange", "Amber", "Forest green", "Midnight navy", "Deep purple", "Electric rose",
    "Slate grey", "Coral", "Tangerine", "Sunshine gold", "Lime green", "Sky blue", "Plum", "Fuchsia",
    "White", "Blush pink", "Soft peach", "Lemon pastel", "Mint green", "Periwinkle", "Soft lavender", "Rose pink"
)

@Composable
private fun StrokeSizeRow(selectedStrokeWidth: Float, onWidthSelected: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawingoPalette.strokeSizes.forEach { width ->
            val selected = selectedStrokeWidth == width
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color(0xFFE9E1F2) else Color.Transparent)
                    .clickable { onWidthSelected(width) }
                    .semantics { contentDescription = "Brush size ${width.toInt()}" },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((width * 0.28f).coerceIn(6f, 25f).dp)
                        .clip(CircleShape)
                        .background(if (selected) Color(0xFF69489B) else Color(0xFF676B73))
                )
            }
        }
    }
}

private fun DrawScope.drawAllCanvasContent(
    completedStrokes: List<DrawnStroke>,
    activeStrokes: List<DrawnStroke>
) {
    for (stroke in completedStrokes) {
        drawSingleStroke(stroke)
    }
    for (stroke in activeStrokes) {
        drawSingleStroke(stroke)
    }
}

private fun DrawScope.drawSingleStroke(stroke: DrawnStroke) {
    if (stroke.points.size < 2) return
    val path = Path()
    path.moveTo(stroke.points[0].x, stroke.points[0].y)
    for (i in 1 until stroke.points.size) {
        path.lineTo(stroke.points[i].x, stroke.points[i].y)
    }
    drawPath(
        path = path,
        color = stroke.color.copy(alpha = stroke.alpha),
        style = Stroke(
            width = stroke.strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
