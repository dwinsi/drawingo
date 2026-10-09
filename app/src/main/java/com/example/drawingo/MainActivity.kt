package com.example.drawingo

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drawingo.audio.SoundManager
import com.example.drawingo.data.DrawingDatabase
import com.example.drawingo.theme.DrawingoTheme
import com.example.drawingo.ui.DrawingCanvas
import com.example.drawingo.ui.DrawingViewModel
import com.example.drawingo.ui.GalleryScreen

/** Main activity hosting the drawing and animation canvas. */
class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.removeLegacyClientApiKey(this)
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

        soundManager = SoundManager(this)
        viewModel.soundManager = soundManager

        enableEdgeToEdge()
        setContent {
            DrawingoTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "gallery") {
                    composable("gallery") {
                        GalleryScreen(
                            viewModel = viewModel,
                            onNavigateToCanvas = { navController.navigate("canvas") }
                        )
                    }
                    composable("canvas") {
                        DrawingCanvas(
                            viewModel = viewModel,
                            onNavigateBack = {
                                if (navController.currentBackStackEntry?.destination?.route == "canvas") {
                                    viewModel.saveCurrentProject("Drawing ${System.currentTimeMillis() % 1000}")
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
