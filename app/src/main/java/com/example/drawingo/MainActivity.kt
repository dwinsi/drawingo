package com.example.drawingo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.theme.DrawingoTheme
import com.example.drawingo.ui.DrawingCanvas
import com.example.drawingo.ui.DrawingViewModel

/** Main activity hosting the drawing and animation canvas. */
class MainActivity : ComponentActivity() {

    private val viewModel: DrawingViewModel by viewModels()
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.removeLegacyClientApiKey(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        soundManager = SoundManager(this)
        viewModel.soundManager = soundManager

        enableEdgeToEdge()
        setContent {
            DrawingoTheme {
                DrawingCanvas(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
