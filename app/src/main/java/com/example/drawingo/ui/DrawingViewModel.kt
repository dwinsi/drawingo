package com.example.drawingo.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong
import java.io.File
import java.io.FileOutputStream

import com.example.drawingo.data.DrawingDao
import com.example.drawingo.data.DrawingProject
import com.example.drawingo.util.ImageUtils
import com.example.drawingo.util.CanvasBitmapUtils
import com.example.drawingo.util.StrokeMapper
import com.example.drawingo.theme.StudioAccent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import android.net.Uri

/**
 * Lightweight MVVM ViewModel managing the Drawingo canvas,
 * tool selection, 2-finger Pan/Scroll & Pinch-Zoom, Undo/Redo,
 * drawing tools, canvas gestures, local projects, and on-device drawing effects.
 */
@OptIn(FlowPreview::class)
class DrawingViewModel(private val drawingDao: DrawingDao) : ViewModel() {

    enum class ProjectSaveStatus { SAVING, SAVED, FAILED }

    private val projectSaveMutex = Mutex()
    private var applicationContext: Context? = null
    private var isLoadingProject = false
    private var projectEditRevision = 0L

    val allProjects = drawingDao.getAllProjects().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _projectSaveStatus = MutableStateFlow(ProjectSaveStatus.SAVED)
    val projectSaveStatus: StateFlow<ProjectSaveStatus> = _projectSaveStatus.asStateFlow()

    private val _canvasReady = MutableStateFlow(false)
    val canvasReady: StateFlow<Boolean> = _canvasReady.asStateFlow()

    private val defaultInitialLayer = com.example.drawingo.model.DrawingLayer(id = System.currentTimeMillis(), name = "Layer 1")
    private val _layers = MutableStateFlow<List<com.example.drawingo.model.DrawingLayer>>(listOf(defaultInitialLayer))
    val layers: StateFlow<List<com.example.drawingo.model.DrawingLayer>> = _layers.asStateFlow()

    private val _selectedLayerId = MutableStateFlow<Long?>(defaultInitialLayer.id)
    val selectedLayerId: StateFlow<Long?> = _selectedLayerId.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _isPlayingLocalAnimation = MutableStateFlow(false)
    val isPlayingLocalAnimation: StateFlow<Boolean> = _isPlayingLocalAnimation.asStateFlow()

    private val _playbackFps = MutableStateFlow(12)
    val playbackFps: StateFlow<Int> = _playbackFps.asStateFlow()
    
    private val _onionSkinEnabled = MutableStateFlow(true)
    val onionSkinEnabled: StateFlow<Boolean> = _onionSkinEnabled.asStateFlow()

    enum class QuickMagicPreset(val label: String) {
        WIGGLE("Wiggle"),
        PULSE("Pulse"),
        FLOAT("Float"),
        SHIMMER("Shimmer"),
        SPIN("Spin"),
        WANDER("Wander"),
        FLIP("Flip"),
        TRANSLATE("Translate"),
        RANDOM("Random"),
        BOUNCE("Bounce"),
        WAVE("Wave"),
        SWING("Swing"),
        JITTER("Jitter"),
        HEARTBEAT("Heartbeat"),
        SPIRAL("Spiral"),
        COLOR_CYCLE("Color")
    }
    
    private val _activePresets = MutableStateFlow<Set<QuickMagicPreset>>(emptySet())
    val activePresets: StateFlow<Set<QuickMagicPreset>> = _activePresets.asStateFlow()

    // Text Tool State
    private val _showTextPrompt = MutableStateFlow(false)
    val showTextPrompt: StateFlow<Boolean> = _showTextPrompt.asStateFlow()

    private val _textPromptInitialValue = MutableStateFlow("")
    val textPromptInitialValue: StateFlow<String> = _textPromptInitialValue.asStateFlow()

    private val _editingTextStrokeId = MutableStateFlow<Long?>(null)

    private var pointerDownCanvasPos: Offset? = null

    private val _textPromptPosition = MutableStateFlow(Offset.Zero)
    val textPromptPosition: StateFlow<Offset> = _textPromptPosition.asStateFlow()

    fun createNewProject() {
        _currentProjectId.value = null
        
        val initialLayer = com.example.drawingo.model.DrawingLayer(id = System.currentTimeMillis(), name = "Layer 1")
        _layers.value = listOf(initialLayer)
        _selectedLayerId.value = initialLayer.id
        _currentFrameIndex.value = 0
        _isPlayingLocalAnimation.value = false
        _activePresets.value = emptySet()
        _backgroundImagePath.value = null
        
        _completedStrokes.value = emptyList() // We can keep this for flat drawing or replace it. Actually we should remove it and use layers.strokes.
        _activeStrokes.value = emptyMap()
        _undoStack.value = emptyList()
        _redoStack.value = emptyList()
        updateUndoRedoStates()
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch { loadProjectFromStorage(projectId) }
    }

    private suspend fun loadProjectFromStorage(projectId: Long) {
        val wasLoadingProject = isLoadingProject
        isLoadingProject = true
        try {
            _currentProjectId.value = projectId
            val project = drawingDao.getProjectById(projectId)
            if (project != null) {
                _backgroundImagePath.value = project.backgroundImagePath
            } else {
                _backgroundImagePath.value = null
            }
            
            val layerEntities = drawingDao.getLayersForProject(projectId)
            val strokeEntities = drawingDao.getStrokesForProject(projectId)
            
            val strokesByLayerAndFrame = strokeEntities.groupBy { Pair(it.layerId, it.frameIndex) }
            
            val loadedLayers = layerEntities.map { layerEntity ->
                // Determine max frame index for this layer
                val layerStrokesKeys = strokesByLayerAndFrame.keys.filter { it.first == layerEntity.id }
                val maxFrame = if (layerStrokesKeys.isNotEmpty()) layerStrokesKeys.maxOf { it.second } else 0
                
                val frames = (0..maxFrame).map { frameIdx ->
                    val strokesForThisFrame = strokesByLayerAndFrame[Pair(layerEntity.id, frameIdx)]?.map { strokeEntity ->
                        StrokeMapper.fromEntity(strokeEntity, strokeIdGenerator.getAndIncrement())
                    } ?: emptyList()
                    com.example.drawingo.model.DrawingFrame(strokes = strokesForThisFrame)
                }

                com.example.drawingo.model.DrawingLayer(
                    id = layerEntity.id,
                    name = layerEntity.name,
                    isVisible = layerEntity.isVisible,
                    opacity = layerEntity.opacity,
                    frames = frames
                )
            }

            _layers.value = if (loadedLayers.isNotEmpty()) loadedLayers else listOf(com.example.drawingo.model.DrawingLayer(id = System.currentTimeMillis(), name = "Layer 1"))
            _selectedLayerId.value = _layers.value.first().id
            _currentFrameIndex.value = 0
            _isPlayingLocalAnimation.value = false
            _activePresets.value = emptySet()
            
            _activeStrokes.value = emptyMap()
            _undoStack.value = emptyList()
            _redoStack.value = emptyList()
            updateUndoRedoStates()
            _projectSaveStatus.value = ProjectSaveStatus.SAVED
        } finally {
            isLoadingProject = wasLoadingProject
        }
    }

    fun saveCurrentProject(name: String, context: Context, onSaved: (() -> Unit)? = null) {
        val revision = ++projectEditRevision
        _projectSaveStatus.value = ProjectSaveStatus.SAVING
        viewModelScope.launch {
            try {
                persistProject(name, context, onSaved)
                if (revision == projectEditRevision) _projectSaveStatus.value = ProjectSaveStatus.SAVED
            } catch (error: Exception) {
                if (revision == projectEditRevision) _projectSaveStatus.value = ProjectSaveStatus.FAILED
                android.util.Log.e("DrawingViewModel", "Project save failed", error)
            }
        }
    }

    private suspend fun persistProject(name: String?, context: Context?, onSaved: (() -> Unit)? = null) {
        projectSaveMutex.withLock {
            val existingProject = _currentProjectId.value?.let { drawingDao.getProjectById(it) }
            val currentBgPath = _backgroundImagePath.value
            val project = existingProject?.copy(
                name = name ?: existingProject.name,
                updatedAt = System.currentTimeMillis(),
                backgroundImagePath = currentBgPath
            ) ?: DrawingProject(name = name ?: "Untitled drawing", backgroundImagePath = currentBgPath)

            val layerEntities = _layers.value.mapIndexed { index, layer ->
                com.example.drawingo.data.LayerEntity(
                    projectId = 0,
                    name = layer.name,
                    isVisible = layer.isVisible,
                    opacity = layer.opacity,
                    orderIndex = index
                )
            }
            val strokeEntities = mutableListOf<com.example.drawingo.data.StrokeEntity>()
            _layers.value.forEachIndexed { layerIndex, layer ->
                layer.frames.forEachIndexed { frameIndex, frame ->
                    frame.strokes.forEachIndexed { strokeIndex, stroke ->
                        strokeEntities.add(
                            StrokeMapper.toEntity(stroke, 0, layerIndex.toLong(), frameIndex, strokeIndex)
                        )
                    }
                }
            }

            val newId = drawingDao.saveProjectContent(project, layerEntities, strokeEntities)
            _currentProjectId.value = newId

            if (context != null) {
                val thumbnailPath = createProjectThumbnail(context, newId)
                if (thumbnailPath != null) {
                    drawingDao.getProjectById(newId)?.let { savedProject ->
                        drawingDao.updateProject(savedProject.copy(thumbnailPath = thumbnailPath))
                    }
                }
            }
            onSaved?.invoke()
        }
    }

    private suspend fun createProjectThumbnail(context: Context, projectId: Long): String? {
        val width = screenWidth.toInt().coerceAtLeast(1)
        val height = screenHeight.toInt().coerceAtLeast(1)
        val frameIndex = _currentFrameIndex.value
        val visibleStrokes = _layers.value
            .filter { it.isVisible }
            .flatMap { layer ->
                layer.frames.getOrNull(frameIndex)?.strokes.orEmpty().map { stroke ->
                    stroke.copy(alpha = stroke.alpha * layer.opacity.coerceIn(0f, 1f))
                }
            }

        val fullBitmap = withContext(Dispatchers.Default) {
            CanvasBitmapUtils.createBitmapFromStrokes(
                strokes = visibleStrokes,
                screenWidth = width,
                screenHeight = height,
                backgroundImagePath = _backgroundImagePath.value
            )
        }

        return withContext(Dispatchers.IO) {
            var thumbnail: Bitmap? = null
            try {
                val scale = minOf(1f, 512f / maxOf(fullBitmap.width, fullBitmap.height))
                val thumbnailBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        fullBitmap,
                        (fullBitmap.width * scale).toInt().coerceAtLeast(1),
                        (fullBitmap.height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                } else fullBitmap
                thumbnail = thumbnailBitmap
                val directory = File(context.filesDir, "thumbnails")
                if (!directory.exists() && !directory.mkdirs()) return@withContext null
                val file = File(directory, "drawing-$projectId.jpg")
                val saved = FileOutputStream(file).use {
                    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 88, it)
                }
                file.takeIf { saved }?.absolutePath
            } catch (_: Exception) {
                null
            } finally {
                thumbnail?.takeIf { it !== fullBitmap && !it.isRecycled }?.recycle()
                if (!fullBitmap.isRecycled) fullBitmap.recycle()
            }
        }
    }

    private val _selectedStrokes = MutableStateFlow<Set<Long>>(emptySet())
    val selectedStrokes: StateFlow<Set<Long>> = _selectedStrokes.asStateFlow()

    private val _lassoTransform = MutableStateFlow(Offset.Zero)
    val lassoTransform: StateFlow<Offset> = _lassoTransform.asStateFlow()

    private val strokeIdGenerator = AtomicLong(1L)

    var soundManager: SoundManager? = null

    // 2-Finger Pan and Zoom State
    private val _canvasScale = MutableStateFlow(1.0f)
    val canvasScale: StateFlow<Float> = _canvasScale.asStateFlow()

    private val _canvasOffsetX = MutableStateFlow(0.0f)
    val canvasOffsetX: StateFlow<Float> = _canvasOffsetX.asStateFlow()

    private val _canvasOffsetY = MutableStateFlow(0.0f)
    val canvasOffsetY: StateFlow<Float> = _canvasOffsetY.asStateFlow()

    // Drawingo Tools & Options
    private val _selectedTool = MutableStateFlow(DrawingTool.PEN)
    val selectedTool: StateFlow<DrawingTool> = _selectedTool.asStateFlow()

    private val _recentTools = MutableStateFlow(listOf(DrawingTool.PEN))
    val recentTools: StateFlow<List<DrawingTool>> = _recentTools.asStateFlow()

    private val _selectedColor = MutableStateFlow(DrawingoPalette.grid[0][0])
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _selectedStrokeWidth = MutableStateFlow(20f)
    val selectedStrokeWidth: StateFlow<Float> = _selectedStrokeWidth.asStateFlow()

    private val _selectedEraserWidth = MutableStateFlow(120f)
    val selectedEraserWidth: StateFlow<Float> = _selectedEraserWidth.asStateFlow()

    private val _selectedPaperStyle = MutableStateFlow(com.example.drawingo.model.CanvasPaperStyle.PURE_WHITE)
    val selectedPaperStyle: StateFlow<com.example.drawingo.model.CanvasPaperStyle> = _selectedPaperStyle.asStateFlow()
    
    private val _backgroundImagePath = MutableStateFlow<String?>(null)
    val backgroundImagePath: StateFlow<String?> = _backgroundImagePath.asStateFlow()

    init {
        viewModelScope.launch {
            isLoadingProject = true
            try {
                drawingDao.getMostRecentProject()?.let { loadProjectFromStorage(it.id) }
            } catch (error: Exception) {
                android.util.Log.e("DrawingViewModel", "Could not restore the latest project", error)
            } finally {
                isLoadingProject = false
                _canvasReady.value = true
            }
        }

        viewModelScope.launch {
            combine(_layers, _backgroundImagePath) { layers, backgroundPath -> layers to backgroundPath }
                .drop(1)
                .filter { !isLoadingProject }
                .onEach { (layers, backgroundPath) ->
                    if (hasProjectContent(layers, backgroundPath)) {
                        projectEditRevision += 1
                        _projectSaveStatus.value = ProjectSaveStatus.SAVING
                    }
                }
                .debounce(750)
                .collect {
                    if (isLoadingProject) return@collect
                    if (hasProjectContent(_layers.value, _backgroundImagePath.value)) saveAutomatically()
                }
        }
    }

    private fun hasProjectContent(
        layers: List<com.example.drawingo.model.DrawingLayer>,
        backgroundPath: String?
    ): Boolean {
        val hasDrawing = layers.any { layer -> layer.frames.any { it.strokes.isNotEmpty() } }
        return _currentProjectId.value != null || hasDrawing || backgroundPath != null || layers.size > 1
    }

    fun setApplicationContext(context: Context) {
        applicationContext = context.applicationContext
    }

    /** Flush meaningful canvas work when the app leaves the foreground. */
    fun saveAutomaticallyNow() {
        val layers = _layers.value
        val hasDrawing = layers.any { layer -> layer.frames.any { it.strokes.isNotEmpty() } }
        if (_currentProjectId.value != null || hasDrawing || _backgroundImagePath.value != null || layers.size > 1) {
            saveAutomatically()
        }
    }

    private fun saveAutomatically() {
        val context = applicationContext ?: return
        val revision = projectEditRevision
        _projectSaveStatus.value = ProjectSaveStatus.SAVING
        viewModelScope.launch {
            try {
                persistProject(name = null, context = context)
                if (revision == projectEditRevision) _projectSaveStatus.value = ProjectSaveStatus.SAVED
            } catch (error: Exception) {
                if (revision == projectEditRevision) _projectSaveStatus.value = ProjectSaveStatus.FAILED
                android.util.Log.e("DrawingViewModel", "Automatic project save failed", error)
            }
        }
    }

    private val _symmetryMode = MutableStateFlow(com.example.drawingo.model.SymmetryMode.NONE)
    val symmetryMode: StateFlow<com.example.drawingo.model.SymmetryMode> = _symmetryMode.asStateFlow()

    // Screen dimensions to calculate symmetry
    var screenWidth = 0f
    var screenHeight = 0f

    // Layer Management Functions
    fun addLayer() {
        val currentLayers = _layers.value.toMutableList()
        val newLayer = com.example.drawingo.model.DrawingLayer(id = System.currentTimeMillis(), name = "Layer ${currentLayers.size + 1}")
        currentLayers.add(0, newLayer) // Add to top of stack
        _layers.value = currentLayers
        _selectedLayerId.value = newLayer.id
    }

    fun deleteLayer(layerId: Long) {
        val currentLayers = _layers.value.toMutableList()
        if (currentLayers.size <= 1) return // Prevent deleting the last layer
        
        currentLayers.removeAll { it.id == layerId }
        _layers.value = currentLayers
        if (_selectedLayerId.value == layerId) {
            _selectedLayerId.value = currentLayers.first().id
        }
    }

    fun selectLayer(layerId: Long) {
        _selectedLayerId.value = layerId
    }

    fun toggleLayerVisibility(layerId: Long) {
        _layers.value = _layers.value.map { 
            if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it 
        }
    }

    fun setLayerOpacity(layerId: Long, opacity: Float) {
        _layers.value = _layers.value.map { 
            if (it.id == layerId) it.copy(opacity = opacity) else it 
        }
    }

    fun reorderLayers(fromIndex: Int, toIndex: Int) {
        val currentLayers = _layers.value.toMutableList()
        val item = currentLayers.removeAt(fromIndex)
        currentLayers.add(toIndex, item)
        _layers.value = currentLayers
    }

    fun cancelTextPrompt() {
        _showTextPrompt.value = false
        _editingTextStrokeId.value = null
    }

    fun addTextStroke(text: String) {
        _showTextPrompt.value = false
        if (text.isBlank()) {
            _editingTextStrokeId.value = null
            return
        }

        val canvasPos = _textPromptPosition.value
        val tool = DrawingTool.TEXT
        val textSize = _selectedStrokeWidth.value * 4f
        val defaultRadius = (textSize * text.length * 0.32f).coerceAtLeast(24f)
        val handlePos = Offset(canvasPos.x + defaultRadius, canvasPos.y)
        val points = listOf(canvasPos, handlePos)
        val bounds = DrawnStroke.calculateTextBounds(text, textSize, canvasPos)

        val editingId = _editingTextStrokeId.value
        if (editingId != null) {
            val activeLayerId = _selectedLayerId.value ?: return
            val frameIndex = _currentFrameIndex.value
            _undoStack.value = _undoStack.value + listOf(_layers.value)
            _redoStack.value = emptyList()
            _layers.value = _layers.value.map { layer ->
                if (layer.id != activeLayerId) layer else {
                    val updatedFrames = layer.frames.toMutableList()
                    val currentFrame = updatedFrames.getOrNull(frameIndex)
                    if (currentFrame != null) {
                        val updatedStrokes = currentFrame.strokes.map { stroke ->
                            if (stroke.id == editingId) {
                                // Keep the existing position and rotation, but apply new text/color/font attributes
                                val oldScaleFactor = if (stroke.points.size >= 2) {
                                    val dx = stroke.points[1].x - stroke.points[0].x
                                    val dy = stroke.points[1].y - stroke.points[0].y
                                    val oldRadius = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                    val oldDefaultRadius = (stroke.strokeWidth * (stroke.text?.length ?: 1) * 0.32f).coerceAtLeast(24f)
                                    if (oldDefaultRadius > 0f) oldRadius / oldDefaultRadius else 1f
                                } else 1f
                                
                                val newScaleFactor = oldScaleFactor
                                val newDefaultRadius = (textSize * text.length * 0.32f).coerceAtLeast(24f)
                                val newRadius = newDefaultRadius * newScaleFactor
                                
                                // Preserve old rotation if we had 2 points
                                val newPoints = if (stroke.points.size >= 2) {
                                    val p0 = stroke.points[0]
                                    val p1 = stroke.points[1]
                                    val angle = kotlin.math.atan2((p1.y - p0.y).toDouble(), (p1.x - p0.x).toDouble()).toFloat()
                                    val newP1 = Offset(
                                        p0.x + newRadius * kotlin.math.cos(angle.toDouble()).toFloat(),
                                        p0.y + newRadius * kotlin.math.sin(angle.toDouble()).toFloat()
                                    )
                                    listOf(p0, newP1)
                                } else points
                                
                                stroke.copy(
                                    text = text,
                                    color = _selectedColor.value,
                                    strokeWidth = textSize,
                                    points = newPoints,
                                    boundingBox = DrawnStroke.calculateTextBounds(text, textSize, stroke.points.firstOrNull() ?: canvasPos, newScaleFactor)
                                )
                            } else stroke
                        }
                        updatedFrames[frameIndex] = currentFrame.copy(strokes = updatedStrokes)
                    }
                    layer.copy(frames = updatedFrames)
                }
            }
            updateUndoRedoStates()
        } else {
            val newStroke = DrawnStroke(
                id = strokeIdGenerator.getAndIncrement(),
                color = _selectedColor.value,
                strokeWidth = textSize,
                alpha = 1.0f,
                tool = tool,
                points = points,
                text = text,
                boundingBox = bounds
            )

            // Inject this directly as a completed stroke
            _activeStrokes.value = _activeStrokes.value + (-2L to newStroke)
            onPointerUp(-2L)
        }
        _editingTextStrokeId.value = null
    }

    fun updateTextStrokeTransform(strokeId: Long, handlePosition: Offset) {
        val activeLayerId = _selectedLayerId.value ?: return
        val frameIndex = _currentFrameIndex.value

        _layers.value = _layers.value.map { layer ->
            if (layer.id != activeLayerId) layer else {
                val updatedFrames = layer.frames.toMutableList()
                val currentFrame = updatedFrames.getOrNull(frameIndex)
                if (currentFrame != null) {
                    val updatedStrokes = currentFrame.strokes.map { stroke ->
                        if (stroke.id == strokeId && stroke.points.isNotEmpty()) {
                            val p0 = stroke.points[0]
                            val newPoints = listOf(p0, handlePosition)
                            val dx = handlePosition.x - p0.x
                            val dy = handlePosition.y - p0.y
                            val currentRadius = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                            val defaultRadius = (stroke.strokeWidth * (stroke.text?.length ?: 1) * 0.32f).coerceAtLeast(24f)
                            val scaleFactor = if (defaultRadius > 0f) currentRadius / defaultRadius else 1f
                            val newBounds = DrawnStroke.calculateTextBounds(
                                text = stroke.text ?: "",
                                textSize = stroke.strokeWidth,
                                center = p0,
                                scaleFactor = scaleFactor
                            )
                            stroke.copy(points = newPoints, boundingBox = newBounds)
                        } else stroke
                    }
                    updatedFrames[frameIndex] = currentFrame.copy(strokes = updatedStrokes)
                }
                layer.copy(frames = updatedFrames)
            }
        }
    }

    fun updateTextStrokePosition(strokeId: Long, dx: Float, dy: Float) {
        val activeLayerId = _selectedLayerId.value ?: return
        val frameIndex = _currentFrameIndex.value

        _layers.value = _layers.value.map { layer ->
            if (layer.id != activeLayerId) layer else {
                val updatedFrames = layer.frames.toMutableList()
                val currentFrame = updatedFrames.getOrNull(frameIndex)
                if (currentFrame != null) {
                    val updatedStrokes = currentFrame.strokes.map { stroke ->
                        if (stroke.id == strokeId && stroke.points.isNotEmpty()) {
                            val newPoints = stroke.points.map { Offset(it.x + dx, it.y + dy) }
                            val newBounds = Rect(
                                stroke.boundingBox.left + dx,
                                stroke.boundingBox.top + dy,
                                stroke.boundingBox.right + dx,
                                stroke.boundingBox.bottom + dy
                            )
                            stroke.copy(points = newPoints, boundingBox = newBounds)
                        } else stroke
                    }
                    updatedFrames[frameIndex] = currentFrame.copy(strokes = updatedStrokes)
                }
                layer.copy(frames = updatedFrames)
            }
        }
    }

    // Frame Management Functions
    fun addFrame() {
        _undoStack.value = _undoStack.value + listOf(_layers.value)
        _redoStack.value = emptyList()
        val nextIdx = _currentFrameIndex.value + 1
        _layers.value = _layers.value.map { layer ->
            val newFrames = layer.frames.toMutableList()
            while (newFrames.size <= nextIdx) {
                newFrames.add(com.example.drawingo.model.DrawingFrame())
            }
            layer.copy(frames = newFrames)
        }
        _currentFrameIndex.value = nextIdx
        updateUndoRedoStates()
    }

    fun duplicateFrame() {
        _undoStack.value = _undoStack.value + listOf(_layers.value)
        _redoStack.value = emptyList()
        val currentIdx = _currentFrameIndex.value
        val nextIdx = currentIdx + 1
        _layers.value = _layers.value.map { layer ->
            val newFrames = layer.frames.toMutableList()
            val currentFrameStrokes = newFrames.getOrNull(currentIdx)?.strokes ?: emptyList()
            
            // Insert a new frame right after the current one with the same strokes
            newFrames.add(nextIdx, com.example.drawingo.model.DrawingFrame(strokes = currentFrameStrokes))
            layer.copy(frames = newFrames)
        }
        _currentFrameIndex.value = nextIdx
        updateUndoRedoStates()
    }

    fun deleteFrame(index: Int) {
        val maxFrames = _layers.value.maxOfOrNull { it.frames.size } ?: 1
        if (maxFrames <= 1) return // Don't delete the last frame
        
        _undoStack.value = _undoStack.value + listOf(_layers.value)
        _redoStack.value = emptyList()
        
        _layers.value = _layers.value.map { layer ->
            val newFrames = layer.frames.toMutableList()
            if (index < newFrames.size) {
                newFrames.removeAt(index)
            }
            if (newFrames.isEmpty()) newFrames.add(com.example.drawingo.model.DrawingFrame())
            layer.copy(frames = newFrames)
        }
        
        if (_currentFrameIndex.value >= _layers.value.maxOf { it.frames.size }) {
            _currentFrameIndex.value = _layers.value.maxOf { it.frames.size } - 1
        }
        updateUndoRedoStates()
    }

    fun selectFrame(index: Int) {
        if (index >= 0) {
            _currentFrameIndex.value = index
        }
    }
    
    fun toggleOnionSkin() {
        _onionSkinEnabled.value = !_onionSkinEnabled.value
    }

    fun setPlaybackFps(fps: Int) {
        _playbackFps.value = fps.coerceIn(1, 24)
    }
    
    // Playback Logic
    fun toggleLocalPlayback() {
        if (_isPlayingLocalAnimation.value) {
            stopLocalPlayback()
        } else {
            startLocalPlayback()
        }
    }
    
    private fun startLocalPlayback() {
        _isPlayingLocalAnimation.value = true
        val maxFrames = _layers.value.maxOfOrNull { it.frames.size } ?: 1
        if (maxFrames <= 1) return // Nothing to animate
        
        animationLoopJob?.cancel()
        animationLoopJob = viewModelScope.launch {
            while (_isPlayingLocalAnimation.value) {
                val nextFrame = (_currentFrameIndex.value + 1) % maxFrames
                _currentFrameIndex.value = nextFrame
                delay(1000L / _playbackFps.value)
            }
        }
    }
    
    private fun stopLocalPlayback() {
        _isPlayingLocalAnimation.value = false
        animationLoopJob?.cancel()
    }

    fun toggleQuickMagicPreset(preset: QuickMagicPreset) {
        val current = _activePresets.value.toMutableSet()
        if (current.contains(preset)) {
            current.remove(preset)
        } else {
            current.add(preset)
        }
        _activePresets.value = current
    }

    fun clearQuickMagicPresets() {
        _activePresets.value = emptySet()
    }

    fun setBackgroundImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value
            val savedPath = ImageUtils.saveImageToInternalStorage(context, uri, projectId)
            if (savedPath != null) {
                _backgroundImagePath.value = savedPath
            }
        }
    }

    // Undo and Redo Stacks (Global for now)
    private val _undoStack = MutableStateFlow<List<List<com.example.drawingo.model.DrawingLayer>>>(emptyList())
    private val _redoStack = MutableStateFlow<List<List<com.example.drawingo.model.DrawingLayer>>>(emptyList())

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Completed drawn strokes (deprecated, use layers instead)
    private val _completedStrokes = MutableStateFlow<List<DrawnStroke>>(emptyList())
    val completedStrokes: StateFlow<List<DrawnStroke>> = _completedStrokes.asStateFlow()

    // Currently active in-flight strokes
    private val _activeStrokes = MutableStateFlow<Map<Long, DrawnStroke>>(emptyMap())
    val activeStrokes: StateFlow<Map<Long, DrawnStroke>> = _activeStrokes.asStateFlow()

    private var animationLoopJob: Job? = null

    companion object {
        private const val MAX_STORED_STROKES = 250
    }

    fun setTool(tool: DrawingTool) {
        if (_activeStrokes.value.isNotEmpty()) {
            _activeStrokes.value = emptyMap()
        }
        if (_selectedTool.value == DrawingTool.LASSO && tool != DrawingTool.LASSO) {
            applyLassoTransformIfAny()
        }
        _selectedTool.value = tool
        _recentTools.value = (listOf(tool) + _recentTools.value.filterNot { it == tool }).take(3)
    }

    fun setColor(color: Color) {
        _selectedColor.value = color
    }

    fun setStrokeWidth(width: Float) {
        _selectedStrokeWidth.value = width
    }

    fun setEraserWidth(width: Float) {
        _selectedEraserWidth.value = width
    }

    fun setPaperStyle(style: com.example.drawingo.model.CanvasPaperStyle) {
        _selectedPaperStyle.value = style
    }

    fun toggleSymmetryMode() {
        _symmetryMode.value = when (_symmetryMode.value) {
            com.example.drawingo.model.SymmetryMode.NONE -> com.example.drawingo.model.SymmetryMode.HORIZONTAL
            com.example.drawingo.model.SymmetryMode.HORIZONTAL -> com.example.drawingo.model.SymmetryMode.VERTICAL
            com.example.drawingo.model.SymmetryMode.VERTICAL -> com.example.drawingo.model.SymmetryMode.QUAD
            com.example.drawingo.model.SymmetryMode.QUAD -> com.example.drawingo.model.SymmetryMode.NONE
        }
    }

    fun onPanAndZoom(zoomChange: Float, panChange: Offset) {
        val newScale = (_canvasScale.value * zoomChange).coerceIn(0.5f, 5.0f)
        _canvasScale.value = newScale
        _canvasOffsetX.value += panChange.x
        _canvasOffsetY.value += panChange.y
    }

    fun resetPanAndZoom() {
        _canvasScale.value = 1.0f
        _canvasOffsetX.value = 0.0f
        _canvasOffsetY.value = 0.0f
    }

    private fun toCanvasCoordinate(screenPos: Offset): Offset {
        val scale = _canvasScale.value
        val ox = _canvasOffsetX.value
        val oy = _canvasOffsetY.value
        return Offset(
            x = (screenPos.x - ox) / scale,
            y = (screenPos.y - oy) / scale
        )
    }

    fun undo() {
        val currentUndo = _undoStack.value
        if (currentUndo.isNotEmpty()) {
            _selectedStrokes.value = emptySet()
            _lassoTransform.value = Offset.Zero
            
            val previousState = currentUndo.last()
            val newUndo = currentUndo.dropLast(1)
            _redoStack.value = _redoStack.value + listOf(_layers.value)
            _undoStack.value = newUndo
            _layers.value = previousState
            updateUndoRedoStates()
        }
    }

    fun redo() {
        val currentRedo = _redoStack.value
        if (currentRedo.isNotEmpty()) {
            _selectedStrokes.value = emptySet()
            _lassoTransform.value = Offset.Zero
            
            val nextState = currentRedo.last()
            val newRedo = currentRedo.dropLast(1)
            _undoStack.value = _undoStack.value + listOf(_layers.value)
            _redoStack.value = newRedo
            _layers.value = nextState
            updateUndoRedoStates()
        }
    }

    fun clearCanvas() {
        if (_layers.value.any { layer -> layer.frames.any { it.strokes.isNotEmpty() } }) {

            _selectedStrokes.value = emptySet()
            _lassoTransform.value = Offset.Zero
            
            _undoStack.value = _undoStack.value + listOf(_layers.value)
            _redoStack.value = emptyList()
            val currentFrameIdx = _currentFrameIndex.value
            _layers.value = _layers.value.map { layer -> 
                val updatedFrames = layer.frames.toMutableList()
                if (currentFrameIdx < updatedFrames.size) {
                    updatedFrames[currentFrameIdx] = updatedFrames[currentFrameIdx].copy(strokes = emptyList())
                }
                layer.copy(frames = updatedFrames)
            }
            updateUndoRedoStates()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = _undoStack.value.isNotEmpty()
        _canRedo.value = _redoStack.value.isNotEmpty()
    }

    fun onPointerDown(pointerId: Long, screenPosition: Offset, pressure: Float = 1.0f) {

        val tool = _selectedTool.value
        val canvasPos = toCanvasCoordinate(screenPosition)

        if (tool == DrawingTool.FILL) {
            fillEnclosedShape(canvasPos)
            return
        }

        // If lasso is active and we tap, check if we are dragging an existing selection
        if (tool == DrawingTool.LASSO && _selectedStrokes.value.isNotEmpty()) {
            _activeStrokes.value = _activeStrokes.value + (pointerId to DrawnStroke(
                id = -1L, // Special ID indicating a lasso drag operation
                color = Color.Transparent,
                tool = tool,
                points = listOf(canvasPos)
            ))
            return
        }

        // If we tap with lasso outside, clear previous selection and start drawing new lasso
        if (tool == DrawingTool.LASSO) {
            applyLassoTransformIfAny()
            _selectedStrokes.value = emptySet()
        }

        val strokeColor = when (tool) {
            DrawingTool.ERASER -> Color.White
            DrawingTool.PEN, DrawingTool.BRUSH,
            DrawingTool.WATERCOLOR, DrawingTool.CRAYON, DrawingTool.SPRAY, DrawingTool.FILL, DrawingTool.TEXT -> _selectedColor.value
            DrawingTool.LASSO -> StudioAccent
        }
        val strokeWidth = when (tool) {
            DrawingTool.PEN -> _selectedStrokeWidth.value
            DrawingTool.BRUSH -> _selectedStrokeWidth.value * 1.8f
            DrawingTool.ERASER -> _selectedEraserWidth.value
            DrawingTool.LASSO -> 4f
            DrawingTool.WATERCOLOR -> _selectedStrokeWidth.value * 2.2f
            DrawingTool.CRAYON -> _selectedStrokeWidth.value * 1.25f
            DrawingTool.SPRAY -> _selectedStrokeWidth.value * 2.0f
            DrawingTool.FILL -> 0f
            DrawingTool.TEXT -> _selectedStrokeWidth.value * 4f // Text scales with brush size
        }
        val alpha = when (tool) {
            DrawingTool.PEN -> 1.0f
            DrawingTool.BRUSH -> 0.85f
            DrawingTool.WATERCOLOR -> 0.68f
            DrawingTool.CRAYON -> 1.0f
            DrawingTool.SPRAY -> 0.75f
            DrawingTool.ERASER, DrawingTool.LASSO, DrawingTool.FILL, DrawingTool.TEXT -> 1.0f
        }
        // If text tool, check if user tapped an existing text stroke to select/edit it
        if (tool == DrawingTool.TEXT) {
            // 1. Check if tapping the scale/rotate handle of an ALREADY selected text stroke
            val selectedIds = _selectedStrokes.value
            if (selectedIds.isNotEmpty()) {
                val selectedTextStroke = _layers.value.flatMap { layer ->
                    layer.frames.getOrNull(_currentFrameIndex.value)?.strokes.orEmpty()
                }.firstOrNull { stroke ->
                    stroke.id in selectedIds && stroke.tool == DrawingTool.TEXT && stroke.points.size >= 2
                }

                if (selectedTextStroke != null) {
                    val handlePos = selectedTextStroke.points[1]
                    val distSq = distanceSquared(canvasPos, handlePos)
                    val handleRadius = 48f
                    if (distSq <= handleRadius * handleRadius) {
                        _undoStack.value = _undoStack.value + listOf(_layers.value)
                        _redoStack.value = emptyList()
                        updateUndoRedoStates()
                        
                        val specialId = -1000000L - selectedTextStroke.id
                        _activeStrokes.value = _activeStrokes.value + (pointerId to DrawnStroke(
                            id = specialId,
                            color = Color.Transparent,
                            tool = DrawingTool.TEXT,
                            points = listOf(canvasPos)
                        ))
                        return
                    }
                }
            }

            // 2. Check if tapping ANY text stroke's bounding box
            val tappedTextStroke = _layers.value.flatMap { layer ->
                layer.frames.getOrNull(_currentFrameIndex.value)?.strokes.orEmpty()
            }.reversed().firstOrNull { stroke ->
                stroke.tool == DrawingTool.TEXT && stroke.boundingBox.contains(canvasPos)
            }

            if (tappedTextStroke != null) {
                if (tappedTextStroke.id !in _selectedStrokes.value) {
                    _selectedStrokes.value = setOf(tappedTextStroke.id)
                }
                
                _undoStack.value = _undoStack.value + listOf(_layers.value)
                _redoStack.value = emptyList()
                updateUndoRedoStates()
                
                val specialId = -2000000L - tappedTextStroke.id
                _activeStrokes.value = _activeStrokes.value + (pointerId to DrawnStroke(
                    id = specialId,
                    color = Color.Transparent,
                    tool = DrawingTool.TEXT,
                    points = listOf(canvasPos) // Store initial touch to track drag distance
                ))
                return
            }

            // 3. Tapping empty space with TEXT tool -> create new text
            _selectedStrokes.value = emptySet()
            _editingTextStrokeId.value = null
            _textPromptPosition.value = canvasPos
            _textPromptInitialValue.value = ""
            _showTextPrompt.value = true
            return
        }

        val newStroke = DrawnStroke(
            id = strokeIdGenerator.getAndIncrement(),
            color = strokeColor,
            strokeWidth = strokeWidth,
            alpha = alpha,
            tool = tool,
            points = listOf(canvasPos),
            pressures = listOf(pressure)
        )

        _activeStrokes.value = _activeStrokes.value + (pointerId to newStroke)
    }

    private data class ContourCandidate(
        val layerId: Long,
        val points: List<Offset>,
        val strokeWidth: Float,
        val area: Float
    )

    private fun fillEnclosedShape(point: Offset) {
        val frameIndex = _currentFrameIndex.value
        val layerBoundaries = _layers.value
            .filter { it.isVisible && it.opacity > 0f }
            .flatMap { layer ->
                layer.frames.getOrNull(frameIndex)?.strokes.orEmpty()
                    .filter { stroke ->
                        stroke.tool != DrawingTool.FILL && stroke.tool != DrawingTool.ERASER &&
                            stroke.tool != DrawingTool.LASSO && stroke.tool != DrawingTool.TEXT &&
                            stroke.points.size >= 2
                    }
                    .map { layer.id to it }
            }

        val contours = findClosedContours(layerBoundaries)
        val matchingContour = contours
            .filter { candidate -> pointInPolygon(point, candidate.points, candidate.strokeWidth) }
            .minByOrNull { it.area } ?: return

        val fillStroke = DrawnStroke(
            id = strokeIdGenerator.getAndIncrement(),
            color = _selectedColor.value,
            tool = DrawingTool.FILL,
            points = matchingContour.points,
            boundingBox = DrawnStroke.calculateBounds(matchingContour.points)
        )
        _undoStack.value = _undoStack.value + listOf(_layers.value)
        _redoStack.value = emptyList()
        _layers.value = _layers.value.map { layer ->
            if (layer.id != matchingContour.layerId) layer else {
                val updatedFrames = layer.frames.toMutableList()
                while (updatedFrames.size <= frameIndex) updatedFrames.add(com.example.drawingo.model.DrawingFrame())
                updatedFrames[frameIndex] = updatedFrames[frameIndex].copy(
                    strokes = updatedFrames[frameIndex].strokes + fillStroke
                )
                layer.copy(frames = updatedFrames)
            }
        }
        updateUndoRedoStates()
    }

    /** Finds closed contours formed by single or multiple strokes. */
    private fun findClosedContours(strokes: List<Pair<Long, DrawnStroke>>): List<ContourCandidate> {
        val candidates = mutableListOf<ContourCandidate>()

        // 1. Single-stroke loop detection (detects full loops, overlapping loops, pigtails, teardrops)
        for ((layerId, stroke) in strokes) {
            val pts = stroke.points
            if (pts.size < 4) continue

            val pathLength = pts.zipWithNext().sumOf { (a, b) ->
                kotlin.math.sqrt(distanceSquared(a, b).toDouble())
            }.toFloat()

            val closureTolerance = maxOf(48f, stroke.strokeWidth * 2.5f, pathLength * 0.25f).coerceIn(40f, 300f)
            val toleranceSq = closureTolerance * closureTolerance

            var bestPolygon: List<Offset>? = null
            var bestLength = -1

            for (j in pts.indices.reversed()) {
                if (j < 3) break
                for (i in 0..j - 3) {
                    val span = j - i
                    if (span <= bestLength) continue
                    if (distanceSquared(pts[i], pts[j]) <= toleranceSq) {
                        val closed = pts.subList(i, j + 1).toMutableList()
                        closed.add(pts[i])
                        bestPolygon = closed
                        bestLength = span
                    }
                }
            }

            if (bestPolygon != null && bestPolygon.size >= 4) {
                val area = polygonArea(bestPolygon)
                if (area > 50f) {
                    candidates.add(ContourCandidate(layerId, bestPolygon, stroke.strokeWidth, area))
                }
            }
        }

        // 2. Multi-stroke loop detection (chains consecutive intersecting/connecting strokes)
        for (startIndex in strokes.indices.reversed()) {
            val (startLayerId, startStroke) = strokes[startIndex]
            val chain = startStroke.points.toMutableList()
            val used = mutableSetOf(startIndex)
            var layerId = startLayerId
            var maxStrokeWidth = startStroke.strokeWidth
            var closedPolygon: List<Offset>? = null

            while (used.size <= strokes.size) {
                val pathLength = chain.zipWithNext().sumOf { (a, b) ->
                    kotlin.math.sqrt(distanceSquared(a, b).toDouble())
                }.toFloat()

                val closureTolerance = maxOf(48f, maxStrokeWidth * 2.5f, pathLength * 0.25f).coerceIn(40f, 300f)
                val toleranceSq = closureTolerance * closureTolerance

                var closureIndex = -1
                if (chain.size >= 4) {
                    for (k in 0..chain.size - 4) {
                        if (distanceSquared(chain.last(), chain[k]) <= toleranceSq) {
                            closureIndex = k
                            break
                        }
                    }
                }

                if (closureIndex >= 0) {
                    val closed = chain.subList(closureIndex, chain.size).toMutableList()
                    closed.add(chain[closureIndex])
                    closedPolygon = closed
                    break
                }

                var nearestIndex = -1
                var nearestAtStart = false
                var nearestDistance = Float.MAX_VALUE
                val endPos = chain.last()

                for (candidateIndex in strokes.indices.reversed()) {
                    if (candidateIndex in used) continue
                    val (candidateLayerId, candidate) = strokes[candidateIndex]
                    val first = candidate.points.first()
                    val last = candidate.points.last()
                    val tol = maxOf(36f, maxOf(maxStrokeWidth, candidate.strokeWidth) * 2f)
                    val distanceToFirst = distanceSquared(endPos, first)
                    val distanceToLast = distanceSquared(endPos, last)
                    val candidateDistance = minOf(distanceToFirst, distanceToLast)
                    if (candidateDistance <= tol * tol && candidateDistance < nearestDistance) {
                        nearestIndex = candidateIndex
                        nearestAtStart = distanceToFirst <= distanceToLast
                        nearestDistance = candidateDistance
                        layerId = candidateLayerId
                    }
                }

                if (nearestIndex < 0) break

                val candidateStroke = strokes[nearestIndex].second
                maxStrokeWidth = maxOf(maxStrokeWidth, candidateStroke.strokeWidth)
                val nextPoints = candidateStroke.points.let { points ->
                    if (nearestAtStart) points else points.asReversed()
                }
                chain.addAll(nextPoints.drop(1))
                used += nearestIndex
            }

            if (closedPolygon != null && closedPolygon.size >= 4) {
                val area = polygonArea(closedPolygon)
                if (area > 50f) {
                    candidates.add(ContourCandidate(layerId, closedPolygon, maxStrokeWidth, area))
                }
            }
        }

        return candidates
    }

    private fun polygonArea(points: List<Offset>): Float {
        if (points.size < 3) return 0f
        var area = 0f
        var j = points.lastIndex
        for (i in points.indices) {
            area += (points[j].x + points[i].x) * (points[j].y - points[i].y)
            j = i
        }
        return kotlin.math.abs(area / 2f)
    }

    private fun distanceSquared(first: Offset, second: Offset): Float {
        val dx = first.x - second.x
        val dy = first.y - second.y
        return dx * dx + dy * dy
    }

    private fun pointInPolygon(point: Offset, polygon: List<Offset>, strokeWidth: Float = 10f): Boolean {
        if (polygon.size < 3) return false

        // 1. Check if tap point is on/near perimeter of the polygon (stroke boundary)
        val boundaryTolerance = maxOf(28f, strokeWidth * 1.8f)
        var previous = polygon.last()
        for (current in polygon) {
            if (isNearSegment(point, previous, current, boundaryTolerance)) {
                return true
            }
            previous = current
        }

        // 2. Check if tap point is inside the polygon using Ray Casting (even-odd rule)
        var inside = false
        previous = polygon.last()
        for (current in polygon) {
            if ((current.y > point.y) != (previous.y > point.y)) {
                val crossingX = (previous.x - current.x) * (point.y - current.y) /
                    (previous.y - current.y) + current.x
                if (point.x < crossingX) {
                    inside = !inside
                }
            }
            previous = current
        }
        return inside
    }

    private fun isNearSegment(point: Offset, start: Offset, end: Offset, tolerance: Float): Boolean {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared == 0f) return distanceSquared(point, start) <= tolerance * tolerance
        val projection = (((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared)
            .coerceIn(0f, 1f)
        val closest = Offset(start.x + projection * dx, start.y + projection * dy)
        return distanceSquared(point, closest) <= tolerance * tolerance
    }

    fun onPointerMove(pointerId: Long, screenPosition: Offset, pressure: Float = 1.0f) {

        val currentStroke = _activeStrokes.value[pointerId] ?: return
        val canvasPos = toCanvasCoordinate(screenPosition)

        // Handle Text Dragging / Transforming
        if (currentStroke.id <= -1000000L && currentStroke.tool == DrawingTool.TEXT) {
            if (currentStroke.id <= -2000000L) {
                // Moving the text
                val targetStrokeId = -currentStroke.id - 2000000L
                val lastPos = currentStroke.points.last()
                val dx = canvasPos.x - lastPos.x
                val dy = canvasPos.y - lastPos.y
                
                val updated = currentStroke.copy(points = currentStroke.points + canvasPos)
                _activeStrokes.value = _activeStrokes.value + (pointerId to updated)
                
                updateTextStrokePosition(targetStrokeId, dx, dy)
                return
            } else {
                // Rotating/Scaling
                val targetStrokeId = -currentStroke.id - 1000000L
                updateTextStrokeTransform(targetStrokeId, canvasPos)
                return
            }
        }

        // Handle Lasso Dragging
        if (currentStroke.id == -1L && currentStroke.tool == DrawingTool.LASSO) {
            val startPos = currentStroke.points.first()
            val dx = canvasPos.x - startPos.x
            val dy = canvasPos.y - startPos.y
            _lassoTransform.value = Offset(dx, dy)
            return
        }

        val lastPoint = currentStroke.points.lastOrNull()

        if (lastPoint != null) {
            val dx = canvasPos.x - lastPoint.x
            val dy = canvasPos.y - lastPoint.y
            // Lower threshold to capture fine handwriting detail.
            if (dx * dx + dy * dy < 1f) return
            
            // Optional: apply a very slight moving average to smooth out hardware digitizer jitter
            // val smoothedPos = Offset(lastPoint.x + dx * 0.7f, lastPoint.y + dy * 0.7f)
            // But with midpoint quadraticTo rendering and historical points, raw points usually look best.
        }

        val updatedStroke = currentStroke.copy(
            points = currentStroke.points + canvasPos,
            pressures = currentStroke.pressures + pressure
        )
        _activeStrokes.value = _activeStrokes.value + (pointerId to updatedStroke)
    }

    fun onPointerUp(pointerId: Long, screenPosition: Offset? = null) {
        pointerDownCanvasPos = null
        
        val activeStroke = _activeStrokes.value[pointerId] ?: return
        _activeStrokes.value = _activeStrokes.value - pointerId

        // Handle Text Touch / Drag Complete
        if (activeStroke.id <= -2000000L && activeStroke.tool == DrawingTool.TEXT) {
            val first = activeStroke.points.first()
            val last = activeStroke.points.last()
            val distSq = distanceSquared(first, last)
            
            // If they didn't move it much, treat as a tap to edit
            if (distSq < 100f) {
                val targetStrokeId = -activeStroke.id - 2000000L
                val tappedTextStroke = _layers.value.flatMap { layer ->
                    layer.frames.getOrNull(_currentFrameIndex.value)?.strokes.orEmpty()
                }.firstOrNull { it.id == targetStrokeId }
                
                if (tappedTextStroke != null) {
                    _editingTextStrokeId.value = tappedTextStroke.id
                    _textPromptPosition.value = tappedTextStroke.points.first()
                    _textPromptInitialValue.value = tappedTextStroke.text ?: ""
                    _showTextPrompt.value = true
                }
            }
            return
        } else if (activeStroke.id <= -1000000L && activeStroke.tool == DrawingTool.TEXT) {
            // Finished rotating/scaling text handle
            return
        }

        val stroke = screenPosition?.let { releasePosition ->
            val canvasPosition = toCanvasCoordinate(releasePosition)
            if (activeStroke.points.lastOrNull() == canvasPosition) activeStroke
            else activeStroke.copy(
                points = activeStroke.points + canvasPosition,
                pressures = activeStroke.pressures + (activeStroke.pressures.lastOrNull() ?: 1f)
            )
        } ?: activeStroke

        if (stroke.tool == DrawingTool.LASSO && stroke.id == -1L) {
            // Finished dragging lasso selection
            return
        }

        if (stroke.points.isEmpty()) return

        val finalStroke = stroke.copy(
            boundingBox = DrawnStroke.calculateBounds(stroke.points)
        )

        if (stroke.tool == DrawingTool.LASSO) {
            performLassoSelection(finalStroke)
            return
        }

        // Generate symmetry strokes
        val mirroredStrokes = generateMirroredStrokes(finalStroke)
        val allNewStrokes = listOf(finalStroke) + mirroredStrokes

        _undoStack.value = _undoStack.value + listOf(_layers.value)
        _redoStack.value = emptyList()

        val activeLayerId = _selectedLayerId.value ?: _layers.value.firstOrNull()?.id ?: return
        val currentFrameIdx = _currentFrameIndex.value
        
        _layers.value = _layers.value.map { layer ->
            if (layer.id == activeLayerId) {
                // We need to ensure the layer has enough frames to reach currentFrameIdx
                val updatedFrames = layer.frames.toMutableList()
                while (updatedFrames.size <= currentFrameIdx) {
                    updatedFrames.add(com.example.drawingo.model.DrawingFrame())
                }
                
                val currentFrame = updatedFrames[currentFrameIdx]
                val currentStrokes = currentFrame.strokes
                val updatedStrokes = if (currentStrokes.size >= MAX_STORED_STROKES) {
                    currentStrokes.drop(currentStrokes.size - MAX_STORED_STROKES + allNewStrokes.size) + allNewStrokes
                } else {
                    currentStrokes + allNewStrokes
                }
                
                updatedFrames[currentFrameIdx] = currentFrame.copy(strokes = updatedStrokes)
                layer.copy(frames = updatedFrames)
            } else layer
        }
        updateUndoRedoStates()
    }

    private fun generateMirroredStrokes(originalStroke: DrawnStroke): List<DrawnStroke> {
        val mode = _symmetryMode.value
        if (mode == com.example.drawingo.model.SymmetryMode.NONE || screenWidth == 0f || screenHeight == 0f) return emptyList()

        val midX = screenWidth / 2f
        val midY = screenHeight / 2f

        return buildList {
            if (mode == com.example.drawingo.model.SymmetryMode.VERTICAL || mode == com.example.drawingo.model.SymmetryMode.QUAD) {
                val mirroredPoints = originalStroke.points.map { Offset(midX + (midX - it.x), it.y) }
                add(originalStroke.copy(
                    id = strokeIdGenerator.getAndIncrement(),
                    points = mirroredPoints,
                    boundingBox = DrawnStroke.calculateBounds(mirroredPoints)
                ))
            }
            if (mode == com.example.drawingo.model.SymmetryMode.HORIZONTAL || mode == com.example.drawingo.model.SymmetryMode.QUAD) {
                val mirroredPoints = originalStroke.points.map { Offset(it.x, midY + (midY - it.y)) }
                add(originalStroke.copy(
                    id = strokeIdGenerator.getAndIncrement(),
                    points = mirroredPoints,
                    boundingBox = DrawnStroke.calculateBounds(mirroredPoints)
                ))
            }
            if (mode == com.example.drawingo.model.SymmetryMode.QUAD) {
                val mirroredPoints = originalStroke.points.map { Offset(midX + (midX - it.x), midY + (midY - it.y)) }
                add(originalStroke.copy(
                    id = strokeIdGenerator.getAndIncrement(),
                    points = mirroredPoints,
                    boundingBox = DrawnStroke.calculateBounds(mirroredPoints)
                ))
            }
        }
    }

    private fun performLassoSelection(lassoStroke: DrawnStroke) {
        val lassoBounds = lassoStroke.boundingBox
        val activeLayerId = _selectedLayerId.value ?: return
        val currentLayer = _layers.value.find { it.id == activeLayerId } ?: return
        val currentFrame = currentLayer.frames.getOrNull(_currentFrameIndex.value) ?: return

        // Simple bounding box intersection for selection
        val newlySelected = currentFrame.strokes.filter { stroke ->
            val bounds = stroke.boundingBox
            // Check if bounds intersect
            bounds.left <= lassoBounds.right && bounds.right >= lassoBounds.left &&
            bounds.top <= lassoBounds.bottom && bounds.bottom >= lassoBounds.top
        }.map { it.id }.toSet()

        _selectedStrokes.value = newlySelected
        _lassoTransform.value = Offset.Zero
    }

    private fun applyLassoTransformIfAny() {
        val transform = _lassoTransform.value
        val selectedIds = _selectedStrokes.value
        
        if (transform != Offset.Zero && selectedIds.isNotEmpty()) {
            val activeLayerId = _selectedLayerId.value ?: return
            
            _undoStack.value = _undoStack.value + listOf(_layers.value)
            _redoStack.value = emptyList()

            _layers.value = _layers.value.map { layer ->
                if (layer.id == activeLayerId) {
                    val currentFrameIdx = _currentFrameIndex.value
                    val updatedFrames = layer.frames.toMutableList()
                    val currentFrame = updatedFrames.getOrNull(currentFrameIdx)
                    
                    if (currentFrame != null) {
                        val updatedStrokes = currentFrame.strokes.map { stroke ->
                            if (stroke.id in selectedIds) {
                                val shiftedPoints = stroke.points.map { Offset(it.x + transform.x, it.y + transform.y) }
                                stroke.copy(
                                    points = shiftedPoints,
                                    boundingBox = DrawnStroke.calculateBounds(shiftedPoints)
                                )
                            } else stroke
                        }
                        updatedFrames[currentFrameIdx] = currentFrame.copy(strokes = updatedStrokes)
                        layer.copy(frames = updatedFrames)
                    } else layer
                } else layer
            }
            updateUndoRedoStates()
        }
        
        _lassoTransform.value = Offset.Zero
        _selectedStrokes.value = emptySet()
    }

    fun onPointerCancel(pointerId: Long) {
        pointerDownCanvasPos = null
        _activeStrokes.value = _activeStrokes.value - pointerId
    }

    override fun onCleared() {
        super.onCleared()
        soundManager?.release()
        soundManager = null
    }
}
