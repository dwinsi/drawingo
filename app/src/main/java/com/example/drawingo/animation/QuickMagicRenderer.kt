package com.example.drawingo.animation

import androidx.compose.ui.geometry.Offset
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset
import kotlin.math.sin

object QuickMagicRenderer {
    
    fun processMagicStroke(stroke: DrawnStroke, preset: QuickMagicPreset, timeMs: Long, groupCenter: Offset? = null): DrawnStroke? {
        if (stroke.points.size < 2) return stroke

        return when (preset) {
            QuickMagicPreset.NONE -> stroke
            QuickMagicPreset.WIGGLE -> wiggleStroke(stroke, timeMs)
            QuickMagicPreset.PULSE -> pulseStroke(stroke, timeMs)
            QuickMagicPreset.FLOAT -> floatStroke(stroke, timeMs)
            QuickMagicPreset.SHIMMER -> shimmerStroke(stroke, timeMs)
            QuickMagicPreset.SPIN -> spinStroke(stroke, timeMs, groupCenter)
            QuickMagicPreset.WANDER -> wanderStroke(stroke, timeMs)
            QuickMagicPreset.FLIP -> flipStroke(stroke, timeMs, groupCenter)
        }
    }

    private fun spinStroke(stroke: DrawnStroke, timeMs: Long, groupCenter: Offset?): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke
        val timeSec = (timeMs % 100000L) / 1000f
        
        val cx: Float
        val cy: Float
        
        if (groupCenter != null) {
            cx = groupCenter.x
            cy = groupCenter.y
        } else {
            var minX = Float.POSITIVE_INFINITY
            var minY = Float.POSITIVE_INFINITY
            var maxX = Float.NEGATIVE_INFINITY
            var maxY = Float.NEGATIVE_INFINITY
            
            stroke.points.forEach { p ->
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
            cx = (minX + maxX) / 2f
            cy = (minY + maxY) / 2f
        }

        val angle = timeSec * 2f // radians per second
        val cosA = kotlin.math.cos(angle)
        val sinA = kotlin.math.sin(angle)

        val spunPoints = stroke.points.map { p ->
            val dx = p.x - cx
            val dy = p.y - cy
            val nx = dx * cosA - dy * sinA
            val ny = dx * sinA + dy * cosA
            Offset(nx + cx, ny + cy)
        }
        return stroke.copy(points = spunPoints)
    }

    private fun wanderStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        // Overlapping sine waves for a smooth, pseudo-random wander. Remove stroke.id staggering.
        val dx = sin(timeSec * 0.8f) * 30f + sin(timeSec * 0.3f) * 15f
        val dy = kotlin.math.cos(timeSec * 0.7f) * 30f + sin(timeSec * 0.4f) * 15f
        
        val wanderedPoints = stroke.points.map { p ->
            Offset(p.x + dx, p.y + dy)
        }
        return stroke.copy(points = wanderedPoints)
    }

    private fun flipStroke(stroke: DrawnStroke, timeMs: Long, groupCenter: Offset?): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke
        val timeSec = (timeMs % 100000L) / 1000f
        
        val cx: Float
        
        if (groupCenter != null) {
            cx = groupCenter.x
        } else {
            var minX = Float.POSITIVE_INFINITY
            var maxX = Float.NEGATIVE_INFINITY
            stroke.points.forEach { p ->
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
            }
            cx = (minX + maxX) / 2f
        }
        
        // Scale from 1 to -1 to create a 3D flip effect horizontally
        val scaleX = kotlin.math.cos(timeSec * 3f)
        
        val flippedPoints = stroke.points.map { p ->
            val dx = p.x - cx
            val nx = dx * scaleX
            Offset(nx + cx, p.y)
        }
        return stroke.copy(points = flippedPoints)
    }

    private fun floatStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        val frequency = 2f
        val amplitude = stroke.strokeWidth * 0.8f
        // Offset entire stroke up and down, remove stroke.id staggering
        val floatY = sin(timeSec * frequency) * amplitude
        
        val floatedPoints = stroke.points.map { point ->
            Offset(point.x, point.y + floatY)
        }
        return stroke.copy(points = floatedPoints)
    }
    
    private fun shimmerStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        val shimmerPoints = stroke.points.mapIndexed { index, point ->
            // Create a wave traveling along the stroke
            val shimmer = sin(index * 0.2f - timeSec * 10f)
            if (shimmer > 0.8f) { // Only distort peaks of the wave
                val spread = (shimmer - 0.8f) * 5f * stroke.strokeWidth
                Offset(point.x + (Math.random() - 0.5f).toFloat() * spread, 
                       point.y + (Math.random() - 0.5f).toFloat() * spread)
            } else {
                point
            }
        }
        return stroke.copy(points = shimmerPoints)
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
        // Pulse alpha between 0.3 and 1.0 based on time. Removed stroke.id staggering.
        val pulse = (sin(timeSec * 3f) + 1f) / 2f 
        val pulsedAlpha = stroke.alpha * (0.3f + pulse * 0.7f)
        val pulsedWidth = stroke.strokeWidth * (0.8f + pulse * 0.4f)

        return stroke.copy(alpha = pulsedAlpha, strokeWidth = pulsedWidth)
    }
}
