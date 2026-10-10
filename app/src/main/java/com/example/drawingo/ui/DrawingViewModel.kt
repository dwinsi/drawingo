package com.example.drawingo.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingo.animation.ParticleEngine
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.model.AnimatedDrawingEntity
import com.example.drawingo.model.AnimationSceneResult
import com.example.drawingo.model.AnimationSceneType
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.model.Particle
import com.example.drawingo.net.AdcBackendClient
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

import com.example.drawingo.data.DrawingDao
import com.example.drawingo.data.DrawingProject
import com.example.drawingo.util.StrokeMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/**
 * Lightweight MVVM ViewModel managing the Drawingo canvas,
 * tool selection, 2-finger Pan/Scroll & Pinch-Zoom, Undo/Redo,
 * drawing tools, canvas gestures, and optional cloud-assisted drawing animation.
 */
class DrawingViewModel(private val drawingDao: DrawingDao) : ViewModel() {

    val allProjects = drawingDao.getAllProjects().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _layers = MutableStateFlow<List<com.example.drawingo.model.DrawingLayer>>(emptyList())
    val layers: StateFlow<List<com.example.drawingo.model.DrawingLayer>> = _layers.asStateFlow()

    private val _selectedLayerId = MutableStateFlow<Long?>(null)
    val selectedLayerId: StateFlow<Long?> = _selectedLayerId.asStateFlow()

    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()

    private val _isPlayingLocalAnimation = MutableStateFlow(false)
    val isPlayingLocalAnimation: StateFlow<Boolean> = _isPlayingLocalAnimation.asStateFlow()

    private val _playbackFps = MutableStateFlow(12)
    val playbackFps: StateFlow<Int> = _playbackFps.asStateFlow()
    
    private val _onionSkinEnabled = MutableStateFlow(true)
    val onionSkinEnabled: StateFlow<Boolean> = _onionSkinEnabled.asStateFlow()

    enum class QuickMagicPreset { NONE, WIGGLE, PULSE, DRAW_ON }
    
    private val _activePreset = MutableStateFlow(QuickMagicPreset.NONE)
    val activePreset: StateFlow<QuickMagicPreset> = _activePreset.asStateFlow()

    fun createNewProject() {
        _currentProjectId.value = null
        
        val initialLayer = com.example.drawingo.model.DrawingLayer(id = System.currentTimeMillis(), name = "Layer 1")
        _layers.value = listOf(initialLayer)
        _selectedLayerId.value = initialLayer.id
        _currentFrameIndex.value = 0
        _isPlayingLocalAnimation.value = false
        _activePreset.value = QuickMagicPreset.NONE
        
        _completedStrokes.value = emptyList() // We can keep this for flat drawing or replace it. Actually we should remove it and use layers.strokes.
        _activeStrokes.value = emptyMap()
        _undoStack.value = emptyList()
        _redoStack.value = emptyList()
        updateUndoRedoStates()
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _currentProjectId.value = projectId
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
            _activePreset.value = QuickMagicPreset.NONE
            
            _activeStrokes.value = emptyMap()
            _undoStack.value = emptyList()
            _redoStack.value = emptyList()
            updateUndoRedoStates()
        }
    }

    fun saveCurrentProject(name: String) {
        viewModelScope.launch {
            val projectId = _currentProjectId.value
            val newId = if (projectId == null) {
                // Insert new project
                val project = DrawingProject(name = name)
                drawingDao.insertProject(project)
            } else {
                val project = drawingDao.getProjectById(projectId) ?: return@launch
                drawingDao.updateProject(project.copy(name = name, updatedAt = System.currentTimeMillis()))
                projectId
            }
            _currentProjectId.value = newId
            
            // Rebuild layers and strokes
            drawingDao.deleteLayersForProject(newId)
            drawingDao.deleteStrokesForProject(newId)
            
            val layerEntities = _layers.value.mapIndexed { index, layer ->
                com.example.drawingo.data.LayerEntity(
                    projectId = newId,
                    name = layer.name,
                    isVisible = layer.isVisible,
                    opacity = layer.opacity,
                    orderIndex = index
                )
            }
            
            // Insert layers and get their new IDs
            val newLayerIds = drawingDao.insertLayers(layerEntities)
            
            // Now associate strokes with these new Layer IDs and Frame Indices
            val allStrokeEntities = mutableListOf<com.example.drawingo.data.StrokeEntity>()
            _layers.value.forEachIndexed { layerIndex, layer ->
                val assignedLayerId = newLayerIds[layerIndex]
                layer.frames.forEachIndexed { frameIdx, frame ->
                    frame.strokes.forEachIndexed { strokeIndex, stroke ->
                        allStrokeEntities.add(StrokeMapper.toEntity(stroke, newId, assignedLayerId, frameIdx, strokeIndex))
                    }
                }
            }
            drawingDao.insertStrokes(allStrokeEntities)
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

    private val _selectedColor = MutableStateFlow(DrawingoPalette.grid[0][0])
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _selectedStrokeWidth = MutableStateFlow(20f)
    val selectedStrokeWidth: StateFlow<Float> = _selectedStrokeWidth.asStateFlow()

    private val _selectedEraserWidth = MutableStateFlow(32f)
    val selectedEraserWidth: StateFlow<Float> = _selectedEraserWidth.asStateFlow()

    private val _selectedPaperStyle = MutableStateFlow(com.example.drawingo.model.CanvasPaperStyle.PURE_WHITE)
    val selectedPaperStyle: StateFlow<com.example.drawingo.model.CanvasPaperStyle> = _selectedPaperStyle.asStateFlow()

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

    fun setQuickMagicPreset(preset: QuickMagicPreset) {
        _activePreset.value = preset
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

    // Drawing analysis status
    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    private val _showGeminiDialog = MutableStateFlow(false)
    val showGeminiDialog: StateFlow<Boolean> = _showGeminiDialog.asStateFlow()

    private val _animationStatus = MutableStateFlow<String?>(null)
    val animationStatus: StateFlow<String?> = _animationStatus.asStateFlow()

    private val _animationSubject = MutableStateFlow("Your drawing")
    val animationSubject: StateFlow<String> = _animationSubject.asStateFlow()
    private val _isVideoGenerating = MutableStateFlow(false)
    val isVideoGenerating: StateFlow<Boolean> = _isVideoGenerating.asStateFlow()
    private val _generatedVideo = MutableStateFlow<File?>(null)
    val generatedVideo: StateFlow<File?> = _generatedVideo.asStateFlow()
    private val _videoError = MutableStateFlow<String?>(null)
    val videoError: StateFlow<String?> = _videoError.asStateFlow()
    private var currentGeminiInteractionId: String = ""

    // Drawing-to-Animation Core State
    private val _isAnimationActive = MutableStateFlow(false)
    val isAnimationActive: StateFlow<Boolean> = _isAnimationActive.asStateFlow()

    private val _activeAnimationScene = MutableStateFlow<AnimationSceneType?>(null)
    val activeAnimationScene: StateFlow<AnimationSceneType?> = _activeAnimationScene.asStateFlow()

    private val _animatedEntity = MutableStateFlow<AnimatedDrawingEntity?>(null)
    val animatedEntity: StateFlow<AnimatedDrawingEntity?> = _animatedEntity.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _animationProgress = MutableStateFlow(0f)
    val animationProgress: StateFlow<Float> = _animationProgress.asStateFlow()

    private var animationLoopJob: Job? = null

    companion object {
        private const val MAX_STORED_STROKES = 250
    }

    fun setTool(tool: DrawingTool) {
        if (_selectedTool.value == DrawingTool.LASSO && tool != DrawingTool.LASSO) {
            applyLassoTransformIfAny()
        }
        _selectedTool.value = tool
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
            stopAnimation()
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
            DrawingTool.WATERCOLOR, DrawingTool.CRAYON -> _selectedColor.value
            DrawingTool.LASSO -> Color(0xFF69489B) // Purple color for lasso line
        }
        val strokeWidth = when (tool) {
            DrawingTool.PEN -> _selectedStrokeWidth.value
            DrawingTool.BRUSH -> _selectedStrokeWidth.value * 1.8f
            DrawingTool.ERASER -> _selectedEraserWidth.value
            DrawingTool.LASSO -> 4f
            DrawingTool.WATERCOLOR -> _selectedStrokeWidth.value * 2.2f
            DrawingTool.CRAYON -> _selectedStrokeWidth.value * 1.25f
        }
        val alpha = when (tool) {
            DrawingTool.PEN -> 1.0f
            DrawingTool.BRUSH -> 0.85f
            DrawingTool.WATERCOLOR -> 0.68f
            DrawingTool.CRAYON -> 0.95f
            DrawingTool.ERASER, DrawingTool.LASSO -> 1.0f
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

    fun onPointerMove(pointerId: Long, screenPosition: Offset, pressure: Float = 1.0f) {

        val currentStroke = _activeStrokes.value[pointerId] ?: return
        val canvasPos = toCanvasCoordinate(screenPosition)
        
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
            if (dx * dx + dy * dy < 9f) return
        }

        val updatedStroke = currentStroke.copy(
            points = currentStroke.points + canvasPos,
            pressures = currentStroke.pressures + pressure
        )
        _activeStrokes.value = _activeStrokes.value + (pointerId to updatedStroke)
    }

    fun onPointerUp(pointerId: Long) {

        val stroke = _activeStrokes.value[pointerId] ?: return
        _activeStrokes.value = _activeStrokes.value - pointerId

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

    /**
     * Triggers Gemini AI Drawing-to-Animation recognition via ADC Backend Client with local fallback.
     */
    fun triggerDrawingAnimation(context: Context, canvasBitmap: Bitmap, allowCloudAi: Boolean) {
        val strokes = _layers.value.flatMap { layer -> 
            val frame = layer.frames.getOrNull(_currentFrameIndex.value)
            frame?.strokes ?: emptyList()
        }
        if (strokes.isEmpty()) return

        viewModelScope.launch {
            _isGeminiLoading.value = true
            _animationStatus.value = null
            currentGeminiInteractionId = UUID.randomUUID().toString()
            soundManager?.playChimeTwinkle()

            val sceneResult = if (allowCloudAi) {
                AdcBackendClient.analyzeDrawing(context, canvasBitmap, currentGeminiInteractionId)
            } else null

            _isGeminiLoading.value = false
            if (sceneResult == null) {
                _animationStatus.value = if (allowCloudAi) {
                    "Cloud analysis could not connect. Showing a local animation preview."
                } else {
                    "Cloud animation is off. Enable it in Settings to analyze this drawing."
                }
            }
            val activeScene = sceneResult ?: AnimationSceneResult(AnimationSceneType.ABSTRACT_FLOW, "Your drawing")
            _animationSubject.value = sceneResult?.subjectName ?: "Your drawing"
            _showGeminiDialog.value = true
            _activeAnimationScene.value = activeScene.sceneType

            var minX = Float.POSITIVE_INFINITY
            var minY = Float.POSITIVE_INFINITY
            var maxX = Float.NEGATIVE_INFINITY
            var maxY = Float.NEGATIVE_INFINITY
            for (s in strokes) {
                for (p in s.points) {
                    if (p.x < minX) minX = p.x
                    if (p.x > maxX) maxX = p.x
                    if (p.y < minY) minY = p.y
                    if (p.y > maxY) maxY = p.y
                }
            }
            val bounds = Rect(minX, minY, maxX, maxY)
            _animatedEntity.value = AnimatedDrawingEntity(strokes = strokes, bounds = bounds)

            startAnimationLoop()
        }
    }

    private fun startAnimationLoop() {
        animationLoopJob?.cancel()
        _isAnimationActive.value = true
        _animationProgress.value = 0f
        _particles.value = emptyList()

        animationLoopJob = viewModelScope.launch {
            val totalDurationMs = 5000L
            val stepMs = 16L
            var elapsedMs = 0L
            var splashTriggered = false

            while (_isAnimationActive.value) {
                delay(stepMs)
                elapsedMs += stepMs
                val progress = (elapsedMs.toFloat() % totalDurationMs) / totalDurationMs
                _animationProgress.value = progress

                val scene = _activeAnimationScene.value
                val newParticles = mutableListOf<Particle>()

                when (scene) {
                    AnimationSceneType.OCEAN_LEAP -> {
                        if ((progress in 0.06f..0.12f || progress in 0.88f..0.94f) && !splashTriggered) {
                            splashTriggered = true
                            soundManager?.playBubblePop()
                            newParticles.addAll(ParticleEngine.createWaterSplash(x = 400f, y = 600f, count = 16))
                        } else if (progress > 0.15f && progress < 0.85f) {
                            splashTriggered = false
                        }
                        if (Random.nextFloat() < 0.18f) {
                            newParticles.addAll(ParticleEngine.createBubbles(x = Random.nextFloat() * 800f, y = 700f, count = 2))
                        }
                    }
                    AnimationSceneType.SPACE_LAUNCH -> {
                        if (Random.nextFloat() < 0.35f) {
                            newParticles.addAll(ParticleEngine.createSmokePuffs(x = 300f + progress * 400f, y = 600f - progress * 400f, count = 2))
                        }
                    }
                    AnimationSceneType.SKY_FLIGHT, AnimationSceneType.ABSTRACT_FLOW -> {
                        if (Random.nextFloat() < 0.25f) {
                            newParticles.addAll(ParticleEngine.createStarDust(x = 200f + progress * 600f, y = 300f + sin(progress * 10f) * 100f, count = 3))
                        }
                    }
                    else -> {}
                }

                val currentParticles = _particles.value + newParticles
                _particles.value = ParticleEngine.updateParticles(currentParticles, stepMs)
            }
        }
    }

    fun stopAnimation() {
        _isAnimationActive.value = false
        _activeAnimationScene.value = null
        _animatedEntity.value = null
        _particles.value = emptyList()
        animationLoopJob?.cancel()
    }

    fun closeGeminiDialog() {
        _showGeminiDialog.value = false
    }

    fun generateVideo(context: Context, canvasBitmap: Bitmap, prompt: String, aspectRatio: String = "16:9") {
        if (_isVideoGenerating.value) return
        viewModelScope.launch {
            _isVideoGenerating.value = true
            _videoError.value = null
            _generatedVideo.value?.delete()
            _generatedVideo.value = null
            val result = AdcBackendClient.generateVideo(context, canvasBitmap, prompt, aspectRatio)
            _generatedVideo.value = result.file
            if (result.file == null) {
                _videoError.value = result.errorMessage ?: "Could not create the video. Check the backend setup and try again."
            }
            _isVideoGenerating.value = false
        }
    }

    fun closeGeneratedVideo() {
        _generatedVideo.value?.delete()
        _generatedVideo.value = null
    }

    fun onPointerCancel(pointerId: Long) {
        _activeStrokes.value = _activeStrokes.value - pointerId
    }

    override fun onCleared() {
        super.onCleared()
        stopAnimation()
        _generatedVideo.value?.delete()
        _generatedVideo.value = null
        soundManager?.release()
        soundManager = null
    }
}
