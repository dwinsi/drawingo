package com.example.drawingo.animation

import androidx.compose.ui.geometry.Offset
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset
import kotlin.math.sin

object QuickMagicRenderer {
    
    fun processMagicStroke(stroke: DrawnStroke, preset: QuickMagicPreset, timeMs: Long): DrawnStroke? {
        if (stroke.points.size < 2) return stroke

        return when (preset) {
            QuickMagicPreset.NONE -> stroke
            QuickMagicPreset.WIGGLE -> wiggleStroke(stroke, timeMs)
            QuickMagicPreset.PULSE -> pulseStroke(stroke, timeMs)
            QuickMagicPreset.DRAW_ON -> drawOnStroke(stroke, timeMs)
        }
    }

    private fun wiggleStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        val frequency = 5f
        val amplitude = stroke.strokeWidth * 0.5f

        val wiggledPoints = stroke.points.map { point ->
            val wiggle = sin((point.x + point.y) * 0.01f + timeSec * frequency) * amplitude
            Offset(point.x + wiggle, point.y + wiggle)
        }
        return stroke.copy(points = wiggledPoints)
    }

    private fun pulseStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        // Pulse alpha between 0.3 and 1.0 based on time and stroke ID to stagger the pulse
        val pulse = (sin(timeSec * 3f + (stroke.id * 0.5f)) + 1f) / 2f 
        val pulsedAlpha = stroke.alpha * (0.3f + pulse * 0.7f)
        val pulsedWidth = stroke.strokeWidth * (0.8f + pulse * 0.4f)

        return stroke.copy(alpha = pulsedAlpha, strokeWidth = pulsedWidth)
    }

    private fun drawOnStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke? {
        // Draw-On animation completes in ~2 seconds, then resets
        val durationMs = 2000f
        val progress = (timeMs % durationMs) / durationMs
        
        // Stagger start times slightly based on stroke order
        val staggerOffset = (stroke.id % 10) * 0.05f
        val localProgress = ((progress - staggerOffset) * 1.5f).coerceIn(0f, 1f)
        
        val pointsToDraw = (stroke.points.size * localProgress).toInt()
        
        if (pointsToDraw < 2) return null

        val newPoints = stroke.points.take(pointsToDraw)
        val newPressures = if (stroke.pressures.size == stroke.points.size) {
            stroke.pressures.take(pointsToDraw)
        } else {
            stroke.pressures
        }

        return stroke.copy(points = newPoints, pressures = newPressures)
    }
}
