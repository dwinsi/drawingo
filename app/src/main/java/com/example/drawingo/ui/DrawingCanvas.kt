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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.example.drawingo.model.CanvasPaperStyle
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.model.GooglyEyePair
import com.example.drawingo.model.MagicCompanion
import com.example.drawingo.model.StockSketch
import com.example.drawingo.theme.ElectricCyan
import com.example.drawingo.util.CanvasBitmapUtils

val ToolDockBackgroundColor = Color(0xFFFFFFFF)
val DockIconBackground = Color(0xFFF3F4F6)
val ActiveToolHighlight = Color(0xFFFF6B9E)
val DockShadow = Color(0x33000000)

enum class DrawerLevel {
    HIDDEN,
    COMPACT,
    FULL
}

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
    val paperStyle by viewModel.paperStyle.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedStrokeWidth by viewModel.selectedStrokeWidth.collectAsState()
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
    var drawerLevel by remember { mutableStateOf(DrawerLevel.HIDDEN) }

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
            .background(getPaperBackgroundColor(paperStyle))
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
                    drawCanvasPaperBackground(paperStyle)

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
            paperStyle = paperStyle,
            canUndo = canUndo,
            canRedo = canRedo,
            isAnimationActive = isAnimationActive,
            onModeChanged = { viewModel.setCanvasMode(it) },
            onPaperStyleCycle = { viewModel.cyclePaperStyle() },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onClear = { viewModel.clearCanvas() },
            onAnimateDrawingClick = {
                val bitmap = CanvasBitmapUtils.createBitmapFromStrokes(completedStrokes, paperStyle = paperStyle)
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
                selectedStrokeWidth = selectedStrokeWidth,
                drawerLevel = drawerLevel,
                onToolClick = { tool ->
                    when (tool) {
                        DrawingTool.LASSO, DrawingTool.ERASER -> {
                            drawerLevel = DrawerLevel.HIDDEN
                            viewModel.setTool(tool)
                        }
                        DrawingTool.PEN, DrawingTool.HIGHLIGHTER, DrawingTool.BRUSH -> {
                            if (selectedTool == tool) {
                                drawerLevel = when (drawerLevel) {
                                    DrawerLevel.HIDDEN -> DrawerLevel.COMPACT
                                    DrawerLevel.COMPACT -> DrawerLevel.FULL
                                    DrawerLevel.FULL -> DrawerLevel.HIDDEN
                                }
                            } else {
                                drawerLevel = DrawerLevel.COMPACT
                                viewModel.setTool(tool)
                            }
                        }
                    }
                },
                onTogglePull = {
                    drawerLevel = if (drawerLevel == DrawerLevel.FULL) DrawerLevel.COMPACT else DrawerLevel.FULL
                },
                onColorSelected = { color ->
                    viewModel.setColor(color)
                    drawerLevel = DrawerLevel.HIDDEN
                },
                onWidthSelected = { viewModel.setStrokeWidth(it) },
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

        // Active Sketch Indicator Badge (Floating on Top-Left)
        if (selectedSketch != null && !isAnimationActive) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 70.dp, start = 16.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xD910131E))
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .clickable { showSketchPicker = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedSketch!!.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedSketch!!.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🖍️", fontSize = 12.sp)
                }
            }
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
    paperStyle: CanvasPaperStyle,
    canUndo: Boolean,
    canRedo: Boolean,
    isAnimationActive: Boolean,
    onModeChanged: (CanvasMode) -> Unit,
    onPaperStyleCycle: () -> Unit,
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(ToolDockBackgroundColor)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (canvasMode == CanvasMode.DRAWINGO) ActiveToolHighlight else Color.Transparent)
                    .clickable { onModeChanged(CanvasMode.DRAWINGO) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "🎨 Drawingo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (canvasMode == CanvasMode.DRAWINGO) Color.White else Color(0xFF6B7280)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (canvasMode == CanvasMode.TODDLER_MAGIC) ActiveToolHighlight else Color.Transparent)
                    .clickable { onModeChanged(CanvasMode.TODDLER_MAGIC) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "✨ Magic",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (canvasMode == CanvasMode.TODDLER_MAGIC) Color.White else Color(0xFF6B7280)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(ToolDockBackgroundColor)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (isAnimationActive) {
                // Replay Voice Button (🔊)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4FC3F7)) // Sky Blue
                        .clickable { onReplayVoiceClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔊", fontSize = 24.sp)
                }

                // Stop Animation Button (✖)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B9E)) // Pink
                        .clickable { onStopAnimationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✖", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                // Animate My Drawing Button (🪄✨)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB39DDB)) // Soft Purple
                        .clickable { onAnimateDrawingClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪄", fontSize = 26.sp)
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DockIconBackground)
                    .clickable { onPaperStyleCycle() },
                contentAlignment = Alignment.Center
            ) {
                Text("📄", fontSize = 24.sp)
            }

            // Coloring Sketches Book Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (selectedSketch != null) Color(0xFFFFD166) else DockIconBackground) // Sunny Yellow
                    .then(if (selectedSketch != null) Modifier.border(3.dp, Color(0xFFFF9F1C), CircleShape) else Modifier)
                    .clickable { onSketchesClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(selectedSketch?.emoji ?: "🎨", fontSize = 24.sp)
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canUndo) DockIconBackground else Color(0xFFF9FAFB))
                    .clickable(enabled = canUndo) { onUndo() },
                contentAlignment = Alignment.Center
            ) {
                Text("⏪", fontSize = 20.sp, modifier = Modifier.padding(if (!canUndo) 2.dp else 0.dp))
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canRedo) DockIconBackground else Color(0xFFF9FAFB))
                    .clickable(enabled = canRedo) { onRedo() },
                contentAlignment = Alignment.Center
            ) {
                Text("⏩", fontSize = 20.sp, modifier = Modifier.padding(if (!canRedo) 2.dp else 0.dp))
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DockIconBackground)
                    .clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Text("🗑️", fontSize = 24.sp)
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DockIconBackground)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun BottomDrawingoDock(
    canvasMode: CanvasMode,
    selectedTool: DrawingTool,
    selectedColor: Color,
    selectedStrokeWidth: Float,
    drawerLevel: DrawerLevel,
    onToolClick: (DrawingTool) -> Unit,
    onTogglePull: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onWidthSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(3.dp, Color(0xFFE5E7EB), RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(ToolDockBackgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = drawerLevel != DrawerLevel.HIDDEN,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DockIconBackground)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (width in DrawingoPalette.strokeSizes) {
                        val isSelected = selectedStrokeWidth == width
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ActiveToolHighlight else Color.Transparent)
                                .clickable { onWidthSelected(width) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((width * 0.4f).coerceIn(8f, 32f).dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else Color(0xFF9CA3AF))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val visibleRows = if (drawerLevel == DrawerLevel.FULL) DrawingoPalette.grid else listOf(DrawingoPalette.grid[0])
                for (row in visibleRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (color in row) {
                            val isSelected = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 4.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tools = listOf(
                Pair(DrawingTool.PEN, "✏️"),
                Pair(DrawingTool.HIGHLIGHTER, "🖍️"),
                Pair(DrawingTool.BRUSH, "🖌️"),
                Pair(DrawingTool.ERASER, "🧹"),
                Pair(DrawingTool.LASSO, "🪄")
            )
            for ((tool, icon) in tools) {
                if (canvasMode == CanvasMode.TODDLER_MAGIC && tool == DrawingTool.LASSO) continue
                
                val isSelected = selectedTool == tool
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ActiveToolHighlight else DockIconBackground)
                        .clickable { onToolClick(tool) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 32.sp
                    )
                }
            }

            if (canvasMode == CanvasMode.DRAWINGO) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DockIconBackground)
                        .clickable { onTogglePull() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (drawerLevel == DrawerLevel.FULL) "🔽" else "🔼",
                        fontSize = 32.sp
                    )
                }
            }
        }
    }
}

private fun getPaperBackgroundColor(style: CanvasPaperStyle): Color {
    return when (style) {
        CanvasPaperStyle.PURE_WHITE -> Color.White
        CanvasPaperStyle.BLUE_GRID -> Color(0xFFF0F8FF)
        CanvasPaperStyle.COSMIC_NIGHT -> Color(0xFF121826)
        CanvasPaperStyle.WARM_CREAM -> Color(0xFFFFFDF5)
    }
}

private fun DrawScope.drawCanvasPaperBackground(style: CanvasPaperStyle) {
    if (style == CanvasPaperStyle.BLUE_GRID) {
        val step = 40f
        val gridColor = Color(0x220066FF)
        var x = 0f
        while (x < size.width) {
            drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
            y += step
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
