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

    fun createNewProject() {
        _currentProjectId.value = null
        _completedStrokes.value = emptyList()
        _activeStrokes.value = emptyMap()
        _undoStack.value = emptyList()
        _redoStack.value = emptyList()
        updateUndoRedoStates()
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            _currentProjectId.value = projectId
            val strokes = drawingDao.getStrokesForProject(projectId)
            val drawnStrokes = strokes.mapIndexed { index, entity ->
                StrokeMapper.fromEntity(entity, strokeIdGenerator.getAndIncrement())
            }
            _completedStrokes.value = drawnStrokes
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
            
            // Delete old strokes and insert new ones
            drawingDao.deleteStrokesForProject(newId)
            val strokeEntities = _completedStrokes.value.mapIndexed { index, stroke ->
                StrokeMapper.toEntity(stroke, newId, index)
            }
            drawingDao.insertStrokes(strokeEntities)
        }
    }

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

    // Undo and Redo Stacks
    private val _undoStack = MutableStateFlow<List<List<DrawnStroke>>>(emptyList())
    private val _redoStack = MutableStateFlow<List<List<DrawnStroke>>>(emptyList())

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Completed drawn strokes
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
            val previousState = currentUndo.last()
            val newUndo = currentUndo.dropLast(1)
            _redoStack.value = _redoStack.value + listOf(_completedStrokes.value)
            _undoStack.value = newUndo
            _completedStrokes.value = previousState
            updateUndoRedoStates()
        }
    }

    fun redo() {
        val currentRedo = _redoStack.value
        if (currentRedo.isNotEmpty()) {
            val nextState = currentRedo.last()
            val newRedo = currentRedo.dropLast(1)
            _undoStack.value = _undoStack.value + listOf(_completedStrokes.value)
            _redoStack.value = newRedo
            _completedStrokes.value = nextState
            updateUndoRedoStates()
        }
    }

    fun clearCanvas() {
        if (_completedStrokes.value.isNotEmpty()) {
            stopAnimation()
            _undoStack.value = _undoStack.value + listOf(_completedStrokes.value)
            _redoStack.value = emptyList()
            _completedStrokes.value = emptyList()
            updateUndoRedoStates()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = _undoStack.value.isNotEmpty()
        _canRedo.value = _redoStack.value.isNotEmpty()
    }

    fun onPointerDown(pointerId: Long, screenPosition: Offset) {

        val tool = _selectedTool.value
        val canvasPos = toCanvasCoordinate(screenPosition)

        val strokeColor = when (tool) {
            DrawingTool.ERASER -> Color.White
            DrawingTool.PEN, DrawingTool.HIGHLIGHTER, DrawingTool.BRUSH,
            DrawingTool.WATERCOLOR, DrawingTool.CRAYON, DrawingTool.LASSO -> _selectedColor.value
        }
        val strokeWidth = when (tool) {
            DrawingTool.PEN -> _selectedStrokeWidth.value
            DrawingTool.HIGHLIGHTER -> _selectedStrokeWidth.value * 2.2f
            DrawingTool.BRUSH -> _selectedStrokeWidth.value * 1.8f
            DrawingTool.ERASER -> _selectedEraserWidth.value
            DrawingTool.LASSO -> 4f
            DrawingTool.WATERCOLOR -> _selectedStrokeWidth.value * 2.2f
            DrawingTool.CRAYON -> _selectedStrokeWidth.value * 1.25f
        }
        val alpha = when (tool) {
            DrawingTool.PEN -> 1.0f
            DrawingTool.HIGHLIGHTER -> 0.38f
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
            points = listOf(canvasPos)
        )

        _activeStrokes.value = _activeStrokes.value + (pointerId to newStroke)
    }

    fun onPointerMove(pointerId: Long, screenPosition: Offset) {

        val currentStroke = _activeStrokes.value[pointerId] ?: return
        val canvasPos = toCanvasCoordinate(screenPosition)
        val lastPoint = currentStroke.points.lastOrNull()

        if (lastPoint != null) {
            val dx = canvasPos.x - lastPoint.x
            val dy = canvasPos.y - lastPoint.y
            if (dx * dx + dy * dy < 9f) return
        }

        val updatedStroke = currentStroke.copy(
            points = currentStroke.points + canvasPos
        )
        _activeStrokes.value = _activeStrokes.value + (pointerId to updatedStroke)
    }

    fun onPointerUp(pointerId: Long) {

        val stroke = _activeStrokes.value[pointerId] ?: return
        _activeStrokes.value = _activeStrokes.value - pointerId

        if (stroke.points.isEmpty()) return

        val finalStroke = stroke.copy(
            boundingBox = DrawnStroke.calculateBounds(stroke.points)
        )

        _undoStack.value = _undoStack.value + listOf(_completedStrokes.value)
        _redoStack.value = emptyList()
        val currentList = _completedStrokes.value
        _completedStrokes.value = if (currentList.size >= MAX_STORED_STROKES) {
            currentList.drop(currentList.size - MAX_STORED_STROKES + 1) + finalStroke
        } else {
            currentList + finalStroke
        }
        updateUndoRedoStates()
    }

    /**
     * Triggers Gemini AI Drawing-to-Animation recognition via ADC Backend Client with local fallback.
     */
    fun triggerDrawingAnimation(context: Context, canvasBitmap: Bitmap, allowCloudAi: Boolean) {
        val strokes = _completedStrokes.value
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
