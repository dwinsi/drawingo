package com.example.drawingo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.drawingo.audio.NaturalAudioPlayer
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.audio.TextToSpeechManager
import com.example.drawingo.device.KioskManager
import com.example.drawingo.theme.DrawingoTheme
import com.example.drawingo.ui.DrawingCanvas
import com.example.drawingo.ui.DrawingViewModel
import kotlin.math.hypot

/**
 * Main Activity for Drawingo hosting the interactive kid canvas,
 * optional Kiosk Mode enforcement, and 4-finger 3-second long-press exit mechanism.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()
    private lateinit var soundManager: SoundManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var naturalAudioPlayer: NaturalAudioPlayer

    // Hidden Exit Mechanism State
    private val exitHoldProgress = mutableFloatStateOf(0f)
    private val exitTouchCentroid = mutableStateOf<Offset?>(null)

    private var fourFingerStartTime: Long = 0L
    private var isTrackingFourFingers: Boolean = false
    private val initialFingerPositions = FloatArray(8)
    private val exitCheckHandler = Handler(Looper.getMainLooper())

    private val exitHoldDurationMs = 3000L
    private val maxSlopPixels = 80f

    private val exitTicker = object : Runnable {
        override fun run() {
            if (!isTrackingFourFingers) return

            val elapsed = SystemClock.uptimeMillis() - fourFingerStartTime
            val progress = (elapsed.toFloat() / exitHoldDurationMs).coerceIn(0f, 1f)
            exitHoldProgress.floatValue = progress

            if (elapsed >= exitHoldDurationMs) {
                Log.i(TAG, "4-finger 3-second hold detected. Exiting Kiosk Mode.")
                performKioskExit()
            } else {
                exitCheckHandler.postDelayed(this, 16L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        soundManager = SoundManager(this)
        textToSpeechManager = TextToSpeechManager(this)
        naturalAudioPlayer = NaturalAudioPlayer(this)

        viewModel.soundManager = soundManager
        viewModel.textToSpeechManager = textToSpeechManager
        viewModel.naturalAudioPlayer = naturalAudioPlayer

        enableEdgeToEdge()
        if (KioskManager.isKioskModeEnabled(this)) {
            hideSystemUI()
        }

        setContent {
            DrawingoTheme {
                DrawingCanvas(
                    viewModel = viewModel,
                    exitHoldProgress = exitHoldProgress.floatValue,
                    exitTouchCentroid = exitTouchCentroid.value,
                    onKioskToggled = { enabled ->
                        KioskManager.setKioskModeEnabled(this, enabled)
                        if (enabled) {
                            hideSystemUI()
                            KioskManager.applyKioskState(this)
                        } else {
                            showSystemUI()
                            KioskManager.applyKioskState(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (KioskManager.isKioskModeEnabled(this)) {
            hideSystemUI()
            KioskManager.applyKioskState(this)
        } else {
            showSystemUI()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && KioskManager.isKioskModeEnabled(this)) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (KioskManager.isKioskModeEnabled(this)) {
            handleRawMultiTouch(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun handleRawMultiTouch(ev: MotionEvent) {
        val action = ev.actionMasked
        val pointerCount = ev.pointerCount

        if (pointerCount == 4 && (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_POINTER_DOWN)) {
            val x0 = ev.getX(0)
            val y0 = ev.getY(0)
            val x1 = ev.getX(1)
            val y1 = ev.getY(1)
            val x2 = ev.getX(2)
            val y2 = ev.getY(2)
            val x3 = ev.getX(3)
            val y3 = ev.getY(3)

            if (!isTrackingFourFingers) {
                isTrackingFourFingers = true
                fourFingerStartTime = SystemClock.uptimeMillis()
                initialFingerPositions[0] = x0
                initialFingerPositions[1] = y0
                initialFingerPositions[2] = x1
                initialFingerPositions[3] = y1
                initialFingerPositions[4] = x2
                initialFingerPositions[5] = y2
                initialFingerPositions[6] = x3
                initialFingerPositions[7] = y3

                val centroid = Offset((x0 + x1 + x2 + x3) / 4f, (y0 + y1 + y2 + y3) / 4f)
                exitTouchCentroid.value = centroid
                exitHoldProgress.floatValue = 0f

                exitCheckHandler.removeCallbacks(exitTicker)
                exitCheckHandler.post(exitTicker)
            } else {
                val dist0 = hypot(x0 - initialFingerPositions[0], y0 - initialFingerPositions[1])
                val dist1 = hypot(x1 - initialFingerPositions[2], y1 - initialFingerPositions[3])
                val dist2 = hypot(x2 - initialFingerPositions[4], y2 - initialFingerPositions[5])
                val dist3 = hypot(x3 - initialFingerPositions[6], y3 - initialFingerPositions[7])

                if (dist0 > maxSlopPixels || dist1 > maxSlopPixels || dist2 > maxSlopPixels || dist3 > maxSlopPixels) {
                    cancelFourFingerTracking()
                } else {
                    val centroid = Offset((x0 + x1 + x2 + x3) / 4f, (y0 + y1 + y2 + y3) / 4f)
                    exitTouchCentroid.value = centroid
                }
            }
        } else {
            if (isTrackingFourFingers) {
                cancelFourFingerTracking()
            }
        }
    }

    private fun cancelFourFingerTracking() {
        isTrackingFourFingers = false
        exitCheckHandler.removeCallbacks(exitTicker)
        exitHoldProgress.floatValue = 0f
        exitTouchCentroid.value = null
    }

    private fun performKioskExit() {
        cancelFourFingerTracking()
        KioskManager.setKioskModeEnabled(this, false)
        showSystemUI()
        KioskManager.applyKioskState(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelFourFingerTracking()
        soundManager.release()
        textToSpeechManager.release()
        naturalAudioPlayer.release()
    }

    companion object {
        private const val TAG = "DrawingoMainActivity"
    }
}
