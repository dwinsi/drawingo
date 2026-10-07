package com.example.drawingo.ui

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.viewinterop.AndroidView
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
    val isVideoGenerating by viewModel.isVideoGenerating.collectAsState()
    val generatedVideo by viewModel.generatedVideo.collectAsState()
    val videoError by viewModel.videoError.collectAsState()

    // Animation States
    val isAnimationActive by viewModel.isAnimationActive.collectAsState()
    val activeAnimationScene by viewModel.activeAnimationScene.collectAsState()
    val animatedEntity by viewModel.animatedEntity.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val animationProgress by viewModel.animationProgress.collectAsState()

    var showAppSettings by remember { mutableStateOf(false) }
    var showVideoPrompt by remember { mutableStateOf(false) }
    var videoPrompt by remember { mutableStateOf("Gently bring the main subject to life with calm, flowing movement.") }

    var frameTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis {
                frameTimeMs = System.currentTimeMillis()
            }
        }
    }
    LaunchedEffect(generatedVideo) {
        if (generatedVideo != null) showVideoPrompt = false
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
            onGenerateVideoClick = { showVideoPrompt = true },
            onStopAnimationClick = { viewModel.stopAnimation() },
            onSettingsClick = { showAppSettings = true },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        )

        if (showVideoPrompt) {
            Dialog(onDismissRequest = { if (!isVideoGenerating) showVideoPrompt = false }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.94f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFFFEFA),
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Bring your drawing to life", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text(
                            if (AppPreferences.isCloudAiAllowed(context))
                                "A copy of this drawing and your prompt will be sent to Google Cloud to create a 4-second video. The model may reinterpret the drawing, and each generation may incur a backend charge."
                            else "Cloud AI is off. Enable it in Settings before sending a drawing to Google Cloud.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555866)
                        )
                        OutlinedTextField(
                            value = videoPrompt,
                            onValueChange = { videoPrompt = it.take(800) },
                            label = { Text("Describe the motion") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )
                        videoError?.let { Text(it, color = Color(0xFFB4234D), style = MaterialTheme.typography.bodySmall) }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.TextButton(onClick = { showVideoPrompt = false }, enabled = !isVideoGenerating) { Text("Cancel") }
                            Button(
                                enabled = AppPreferences.isCloudAiAllowed(context) && !isVideoGenerating && completedStrokes.isNotEmpty(),
                                onClick = {
                                    val bitmap = CanvasBitmapUtils.createBitmapFromStrokes(completedStrokes)
                                    viewModel.generateVideo(context, bitmap, videoPrompt)
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53C65))
                            ) {
                                if (isVideoGenerating) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else Text("Create video")
                            }
                        }
                    }
                }
            }
        }

        if (generatedVideo != null) {
            Dialog(onDismissRequest = { viewModel.closeGeneratedVideo() }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFFFEFA),
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Your drawing in motion", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
                            factory = { viewContext ->
                                VideoView(viewContext).apply {
                                    val controller = MediaController(viewContext)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setVideoURI(Uri.fromFile(generatedVideo))
                                    setOnPreparedListener { it.isLooping = true; start() }
                                }
                            },
                            update = { view -> if (!view.isPlaying) view.start() }
                        )
                        Button(onClick = { viewModel.closeGeneratedVideo() }, modifier = Modifier.align(Alignment.End), shape = CircleShape) { Text("Done") }
                    }
                }
            }
        }

        if (isVideoGenerating) {
            Box(Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                    Row(Modifier.padding(22.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color(0xFFC53C65))
                        Text("Creating your video… this can take a few minutes.", color = Color(0xFF343849))
                    }
                }
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
    onGenerateVideoClick: () -> Unit,
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
            onAnimeClick = onAnimateDrawingClick,
            onGenerateVideoClick = onGenerateVideoClick
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
    onAnimeClick: () -> Unit,
    onGenerateVideoClick: () -> Unit
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (animeEnabled) Color(0xFFE4F2F0) else Color(0xFFF2F0EC))
                .clickable(enabled = animeEnabled, onClick = onGenerateVideoClick)
                .semantics { contentDescription = "Create AI video from drawing" },
            contentAlignment = Alignment.Center
        ) {
            Text("🎬", fontSize = 24.sp)
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
                            Triple(DrawingTool.WATERCOLOR, "💧", "Watercolor"),
                            Triple(DrawingTool.CRAYON, "🖍", "Crayon"),
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
                                if (tool == DrawingTool.WATERCOLOR || tool == DrawingTool.CRAYON) {
                                    Canvas(Modifier.width(42.dp).height(12.dp)) {
                                        val previewPath = Path().apply {
                                            moveTo(3.dp.toPx(), size.height * 0.62f)
                                            cubicTo(size.width * 0.3f, -size.height, size.width * 0.65f, size.height * 1.8f, size.width - 3.dp.toPx(), size.height * 0.38f)
                                        }
                                        if (tool == DrawingTool.WATERCOLOR) {
                                            drawPath(previewPath, Color(0xFF4388A4).copy(alpha = 0.38f), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                            drawCircle(Color(0xFF4388A4).copy(alpha = 0.3f), radius = 3.dp.toPx(), center = Offset(size.width * 0.52f, size.height * 0.56f))
                                        } else {
                                            drawPath(previewPath, Color(0xFF4388A4), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                            listOf(0.25f, 0.48f, 0.7f).forEach { fraction ->
                                                drawCircle(Color.White, radius = 1.dp.toPx(), center = Offset(size.width * fraction, size.height * 0.52f))
                                            }
                                        }
                                    }
                                }
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
    var previousX = stroke.points[0].x
    var previousY = stroke.points[0].y
    path.moveTo(previousX, previousY)
    for (i in 1 until stroke.points.size) {
        val currentX = stroke.points[i].x
        val currentY = stroke.points[i].y
        val midX = (previousX + currentX) / 2f
        val midY = (previousY + currentY) / 2f
        path.quadraticTo(previousX, previousY, midX, midY)
        previousX = currentX
        previousY = currentY
    }
    path.lineTo(previousX, previousY)
    when (stroke.tool) {
        DrawingTool.WATERCOLOR -> {
            // A broad translucent wash with irregular pigment blooms reads as a wet medium.
            drawPath(path, stroke.color.copy(alpha = stroke.alpha * 0.24f), style = Stroke(stroke.strokeWidth * 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(path, stroke.color.copy(alpha = stroke.alpha * 0.32f), style = Stroke(stroke.strokeWidth * 1.12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawWatercolorBlooms(stroke)
        }
        DrawingTool.CRAYON -> {
            // A broken core plus high-contrast paper flecks exposes the canvas through wax.
            drawPath(path, stroke.color.copy(alpha = stroke.alpha * 0.76f), style = Stroke(stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCrayonTexture(stroke)
        }
        else -> drawPath(
            path = path,
            color = stroke.color.copy(alpha = stroke.alpha),
            style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
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

private fun DrawScope.drawCrayonTexture(stroke: DrawnStroke) {
    val stride = (stroke.points.size / 72).coerceAtLeast(1)
    stroke.points.indices.step(stride).forEach { index ->
        val point = stroke.points[index]
        val seed = stroke.id xor (index.toLong() * 0x45D9F3BL)
        val side = if ((seed and 1L) == 0L) -1f else 1f
        val across = side * stroke.strokeWidth * (0.2f + ((seed ushr 4 and 7).toFloat() * 0.045f))
        
        // Randomize base sizes
        val randRadiusMult = 0.4f + ((seed ushr 7 and 15).toFloat() / 15f) * 1.6f
        val randLengthMult = 1.5f + ((seed ushr 11 and 15).toFloat() / 15f) * 4.5f
        
        val baseRadius = (stroke.strokeWidth * 0.055f).coerceAtLeast(0.8f)
        val radius = baseRadius * randRadiusMult
        
        val drawLines = (seed and 3L) == 0L

        if (drawLines && index > 0) {
            val prevPoint = stroke.points[index - 1]
            val dirX = point.x - prevPoint.x
            val dirY = point.y - prevPoint.y
            val len = kotlin.math.hypot(dirX.toDouble(), dirY.toDouble()).toFloat()
            val dx = if (len > 0) dirX / len else 1f
            val dy = if (len > 0) dirY / len else 0f
            val lineLength = radius * randLengthMult
            
            drawLine(
                color = Color.White.copy(alpha = 0.58f),
                start = Offset(point.x + across, point.y + side * radius),
                end = Offset(point.x + across + dx * lineLength, point.y + side * radius + dy * lineLength),
                strokeWidth = radius,
                cap = StrokeCap.Round
            )
        } else {
            drawCircle(
                color = Color.White.copy(alpha = 0.68f),
                radius = radius,
                center = Offset(point.x + across, point.y + side * radius)
            )
        }
        
        if (index % 3 == 0) {
            val randColorRadiusMult = 0.4f + ((seed ushr 15 and 15).toFloat() / 15f) * 1.6f
            val colorRadius = baseRadius * randColorRadiusMult * 0.85f
            
            if (drawLines && index < stroke.points.size - 1) {
                val nextPoint = stroke.points[index + 1]
                val dirX = nextPoint.x - point.x
                val dirY = nextPoint.y - point.y
                val len = kotlin.math.hypot(dirX.toDouble(), dirY.toDouble()).toFloat()
                val dx = if (len > 0) dirX / len else 1f
                val dy = if (len > 0) dirY / len else 0f
                val lineLength = colorRadius * randLengthMult

                drawLine(
                    color = stroke.color.copy(alpha = stroke.alpha * 0.72f),
                    start = Offset(point.x - across * 0.55f, point.y - side * colorRadius),
                    end = Offset(point.x - across * 0.55f + dx * lineLength, point.y - side * colorRadius + dy * lineLength),
                    strokeWidth = colorRadius,
                    cap = StrokeCap.Round
                )
            } else {
                drawCircle(
                    color = stroke.color.copy(alpha = stroke.alpha * 0.82f),
                    radius = colorRadius,
                    center = Offset(point.x - across * 0.55f, point.y - side * colorRadius)
                )
            }
        }
    }
}
