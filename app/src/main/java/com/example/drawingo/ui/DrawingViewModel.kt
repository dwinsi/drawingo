package com.example.drawingo.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingo.ai.GeminiMagicManager
import com.example.drawingo.animation.ParticleEngine
import com.example.drawingo.audio.NaturalAudioPlayer
import com.example.drawingo.audio.NaturalVoiceManager
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.audio.TextToSpeechManager
import com.example.drawingo.model.AnimatedDrawingEntity
import com.example.drawingo.model.AnimationSceneResult
import com.example.drawingo.model.AnimationSceneType
import com.example.drawingo.model.CanvasMode
import com.example.drawingo.model.CanvasPaperStyle
import com.example.drawingo.model.CreatureCategory
import com.example.drawingo.model.CreatureType
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.model.GooglyEyePair
import com.example.drawingo.model.MagicCompanion
import com.example.drawingo.model.NeonPalette
import com.example.drawingo.model.Particle
import com.example.drawingo.net.AdcBackendClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight MVVM ViewModel managing Drawingo canvas mode,
 * tool selection, 2-finger Pan/Scroll & Pinch-Zoom, Undo/Redo,
 * Toddler Magic mode, and Gemini AI Drawing-to-Animation with ADC Backend Integration.
 */
class DrawingViewModel : ViewModel() {

    private val strokeIdGenerator = AtomicLong(1L)
    private val eyeIdGenerator = AtomicLong(1L)
    private val companionIdGenerator = AtomicLong(1L)
    private var colorIndex = 0
    private var creatureCycleIndex = 0

    var soundManager: SoundManager? = null
    var textToSpeechManager: TextToSpeechManager? = null
    var naturalAudioPlayer: NaturalAudioPlayer? = null

    // Mode: DRAWINGO vs TODDLER_MAGIC
    private val _canvasMode = MutableStateFlow(CanvasMode.DRAWINGO)
    val canvasMode: StateFlow<CanvasMode> = _canvasMode.asStateFlow()

    // Canvas Paper Style
    private val _paperStyle = MutableStateFlow(CanvasPaperStyle.PURE_WHITE)
    val paperStyle: StateFlow<CanvasPaperStyle> = _paperStyle.asStateFlow()

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

    // Spawned googly eyes
    private val _googlyEyes = MutableStateFlow<List<GooglyEyePair>>(emptyList())
    val googlyEyes: StateFlow<List<GooglyEyePair>> = _googlyEyes.asStateFlow()

    // Spawned magical companions
    private val _magicCompanions = MutableStateFlow<List<MagicCompanion>>(emptyList())
    val magicCompanions: StateFlow<List<MagicCompanion>> = _magicCompanions.asStateFlow()

    // Wipe animation state
    private val _wipeProgress = MutableStateFlow(0f)
    val wipeProgress: StateFlow<Float> = _wipeProgress.asStateFlow()

    private val _isWiping = MutableStateFlow(false)
    val isWiping: StateFlow<Boolean> = _isWiping.asStateFlow()

    // Gemini AI Magic State
    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    private val _geminiRhymeText = MutableStateFlow<String?>(null)
    val geminiRhymeText: StateFlow<String?> = _geminiRhymeText.asStateFlow()

    private val _showGeminiDialog = MutableStateFlow(false)
    val showGeminiDialog: StateFlow<Boolean> = _showGeminiDialog.asStateFlow()

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

    private var inactivityJob: Job? = null
    private var wipeAnimationJob: Job? = null
    private var animationLoopJob: Job? = null

    companion object {
        const val INACTIVITY_TIMEOUT_MS = 60_000L
        const val WIPE_DURATION_MS = 1_200L
        private const val MAX_STORED_EYES = 35
        private const val MAX_STORED_COMPANIONS = 35
        private const val MAX_STORED_STROKES = 250

        private val CompanionColorPresets = listOf(
            Pair(Color(0xFFFF4081), Color(0xFFB026FF)),
            Pair(Color(0xFF00E5FF), Color(0xFFFF1493)),
            Pair(Color(0xFFFF6D00), Color(0xFF202124)),
            Pair(Color(0xFFFFD600), Color(0xFFFF5252)),
            Pair(Color(0xFFFFD54F), Color(0xFFFF9100)),
            Pair(Color(0xFFFFF8E1), Color(0xFFFF80AB)),
            Pair(Color(0xFF80D8FF), Color(0xFFB388FF)),
            Pair(Color(0xFF00E5FF), Color(0xFFFFD600)),
            Pair(Color(0xFF1E1B2E), Color(0xFFFF9100)),
            Pair(Color(0xFFFF007F), Color(0xFF00E5FF)),
            Pair(Color(0xFFFF1493), Color(0xFFFFD600)),
            Pair(Color(0xFFFFD600), Color(0xFFFF6D00))
        )
    }

    init {
        resetInactivityTimer()
    }

    fun setCanvasMode(mode: CanvasMode) {
        _canvasMode.value = mode
    }

    fun cyclePaperStyle() {
        val styles = CanvasPaperStyle.values()
        val nextIndex = (paperStyle.value.ordinal + 1) % styles.size
        _paperStyle.value = styles[nextIndex]
    }

    fun setPaperStyle(style: CanvasPaperStyle) {
        _paperStyle.value = style
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
        if (_completedStrokes.value.isNotEmpty() || _googlyEyes.value.isNotEmpty() || _magicCompanions.value.isNotEmpty()) {
            stopAnimation()
            _undoStack.value = _undoStack.value + listOf(_completedStrokes.value)
            _redoStack.value = emptyList()
            _completedStrokes.value = emptyList()
            _googlyEyes.value = emptyList()
            _magicCompanions.value = emptyList()
            updateUndoRedoStates()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = _undoStack.value.isNotEmpty()
        _canRedo.value = _redoStack.value.isNotEmpty()
    }

    fun onPointerDown(pointerId: Long, screenPosition: Offset) {
        cancelActiveWipe()
        resetInactivityTimer()

        val mode = _canvasMode.value
        val tool = _selectedTool.value
        val canvasPos = toCanvasCoordinate(screenPosition)

        val newStroke = if (mode == CanvasMode.DRAWINGO) {
            val strokeColor = when (tool) {
                DrawingTool.ERASER -> Color.White
                DrawingTool.PEN, DrawingTool.HIGHLIGHTER, DrawingTool.BRUSH, DrawingTool.LASSO -> _selectedColor.value
            }
            val strokeWidth = when (tool) {
                DrawingTool.PEN -> _selectedStrokeWidth.value
                DrawingTool.HIGHLIGHTER -> _selectedStrokeWidth.value * 2.2f
                DrawingTool.BRUSH -> _selectedStrokeWidth.value * 1.8f
                DrawingTool.ERASER -> _selectedStrokeWidth.value * 2.8f
                DrawingTool.LASSO -> 4f
            }
            val alpha = when (tool) {
                DrawingTool.PEN -> 1.0f
                DrawingTool.HIGHLIGHTER -> 0.38f
                DrawingTool.BRUSH -> 0.85f
                DrawingTool.ERASER, DrawingTool.LASSO -> 1.0f
            }

            DrawnStroke(
                id = strokeIdGenerator.getAndIncrement(),
                color = strokeColor,
                strokeWidth = strokeWidth,
                alpha = alpha,
                tool = tool,
                points = listOf(canvasPos)
            )
        } else {
            val nextColor = NeonPalette.getColor(colorIndex++)
            DrawnStroke(
                id = strokeIdGenerator.getAndIncrement(),
                color = nextColor,
                strokeWidth = 32f,
                alpha = 1.0f,
                tool = DrawingTool.PEN,
                points = listOf(canvasPos)
            )
        }

        _activeStrokes.value = _activeStrokes.value + (pointerId to newStroke)
    }

    fun onPointerMove(pointerId: Long, screenPosition: Offset) {
        resetInactivityTimer()

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
        resetInactivityTimer()

        val stroke = _activeStrokes.value[pointerId] ?: return
        _activeStrokes.value = _activeStrokes.value - pointerId

        if (stroke.points.isEmpty()) return

        val finalStroke = stroke.copy(
            boundingBox = DrawnStroke.calculateBounds(stroke.points)
        )

        if (_canvasMode.value == CanvasMode.DRAWINGO) {
            _undoStack.value = _undoStack.value + listOf(_completedStrokes.value)
            _redoStack.value = emptyList()

            val currentList = _completedStrokes.value
            _completedStrokes.value = if (currentList.size >= MAX_STORED_STROKES) {
                currentList.drop(currentList.size - MAX_STORED_STROKES + 1) + finalStroke
            } else {
                currentList + finalStroke
            }
            updateUndoRedoStates()
        } else {
            val width = finalStroke.boundingBox.width
            val height = finalStroke.boundingBox.height
            val isTap = finalStroke.points.size <= 1 || (width < 10f && height < 10f)

            if (isTap) {
                val tapPos = finalStroke.points.firstOrNull() ?: Offset(finalStroke.boundingBox.left, finalStroke.boundingBox.top)
                spawnMagicalCompanionAt(tapPos, isDirectTap = true)
            } else {
                val strokeCenter = finalStroke.boundingBox.center
                spawnMagicalCompanionAt(strokeCenter, isDirectTap = false)
                spawnGooglyEyes(finalStroke.boundingBox, finalStroke.points)
            }
        }
    }

    fun spawnMagicalCompanionAt(position: Offset, isDirectTap: Boolean) {
        val allTypes = CreatureType.values()
        val type = allTypes[(creatureCycleIndex++) % allTypes.size]

        val colors = CompanionColorPresets[(type.ordinal) % CompanionColorPresets.size]
        val baseSize = if (isDirectTap) 105f else 92f

        val companion = MagicCompanion(
            id = companionIdGenerator.getAndIncrement(),
            type = type,
            position = position,
            size = baseSize,
            primaryColor = colors.first,
            secondaryColor = colors.second,
            spawnTimestamp = System.currentTimeMillis()
        )

        val currentCompanions = _magicCompanions.value
        _magicCompanions.value = if (currentCompanions.size >= MAX_STORED_COMPANIONS) {
            currentCompanions.drop(currentCompanions.size - MAX_STORED_COMPANIONS + 1) + companion
        } else {
            currentCompanions + companion
        }

        when (type.category) {
            CreatureCategory.BUTTERFLY, CreatureCategory.FLOWER, CreatureCategory.BIRD -> {
                soundManager?.playChimeTwinkle()
            }
            CreatureCategory.WILD_ANIMAL -> {
                soundManager?.playSqueak()
            }
            CreatureCategory.SEA_CREATURE, CreatureCategory.DOODLE_FACE -> {
                soundManager?.playBubblePop()
            }
        }
    }

    private fun spawnGooglyEyes(bounds: Rect, points: List<Offset>) {
        val width = bounds.width
        val height = bounds.height

        val eyeRadius: Float
        val leftCenter: Offset
        val rightCenter: Offset

        if (width < 30f && height < 30f) {
            val center = points.firstOrNull() ?: Offset(bounds.left, bounds.top)
            eyeRadius = 32f
            val spacing = eyeRadius * 2.1f
            leftCenter = Offset(center.x - spacing / 2f, center.y - 10f)
            rightCenter = Offset(center.x + spacing / 2f, center.y - 10f)
        } else {
            val baseDim = min(width, height)
            eyeRadius = (baseDim * 0.22f).coerceIn(28f, 72f)
            val spacing = eyeRadius * 2.2f

            val cx = bounds.left + width / 2f
            val cy = if (height > eyeRadius * 2.5f) {
                bounds.top + height * 0.35f
            } else {
                bounds.top + height * 0.5f
            }

            leftCenter = Offset(cx - spacing / 2f, cy)
            rightCenter = Offset(cx + spacing / 2f, cy)
        }

        val pupilRadius = eyeRadius * 0.44f
        val maxOffset = eyeRadius * 0.38f

        val mode = Random.nextInt(4)
        val (leftPupilOffset, rightPupilOffset) = when (mode) {
            0 -> Pair(Offset(maxOffset * 0.7f, maxOffset * 0.5f), Offset(-maxOffset * 0.7f, maxOffset * 0.5f))
            1 -> {
                val angle = Random.nextFloat() * 6.28f
                val dist = maxOffset * (0.4f + Random.nextFloat() * 0.6f)
                val offset = Offset(cos(angle) * dist, sin(angle) * dist)
                Pair(offset, offset)
            }
            2 -> Pair(Offset(-maxOffset * 0.7f, 0f), Offset(maxOffset * 0.7f, 0f))
            else -> Pair(Offset(0f, maxOffset * 0.8f), Offset(0f, maxOffset * 0.8f))
        }

        val eyePair = GooglyEyePair(
            id = eyeIdGenerator.getAndIncrement(),
            leftCenter = leftCenter,
            rightCenter = rightCenter,
            radius = eyeRadius,
            pupilRadius = pupilRadius,
            leftPupilOffset = leftPupilOffset,
            rightPupilOffset = rightPupilOffset,
            hasSmile = true,
            spawnTimestamp = System.currentTimeMillis()
        )

        val currentEyes = _googlyEyes.value
        _googlyEyes.value = if (currentEyes.size >= MAX_STORED_EYES) {
            currentEyes.drop(currentEyes.size - MAX_STORED_EYES + 1) + eyePair
        } else {
            currentEyes + eyePair
        }
    }

    /**
     * Triggers Gemini AI Drawing-to-Animation recognition via ADC Backend Client with local fallback.
     */
    fun triggerDrawingAnimation(context: Context, canvasBitmap: Bitmap, apiKey: String) {
        val strokes = _completedStrokes.value
        if (strokes.isEmpty()) return

        viewModelScope.launch {
            _isGeminiLoading.value = true
            soundManager?.playChimeTwinkle()

            // 1. Try ADC Backend Client first
            var sceneResult = AdcBackendClient.analyzeDrawing(canvasBitmap)

            // 2. Fallback to Gemini AI direct API if ADC backend client is offline
            if (sceneResult == null) {
                sceneResult = GeminiMagicManager.analyzeDrawingForAnimation(canvasBitmap, apiKey)
            }

            _isGeminiLoading.value = false
            _geminiRhymeText.value = sceneResult.rhymeText
            _showGeminiDialog.value = true
            _activeAnimationScene.value = sceneResult.sceneType

            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
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
            speakNaturalVoiceRhyme(context, sceneResult.rhymeText, apiKey)
        }
    }

    private suspend fun speakNaturalVoiceRhyme(context: Context, text: String, apiKey: String) {
        // 1. Try ADC Backend Client for speech synthesis
        var naturalAudioFile = AdcBackendClient.synthesizeSpeech(context, text)

        // 2. Fallback to NaturalVoiceManager / local cache
        if (naturalAudioFile == null) {
            naturalAudioFile = NaturalVoiceManager.fetchNaturalVoiceAudio(context, text, apiKey)
        }

        if (naturalAudioFile != null && naturalAudioFile.exists()) {
            textToSpeechManager?.stop()
            naturalAudioPlayer?.playAudioFile(naturalAudioFile)
        } else {
            naturalAudioPlayer?.stop()
            textToSpeechManager?.speakRhyme(text)
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
                    AnimationSceneType.SKY_FLIGHT, AnimationSceneType.MAGIC_DANCE -> {
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
        naturalAudioPlayer?.stop()
        textToSpeechManager?.stop()
    }

    fun replayGeminiSpeech(context: Context, apiKey: String) {
        val text = _geminiRhymeText.value
        if (!text.isNullOrBlank()) {
            if (naturalAudioPlayer?.isPlaying() == false) {
                naturalAudioPlayer?.replay()
            } else {
                viewModelScope.launch {
                    speakNaturalVoiceRhyme(context, text, apiKey)
                }
            }
        }
    }

    fun closeGeminiDialog() {
        _showGeminiDialog.value = false
        naturalAudioPlayer?.stop()
        textToSpeechManager?.stop()
    }

    fun onPointerCancel(pointerId: Long) {
        _activeStrokes.value = _activeStrokes.value - pointerId
        resetInactivityTimer()
    }

    fun resetInactivityTimer() {
        inactivityJob?.cancel()
        inactivityJob = viewModelScope.launch {
            delay(INACTIVITY_TIMEOUT_MS)
            if (_completedStrokes.value.isNotEmpty() || _googlyEyes.value.isNotEmpty() ||
                _activeStrokes.value.isNotEmpty() || _magicCompanions.value.isNotEmpty()) {
                triggerWipeAndClear()
            }
        }
    }

    fun triggerWipeAndClear() {
        stopAnimation()
        wipeAnimationJob?.cancel()
        wipeAnimationJob = viewModelScope.launch {
            _isWiping.value = true
            val totalSteps = (WIPE_DURATION_MS / 16L).toInt().coerceAtLeast(1)

            for (step in 1..totalSteps) {
                delay(16L)
                _wipeProgress.value = (step.toFloat() / totalSteps).coerceIn(0f, 1f)
            }

            _completedStrokes.value = emptyList()
            _activeStrokes.value = emptyMap()
            _googlyEyes.value = emptyList()
            _magicCompanions.value = emptyList()
            _wipeProgress.value = 0f
            _isWiping.value = false
        }
    }

    private fun cancelActiveWipe() {
        if (_isWiping.value) {
            wipeAnimationJob?.cancel()
            _isWiping.value = false
            _wipeProgress.value = 0f
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAnimation()
        inactivityJob?.cancel()
        wipeAnimationJob?.cancel()
        soundManager?.release()
        soundManager = null
        textToSpeechManager?.release()
        textToSpeechManager = null
        naturalAudioPlayer?.release()
        naturalAudioPlayer = null
    }
}
