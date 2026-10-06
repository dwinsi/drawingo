package com.example.drawingo.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.drawingo.R
import com.example.drawingo.animation.CanvasAnimationRenderer
import com.example.drawingo.device.KioskManager
import com.example.drawingo.model.CanvasMode
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.model.GooglyEyePair
import com.example.drawingo.model.MagicCompanion
import com.example.drawingo.model.StockSketch
import com.example.drawingo.theme.ElectricCyan
import com.example.drawingo.util.CanvasBitmapUtils

val ToolDockBackgroundColor = Color(0xFFFFFEFA)
val DockIconBackground = Color(0xFFF3EFE7)
val ActiveToolHighlight = Color(0xFFC53C65)
val DockShadow = Color(0x33000000)

@Composable
fun DrawingCanvas(
    viewModel: DrawingViewModel,
    exitHoldProgress: Float = 0f,
    exitTouchCentroid: Offset? = null,
    remainingScreenTimeMs: Long = Long.MAX_VALUE,
    onScreenTimeLimitChanged: () -> Unit = {},
    onKioskToggled: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canvasMode by viewModel.canvasMode.collectAsState()
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
    val googlyEyes by viewModel.googlyEyes.collectAsState()
    val magicCompanions by viewModel.magicCompanions.collectAsState()
    val wipeProgress by viewModel.wipeProgress.collectAsState()
    val isWiping by viewModel.isWiping.collectAsState()

    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()
    val geminiRhymeText by viewModel.geminiRhymeText.collectAsState()
    val showGeminiDialog by viewModel.showGeminiDialog.collectAsState()

    // Animation States
    val isAnimationActive by viewModel.isAnimationActive.collectAsState()
    val activeAnimationScene by viewModel.activeAnimationScene.collectAsState()
    val animatedEntity by viewModel.animatedEntity.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val animationProgress by viewModel.animationProgress.collectAsState()

    var showParentSettings by remember { mutableStateOf(false) }

    // Stock Sketches & Coloring Templates State
    val stockSketches by viewModel.stockSketches.collectAsState()
    val selectedSketch by viewModel.selectedSketch.collectAsState()
    val activeSketchBitmap by viewModel.activeSketchBitmap.collectAsState()
    val isSketchesLoading by viewModel.isSketchesLoading.collectAsState()
    var showSketchPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadStockSketches(context)
    }

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
                val canvasWidth = size.width
                val canvasHeight = size.height

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
                        if (isWiping && wipeProgress > 0f) {
                            val wipeY = canvasHeight * wipeProgress
                            val remainingTop = wipeY

                            clipRect(
                                left = 0f,
                                top = remainingTop,
                                right = canvasWidth,
                                bottom = canvasHeight,
                                clipOp = ClipOp.Intersect
                            ) {
                                drawAllCanvasContent(
                                    completedStrokes = completedStrokes,
                                    activeStrokes = activeStrokes.values.toList(),
                                    currentTimeMs = frameTimeMs,
                                    googlyEyes = googlyEyes,
                                    magicCompanions = magicCompanions
                                )
                                activeSketchBitmap?.let { bmp ->
                                    drawStockSketchTemplate(
                                        drawScope = this,
                                        bitmap = bmp,
                                        canvasWidth = canvasWidth,
                                        canvasHeight = canvasHeight
                                    )
                                }
                            }

                            val waveHeight = 60f
                            val waveBrush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x8000F5FF),
                                    Color(0xFF39FF14),
                                    Color(0xFFFF1493),
                                    Color.Transparent
                                ),
                                startY = (wipeY - waveHeight).coerceAtLeast(0f),
                                endY = (wipeY + waveHeight).coerceAtMost(canvasHeight)
                            )
                            drawRect(
                                brush = waveBrush,
                                topLeft = Offset(0f, (wipeY - waveHeight).coerceAtLeast(0f)),
                                size = Size(canvasWidth, waveHeight * 2)
                            )
                        } else {
                            drawAllCanvasContent(
                                completedStrokes = completedStrokes,
                                activeStrokes = activeStrokes.values.toList(),
                                currentTimeMs = frameTimeMs,
                                googlyEyes = googlyEyes,
                                magicCompanions = magicCompanions
                            )
                            activeSketchBitmap?.let { bmp ->
                                drawStockSketchTemplate(
                                    drawScope = this,
                                    bitmap = bmp,
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight
                                )
                            }
                        }
                    }
                }

                if (exitHoldProgress > 0f && exitTouchCentroid != null) {
                    drawExitRing(
                        centroid = exitTouchCentroid,
                        progress = exitHoldProgress
                    )
                }
            }
        }

        // Top Action Bar
        TopKeepBar(
            canvasMode = canvasMode,
            canUndo = canUndo,
            canRedo = canRedo,
            hasArtwork = completedStrokes.isNotEmpty() || magicCompanions.isNotEmpty(),
            isAnimationActive = isAnimationActive,
            onModeChanged = { viewModel.setCanvasMode(it) },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onClear = { viewModel.clearCanvas() },
            onAnimateDrawingClick = {
                val bitmap = CanvasBitmapUtils.createBitmapFromStrokes(completedStrokes)
                viewModel.triggerDrawingAnimation(context, bitmap, KioskManager.isCloudAiAllowed(context))
            },
            onStopAnimationClick = { viewModel.stopAnimation() },
            onReplayVoiceClick = {
                viewModel.replayGeminiSpeech(context, KioskManager.isCloudAiAllowed(context))
            },
            onSettingsClick = { showParentSettings = true },
            selectedSketch = selectedSketch,
            onSketchesClick = { showSketchPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        )

        // Bottom Tool Dock
        if (!isAnimationActive) {
            BottomDrawingoDock(
                canvasMode = canvasMode,
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

        // Loading Overlay during Gemini processing (100% Kid Friendly - Icon/Sparkle only)
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

        // Parent Settings Dialog
        if (showParentSettings) {
            ParentGateDialog(
                isKioskEnabled = KioskManager.isKioskModeEnabled(context),
                isCloudAiAllowed = KioskManager.isCloudAiAllowed(context),
                currentBackendUrl = KioskManager.getBackendUrl(context),
                currentScreenTimeLimit = KioskManager.getScreenTimeLimit(context),
                onKioskToggled = { enabled ->
                    KioskManager.setKioskModeEnabled(context, enabled)
                    onKioskToggled(enabled)
                },
                onCloudAiAllowedChanged = { allowed ->
                    KioskManager.setCloudAiAllowed(context, allowed)
                },
                onBackendUrlSaved = { url ->
                    KioskManager.setBackendUrl(context, url)
                },
                onScreenTimeSaved = { limitMins ->
                    KioskManager.setScreenTimeLimit(context, limitMins)
                    onScreenTimeLimitChanged()
                },
                onDismiss = { showParentSettings = false }
            )
        }

        // Stock Sketch Picker Dialog
        if (showSketchPicker) {
            SketchPickerDialog(
                sketches = stockSketches,
                selectedSketch = selectedSketch,
                isLoading = isSketchesLoading,
                onSketchSelected = { sketch ->
                    viewModel.selectSketch(context, sketch)
                },
                onClearTemplate = {
                    viewModel.clearSketch()
                },
                onRefresh = {
                    viewModel.loadStockSketches(context, forceRefresh = true)
                },
                onDismiss = { showSketchPicker = false }
            )
        }

        if (remainingScreenTimeMs <= 0L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF51E2333))
                    .clickable(onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("🌙", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Drawing time is finished for today!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { showParentSettings = true }) {
                        Text("Ask a grown-up")
                    }
                }
            }
        }
    }
}

@Composable
fun TopKeepBar(
    canvasMode: CanvasMode,
    canUndo: Boolean,
    canRedo: Boolean,
    hasArtwork: Boolean,
    isAnimationActive: Boolean,
    onModeChanged: (CanvasMode) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onAnimateDrawingClick: () -> Unit,
    onStopAnimationClick: () -> Unit,
    onReplayVoiceClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    selectedSketch: StockSketch? = null,
    onSketchesClick: () -> Unit = {},
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
            canvasMode = canvasMode,
            onModeChanged = onModeChanged,
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
                            text = { Text("🔊  Hear it again") },
                            onClick = { actionsExpanded = false; onReplayVoiceClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("⏹  Stop animation") },
                            onClick = { actionsExpanded = false; onStopAnimationClick() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("📚  ${selectedSketch?.title ?: "Coloring pages"}") },
                        onClick = { actionsExpanded = false; onSketchesClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("🔒  Parent settings") },
                        onClick = { actionsExpanded = false; onSettingsClick() }
                    )
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Start a fresh picture?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Your drawing will be cleared from the canvas.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirmation = false
                    onClear()
                }) { Text("Clear drawing") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) { Text("Keep drawing") }
            },
            containerColor = Color(0xFFFFFEFA)
        )
    }
}

@Composable
private fun ModeSelector(
    canvasMode: CanvasMode,
    onModeChanged: (CanvasMode) -> Unit,
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
        ModeOption("🎨", "Draw", canvasMode == CanvasMode.DRAWINGO) { onModeChanged(CanvasMode.DRAWINGO) }
        ModeOption("✨", "Magic", canvasMode == CanvasMode.TODDLER_MAGIC) { onModeChanged(CanvasMode.TODDLER_MAGIC) }
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
private fun ModeOption(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) ActiveToolHighlight else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$label mode" },
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 24.sp)
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
    canvasMode: CanvasMode,
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
        if (canvasMode == CanvasMode.TODDLER_MAGIC) {
            Text(
                "✨  Tap or draw to meet a new friend!",
                color = Color(0xFF343849),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
            )
        } else {
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
    activeStrokes: List<DrawnStroke>,
    currentTimeMs: Long,
    googlyEyes: List<GooglyEyePair>,
    magicCompanions: List<MagicCompanion>
) {
    for (stroke in completedStrokes) {
        drawSingleStroke(stroke)
    }
    for (stroke in activeStrokes) {
        drawSingleStroke(stroke)
    }
    for (eyePair in googlyEyes) {
        GooglyEyeRenderer.drawGooglyEyePair(this, eyePair, currentTimeMs)
    }
    for (companion in magicCompanions) {
        MagicCreatureRenderer.drawCompanion(this, companion, currentTimeMs)
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

private fun DrawScope.drawExitRing(centroid: Offset, progress: Float) {
    val radius = 120f
    drawCircle(
        color = Color(0x3300F0FF),
        radius = radius,
        center = centroid
    )
    drawCircle(
        color = ElectricCyan,
        radius = radius,
        center = centroid,
        style = Stroke(width = 8f)
    )
    drawArc(
        color = Color(0xFFFF007F),
        startAngle = -90f,
        sweepAngle = progress * 360f,
        useCenter = false,
        topLeft = Offset(centroid.x - radius, centroid.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 12f, cap = StrokeCap.Round)
    )
}

private fun drawStockSketchTemplate(
    drawScope: DrawScope,
    bitmap: Bitmap,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val bmpAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
    val margin = 80f
    val availW = (canvasWidth - margin * 2).coerceAtLeast(100f)
    val availH = (canvasHeight - margin * 2).coerceAtLeast(100f)

    val targetWidth: Float
    val targetHeight: Float
    if (availW / availH > bmpAspect) {
        targetHeight = availH
        targetWidth = availH * bmpAspect
    } else {
        targetWidth = availW
        targetHeight = availW / bmpAspect
    }

    val left = (canvasWidth - targetWidth) / 2f
    val top = (canvasHeight - targetHeight) / 2f

    drawScope.drawImage(
        image = bitmap.asImageBitmap(),
        dstOffset = IntOffset(left.toInt(), top.toInt()),
        dstSize = IntSize(targetWidth.toInt(), targetHeight.toInt())
    )
}
