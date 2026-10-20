package com.example.drawingo.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.material3.AlertDialog
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
import com.example.drawingo.AppPreferences
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.util.CanvasBitmapUtils
import com.example.drawingo.util.CrayonRenderer

val ToolDockBackgroundColor = com.example.drawingo.theme.MidnightIndigo
val DockIconBackground = com.example.drawingo.theme.IndigoGloss
val ActiveToolHighlight = com.example.drawingo.theme.LuxGold
val DockShadow = Color(0x33000000)

@Composable
fun DrawingCanvas(
    viewModel: DrawingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedTool by viewModel.selectedTool.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val selectedStrokeWidth by viewModel.selectedStrokeWidth.collectAsState()
    val selectedPaperStyle by viewModel.selectedPaperStyle.collectAsState()
    val symmetryMode by viewModel.symmetryMode.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val selectedEraserWidth by viewModel.selectedEraserWidth.collectAsState()

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

    var showToolsPopover by remember { mutableStateOf(false) }
    var showPalettePopover by remember { mutableStateOf(false) }
    var showEraserPopover by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var isLeftHanded by remember { mutableStateOf(false) }
    var lastDrawingTool by remember { mutableStateOf(if (selectedTool != DrawingTool.ERASER) selectedTool else DrawingTool.PEN) }

    LaunchedEffect(isDrawing) {
        if (isDrawing) {
            showToolsPopover = false
            showPalettePopover = false
            showEraserPopover = false
        }
    }
    
    val layers by viewModel.layers.collectAsState()
    val selectedLayerId by viewModel.selectedLayerId.collectAsState()
    
    val selectedStrokes by viewModel.selectedStrokes.collectAsState()
    val lassoTransform by viewModel.lassoTransform.collectAsState()

    // Animation States
    val activeAnimationScene by viewModel.activeAnimationScene.collectAsState()
    val animatedEntity by viewModel.animatedEntity.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val animationProgress by viewModel.animationProgress.collectAsState()

    var showAppSettings by remember { mutableStateOf(false) }
    var showApiLogsPage by remember { mutableStateOf(false) }
    var showPaperStyleMenu by remember { mutableStateOf(false) }
    var showLayersMenu by remember { mutableStateOf(false) }
    var showAnimationDock by remember { mutableStateOf(false) }
    var showMenus by remember { mutableStateOf(false) }
    var isZenMode by remember { mutableStateOf(false) }
    val showTextPrompt by viewModel.showTextPrompt.collectAsState()
    
    // Animation States
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsState()
    val isPlayingLocalAnimation by viewModel.isPlayingLocalAnimation.collectAsState()
    val onionSkinEnabled by viewModel.onionSkinEnabled.collectAsState()
    val activePresets by viewModel.activePresets.collectAsState()
    val backgroundImagePath by viewModel.backgroundImagePath.collectAsState()

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
                            activePresets = activePresets,
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
                onExportClick = { format -> 
                    val flatStrokes = layers.flatMap { layer -> layer.frames.getOrNull(currentFrameIndex)?.strokes ?: emptyList() }
                    val bitmap = CanvasBitmapUtils.createBitmapFromStrokes(flatStrokes, viewModel.screenWidth.toInt(), viewModel.screenHeight.toInt(), backgroundImagePath = backgroundImagePath)
                    com.example.drawingo.util.ExportUtils.saveBitmapToGallery(context, bitmap, "Drawingo_${System.currentTimeMillis()}", format == "PNG")
                },
                onGalleryClick = onNavigateBack,
                onSettingsClick = { showAppSettings = true },
                onApiLogsClick = { showApiLogsPage = true },
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (!isZenMode) {
            FloatingColorPalette(
                selectedColor = selectedColor,
                onColorSelected = { viewModel.setColor(it) },
                isExpanded = showPalettePopover,
                onToggleExpanded = {
                    showPalettePopover = !showPalettePopover
                    if (showPalettePopover) {
                        showToolsPopover = false
                        showEraserPopover = false
                    }
                },
                isLeftHanded = isLeftHanded,
                modifier = Modifier
                    .align(if (isLeftHanded) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 16.dp)
                    .graphicsLayer { alpha = controlsAlpha }
            )

            FloatingToolRail(
                selectedTool = selectedTool,
                selectedStrokeWidth = selectedStrokeWidth,
                selectedEraserWidth = selectedEraserWidth,
                selectedColor = selectedColor,
                isToolsExpanded = showToolsPopover,
                isEraserExpanded = showEraserPopover,
                isLeftHanded = isLeftHanded,
                onToggleToolsExpanded = {
                    showToolsPopover = !showToolsPopover
                    if (showToolsPopover) {
                        showPalettePopover = false
                        showEraserPopover = false
                        if (selectedTool == DrawingTool.ERASER) {
                            viewModel.setTool(lastDrawingTool)
                        }
                    }
                },
                onToggleEraser = {
                    if (selectedTool == DrawingTool.ERASER) {
                        viewModel.setTool(lastDrawingTool)
                        showEraserPopover = false
                    } else {
                        lastDrawingTool = selectedTool
                        viewModel.setTool(DrawingTool.ERASER)
                        showToolsPopover = false
                    }
                },
                onLongPressEraser = {
                    showEraserPopover = true
                    showToolsPopover = false
                    showPalettePopover = false
                },
                onToolSelected = { tool ->
                    lastDrawingTool = tool
                    viewModel.setTool(tool)
                },
                onStrokeWidthSelected = { viewModel.setStrokeWidth(it) },
                onEraserWidthSelected = { viewModel.setEraserWidth(it) },
                onClearCanvasRequested = { showClearConfirmDialog = true },
                onToggleHandedness = { isLeftHanded = !isLeftHanded },
                modifier = Modifier
                    .align(if (isLeftHanded) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
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
                color = ToolDockBackgroundColor.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, com.example.drawingo.theme.SoftGold.copy(alpha = 0.7f)),
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
                        tint = com.example.drawingo.theme.LuxGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Exit Focus Mode",
                        color = Color.White,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Clear", color = Color.White)
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
                    onionSkinEnabled = onionSkinEnabled,
                    activePresets = activePresets,
                    onFrameSelect = { viewModel.selectFrame(it) },
                    onAddFrame = { viewModel.addFrame() },
                    onDuplicateFrame = { viewModel.duplicateFrame() },
                    onDeleteFrame = { viewModel.deleteFrame(it) },
                    onTogglePlayback = { viewModel.toggleLocalPlayback() },
                    onToggleOnionSkin = { viewModel.toggleOnionSkin() },
                    onTogglePreset = { viewModel.toggleQuickMagicPreset(it) },
                    onClearPresets = { viewModel.clearQuickMagicPresets() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showPaperStyleMenu) {
            Dialog(onDismissRequest = { showPaperStyleMenu = false }) {
                Surface(
                    modifier = Modifier.width(320.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFFFEFA),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Paper Style",
                            color = Color(0xFF343849),
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
                                        .background(if (style == selectedPaperStyle) Color(0xFFE9E2F0) else Color.Transparent)
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
                                        color = if (style == selectedPaperStyle) Color(0xFF69489B) else Color(0xFF343849)
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

        if (showAppSettings) {
            AppSettingsDialog(
                cloudAiEnabled = AppPreferences.isCloudAiAllowed(context),
                backendUrl = AppPreferences.getBackendUrl(context),
                onCloudAiEnabledChanged = { AppPreferences.setCloudAiAllowed(context, it) },
                onBackendUrlSaved = { AppPreferences.setBackendUrl(context, it) },
                onOpenApiLogs = {
                    showAppSettings = false
                    showApiLogsPage = true
                },
                onDismiss = { showAppSettings = false }
            )
        }

        if (showApiLogsPage) {
            ApiLogsScreen(
                onBack = { showApiLogsPage = false }
            )
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
    onExportClick: (String) -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onApiLogsClick: () -> Unit,
    viewModel: DrawingViewModel,
    modifier: Modifier = Modifier
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    val symmetryMode by viewModel.symmetryMode.collectAsState()

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(ToolDockBackgroundColor)
            .border(1.dp, com.example.drawingo.theme.SoftIndigo, RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactAction(Icons.Rounded.Close, "Hide menus", size = 44.dp, onClick = onHideMenuClick)
            CompactAction(Icons.Rounded.Image, "Gallery", size = 44.dp, onClick = onGalleryClick)
            CompactAction(
                icon = Icons.Rounded.CenterFocusWeak,
                label = "Focus Mode",
                size = 44.dp,
                onClick = onZenModeClick
            )
            CompactAction(
                icon = Icons.Rounded.AutoAwesome,
                label = "Animation",
                selected = isAnimationDockVisible,
                size = 44.dp,
                onClick = onToggleAnimationDock
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactAction(Icons.Rounded.Layers, "Layers", size = 40.dp, onClick = onLayersClick)
            CompactAction(Icons.AutoMirrored.Rounded.Undo, "Undo", enabled = canUndo, size = 40.dp, onClick = onUndo)
            CompactAction(Icons.AutoMirrored.Rounded.Redo, "Redo", enabled = canRedo, size = 40.dp, onClick = onRedo)
            CompactAction(
                icon = Icons.Rounded.Flip, 
                label = "Symmetry", 
                selected = symmetryMode != com.example.drawingo.model.SymmetryMode.NONE,
                size = 40.dp, 
                onClick = { viewModel.toggleSymmetryMode() }
            )
            CompactAction(Icons.Rounded.Delete, "Clear drawing", size = 40.dp, onClick = { showClearConfirmation = true })
            Box {
                CompactAction(Icons.Rounded.MoreVert, "More actions", size = 40.dp, onClick = { actionsExpanded = true })
                DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Paper style") },
                        onClick = { actionsExpanded = false; onPaperStyleClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Save as PNG") },
                        onClick = { actionsExpanded = false; onExportClick("PNG") }
                    )
                    DropdownMenuItem(
                        text = { Text("Save as JPEG") },
                        onClick = { actionsExpanded = false; onExportClick("JPEG") }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { actionsExpanded = false; onSettingsClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("API & Cloud logs") },
                        onClick = { actionsExpanded = false; onApiLogsClick() }
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
            .background(when { selected -> ActiveToolHighlight; enabled -> DockIconBackground; else -> Color(0xFF1E2036) })
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = when { selected -> com.example.drawingo.theme.ObsidianBlack; enabled -> Color.White; else -> Color(0xFF5A5D7A) }
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
        color = Color(0xFFFCFCFD),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x18000000))
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
                    color = Color(0xFF2C2F42)
                )
                Text(
                    "${selectedEraserWidth.toInt()} px",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
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
                        .background(Color(0xFFF1F2F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((selectedEraserWidth * 0.28f).coerceIn(8f, 38f).dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF4A4E68), CircleShape)
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
                            .background(if (isSelected) ActiveToolHighlight else Color(0xFFF1F2F6))
                            .clickable { onEraserWidthSelected(size) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) com.example.drawingo.theme.ObsidianBlack else Color(0xFF4A4E68)
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F2F6))
            .border(1.dp, Color(0x18000000), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val displaySize = (strokeWidth * 0.75f).coerceIn(4f, 38f).dp
        when (tool) {
            DrawingTool.WATERCOLOR -> {
                Box(
                    modifier = Modifier
                        .size(displaySize)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.45f))
                        .border(1.dp, color.copy(alpha = 0.85f), CircleShape)
                )
            }
            DrawingTool.CRAYON -> {
                Box(
                    modifier = Modifier
                        .size(displaySize)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.85f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
            }
            DrawingTool.BRUSH -> {
                Box(
                    modifier = Modifier
                        .size(displaySize)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.90f))
                )
            }
            DrawingTool.ERASER -> {
                Box(
                    modifier = Modifier
                        .size(displaySize)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFF6B7280), CircleShape)
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(displaySize)
                        .clip(CircleShape)
                        .background(color)
                )
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
        Triple(DrawingTool.TEXT, Icons.Rounded.TextFields, "Text"),
        Triple(DrawingTool.LASSO, Icons.Rounded.SelectAll, "Lasso")
    )

    Surface(
        modifier = Modifier.width(290.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFCFCFD),
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x18000000))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Drawing Tools",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF2C2F42)
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
                            .background(if (isSelected) ActiveToolHighlight else Color(0xFFF1F2F6))
                            .clickable { onToolSelected(tool) }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (isSelected) com.example.drawingo.theme.ObsidianBlack else Color(0xFF3B4058),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            label,
                            color = if (isSelected) com.example.drawingo.theme.ObsidianBlack else Color(0xFF3B4058),
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
                    color = Color(0xFF2C2F42)
                )
                Text(
                    "${selectedStrokeWidth.toInt()} px",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
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
                            .background(if (isSelected) ActiveToolHighlight else Color(0xFFF1F2F6))
                            .clickable { onStrokeWidthSelected(size) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) com.example.drawingo.theme.ObsidianBlack else Color(0xFF4A4E68)
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
        DrawingTool.TEXT -> Icons.Rounded.TextFields
        DrawingTool.LASSO -> Icons.Rounded.SelectAll
        DrawingTool.ERASER -> Icons.Rounded.Brush
    }
    val toolLabel = when (selectedTool) {
        DrawingTool.PEN -> "Pen"
        DrawingTool.BRUSH -> "Brush"
        DrawingTool.WATERCOLOR -> "Water"
        DrawingTool.CRAYON -> "Crayon"
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
                                .background(if (isEraserActive) ActiveToolHighlight else Color(0xF8FFFFFF))
                                .border(
                                    if (isEraserActive) 2.dp else 1.2.dp,
                                    if (isEraserActive) com.example.drawingo.theme.ObsidianBlack else Color(0x22000000),
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
                                tint = if (isEraserActive) com.example.drawingo.theme.ObsidianBlack else Color(0xFF3B4058),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            "Eraser",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2F42)
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
                                .background(if (isToolActive) Color(0xF8FFFFFF) else Color(0xD8ECECF0))
                                .border(
                                    if (isToolActive) 1.8.dp else 1.dp,
                                    if (isToolActive) com.example.drawingo.theme.SoftIndigo else Color(0x18000000),
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
                                tint = if (isToolActive) com.example.drawingo.theme.MidnightIndigo else Color(0x603B4058),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            toolLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToolActive) Color(0xFF2C2F42) else Color(0x702C2F42)
                        )
                    }

                    // 3. Handedness Toggle Icon
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xE0FFFFFF))
                            .border(1.dp, Color(0x18000000), CircleShape)
                            .clickable { onToggleHandedness() }
                            .semantics { contentDescription = "Switch controls to ${if (isLeftHanded) "right" else "left"} side" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF5A5D7A),
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
                            .background(Color(0xF8FFFFFF))
                            .border(1.2.dp, Color(0x22000000), CircleShape)
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
                        color = Color(0xFF2C2F42)
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
                        color = Color(0xFFFCFCFD),
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x18000000))
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
                                color = Color(0xFF2C2F42),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            PrimaryColorRow(selectedColor, onColorSelected)
                            DrawingoPalette.grid.drop(1).forEachIndexed { index, colors ->
                                ColorSwatchRow(colors, selectedColor, onColorSelected, firstColorIndex = index * 8 + 8)
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F2F6))
                                    .clickable { showColorMixer = true }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Colorize, contentDescription = null, tint = Color(0xFF3B4058), modifier = Modifier.size(16.dp))
                                Text("Mix Custom Color", fontWeight = FontWeight.Bold, color = Color(0xFF3B4058), fontSize = 11.sp)
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

private fun DrawScope.drawAllCanvasContent(
    layers: List<com.example.drawingo.model.DrawingLayer>,
    activeStrokes: List<DrawnStroke>,
    activeLayerId: Long?,
    currentFrameIndex: Int,
    onionSkinEnabled: Boolean,
    activePresets: Set<DrawingViewModel.QuickMagicPreset>,
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
            prevFrame?.strokes?.forEach { stroke ->
                val ghostStroke = stroke.copy(alpha = stroke.alpha * layerAlpha * 0.3f)
                drawSingleStroke(ghostStroke)
            }
        }
        
        // Draw current frame
        val currentFrame = layer.frames.getOrNull(currentFrameIndex)
        currentFrame?.strokes?.forEach { stroke ->
            var finalStroke = stroke
            
            // Apply visual transform if this stroke is currently selected by the lasso
            if (stroke.id in selectedStrokeIds) {
                val shiftedPoints = stroke.points.map { Offset(it.x + lassoTransform.x, it.y + lassoTransform.y) }
                finalStroke = stroke.copy(points = shiftedPoints)
            }
            
            val strokeWithAlpha = finalStroke.copy(alpha = finalStroke.alpha * layerAlpha)
            
            if (activePresets.isNotEmpty()) {
                // Animate only if no strokes are selected, OR if this specific stroke is selected
                if (selectedStrokeIds.isEmpty() || stroke.id in selectedStrokeIds) {
                    val magicStroke = processMagicStroke(strokeWithAlpha, activePresets, timeMs, groupCenter, groupBounds, size)
                    if (magicStroke != null) drawSingleStroke(magicStroke)
                } else {
                    drawSingleStroke(strokeWithAlpha)
                }
            } else {
                drawSingleStroke(strokeWithAlpha)
            }
            
            // Draw selection highlight bounding box only if no animation is currently active
            if (stroke.id in selectedStrokeIds && activePresets.isEmpty()) {
                val bounds = DrawnStroke.calculateBounds(finalStroke.points)
                drawRect(
                    color = Color(0xFFC53C65).copy(alpha = 0.3f),
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
            // A broad translucent wash with irregular pigment blooms reads as a wet medium.
            drawPath(path, s.color.copy(alpha = s.alpha * 0.24f), style = Stroke(s.strokeWidth * 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, s.color.copy(alpha = s.alpha * 0.32f), style = Stroke(s.strokeWidth * 1.12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawWatercolorBlooms(s)
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

private fun DrawScope.drawWatercolorBlooms(stroke: DrawnStroke) {
    val stride = (stroke.points.size / 32).coerceAtLeast(1)
    stroke.points.indices.step(stride).forEach { index ->
        val point = stroke.points[index]
        val seed = stroke.id xor (index.toLong() * 31L)
        
        val randRadiusMult = 0.4f + ((seed ushr 12 and 15).toFloat() / 15f) * 1.8f
        val offset = (((seed ushr 8) and 15).toFloat() - 7.5f) * stroke.strokeWidth * 0.12f
        val baseRadius = stroke.strokeWidth * (0.22f + ((seed and 7).toFloat() * 0.025f))
        val radius = (baseRadius * randRadiusMult).coerceAtLeast(1f)
        
        val randAlphaMult = 0.5f + ((seed ushr 16 and 15).toFloat() / 15f) * 1.0f
        val alpha = (stroke.alpha * 0.24f * randAlphaMult).coerceIn(0f, 1f)
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(stroke.color.copy(alpha = alpha), stroke.color.copy(alpha = 0f)),
                center = Offset(point.x + offset, point.y - offset),
                radius = radius
            ),
            radius = radius,
            center = Offset(point.x + offset, point.y - offset)
        )
        
        // Random stray droplets for more realistic liquid spatter
        if ((seed and 3L) == 0L) {
            val dropRadiusMult = 0.2f + ((seed ushr 20 and 7).toFloat() / 7f) * 1.5f
            val dropOffset = (((seed ushr 24) and 15).toFloat() - 7.5f) * stroke.strokeWidth * 0.35f
            val dropRadius = (stroke.strokeWidth * 0.06f * dropRadiusMult).coerceAtLeast(0.5f)
            val dropAlpha = (stroke.alpha * 0.35f).coerceIn(0f, 1f)
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(stroke.color.copy(alpha = dropAlpha), stroke.color.copy(alpha = 0f)),
                    center = Offset(point.x - dropOffset, point.y + dropOffset),
                    radius = dropRadius
                ),
                radius = dropRadius,
                center = Offset(point.x - dropOffset, point.y + dropOffset)
            )
        }
    }
}
