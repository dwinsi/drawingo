package com.example.drawingo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.data.DrawingDatabase
import com.example.drawingo.theme.DrawingoTheme
import com.example.drawingo.theme.DrawingoThemeMode
import com.example.drawingo.ui.DrawingCanvas
import com.example.drawingo.ui.DrawingViewModel
import com.example.drawingo.ui.GalleryScreen

/** Main activity hosting the drawing and animation canvas. */
class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var drawingViewModel: DrawingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val database = DrawingDatabase.getDatabase(this)
        
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DrawingViewModel(database.drawingDao()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        val viewModel: DrawingViewModel by viewModels { factory }
        drawingViewModel = viewModel
        viewModel.setApplicationContext(applicationContext)

        soundManager = SoundManager(this)
        viewModel.soundManager = soundManager

        enableEdgeToEdge()
        setContent {
            val preferences = remember {
                getSharedPreferences("drawingo_preferences", MODE_PRIVATE)
            }
            var themeMode by remember {
                mutableStateOf(
                    preferences.getString("theme_mode", DrawingoThemeMode.SYSTEM.name)
                        ?.let { runCatching { DrawingoThemeMode.valueOf(it) }.getOrNull() }
                        ?: DrawingoThemeMode.SYSTEM
                )
            }

            DrawingoTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "canvas") {
                    composable("canvas") {
                        DrawingCanvas(
                            viewModel = viewModel,
                            onNavigateBack = {
                                viewModel.saveCurrentProject("Drawing ${System.currentTimeMillis() % 1000}", this@MainActivity)
                                navController.navigate("gallery")
                            },
                            modifier = Modifier.fillMaxSize(),
                            themeMode = themeMode,
                            onThemeModeChange = { mode ->
                                themeMode = mode
                                preferences.edit().putString("theme_mode", mode.name).apply()
                            }
                        )
                    }
                    composable("gallery") {
                        GalleryScreen(
                            viewModel = viewModel,
                            onNavigateToCanvas = {
                                navController.popBackStack("canvas", inclusive = false)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        if (::drawingViewModel.isInitialized) {
            drawingViewModel.saveAutomaticallyNow()
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
