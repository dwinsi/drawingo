package com.example.drawingo.ui

import android.net.Uri
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.drawingo.animation.QuickMagicRenderer.processMagicStroke
import com.example.drawingo.model.DrawingFrame
import com.example.drawingo.model.CanvasPaperStyle
import com.example.drawingo.model.AnimationCategory
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import com.example.drawingo.R
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.util.CanvasBitmapUtils
import com.example.drawingo.util.CrayonRenderer
import com.example.drawingo.util.WatercolorRenderer
import com.example.drawingo.theme.StudioAccent
import com.example.drawingo.theme.StudioAccentSoft
import com.example.drawingo.theme.StudioAccentUi
import com.example.drawingo.theme.DrawingoThemeMode
import com.example.drawingo.theme.StudioControl
import com.example.drawingo.theme.StudioMutedText
import com.example.drawingo.theme.StudioOutline
import com.example.drawingo.theme.StudioSurface
import com.example.drawingo.theme.StudioText

@get:Composable
val ToolDockBackgroundColor: Color get() = StudioSurface
@get:Composable
val DockIconBackground: Color get() = StudioControl
@get:Composable
val ActiveToolHighlight: Color get() = StudioAccentSoft
val DockShadow = Color(0x33000000)

@Composable
fun DrawingCanvas(
    viewModel: DrawingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    themeMode: DrawingoThemeMode = DrawingoThemeMode.SYSTEM,
    onThemeModeChange: (DrawingoThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val caddyPreferences = remember {
        context.getSharedPreferences("drawingo_preferences", android.content.Context.MODE_PRIVATE)
    }
    val selectedTool by viewModel.selectedTool.collectAsState()
    val recentTools by viewModel.recentTools.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedStrokeWidth by viewModel.selectedStrokeWidth.collectAsState()
    val selectedPaperStyle by viewModel.selectedPaperStyle.collectAsState()
    val symmetryMode by viewModel.symmetryMode.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val selectedEraserWidth by viewModel.selectedEraserWidth.collectAsState()
    val canvasReady by viewModel.canvasReady.collectAsState()

    val canvasScale by viewModel.canvasScale.collectAsState()
    val canvasOffsetX by viewModel.canvasOffsetX.collectAsState()
    val canvasOffsetY by viewModel.canvasOffsetY.collectAsState()

    val completedStrokes by viewModel.completedStrokes.collectAsState()
    val activeStrokes by viewModel.activeStrokes.collectAsState()
    val isDrawing = activeStrokes.isNotEmpty()
    val controlsAlpha by animateFloatAsState(
        targetValue = if (isDrawing) 0.15f else 1.0f,
        animationSpec = tween(180),
        label = "controlsAlpha"
    )

    var isToolDrawerExpanded by remember { mutableStateOf(false) }
    var toolDockHeightPx by remember { mutableIntStateOf(0) }
    var canvasBoundsPx by remember { mutableStateOf(IntSize.Zero) }
    var toolCaddySizePx by remember { mutableStateOf(IntSize.Zero) }
    var toolCaddyOffsetXDp by remember {
        mutableFloatStateOf(caddyPreferences.getFloat("tool_caddy_offset_x_dp", 0f))
    }
    var toolCaddyOffsetYDp by remember {
        mutableFloatStateOf(caddyPreferences.getFloat("tool_caddy_offset_y_dp", 0f))
    }
    var toolCaddyOnLeft by remember {
        mutableStateOf(
            caddyPreferences.getBoolean("tool_caddy_on_left", false)
        )
    }
    var isToolCaddyExpanded by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isDrawing) {
        if (isDrawing) {
            isToolDrawerExpanded = false
        }
    }
    
    val layers by viewModel.layers.collectAsState()
    val selectedLayerId by viewModel.selectedLayerId.collectAsState()
    
    val selectedStrokes by viewModel.selectedStrokes.collectAsState()
    val lassoTransform by viewModel.lassoTransform.collectAsState()

    var showPaperStyleMenu by remember { mutableStateOf(false) }
    var showLayersMenu by remember { mutableStateOf(false) }
    var showAnimationDock by remember { mutableStateOf(false) }
    var showMenus by remember { mutableStateOf(false) }
    var isZenMode by remember { mutableStateOf(false) }
    val showTextPrompt by viewModel.showTextPrompt.collectAsState()
    
    // Animation States
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsState()
    val isPlayingLocalAnimation by viewModel.isPlayingLocalAnimation.collectAsState()
    val playbackFps by viewModel.playbackFps.collectAsState()
    val onionSkinEnabled by viewModel.onionSkinEnabled.collectAsState()
    val activePresets by viewModel.activePresets.collectAsState()
    val backgroundImagePath by viewModel.backgroundImagePath.collectAsState()

    val replayTimeline = remember(layers, currentFrameIndex) {
        buildStrokeReplayTimeline(layers, currentFrameIndex)
    }
    val replayDurationMs = replayTimeline.durationMs
    var isDrawingReplayMode by remember { mutableStateOf(false) }
    var isDrawingReplayPlaying by remember { mutableStateOf(false) }
    var drawingReplayProgress by remember { mutableFloatStateOf(0f) }
    var drawingReplayStartedAtMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(layers, currentFrameIndex) {
        isDrawingReplayMode = false
        isDrawingReplayPlaying = false
        drawingReplayProgress = 0f
    }

    var frameTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                frameTimeMs = System.currentTimeMillis()
            }
        }
    }

    LaunchedEffect(frameTimeMs, isDrawingReplayPlaying, drawingReplayStartedAtMs, replayDurationMs) {
        if (isDrawingReplayPlaying) {
            val elapsedMs = frameTimeMs - drawingReplayStartedAtMs
            drawingReplayProgress = (elapsedMs.toFloat() / replayDurationMs).coerceIn(0f, 1f)
            if (drawingReplayProgress >= 1f) isDrawingReplayPlaying = false
        }
    }

    val strokeRevealFractions = remember(replayTimeline, drawingReplayProgress, isDrawingReplayMode) {
        if (isDrawingReplayMode && drawingReplayProgress < 1f) {
            replayTimeline.fractionsAt(drawingReplayProgress)
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasBoundsPx = it }
        .background(Color.White)
    ) {
        // Main Interactive Canvas & Animation Scene
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    viewModel.screenWidth = size.width.toFloat()
                    viewModel.screenHeight = size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f || pan != Offset.Zero) {
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
                                    val pressure = change.pressure

                                    if (change.changedToDown()) {
                                        viewModel.onPointerDown(pointerId, position, pressure)
                                        change.consume()
                                    } else if (change.pressed && change.positionChanged()) {
                                        change.historical.forEach { historicalChange ->
                                            val hPos = historicalChange.position
                                            viewModel.onPointerMove(pointerId, hPos, change.pressure)
                                        }
                                        viewModel.onPointerMove(pointerId, position, pressure)
                                        change.consume()
                                    } else if (change.changedToUp()) {
                                        viewModel.onPointerUp(pointerId, position)
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
            // Render Paper Background Layer First (NOT offscreen composited)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPaperStyle(selectedPaperStyle)
            }
            
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Needed for BlendMode.Clear to work properly without clearing the window background
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                // Render Standard Static Canvas on top
                withTransform({
                    translate(canvasOffsetX, canvasOffsetY)
                    scale(canvasScale, canvasScale, pivot = Offset.Zero)
                }) {
                        if (backgroundImagePath != null) {
                            val bitmap = BitmapFactory.decodeFile(backgroundImagePath)?.asImageBitmap()
                            if (bitmap != null) {
                                // Scale to fit center
                                val imgScale = minOf(size.width / bitmap.width, size.height / bitmap.height)
                                val drawWidth = bitmap.width * imgScale
                                val drawHeight = bitmap.height * imgScale
                                val left = (size.width - drawWidth) / 2f
                                val top = (size.height - drawHeight) / 2f
                                
                                withTransform({
                                    translate(left, top)
                                    scale(imgScale, imgScale, pivot = Offset.Zero)
                                }) {
                                    drawImage(image = bitmap)
                                }
                            }
                        }
                        
                        if (symmetryMode != com.example.drawingo.model.SymmetryMode.NONE) {
                            val midX = size.width / 2f
                            val midY = size.height / 2f
                            val dashPathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            val lineColor = Color.Gray.copy(alpha = 0.5f)
                            
                            if (symmetryMode == com.example.drawingo.model.SymmetryMode.VERTICAL || symmetryMode == com.example.drawingo.model.SymmetryMode.QUAD) {
                                drawLine(color = lineColor, start = Offset(midX, 0f), end = Offset(midX, size.height), strokeWidth = 2f, pathEffect = dashPathEffect)
                            }
                            if (symmetryMode == com.example.drawingo.model.SymmetryMode.HORIZONTAL || symmetryMode == com.example.drawingo.model.SymmetryMode.QUAD) {
                                drawLine(color = lineColor, start = Offset(0f, midY), end = Offset(size.width, midY), strokeWidth = 2f, pathEffect = dashPathEffect)
                            }
                        }
                        
                        // Render Text Input bounds if needed, for now just render strokes
                        drawAllCanvasContent(
                            layers = layers,
                            activeStrokes = activeStrokes.values.toList(),
                            activeLayerId = selectedLayerId,
                            currentFrameIndex = currentFrameIndex,
                            onionSkinEnabled = onionSkinEnabled && !isPlayingLocalAnimation,
                            activePresets = if (isDrawingReplayMode) emptySet() else activePresets,
                            strokeRevealFractions = strokeRevealFractions,
                            selectedStrokeIds = selectedStrokes,
                            lassoTransform = lassoTransform,
                            timeMs = frameTimeMs
                        )
                }
            }
        }

        AnimatedVisibility(
            visible = !isZenMode && showMenus,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { alpha = controlsAlpha }
        ) {
            // Top Action Bar
            TopKeepBar(
                canUndo = canUndo,
                canRedo = canRedo,
                isAnimationDockVisible = showAnimationDock,
                onToggleAnimationDock = { showAnimationDock = !showAnimationDock },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onClear = { viewModel.clearCanvas() },
                onHideMenuClick = { showMenus = false },
                onZenModeClick = { isZenMode = true },
                onLayersClick = { showLayersMenu = true },
                onPaperStyleClick = { showPaperStyleMenu = true },
                onSaveEditableProject = {
                    val projectName = "Drawing ${System.currentTimeMillis() % 1000}"
                    viewModel.saveCurrentProject(projectName, context) {
                        android.widget.Toast.makeText(context, "Editable project saved", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onExportClick = { format -> 
                    val flatStrokes = layers.flatMap { layer -> layer.frames.getOrNull(currentFrameIndex)?.strokes ?: emptyList() }
                    val bitmap = CanvasBitmapUtils.createBitmapFromStrokes(flatStrokes, viewModel.screenWidth.toInt(), viewModel.screenHeight.toInt(), backgroundImagePath = backgroundImagePath)
                    com.example.drawingo.util.ExportUtils.saveBitmapToGallery(context, bitmap, "Drawingo_${System.currentTimeMillis()}", format == "PNG")
                },
                onGalleryClick = onNavigateBack,
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                toolCaddyOnLeft = toolCaddyOnLeft,
                onToggleToolCaddySide = {
                    toolCaddyOnLeft = !toolCaddyOnLeft
                    toolCaddyOffsetXDp = 0f
                    toolCaddyOffsetYDp = 0f
                    caddyPreferences.edit()
                        .putBoolean("tool_caddy_on_left", toolCaddyOnLeft)
                        .putFloat("tool_caddy_offset_x_dp", 0f)
                        .putFloat("tool_caddy_offset_y_dp", 0f)
                        .apply()
                }
            )
        }
        
        if (!isZenMode) {
            StudioToolDock(
                selectedTool = selectedTool,
                selectedStrokeWidth = selectedStrokeWidth,
                selectedEraserWidth = selectedEraserWidth,
                selectedColor = selectedColor,
                isExpanded = isToolDrawerExpanded,
                onExpandedChange = { isToolDrawerExpanded = it },
                onColorSelected = { viewModel.setColor(it) },
                onToolSelected = { viewModel.setTool(it) },
                onStrokeWidthSelected = { viewModel.setStrokeWidth(it) },
                onEraserWidthSelected = { viewModel.setEraserWidth(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .onSizeChanged { toolDockHeightPx = it.height }
                    .graphicsLayer { alpha = controlsAlpha }
            )
        }

        if ((isZenMode || !showAnimationDock) && toolDockHeightPx > 0 && recentTools.isNotEmpty()) {
            val density = LocalDensity.current.density
            val toolDockHeight = toolDockHeightPx / density
            val caddyWidth = toolCaddySizePx.width / density
            val caddyHeight = toolCaddySizePx.height / density
            val canvasWidth = canvasBoundsPx.width / density
            val canvasHeight = canvasBoundsPx.height / density
            val baseX = if (toolCaddyOnLeft) 20f else canvasWidth - caddyWidth - 20f
            val baseY = canvasHeight - toolDockHeight - caddyHeight - 12f
            val minX = 12f
            val maxX = (canvasWidth - caddyWidth - 12f).coerceAtLeast(minX)
            val minY = 48f
            val maxY = (canvasHeight - toolDockHeight - caddyHeight - 12f).coerceAtLeast(minY)
            val caddyX = (baseX + toolCaddyOffsetXDp).coerceIn(minX, maxX)
            val caddyY = (baseY + toolCaddyOffsetYDp).coerceIn(minY, maxY)
            val effectiveOffsetX = caddyX - baseX
            val effectiveOffsetY = caddyY - baseY
            RecentToolCaddy(
                tools = recentTools,
                selectedTool = selectedTool,
                isExpanded = isToolCaddyExpanded,
                onToggleExpanded = { isToolCaddyExpanded = !isToolCaddyExpanded },
                onToolSelected = {
                    viewModel.setTool(it)
                    isToolCaddyExpanded = false
                },
                isOnLeft = toolCaddyOnLeft,
                onDrag = { dragAmount ->
                    val nextX = (caddyX + dragAmount.x / density).coerceIn(minX, maxX)
                    val nextY = (caddyY + dragAmount.y / density).coerceIn(minY, maxY)
                    toolCaddyOffsetXDp = nextX - baseX
                    toolCaddyOffsetYDp = nextY - baseY
                },
                onDragFinished = {
                    caddyPreferences.edit()
                        .putFloat("tool_caddy_offset_x_dp", toolCaddyOffsetXDp)
                        .putFloat("tool_caddy_offset_y_dp", toolCaddyOffsetYDp)
                        .apply()
                },
                modifier = Modifier
                    .align(if (toolCaddyOnLeft) Alignment.BottomStart else Alignment.BottomEnd)
                    .offset(
                        x = effectiveOffsetX.dp,
                        y = -(toolDockHeight.dp + 12.dp) + effectiveOffsetY.dp
                    )
                    .padding(
                        start = if (toolCaddyOnLeft) 20.dp else 0.dp,
                        end = if (toolCaddyOnLeft) 0.dp else 20.dp
                    )
                    .onSizeChanged { toolCaddySizePx = it }
                    .graphicsLayer { alpha = controlsAlpha }
            )
        }

        if (!isZenMode && !showMenus) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 12.dp)
                    .graphicsLayer { alpha = controlsAlpha },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactAction(
                    icon = Icons.Rounded.Menu,
                    label = "Show menus",
                    onClick = { showMenus = true }
                )
                CompactAction(
                    icon = Icons.Rounded.CenterFocusWeak,
                    label = "Focus Mode",
                    onClick = { isZenMode = true }
                )
                CompactAction(
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    label = "Undo",
                    enabled = canUndo,
                    onClick = { viewModel.undo() }
                )
                CompactAction(
                    icon = Icons.AutoMirrored.Rounded.Redo,
                    label = "Redo",
                    enabled = canRedo,
                    onClick = { viewModel.redo() }
                )
            }
        }

        // Exit Focus Mode Floating Pill Button
        AnimatedVisibility(
            visible = isZenMode,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        ) {
            Surface(
                onClick = { isZenMode = false },
                shape = CircleShape,
                color = ToolDockBackgroundColor.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, StudioOutline),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.CenterFocusWeak,
                        contentDescription = "Exit Focus Mode",
                        tint = StudioAccentUi,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Exit Focus Mode",
                        color = StudioText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Clear Canvas?", fontWeight = FontWeight.Bold) },
                text = { Text("This will clear all strokes on the current layer. You can undo this action.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearCanvas()
                            showClearConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAccentUi)
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }


        if (showTextPrompt) {
            var textValue by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.cancelTextPrompt() },
                title = { Text("Add Text") },
                text = {
                    TextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        placeholder = { Text("Type here...") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.addTextStroke(textValue) }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelTextPrompt() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = showMenus,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
        ) {
            if (showAnimationDock) {
                AnimationBottomDock(
                    currentFrameIndex = currentFrameIndex,
                    frameCount = layers.maxOfOrNull { it.frames.size } ?: 1,
                    isPlaying = isPlayingLocalAnimation,
                    playbackFps = playbackFps,
                    onionSkinEnabled = onionSkinEnabled,
                    activePresets = activePresets,
                    canReplay = replayTimeline.entries.isNotEmpty(),
                    isReplayPlaying = isDrawingReplayPlaying,
                    replayProgress = drawingReplayProgress,
                    onFrameSelect = { viewModel.selectFrame(it) },
                    onAddFrame = { viewModel.addFrame() },
                    onDuplicateFrame = { viewModel.duplicateFrame() },
                    onDeleteFrame = { viewModel.deleteFrame(it) },
                    onTogglePlayback = { viewModel.toggleLocalPlayback() },
                    onPlaybackFpsChange = { viewModel.setPlaybackFps(it) },
                    onToggleOnionSkin = { viewModel.toggleOnionSkin() },
                    onTogglePreset = { viewModel.toggleQuickMagicPreset(it) },
                    onClearPresets = { viewModel.clearQuickMagicPresets() },
                    onToggleReplay = {
                        if (isDrawingReplayPlaying) {
                            isDrawingReplayPlaying = false
                        } else {
                            isDrawingReplayMode = true
                            if (drawingReplayProgress >= 1f) drawingReplayProgress = 0f
                            if (isPlayingLocalAnimation) viewModel.toggleLocalPlayback()
                            drawingReplayStartedAtMs = System.currentTimeMillis() -
                                (drawingReplayProgress * replayDurationMs).toLong()
                            isDrawingReplayPlaying = true
                        }
                    },
                    onReplaySeek = { progress ->
                        isDrawingReplayMode = true
                        drawingReplayProgress = progress
                        if (isDrawingReplayPlaying) {
                            drawingReplayStartedAtMs = System.currentTimeMillis() -
                                (progress * replayDurationMs).toLong()
                        }
                    },
                    onResetReplay = {
                        isDrawingReplayPlaying = false
                        isDrawingReplayMode = false
                        drawingReplayProgress = 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showPaperStyleMenu) {
            Dialog(onDismissRequest = { showPaperStyleMenu = false }) {
                Surface(
                    modifier = Modifier.width(320.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = StudioSurface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Paper Style",
                            color = StudioText,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CanvasPaperStyle.entries.forEach { style ->
                                val name = style.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (style == selectedPaperStyle) StudioAccentSoft else Color.Transparent)
                                        .clickable {
                                            viewModel.setPaperStyle(style)
                                            showPaperStyleMenu = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        name,
                                        fontWeight = if (style == selectedPaperStyle) FontWeight.Bold else FontWeight.Normal,
                                        color = if (style == selectedPaperStyle) StudioAccentUi else StudioText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLayersMenu) {
            LayersBottomSheet(
                layers = layers,
                selectedLayerId = selectedLayerId,
                onDismiss = { showLayersMenu = false },
                onAddLayer = { viewModel.addLayer() },
                onSelectLayer = { viewModel.selectLayer(it) },
                onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                onDeleteLayer = { viewModel.deleteLayer(it) },
                onOpacityChange = { id, opacity -> viewModel.setLayerOpacity(id, opacity) }
            )
        }

        if (!canvasReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StudioAccent)
            }
        }

    }
}

@Composable
fun TopKeepBar(
    canUndo: Boolean,
    canRedo: Boolean,
    isAnimationDockVisible: Boolean = false,
    onToggleAnimationDock: () -> Unit = {},
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onHideMenuClick: () -> Unit,
    onZenModeClick: () -> Unit = {},
    onLayersClick: () -> Unit,
    onPaperStyleClick: () -> Unit,
    onSaveEditableProject: () -> Unit = {},
    onExportClick: (String) -> Unit,
    onGalleryClick: () -> Unit,
    viewModel: DrawingViewModel,
    modifier: Modifier = Modifier,
    themeMode: DrawingoThemeMode = DrawingoThemeMode.SYSTEM,
    onThemeModeChange: (DrawingoThemeMode) -> Unit = {},
    toolCaddyOnLeft: Boolean = false,
    onToggleToolCaddySide: () -> Unit = {}
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    val symmetryMode by viewModel.symmetryMode.collectAsState()
    val currentProjectId by viewModel.currentProjectId.collectAsState()
    val saveStatus by viewModel.projectSaveStatus.collectAsState()

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(24.dp))
            .background(StudioSurface)
            .border(1.dp, StudioOutline, RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactAction(Icons.Rounded.Close, "Hide menus", size = 40.dp, onClick = onHideMenuClick)
        CompactAction(Icons.Rounded.Image, "Gallery", size = 40.dp, onClick = onGalleryClick)
        CompactAction(
            icon = Icons.Rounded.AutoAwesome,
            label = "Animation",
            selected = isAnimationDockVisible,
            size = 40.dp,
            onClick = onToggleAnimationDock
        )
        CompactAction(Icons.AutoMirrored.Rounded.Undo, "Undo", enabled = canUndo, size = 40.dp, onClick = onUndo)
        CompactAction(Icons.AutoMirrored.Rounded.Redo, "Redo", enabled = canRedo, size = 40.dp, onClick = onRedo)
        CompactAction(Icons.Rounded.Delete, "Clear drawing", size = 40.dp, onClick = { showClearConfirmation = true })
        Box {
            CompactAction(Icons.Rounded.MoreVert, "More actions", size = 40.dp, onClick = { actionsExpanded = true })
            DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Focus mode") },
                    leadingIcon = { Icon(Icons.Rounded.CenterFocusWeak, contentDescription = null) },
                    onClick = { actionsExpanded = false; onZenModeClick() }
                )
                DropdownMenuItem(
                    text = { Text("Layers") },
                    leadingIcon = { Icon(Icons.Rounded.Layers, contentDescription = null) },
                    onClick = { actionsExpanded = false; onLayersClick() }
                )
                DropdownMenuItem(
                    text = { Text(if (symmetryMode == com.example.drawingo.model.SymmetryMode.NONE) "Turn symmetry on" else "Change symmetry") },
                    leadingIcon = { Icon(Icons.Rounded.Flip, contentDescription = null) },
                    onClick = { actionsExpanded = false; viewModel.toggleSymmetryMode() }
                )
                DropdownMenuItem(
                    text = { Text("Paper style") },
                    onClick = { actionsExpanded = false; onPaperStyleClick() }
                )
                DropdownMenuItem(
                    text = { Text("Appearance: ${themeMode.label}") },
                    onClick = {
                        actionsExpanded = false
                        showAppearanceDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Move tool caddy to ${if (toolCaddyOnLeft) "right" else "left"}") },
                    leadingIcon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) },
                    onClick = {
                        actionsExpanded = false
                        onToggleToolCaddySide()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Save editable project") },
                    leadingIcon = { Icon(Icons.Rounded.Save, contentDescription = null) },
                    onClick = { actionsExpanded = false; onSaveEditableProject() }
                )
                DropdownMenuItem(
                    text = { Text("Save as PNG") },
                    onClick = { actionsExpanded = false; onExportClick("PNG") }
                )
                DropdownMenuItem(
                    text = { Text("Save as JPEG") },
                    onClick = { actionsExpanded = false; onExportClick("JPEG") }
                )
            }
        }
        if (currentProjectId != null || saveStatus != DrawingViewModel.ProjectSaveStatus.SAVED) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (saveStatus) {
                    DrawingViewModel.ProjectSaveStatus.SAVING -> CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = StudioAccentUi,
                        strokeWidth = 2.dp
                    )
                    DrawingViewModel.ProjectSaveStatus.SAVED -> Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = StudioAccentUi,
                        modifier = Modifier.size(15.dp)
                    )
                    DrawingViewModel.ProjectSaveStatus.FAILED -> Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFB3261E),
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = when (saveStatus) {
                        DrawingViewModel.ProjectSaveStatus.SAVING -> "Saving"
                        DrawingViewModel.ProjectSaveStatus.SAVED -> "Saved"
                        DrawingViewModel.ProjectSaveStatus.FAILED -> "Save failed"
                    },
                    color = if (saveStatus == DrawingViewModel.ProjectSaveStatus.FAILED) Color(0xFFB3261E) else StudioMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("Appearance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DrawingoThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onThemeModeChange(mode)
                                    showAppearanceDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    onThemeModeChange(mode)
                                    showAppearanceDialog = false
                                }
                            )
                            Text(mode.label, color = StudioText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppearanceDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = StudioSurface,
            titleContentColor = StudioText,
            textContentColor = StudioText
        )
    }

    if (showClearConfirmation) {
        Dialog(onDismissRequest = { showClearConfirmation = false }) {
            Surface(
                modifier = Modifier.width(320.dp),
                shape = RoundedCornerShape(24.dp),
                color = StudioSurface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Start a fresh picture?",
                        color = StudioText,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Your drawing will be cleared from the canvas.",
                        color = StudioMutedText,
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
                            containerColor = StudioControl,
                            contentColor = StudioText
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
                                containerColor = StudioAccentUi,
                                contentColor = MaterialTheme.colorScheme.onPrimary
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
private fun CompactAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            .background(when { selected -> StudioAccentSoft; enabled -> StudioControl; else -> StudioControl.copy(alpha = 0.6f) })
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = when { selected -> StudioAccentUi; enabled -> StudioText; else -> StudioMutedText },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EraserIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        withTransform({
            rotate(degrees = -30f, pivot = Offset(w * 0.5f, h * 0.5f))
        }) {
            val corner = 2.dp.toPx()
            val left = w * 0.22f
            val rectWidth = w * 0.56f
            // Eraser rubber tip
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, h * 0.12f),
                size = Size(rectWidth, h * 0.36f),
                cornerRadius = CornerRadius(corner, corner)
            )
            // Sleeve holder
            drawRoundRect(
                color = tint.copy(alpha = 0.52f),
                topLeft = Offset(left, h * 0.48f),
                size = Size(rectWidth, h * 0.40f),
                cornerRadius = CornerRadius(corner, corner)
            )
            // Accent stripe
            drawLine(
                color = tint,
                start = Offset(left, h * 0.62f),
                end = Offset(left + rectWidth, h * 0.62f),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}

@Composable
private fun EraserPopoverCard(
    selectedEraserWidth: Float,
    onEraserWidthSelected: (Float) -> Unit,
    onClearCanvasRequested: () -> Unit
) {
    Surface(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(20.dp),
        color = StudioSurface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioOutline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Eraser Size",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = StudioText
                )
                Text(
                    "${selectedEraserWidth.toInt()} px",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = StudioMutedText
                )
            }

            // Live Circle Preview + Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioControl),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((selectedEraserWidth * 0.28f).coerceIn(8f, 38f).dp)
                            .clip(CircleShape)
                            .border(1.5.dp, StudioAccentUi, CircleShape)
                            .background(Color(0x1E000000))
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = selectedEraserWidth,
                    onValueChange = onEraserWidthSelected,
                    valueRange = 20f..200f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Presets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(30f to "S", 70f to "M", 120f to "L", 180f to "XL").forEach { (size, label) ->
                    val isSelected = kotlin.math.abs(selectedEraserWidth - size) < 10f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ActiveToolHighlight else StudioControl)
                            .clickable { onEraserWidthSelected(size) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) StudioAccentUi else StudioText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Clear Canvas button
            Button(
                onClick = onClearCanvasRequested,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Rounded.DeleteSweep,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Clear Canvas",
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun LiveStrokePreview(
    tool: DrawingTool,
    strokeWidth: Float,
    color: Color,
    modifier: Modifier = Modifier,
    previewSize: Dp = 48.dp
) {
    Canvas(
        modifier = modifier
            .size(previewSize)
            .clip(RoundedCornerShape(12.dp))
            .background(StudioControl)
            .border(1.dp, StudioOutline, RoundedCornerShape(12.dp))
    ) {
        val points = listOf(
            Offset(size.width * 0.12f, size.height * 0.62f),
            Offset(size.width * 0.32f, size.height * 0.34f),
            Offset(size.width * 0.52f, size.height * 0.64f),
            Offset(size.width * 0.72f, size.height * 0.36f),
            Offset(size.width * 0.88f, size.height * 0.52f)
        )
        val sampleWidth = strokeWidth.coerceIn(2.dp.toPx(), 12.dp.toPx())
        val sampleStroke = DrawnStroke(
            id = 7L,
            color = color,
            strokeWidth = sampleWidth,
            alpha = if (tool == DrawingTool.WATERCOLOR) 0.68f else 1f,
            tool = tool,
            points = points
        )

        when (tool) {
            DrawingTool.WATERCOLOR -> WatercolorRenderer.draw(drawContext.canvas.nativeCanvas, sampleStroke)
            DrawingTool.CRAYON -> CrayonRenderer.drawCrayonStroke(drawContext.canvas.nativeCanvas, sampleStroke)
            DrawingTool.FILL -> drawCircle(color.copy(alpha = 0.78f), radius = size.minDimension * 0.27f)
            DrawingTool.ERASER -> {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, Color.White, style = Stroke(sampleWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            DrawingTool.LASSO -> {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path,
                    StudioAccent,
                    style = Stroke(
                        width = sampleWidth.coerceAtMost(2.dp.toPx()),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                        )
                    )
                )
            }
            DrawingTool.TEXT -> {
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    this.color = color.toArgb()
                    textSize = size.height * 0.48f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText("Aa", size.width / 2f, size.height * 0.68f, paint)
            }
            else -> {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                val width = if (tool == DrawingTool.BRUSH) sampleWidth * 1.8f else sampleWidth
                drawPath(path, color.copy(alpha = if (tool == DrawingTool.BRUSH) 0.85f else 1f), style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

@Composable
private fun RecentToolCaddy(
    tools: List<DrawingTool>,
    selectedTool: DrawingTool,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToolSelected: (DrawingTool) -> Unit,
    isOnLeft: Boolean,
    onDrag: (Offset) -> Unit,
    onDragFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = { currentOnDragFinished() },
            onDragCancel = { currentOnDragFinished() },
            onDrag = { change, dragAmount ->
                change.consume()
                currentOnDrag(dragAmount)
            }
        )
    }
    Box(modifier = modifier.width(68.dp).height(228.dp), contentAlignment = Alignment.Center) {
        if (isExpanded) {
            Surface(
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = StudioSurface,
                border = BorderStroke(1.dp, StudioOutline)
            ) {
                Column(
                    modifier = Modifier.padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp, 28.dp).then(dragModifier)
                            .semantics { contentDescription = "Drag tool caddy" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.DragHandle, contentDescription = null,
                            tint = StudioMutedText, modifier = Modifier.size(20.dp))
                    }
                    tools.take(3).forEach { tool ->
                val selected = selectedTool == tool
                val icon = when (tool) {
                    DrawingTool.PEN -> Icons.Rounded.Edit
                    DrawingTool.BRUSH -> Icons.Rounded.Brush
                    DrawingTool.WATERCOLOR -> Icons.Rounded.WaterDrop
                    DrawingTool.CRAYON -> Icons.Rounded.Draw
                    DrawingTool.FILL -> Icons.Rounded.FormatColorFill
                    DrawingTool.ERASER -> Icons.Rounded.AutoFixOff
                    DrawingTool.TEXT -> Icons.Rounded.TextFields
                    DrawingTool.LASSO -> Icons.Rounded.SelectAll
                }
                val label = when (tool) {
                    DrawingTool.PEN -> "Pen"
                    DrawingTool.BRUSH -> "Brush"
                    DrawingTool.WATERCOLOR -> "Watercolor"
                    DrawingTool.CRAYON -> "Crayon"
                    DrawingTool.FILL -> "Fill"
                    DrawingTool.ERASER -> "Eraser"
                    DrawingTool.TEXT -> "Text"
                    DrawingTool.LASSO -> "Select"
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (selected) StudioAccentSoft else StudioControl)
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) StudioAccentUi else StudioOutline,
                            shape = CircleShape
                        )
                        .clickable { onToolSelected(tool) }
                        .semantics {
                            contentDescription = if (selected) "$label tool, selected" else "$label tool"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) StudioAccentUi else StudioText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                    }
                    Box(
                        modifier = Modifier.size(48.dp, 28.dp).clip(CircleShape)
                            .clickable(onClick = onToggleExpanded)
                            .semantics { contentDescription = "Hide recent tools" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOnLeft) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = StudioMutedText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .align(if (isOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
                    .size(width = 24.dp, height = 64.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioOutline, RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggleExpanded)
                    .then(dragModifier)
                    .semantics { contentDescription = "Show recent tools; drag to move" },
                color = StudioSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOnLeft) Icons.Rounded.ChevronRight else Icons.Rounded.ChevronLeft,
                        contentDescription = null,
                        tint = StudioMutedText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioToolDock(
    selectedTool: DrawingTool,
    selectedStrokeWidth: Float,
    selectedEraserWidth: Float,
    selectedColor: Color,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onColorSelected: (Color) -> Unit,
    onToolSelected: (DrawingTool) -> Unit,
    onStrokeWidthSelected: (Float) -> Unit,
    onEraserWidthSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorMixer by remember { mutableStateOf(false) }
    if (showColorMixer) {
        ColorMixerDialog(
            initialColor = selectedColor,
            onColorMixed = { onColorSelected(it); showColorMixer = false },
            onDismiss = { showColorMixer = false }
        )
    }

    Surface(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = StudioSurface,
        border = BorderStroke(1.dp, StudioOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DrawerHandle(isExpanded, onExpandedChange)
            // This row remains anchored directly below the drawer handle in both states.
            PrimaryColorRow(selectedColor, onColorSelected)

            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSwatchRow(
                        colors = DrawingoPalette.grid.drop(1).flatten(),
                        selectedColor = selectedColor,
                        onColorSelected = onColorSelected,
                        firstColorIndex = 8,
                        horizontalScrollable = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(DrawingTool.PEN, Icons.Rounded.Edit, "Pen"),
                            Triple(DrawingTool.BRUSH, Icons.Rounded.Brush, "Brush"),
                            Triple(DrawingTool.WATERCOLOR, Icons.Rounded.WaterDrop, "Watercolor"),
                            Triple(DrawingTool.CRAYON, Icons.Rounded.Draw, "Crayon"),
                            Triple(DrawingTool.FILL, Icons.Rounded.FormatColorFill, "Fill"),
                            Triple(DrawingTool.ERASER, Icons.Rounded.AutoFixOff, "Eraser"),
                            Triple(DrawingTool.TEXT, Icons.Rounded.TextFields, "Text"),
                            Triple(DrawingTool.LASSO, Icons.Rounded.SelectAll, "Select")
                        ).forEach { (tool, icon, label) ->
                            val selected = selectedTool == tool
                            val hasMaterialPreview = tool in setOf(
                                DrawingTool.PEN,
                                DrawingTool.BRUSH,
                                DrawingTool.WATERCOLOR,
                                DrawingTool.CRAYON
                            )
                            Column(
                                modifier = Modifier
                                    .width(68.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) StudioAccentSoft else StudioControl)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) StudioAccentUi.copy(alpha = 0.55f) else StudioOutline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onToolSelected(tool) }
                                    .semantics { contentDescription = "$label tool" }
                                    .padding(vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                if (hasMaterialPreview) {
                                    LiveStrokePreview(
                                        tool = tool,
                                        strokeWidth = selectedStrokeWidth,
                                        color = selectedColor,
                                        previewSize = 34.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selected) StudioAccentUi else StudioText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = StudioText, maxLines = 1)
                            }
                        }
                    }

                    if (selectedTool != DrawingTool.LASSO) {
                        val eraserSelected = selectedTool == DrawingTool.ERASER
                        val size = if (eraserSelected) selectedEraserWidth else selectedStrokeWidth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (eraserSelected) "Eraser size" else "Stroke size", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StudioText)
                            Text("${size.toInt()} px", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = StudioMutedText)
                        }
                        StrokeSizeRow(
                            selectedStrokeWidth = size,
                            selectedTool = selectedTool,
                            selectedColor = selectedColor,
                            valueRange = if (eraserSelected) 20f..200f else 6f..160f,
                            onWidthSelected = { if (eraserSelected) onEraserWidthSelected(it) else onStrokeWidthSelected(it) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioControl)
                            .clickable { showColorMixer = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Colorize, contentDescription = null, tint = StudioText, modifier = Modifier.size(16.dp))
                        Text("Mix a color", fontWeight = FontWeight.SemiBold, color = StudioText, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolsPopoverCard(
    selectedTool: DrawingTool,
    selectedStrokeWidth: Float,
    selectedColor: Color,
    onToolSelected: (DrawingTool) -> Unit,
    onStrokeWidthSelected: (Float) -> Unit
) {
    val tools = listOf(
        Triple(DrawingTool.PEN, Icons.Rounded.Edit, "Pen"),
        Triple(DrawingTool.BRUSH, Icons.Rounded.Brush, "Brush"),
        Triple(DrawingTool.WATERCOLOR, Icons.Rounded.WaterDrop, "Watercolor"),
        Triple(DrawingTool.CRAYON, Icons.Rounded.Draw, "Crayon"),
        Triple(DrawingTool.FILL, Icons.Rounded.FormatColorFill, "Fill"),
        Triple(DrawingTool.TEXT, Icons.Rounded.TextFields, "Text"),
        Triple(DrawingTool.LASSO, Icons.Rounded.SelectAll, "Lasso")
    )

    Surface(
        modifier = Modifier.width(290.dp),
        shape = RoundedCornerShape(20.dp),
        color = StudioSurface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioOutline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Drawing Tools",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = StudioText
            )

            // Tools Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tools.forEach { (tool, icon, label) ->
                    val isSelected = selectedTool == tool
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ActiveToolHighlight else StudioControl)
                            .clickable { onToolSelected(tool) }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (isSelected) StudioAccentUi else StudioText,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            label,
                            color = StudioText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Brush Size",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = StudioText
                )
                Text(
                    "${selectedStrokeWidth.toInt()} px",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = StudioMutedText
                )
            }

            // Live Circle Preview + Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LiveStrokePreview(
                    tool = selectedTool,
                    strokeWidth = selectedStrokeWidth,
                    color = selectedColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = selectedStrokeWidth,
                    onValueChange = onStrokeWidthSelected,
                    valueRange = 4f..80f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Preset Sizes Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(6f to "Fine", 14f to "Medium", 28f to "Thick", 50f to "Broad").forEach { (size, label) ->
                    val isSelected = kotlin.math.abs(selectedStrokeWidth - size) < 4f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ActiveToolHighlight else StudioControl)
                            .clickable { onStrokeWidthSelected(size) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) StudioAccentUi else StudioText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingToolRail(
    selectedTool: DrawingTool,
    selectedStrokeWidth: Float,
    selectedEraserWidth: Float,
    selectedColor: Color,
    isToolsExpanded: Boolean,
    isEraserExpanded: Boolean,
    isLeftHanded: Boolean,
    onToggleToolsExpanded: () -> Unit,
    onToggleEraser: () -> Unit,
    onLongPressEraser: () -> Unit,
    onToolSelected: (DrawingTool) -> Unit,
    onStrokeWidthSelected: (Float) -> Unit,
    onEraserWidthSelected: (Float) -> Unit,
    onClearCanvasRequested: () -> Unit,
    onToggleHandedness: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEraserActive = selectedTool == DrawingTool.ERASER
    val isToolActive = !isEraserActive

    val currentIcon = when (selectedTool) {
        DrawingTool.PEN -> Icons.Rounded.Edit
        DrawingTool.BRUSH -> Icons.Rounded.Brush
        DrawingTool.WATERCOLOR -> Icons.Rounded.WaterDrop
        DrawingTool.CRAYON -> Icons.Rounded.Draw
        DrawingTool.FILL -> Icons.Rounded.FormatColorFill
        DrawingTool.TEXT -> Icons.Rounded.TextFields
        DrawingTool.LASSO -> Icons.Rounded.SelectAll
        DrawingTool.ERASER -> Icons.Rounded.Brush
    }
    val toolLabel = when (selectedTool) {
        DrawingTool.PEN -> "Pen"
        DrawingTool.BRUSH -> "Brush"
        DrawingTool.WATERCOLOR -> "Water"
        DrawingTool.CRAYON -> "Crayon"
        DrawingTool.FILL -> "Fill"
        DrawingTool.TEXT -> "Text"
        DrawingTool.LASSO -> "Lasso"
        DrawingTool.ERASER -> "Tools"
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val popoversComposable: @Composable () -> Unit = {
                Box {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isEraserExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        EraserPopoverCard(
                            selectedEraserWidth = selectedEraserWidth,
                            onEraserWidthSelected = onEraserWidthSelected,
                            onClearCanvasRequested = onClearCanvasRequested
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isToolsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ToolsPopoverCard(
                            selectedTool = selectedTool,
                            selectedStrokeWidth = selectedStrokeWidth,
                            selectedColor = selectedColor,
                            onToolSelected = onToolSelected,
                            onStrokeWidthSelected = onStrokeWidthSelected
                        )
                    }
                }
            }

            val railButtonsComposable: @Composable () -> Unit = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Eraser Button (FIRST / TOP)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isEraserActive) ActiveToolHighlight else StudioSurface)
                                .border(
                                    if (isEraserActive) 2.dp else 1.2.dp,
                                    if (isEraserActive) StudioAccentUi else StudioOutline,
                                    CircleShape
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onToggleEraser() },
                                        onLongPress = { onLongPressEraser() }
                                    )
                                }
                                .semantics { contentDescription = "Eraser tool. Tap to toggle, long-press for size." },
                            contentAlignment = Alignment.Center
                        ) {
                            EraserIcon(
                                tint = if (isEraserActive) StudioAccentUi else StudioText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            "Eraser",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioText
                        )
                    }

                    // 2. Tools Button (SECOND / BOTTOM)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isToolActive) StudioSurface else StudioControl)
                                .border(
                                    if (isToolActive) 1.8.dp else 1.dp,
                                    if (isToolActive) StudioAccentUi else StudioOutline,
                                    CircleShape
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onToggleToolsExpanded() }
                                    )
                                }
                                .semantics { contentDescription = "Drawing tools" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentIcon,
                                contentDescription = toolLabel,
                                tint = if (isToolActive) StudioAccentUi else StudioMutedText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            toolLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToolActive) StudioText else StudioMutedText
                        )
                    }

                    // 3. Handedness Toggle Icon
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(StudioSurface)
                            .border(1.dp, StudioOutline, CircleShape)
                            .clickable { onToggleHandedness() }
                            .semantics { contentDescription = "Switch controls to ${if (isLeftHanded) "right" else "left"} side" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = StudioMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (isLeftHanded) {
                // Rail on left: buttons on left, popover opens to right
                railButtonsComposable()
                popoversComposable()
            } else {
                // Rail on right: popover opens to left, buttons on right
                popoversComposable()
                railButtonsComposable()
            }
        }
    }
}

@Composable
fun FloatingColorPalette(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isLeftHanded: Boolean,
    modifier: Modifier = Modifier
) {
    var showColorMixer by remember { mutableStateOf(false) }

    if (showColorMixer) {
        ColorMixerDialog(
            initialColor = selectedColor,
            onColorMixed = { color ->
                onColorSelected(color)
                showColorMixer = false
            },
            onDismiss = { showColorMixer = false }
        )
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val buttonComposable: @Composable () -> Unit = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(StudioSurface)
                            .border(1.2.dp, StudioOutline, CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onToggleExpanded() }
                                )
                            }
                            .semantics { contentDescription = "Color palette" },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(selectedColor)
                                .border(1.5.dp, Color(0x35000000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val lum = selectedColor.red * 0.299f + selectedColor.green * 0.587f + selectedColor.blue * 0.114f
                            val emblemTint = if (lum < 0.55f) Color.White else Color(0xFF222222)
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = emblemTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        "Color",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioText
                    )
                }
            }

            val popoverComposable: @Composable () -> Unit = {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.width(280.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = StudioSurface,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioOutline)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Color Palette",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = StudioText,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            PrimaryColorRow(selectedColor, onColorSelected)
                            DrawingoPalette.grid.drop(1).forEachIndexed { index, colors ->
                                ColorSwatchRow(colors, selectedColor, onColorSelected, firstColorIndex = index * 8 + 8)
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StudioControl)
                                    .clickable { showColorMixer = true }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Colorize, contentDescription = null, tint = StudioText, modifier = Modifier.size(16.dp))
                                Text("Mix Custom Color", fontWeight = FontWeight.Bold, color = StudioText, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (isLeftHanded) {
                // Palette on right side: popover to left, button on right
                popoverComposable()
                buttonComposable()
            } else {
                // Palette on left side: button on left, popover to right
                buttonComposable()
                popoverComposable()
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
                .background(StudioOutline)
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
                .border(if (selected) 3.dp else 1.dp, if (selected) StudioAccentUi else StudioOutline, CircleShape),
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
    firstColorIndex: Int,
    horizontalScrollable: Boolean = false
) {
    if (horizontalScrollable) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEachIndexed { index, color ->
                ColorSwatch(
                    color = color,
                    selected = color == selectedColor,
                    colorName = ALL_COLOR_NAMES.getOrElse(firstColorIndex + index) { "Custom" },
                    onClick = { onColorSelected(color) },
                    touchSize = 44.dp
                )
            }
        }
    } else {
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
}

private val ALL_COLOR_NAMES = listOf(
    "Black", "Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink",
    "Charcoal", "Deep crimson", "Deep burnt orange", "Amber", "Forest green", "Midnight navy", "Deep purple", "Electric rose",
    "Slate grey", "Coral", "Tangerine", "Sunshine gold", "Lime green", "Sky blue", "Plum", "Fuchsia",
    "White", "Blush pink", "Soft peach", "Lemon pastel", "Mint green", "Periwinkle", "Soft lavender", "Rose pink"
)

@Composable
private fun StrokeSizeRow(
    selectedStrokeWidth: Float,
    selectedTool: DrawingTool,
    selectedColor: Color,
    valueRange: ClosedFloatingPointRange<Float>,
    onWidthSelected: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiveStrokePreview(
            tool = selectedTool,
            strokeWidth = selectedStrokeWidth,
            color = selectedColor,
            previewSize = 40.dp
        )
        Slider(
            value = selectedStrokeWidth.coerceIn(valueRange),
            onValueChange = onWidthSelected,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = StudioAccentUi,
                activeTrackColor = StudioAccentUi,
                inactiveTrackColor = StudioOutline
            )
        )
    }
}

private fun DrawScope.drawPaperStyle(style: CanvasPaperStyle) {
    when (style) {
        CanvasPaperStyle.PURE_WHITE -> {
            drawRect(color = Color(0xFFFAFAF7))
            val shadowColor = Color(0x0C000000)
            val edgeDepth = 24.dp.toPx()
            drawRect(
                brush = Brush.verticalGradient(listOf(shadowColor, Color.Transparent), 0f, edgeDepth),
                size = Size(size.width, edgeDepth)
            )
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, shadowColor), size.height - edgeDepth, size.height),
                topLeft = Offset(0f, size.height - edgeDepth),
                size = Size(size.width, edgeDepth)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(shadowColor, Color.Transparent), 0f, edgeDepth),
                size = Size(edgeDepth, size.height)
            )
            drawRect(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, shadowColor), size.width - edgeDepth, size.width),
                topLeft = Offset(size.width - edgeDepth, 0f),
                size = Size(edgeDepth, size.height)
            )
        }
        CanvasPaperStyle.GRID -> {
            drawRect(color = Color(0xFFF8F9FA))
            val gridSize = 40.dp.toPx()
            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = Color(0xFFDEE2E6),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = Color(0xFFDEE2E6),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        CanvasPaperStyle.RULED -> {
            drawRect(color = Color(0xFFFDFBF7))
            val spacing = 48.dp.toPx()
            
            // Draw margin line
            drawLine(
                color = Color(0xFFFFCDD2).copy(alpha = 0.6f),
                start = Offset(64.dp.toPx(), 0f),
                end = Offset(64.dp.toPx(), size.height),
                strokeWidth = 2.dp.toPx()
            )
            
            for (y in 0..size.height.toInt() step spacing.toInt()) {
                if (y == 0) continue
                drawLine(
                    color = Color(0xFF90CAF9).copy(alpha = 0.5f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        CanvasPaperStyle.KRAFT -> {
            drawRect(color = Color(0xFFD4B594))
            // simple texture dots
            val stride = 12.dp.toPx().toInt()
            for (x in 0..size.width.toInt() step stride) {
                for (y in 0..size.height.toInt() step stride) {
                    val noise = (x * y * 31 % 100) / 100f
                    if (noise > 0.5f) {
                        drawCircle(
                            color = Color(0xFF8B5A2B).copy(alpha = 0.1f + (noise * 0.1f)),
                            radius = 2f,
                            center = Offset(x.toFloat() + (noise * 5f), y.toFloat() + (noise * 5f))
                        )
                    }
                }
            }
        }
    }
}

private data class StrokeReplayEntry(
    val stroke: DrawnStroke,
    val startDistance: Float,
    val durationDistance: Float
)

private data class StrokeReplayTimeline(
    val entries: List<StrokeReplayEntry>,
    val totalDistance: Float
) {
    val durationMs: Long = ((totalDistance / 700f) * 1000f).toLong().coerceIn(1_200L, 24_000L)

    fun fractionsAt(progress: Float): Map<Long, Float> {
        val distance = totalDistance * progress.coerceIn(0f, 1f)
        return entries.mapNotNull { entry ->
            val revealed = ((distance - entry.startDistance) / entry.durationDistance).coerceIn(0f, 1f)
            if (revealed > 0f) entry.stroke.id to revealed else null
        }.toMap()
    }
}

private fun buildStrokeReplayTimeline(
    layers: List<com.example.drawingo.model.DrawingLayer>,
    frameIndex: Int
): StrokeReplayTimeline {
    var distance = 0f
    val entries = buildList {
        layers.filter { it.isVisible && it.opacity > 0f }.forEach { layer ->
            layer.frames.getOrNull(frameIndex)?.strokes.orEmpty().filter { it.tool != DrawingTool.FILL }.forEach { stroke ->
                val pathDistance = stroke.points.zipWithNext { a, b ->
                    kotlin.math.hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble()).toFloat()
                }.sum()
                val durationDistance = pathDistance.coerceAtLeast((stroke.strokeWidth * 2f).coerceAtLeast(16f))
                add(StrokeReplayEntry(stroke, distance, durationDistance))
                distance += durationDistance
            }
        }
    }
    return StrokeReplayTimeline(entries, distance)
}

private fun DrawnStroke.revealedTo(fraction: Float): DrawnStroke {
    if (fraction >= 1f) return this
    if (points.size < 2 || tool == DrawingTool.TEXT) return copy(points = emptyList(), pressures = emptyList())

    val totalLength = points.zipWithNext { a, b ->
        kotlin.math.hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble()).toFloat()
    }.sum()
    val targetLength = totalLength * fraction.coerceIn(0f, 1f)
    if (targetLength <= 0f) return copy(points = emptyList(), pressures = emptyList())

    val revealedPoints = mutableListOf(points.first())
    val revealedPressures = if (pressures.size == points.size) mutableListOf(pressures.first()) else null
    var coveredLength = 0f
    for (index in 1 until points.size) {
        val start = points[index - 1]
        val end = points[index]
        val segmentLength = kotlin.math.hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble()).toFloat()
        if (coveredLength + segmentLength >= targetLength && segmentLength > 0f) {
            val segmentFraction = ((targetLength - coveredLength) / segmentLength).coerceIn(0f, 1f)
            revealedPoints += Offset(
                start.x + (end.x - start.x) * segmentFraction,
                start.y + (end.y - start.y) * segmentFraction
            )
            if (revealedPressures != null) {
                val startPressure = pressures[index - 1]
                val endPressure = pressures[index]
                revealedPressures += startPressure + (endPressure - startPressure) * segmentFraction
            }
            break
        }
        revealedPoints += end
        if (revealedPressures != null) revealedPressures += pressures[index]
        coveredLength += segmentLength
    }
    return copy(
        points = revealedPoints,
        pressures = revealedPressures ?: pressures,
        boundingBox = DrawnStroke.calculateBounds(revealedPoints)
    )
}

private fun DrawScope.drawAllCanvasContent(
    layers: List<com.example.drawingo.model.DrawingLayer>,
    activeStrokes: List<DrawnStroke>,
    activeLayerId: Long?,
    currentFrameIndex: Int,
    onionSkinEnabled: Boolean,
    activePresets: Set<DrawingViewModel.QuickMagicPreset>,
    strokeRevealFractions: Map<Long, Float>? = null,
    selectedStrokeIds: Set<Long> = emptySet(),
    lassoTransform: Offset = Offset.Zero,
    timeMs: Long
) {
    // Determine the global target group for animation across all layers
    val globalTargetStrokes = mutableListOf<DrawnStroke>()
    if (selectedStrokeIds.isNotEmpty()) {
        layers.forEach { layer ->
            val currentFrame = layer.frames.getOrNull(currentFrameIndex)
            currentFrame?.strokes?.forEach { if (it.id in selectedStrokeIds) globalTargetStrokes.add(it) }
        }
    } else {
        layers.forEach { layer ->
            if (layer.isVisible && layer.opacity > 0f) {
                val currentFrame = layer.frames.getOrNull(currentFrameIndex)
                currentFrame?.strokes?.let { globalTargetStrokes.addAll(it) }
            }
        }
    }
    
    // Calculate the unified global center and bounds of the target group
    var groupCenter: Offset? = null
    var groupBounds: androidx.compose.ui.geometry.Rect? = null
    if (globalTargetStrokes.isNotEmpty()) {
        var minX = Float.POSITIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        
        globalTargetStrokes.forEach { stroke ->
            val points = if (stroke.id in selectedStrokeIds) {
                stroke.points.map { Offset(it.x + lassoTransform.x, it.y + lassoTransform.y) }
            } else stroke.points
            
            points.forEach { p ->
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
        }
        if (minX != Float.POSITIVE_INFINITY) {
            groupCenter = Offset((minX + maxX) / 2f, (minY + maxY) / 2f)
            groupBounds = androidx.compose.ui.geometry.Rect(minX, minY, maxX, maxY)
        }
    }

    for (layer in layers) {
        if (!layer.isVisible) continue
        
        val layerAlpha = layer.opacity
        if (layerAlpha <= 0f) continue
        
        // Draw onion skin (previous frame)
        if (onionSkinEnabled && currentFrameIndex > 0) {
            val prevFrame = layer.frames.getOrNull(currentFrameIndex - 1)
            prevFrame?.strokes?.sortedBy { if (it.tool == DrawingTool.FILL) 0 else 1 }?.forEach { stroke ->
                val ghostStroke = stroke.copy(alpha = stroke.alpha * layerAlpha * 0.3f)
                drawSingleStroke(ghostStroke)
            }
        }
        
        // Draw current frame
        val currentFrame = layer.frames.getOrNull(currentFrameIndex)
        currentFrame?.strokes?.filter { it.tool == DrawingTool.FILL }?.forEach { stroke ->
            drawSingleStroke(stroke.copy(alpha = stroke.alpha * layerAlpha))
        }
        currentFrame?.strokes?.filter { it.tool != DrawingTool.FILL }?.forEach { stroke ->
            val revealFraction = strokeRevealFractions?.get(stroke.id)
            if (strokeRevealFractions != null && revealFraction == null) return@forEach
            var finalStroke = stroke
            
            // Apply visual transform if this stroke is currently selected by the lasso
            if (stroke.id in selectedStrokeIds) {
                val shiftedPoints = stroke.points.map { Offset(it.x + lassoTransform.x, it.y + lassoTransform.y) }
                finalStroke = stroke.copy(points = shiftedPoints)
            }
            
            val strokeWithAlpha = finalStroke.copy(alpha = finalStroke.alpha * layerAlpha)
            val replayStroke = if (strokeRevealFractions != null) {
                strokeWithAlpha.revealedTo(revealFraction ?: 0f)
            } else strokeWithAlpha
            
            if (activePresets.isNotEmpty()) {
                // Animate only if no strokes are selected, OR if this specific stroke is selected
                if (selectedStrokeIds.isEmpty() || stroke.id in selectedStrokeIds) {
                    val magicStroke = processMagicStroke(replayStroke, activePresets, timeMs, groupCenter, groupBounds, size)
                    if (magicStroke != null) drawSingleStroke(magicStroke)
                } else {
                    drawSingleStroke(replayStroke)
                }
            } else {
                drawSingleStroke(replayStroke)
            }
            
            // Draw selection highlight bounding box only if no animation is currently active
            if (stroke.id in selectedStrokeIds && activePresets.isEmpty()) {
                val bounds = DrawnStroke.calculateBounds(finalStroke.points)
                drawRect(
                    color = StudioAccent.copy(alpha = 0.3f),
                    topLeft = Offset(bounds.left, bounds.top),
                    size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }
        }
        
        // Draw the active in-flight strokes if this is the active layer
        if (layer.id == activeLayerId) {
            for (stroke in activeStrokes) {
                // If it's a lasso drag stroke (-1L), we don't draw it here, we already transformed the selected strokes visually
                if (stroke.id == -1L && stroke.tool == DrawingTool.LASSO) continue
                drawSingleStroke(stroke.copy(alpha = stroke.alpha * layerAlpha))
            }
        }
    }
}

private fun DrawScope.drawSingleStroke(stroke: DrawnStroke) {
    if (stroke.points.isEmpty()) return

    if (stroke.tool == DrawingTool.FILL) {
        val fillPath = Path().apply {
            moveTo(stroke.points.first().x, stroke.points.first().y)
            stroke.points.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
        drawPath(fillPath, stroke.color.copy(alpha = stroke.alpha))
        return
    }
    
    // Normalize single point strokes into a tiny segment so cap=Round draws a perfect circle
    val s = if (stroke.points.size == 1) {
        val p = stroke.points[0]
        stroke.copy(
            points = listOf(p, Offset(p.x + 0.1f, p.y)),
            pressures = if (stroke.pressures.size == 1) listOf(stroke.pressures[0], stroke.pressures[0]) else stroke.pressures
        )
    } else {
        stroke
    }
    
    // If the stroke supports pressure and we have pressure data, draw variable width
    if ((s.tool == DrawingTool.PEN || s.tool == DrawingTool.BRUSH) && s.pressures.size == s.points.size) {
        var previousX = s.points[0].x
        var previousY = s.points[0].y
        var previousPressure = s.pressures[0]
        
        var lastDrawX = previousX
        var lastDrawY = previousY
        
        for (i in 1 until s.points.size) {
            val currentX = s.points[i].x
            val currentY = s.points[i].y
            val currentPressure = s.pressures[i]
            
            val midX = (previousX + currentX) / 2f
            val midY = (previousY + currentY) / 2f
            
            // Map pressure to width (smooth transition between previous and current pressure)
            val avgWidth = s.strokeWidth * (0.2f + ((previousPressure + currentPressure) / 2f) * 1.5f)
            
            val segmentPath = Path()
            segmentPath.moveTo(lastDrawX, lastDrawY)
            segmentPath.quadraticTo(previousX, previousY, midX, midY)
            
            drawPath(
                path = segmentPath,
                color = s.color.copy(alpha = s.alpha),
                style = Stroke(width = avgWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            
            lastDrawX = midX
            lastDrawY = midY
            previousX = currentX
            previousY = currentY
            previousPressure = currentPressure
        }
        
        // Final segment connecting to the last point
        val finalPath = Path()
        finalPath.moveTo(lastDrawX, lastDrawY)
        finalPath.lineTo(previousX, previousY)
        val finalWidth = s.strokeWidth * (0.2f + previousPressure * 1.5f)
        
        drawPath(
            path = finalPath,
            color = s.color.copy(alpha = s.alpha),
            style = Stroke(width = finalWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        return
    }

    // Standard uniform path drawing (fallback or for non-pressure tools)
    val path = Path()
    var previousX = s.points[0].x
    var previousY = s.points[0].y
    path.moveTo(previousX, previousY)
    for (i in 1 until s.points.size) {
        val currentX = s.points[i].x
        val currentY = s.points[i].y
        val midX = (previousX + currentX) / 2f
        val midY = (previousY + currentY) / 2f
        path.quadraticTo(previousX, previousY, midX, midY)
        previousX = currentX
        previousY = currentY
    }
    path.lineTo(previousX, previousY)
    when (s.tool) {
        DrawingTool.WATERCOLOR -> {
            WatercolorRenderer.draw(drawContext.canvas.nativeCanvas, s)
        }
        DrawingTool.CRAYON -> {
            CrayonRenderer.drawCrayonStroke(drawContext.canvas.nativeCanvas, s)
        }
        DrawingTool.ERASER -> {
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = s.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                blendMode = BlendMode.Clear
            )
        }
        DrawingTool.TEXT -> {
            val text = s.text
            if (text != null) {
                val paint = android.graphics.Paint().apply {
                    color = s.color.toArgb()
                    textSize = s.strokeWidth
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    previousX,
                    previousY + (paint.textSize / 3f), // approximate vertical centering
                    paint
                )
            }
        }
        else -> drawPath(
            path = path,
            color = s.color.copy(alpha = s.alpha),
            style = Stroke(width = s.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
