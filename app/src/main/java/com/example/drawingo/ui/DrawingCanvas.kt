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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import com.example.drawingo.R
import com.example.drawingo.animation.CanvasAnimationRenderer
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

    val canvasScale by viewModel.canvasScale.collectAsState()
    val canvasOffsetX by viewModel.canvasOffsetX.collectAsState()
    val canvasOffsetY by viewModel.canvasOffsetY.collectAsState()

    val completedStrokes by viewModel.completedStrokes.collectAsState()
    val activeStrokes by viewModel.activeStrokes.collectAsState()
    
    val layers by viewModel.layers.collectAsState()
    val selectedLayerId by viewModel.selectedLayerId.collectAsState()
    
    val selectedStrokes by viewModel.selectedStrokes.collectAsState()
    val lassoTransform by viewModel.lassoTransform.collectAsState()

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
    var showApiLogsPage by remember { mutableStateOf(false) }
    var showPaperStyleMenu by remember { mutableStateOf(false) }
    var showLayersMenu by remember { mutableStateOf(false) }
    var showAnimationDock by remember { mutableStateOf(false) }
    var showMenus by remember { mutableStateOf(false) }
    val showTextPrompt by viewModel.showTextPrompt.collectAsState()
    
    // Animation States
    val currentFrameIndex by viewModel.currentFrameIndex.collectAsState()
    val isPlayingLocalAnimation by viewModel.isPlayingLocalAnimation.collectAsState()
    val onionSkinEnabled by viewModel.onionSkinEnabled.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()
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
                                    val pressure = change.pressure

                                    if (change.changedToDown()) {
                                        viewModel.onPointerDown(pointerId, position, pressure)
                                        change.consume()
                                    } else if (change.pressed && change.positionChanged()) {
                                        change.historical.forEach { historicalChange ->
                                            // Fallback to current pressure if historical pressure isn't available
                                            // (Some older Compose versions didn't expose it, but it's usually there)
                                            val hPos = historicalChange.position
                                            // To be safe, we can just use the current change.pressure or check if historicalChange has it.
                                            // Actually, HistoricalChange DOES NOT have pressure in standard Compose APIs up to recently.
                                            // Wait, let's see if historicalChange.pressure compiles.
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
                if (!isAnimationActive || activeAnimationScene == null) {
                    drawPaperStyle(selectedPaperStyle)
                }
            }
            
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Needed for BlendMode.Clear to work properly without clearing the window background
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
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
                                activePreset = activePreset,
                                selectedStrokeIds = selectedStrokes,
                                lassoTransform = lassoTransform,
                                timeMs = frameTimeMs
                            )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showMenus,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            // Top Action Bar
            TopKeepBar(
                canUndo = canUndo,
                canRedo = canRedo,
                hasArtwork = layers.any { layer -> layer.frames.any { it.strokes.isNotEmpty() } },
                isAnimationActive = isAnimationActive,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onClear = { viewModel.clearCanvas() },
                onHideMenuClick = { showMenus = false },
                onAnimateDrawingClick = {
                    showAnimationDock = !showAnimationDock
                },
                onStopAnimationClick = {
                    viewModel.stopAnimation()
                },
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
        
        FloatingColorPalette(
            selectedColor = selectedColor,
            onColorSelected = { viewModel.setColor(it) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 60.dp)
        )
        FloatingToolsPalette(
            selectedTool = selectedTool,
            selectedStrokeWidth = selectedStrokeWidth,
            onToolClick = { viewModel.setTool(it) },
            onWidthSelected = { viewModel.setStrokeWidth(it) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, top = 60.dp)
        )
        FloatingEraserButton(
            selectedTool = selectedTool,
            onToggle = { 
                if (selectedTool == DrawingTool.ERASER) {
                    viewModel.setTool(DrawingTool.PEN) // Fallback when toggling off
                } else {
                    viewModel.setTool(DrawingTool.ERASER)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, top = 116.dp)
        )

        if (!showMenus) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(12.dp)
            ) {
                CompactAction(
                    icon = Icons.Rounded.Menu,
                    label = "Show menus",
                    onClick = { showMenus = true }
                )
            }
        }

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
                    activePreset = activePreset,
                    onFrameSelect = { viewModel.selectFrame(it) },
                    onAddFrame = { viewModel.addFrame() },
                    onDuplicateFrame = { viewModel.duplicateFrame() },
                    onDeleteFrame = { viewModel.deleteFrame(it) },
                    onTogglePlayback = { viewModel.toggleLocalPlayback() },
                    onToggleOnionSkin = { viewModel.toggleOnionSkin() },
                    onSelectPreset = { viewModel.setQuickMagicPreset(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                        color = com.example.drawingo.theme.SoftIndigo,
                        strokeWidth = 4.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text("✨", fontSize = 32.sp)
                }
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
    hasArtwork: Boolean,
    isAnimationActive: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onHideMenuClick: () -> Unit,
    onAnimateDrawingClick: () -> Unit,
    onStopAnimationClick: () -> Unit,
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
            if (isAnimationActive) {
                CompactAction(Icons.Rounded.ArrowBack, "Back to Drawing", size = 44.dp, onClick = onStopAnimationClick)
            } else {
                CompactAction(Icons.Rounded.Image, "Gallery", size = 44.dp, onClick = onGalleryClick)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasArtwork) com.example.drawingo.theme.LuxGold else com.example.drawingo.theme.IndigoGloss)
                    .clickable(enabled = hasArtwork, onClick = onAnimateDrawingClick)
                    .semantics { contentDescription = "Anime drawing" },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = if (hasArtwork) com.example.drawingo.theme.ObsidianBlack else Color(0xFF5A5D7A))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
            CompactAction(Icons.Rounded.Layers, "Layers", size = 40.dp, onClick = onLayersClick)
            CompactAction(Icons.Rounded.Undo, "Undo", enabled = canUndo, size = 40.dp, onClick = onUndo)
            CompactAction(Icons.Rounded.Redo, "Redo", enabled = canRedo, size = 40.dp, onClick = onRedo)
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
                    if (isAnimationActive) {
                        DropdownMenuItem(
                            text = { Text("Stop animation") },
                            onClick = { actionsExpanded = false; onStopAnimationClick() }
                        )
                    }
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
fun FloatingToolsPalette(
    selectedTool: DrawingTool,
    selectedStrokeWidth: Float,
    onToolClick: (DrawingTool) -> Unit,
    onWidthSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
    var sizesExpanded by remember { mutableStateOf(false) }

    val tools = listOf(
        Triple(DrawingTool.PEN, Icons.Rounded.Draw, "Pen"),
        Triple(DrawingTool.BRUSH, Icons.Rounded.Brush, "Brush"),
        Triple(DrawingTool.WATERCOLOR, Icons.Rounded.WaterDrop, "Watercolor"),
        Triple(DrawingTool.CRAYON, Icons.Rounded.Draw, "Crayon"),
        Triple(DrawingTool.LASSO, Icons.Rounded.Highlight, "Lasso"),
        Triple(DrawingTool.TEXT, Icons.Rounded.TextFields, "Text")
    )
    val currentIcon = tools.find { it.first == selectedTool }?.second ?: Icons.Rounded.Brush
    val isToolActive = selectedTool != DrawingTool.ERASER

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Floating tools button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(ToolDockBackgroundColor)
                    .border(
                        1.5.dp,
                        if (isToolActive) com.example.drawingo.theme.SoftIndigo else Color(0xFF4A4E68),
                        CircleShape
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var isDragging = false
                            val pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (change.changedToUp()) {
                                    if (!isDragging) {
                                        expanded = !expanded
                                    }
                                    break
                                }
                                if (change.isConsumed) break
                                val dx = change.position.x - change.previousPosition.x
                                val dy = change.position.y - change.previousPosition.y
                                if (!isDragging && kotlin.math.hypot(change.position.x - down.position.x, change.position.y - down.position.y) > 8f) {
                                    isDragging = true
                                }
                                if (isDragging) {
                                    change.consume()
                                    offsetX += dx
                                    offsetY += dy
                                }
                            }
                        }
                    }
                    .semantics { contentDescription = "Drawing tools" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentIcon,
                    contentDescription = "Tools",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Expanded tools dock
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ToolDockBackgroundColor)
                        .border(1.dp, com.example.drawingo.theme.SoftIndigo, RoundedCornerShape(20.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tools.forEach { (tool, icon, label) ->
                            val isSelected = selectedTool == tool
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) ActiveToolHighlight else DockIconBackground)
                                    .clickable { onToolClick(tool); expanded = false }
                                    .semantics { contentDescription = "$label tool" }
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isSelected) com.example.drawingo.theme.ObsidianBlack else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    label,
                                    color = if (isSelected) com.example.drawingo.theme.ObsidianBlack else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(DockIconBackground)
                                .clickable { sizesExpanded = !sizesExpanded }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Straighten, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Brush Size", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }
                        DropdownMenu(
                            expanded = sizesExpanded,
                            onDismissRequest = { sizesExpanded = false },
                            modifier = Modifier.background(Color(0xFFFFFEFA))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "Brush size",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF343849),
                                    fontSize = 12.sp
                                )
                                StrokeSizeRow(selectedStrokeWidth) { width -> 
                                    onWidthSelected(width)
                                    sizesExpanded = false 
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
fun FloatingColorPalette(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
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

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // Floating color palette button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(ToolDockBackgroundColor)
                    .border(1.5.dp, com.example.drawingo.theme.SoftIndigo, CircleShape)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var isDragging = false
                            val pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (change.changedToUp()) {
                                    if (!isDragging) {
                                        expanded = !expanded
                                    }
                                    break
                                }
                                if (change.isConsumed) break
                                val dx = change.position.x - change.previousPosition.x
                                val dy = change.position.y - change.previousPosition.y
                                if (!isDragging && kotlin.math.hypot(change.position.x - down.position.x, change.position.y - down.position.y) > 8f) {
                                    isDragging = true
                                }
                                if (isDragging) {
                                    change.consume()
                                    offsetX += dx
                                    offsetY += dy
                                }
                            }
                        }
                    }
                    .semantics { contentDescription = "Color palette" },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = "Color palette",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                // Selected color indicator badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                )
            }

            // Expanded color palette
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(280.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ToolDockBackgroundColor)
                        .border(1.dp, com.example.drawingo.theme.SoftIndigo, RoundedCornerShape(20.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryColorRow(selectedColor, onColorSelected)
                    DrawingoPalette.grid.drop(1).forEachIndexed { index, colors ->
                        ColorSwatchRow(colors, selectedColor, onColorSelected, firstColorIndex = index * 8 + 8)
                    }
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DockIconBackground)
                            .clickable { showColorMixer = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Colorize, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Mix Custom Color", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingEraserButton(
    selectedTool: DrawingTool,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val isEraser = selectedTool == DrawingTool.ERASER

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isEraser) ActiveToolHighlight else ToolDockBackgroundColor)
                .border(
                    1.5.dp,
                    if (isEraser) com.example.drawingo.theme.ObsidianBlack else com.example.drawingo.theme.SoftIndigo,
                    CircleShape
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var isDragging = false
                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (change.changedToUp()) {
                                if (!isDragging) {
                                    onToggle()
                                }
                                break
                            }
                            if (change.isConsumed) break
                            val dx = change.position.x - change.previousPosition.x
                            val dy = change.position.y - change.previousPosition.y
                            if (!isDragging && kotlin.math.hypot(change.position.x - down.position.x, change.position.y - down.position.y) > 8f) {
                                isDragging = true
                            }
                            if (isDragging) {
                                change.consume()
                                offsetX += dx
                                offsetY += dy
                            }
                        }
                    }
                }
                .semantics { contentDescription = "Eraser tool" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoFixHigh,
                contentDescription = "Eraser",
                tint = if (isEraser) com.example.drawingo.theme.ObsidianBlack else Color.White,
                modifier = Modifier.size(22.dp)
            )
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
            drawRect(color = Color.White)
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
    activePreset: DrawingViewModel.QuickMagicPreset,
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
            
            if (activePreset != DrawingViewModel.QuickMagicPreset.NONE) {
                // Animate only if no strokes are selected, OR if this specific stroke is selected
                if (selectedStrokeIds.isEmpty() || stroke.id in selectedStrokeIds) {
                    val magicStroke = processMagicStroke(strokeWithAlpha, activePreset, timeMs, groupCenter, groupBounds, size)
                    if (magicStroke != null) drawSingleStroke(magicStroke)
                } else {
                    drawSingleStroke(strokeWithAlpha)
                }
            } else {
                drawSingleStroke(strokeWithAlpha)
            }
            
            // Draw selection highlight bounding box only if no animation is currently active
            if (stroke.id in selectedStrokeIds && activePreset == DrawingViewModel.QuickMagicPreset.NONE) {
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


