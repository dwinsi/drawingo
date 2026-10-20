package com.example.drawingo.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object QuickMagicRenderer {
    
    fun processMagicStroke(
        stroke: DrawnStroke,
        presets: Set<QuickMagicPreset>,
        timeMs: Long,
        groupCenter: Offset? = null,
        groupBounds: Rect? = null,
        canvasSize: Size? = null
    ): DrawnStroke? {
        if (stroke.points.size < 2 || presets.isEmpty()) return stroke

        var currentStroke = stroke

        // Apply point-level distortions first
        if (presets.contains(QuickMagicPreset.WIGGLE)) currentStroke = wiggleStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.WAVE)) currentStroke = waveStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.JITTER)) currentStroke = jitterStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.SHIMMER)) currentStroke = shimmerStroke(currentStroke, timeMs)

        // Apply scale/alpha effects
        if (presets.contains(QuickMagicPreset.PULSE)) currentStroke = pulseStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.HEARTBEAT)) currentStroke = heartbeatStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.COLOR_CYCLE)) currentStroke = colorCycleStroke(currentStroke, timeMs)

        // Apply rotations
        if (presets.contains(QuickMagicPreset.SPIN)) currentStroke = spinStroke(currentStroke, timeMs, groupCenter)
        if (presets.contains(QuickMagicPreset.FLIP)) currentStroke = flipStroke(currentStroke, timeMs, groupCenter)
        if (presets.contains(QuickMagicPreset.SWING)) currentStroke = swingStroke(currentStroke, timeMs, groupCenter, groupBounds)
        if (presets.contains(QuickMagicPreset.SPIRAL)) currentStroke = spiralStroke(currentStroke, timeMs, groupCenter)

        // Apply translations last
        if (presets.contains(QuickMagicPreset.FLOAT)) currentStroke = floatStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.BOUNCE)) currentStroke = bounceStroke(currentStroke, timeMs, groupBounds)
        if (presets.contains(QuickMagicPreset.WANDER)) currentStroke = wanderStroke(currentStroke, timeMs)
        if (presets.contains(QuickMagicPreset.TRANSLATE)) currentStroke = translateStroke(currentStroke, timeMs, groupBounds, canvasSize)
        if (presets.contains(QuickMagicPreset.RANDOM)) currentStroke = randomRoamStroke(currentStroke, timeMs, groupCenter, groupBounds, canvasSize)

        return currentStroke
    }

    private fun bounceStroke(stroke: DrawnStroke, timeMs: Long, groupBounds: Rect?): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke
        val timeSec = (timeMs % 100000L) / 1000f
        
        // Bounce frequency
        val freq = 2.5f
        // Absolute value of sine gives a bounce effect
        val bounceCurve = kotlin.math.abs(sin(timeSec * freq * PI.toFloat()))
        
        val bounceHeight = 80f
        val dy = -bounceCurve * bounceHeight // move up
        
        // Squash and stretch
        val squash = if (bounceCurve < 0.1f) {
            1f + (0.1f - bounceCurve) * 2f
        } else {
            1f
        }
        val stretch = 1f / squash
        
        val cy = groupBounds?.bottom ?: stroke.points.maxOf { it.y }

        val bouncedPoints = stroke.points.map { p ->
            val stretchedY = cy + (p.y - cy) * stretch
            Offset(p.x, stretchedY + dy)
        }
        return stroke.copy(points = bouncedPoints)
    }

    private fun waveStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke
        val timeSec = (timeMs % 100000L) / 1000f
        val wavePoints = stroke.points.mapIndexed { index, p ->
            val phase = index * 0.1f - timeSec * 5f
            val dy = sin(phase) * (stroke.strokeWidth * 0.8f)
            Offset(p.x, p.y + dy)
        }
        return stroke.copy(points = wavePoints)
    }

    private fun swingStroke(stroke: DrawnStroke, timeMs: Long, groupCenter: Offset?, groupBounds: Rect?): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke
        val timeSec = (timeMs % 100000L) / 1000f
        
        // Anchor point is the top of the artwork
        val anchorX = groupCenter?.x ?: stroke.points.map { it.x }.average().toFloat()
        val anchorY = groupBounds?.top ?: stroke.points.minOf { it.y }
        
        val angle = sin(timeSec * 2f) * 0.3f // Swing +/- ~17 degrees
        val cosA = cos(angle)
        val sinA = sin(angle)
        
        val swungPoints = stroke.points.map { p ->
            val dx = p.x - anchorX
            val dy = p.y - anchorY
            val nx = dx * cosA - dy * sinA
            val ny = dx * sinA + dy * cosA
            Offset(nx + anchorX, ny + anchorY)
        }
        return stroke.copy(points = swungPoints)
    }

    private fun jitterStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val seed = timeMs / 50L // change every 50ms
        val random = kotlin.random.Random(seed)
        val amp = stroke.strokeWidth * 0.3f
        
        val jitteredPoints = stroke.points.map { p ->
            Offset(
                p.x + (random.nextFloat() - 0.5f) * amp,
                p.y + (random.nextFloat() - 0.5f) * amp
            )
        }
        return stroke.copy(points = jitteredPoints)
    }

    private fun heartbeatStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        val beat = (timeSec * 1.5f) % 1f
        // Thump thump...
        val scale = when {
            beat < 0.1f -> 1f + (beat / 0.1f) * 0.15f
            beat < 0.2f -> 1.15f - ((beat - 0.1f) / 0.1f) * 0.15f
            beat < 0.3f -> 1f + ((beat - 0.2f) / 0.1f) * 0.15f
            beat < 0.4f -> 1.15f - ((beat - 0.3f) / 0.1f) * 0.15f
            else -> 1f
        }
        return stroke.copy(strokeWidth = stroke.strokeWidth * scale)
    }

    private fun spiralStroke(stroke: DrawnStroke, timeMs: Long, groupCenter: Offset?): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke
        val timeSec = (timeMs % 100000L) / 1000f
        
        val cx = groupCenter?.x ?: stroke.points.map { it.x }.average().toFloat()
        val cy = groupCenter?.y ?: stroke.points.map { it.y }.average().toFloat()
        
        val radiusExpand = sin(timeSec * 2f) * 30f
        val angleOffset = timeSec * 1.5f
        val cosA = cos(angleOffset)
        val sinA = sin(angleOffset)
        
        val spiralPoints = stroke.points.map { p ->
            var dx = p.x - cx
            var dy = p.y - cy
            val len = kotlin.math.sqrt(dx*dx + dy*dy)
            if (len > 0.1f) {
                dx += (dx / len) * radiusExpand
                dy += (dy / len) * radiusExpand
            }
            val nx = dx * cosA - dy * sinA
            val ny = dx * sinA + dy * cosA
            Offset(nx + cx, ny + cy)
        }
        return stroke.copy(points = spiralPoints)
    }

    private fun colorCycleStroke(stroke: DrawnStroke, timeMs: Long): DrawnStroke {
        val timeSec = (timeMs % 100000L) / 1000f
        val h = (timeSec * 50f) % 360f
        val color = androidx.compose.ui.graphics.Color.hsv(h, 0.8f, 1f)
        return stroke.copy(color = color)
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

    private fun translateStroke(
        stroke: DrawnStroke,
        timeMs: Long,
        groupBounds: Rect?,
        canvasSize: Size?
    ): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke

        val cycleMs = 6000L
        val t = (timeMs % cycleMs).toFloat() / cycleMs
        val phase = t * 4f
        val phaseIndex = phase.toInt().coerceIn(0, 3)
        val progress = phase - phaseIndex

        // Smooth bell-curve easing (zero velocity at start, peak, and end)
        val factor = (1f - cos(progress * 2f * PI.toFloat())) / 2f

        // Calculate maximum allowable distances so artwork stays comfortably on-screen
        val pad = 30f
        val (forwardDist, backwardDist, upDist, downDist) = if (canvasSize != null && groupBounds != null && canvasSize.width > 0 && canvasSize.height > 0) {
            val maxForward = (canvasSize.width - groupBounds.right - pad).coerceAtLeast(0f)
            val maxBackward = (groupBounds.left - pad).coerceAtLeast(0f)
            val maxUp = (groupBounds.top - pad).coerceAtLeast(0f)
            val maxDown = (canvasSize.height - groupBounds.bottom - pad).coerceAtLeast(0f)

            listOf(
                if (maxForward > 60f) kotlin.math.min(maxForward, 200f) else kotlin.math.max(maxForward, 60f),
                if (maxBackward > 60f) kotlin.math.min(maxBackward, 200f) else kotlin.math.max(maxBackward, 60f),
                if (maxUp > 60f) kotlin.math.min(maxUp, 180f) else kotlin.math.max(maxUp, 60f),
                if (maxDown > 60f) kotlin.math.min(maxDown, 180f) else kotlin.math.max(maxDown, 60f)
            )
        } else {
            listOf(180f, 180f, 160f, 160f)
        }

        val (dx, dy) = when (phaseIndex) {
            0 -> Pair(factor * forwardDist, 0f)         // Forward (right: +X)
            1 -> Pair(-factor * backwardDist, 0f)       // Backward (left: -X)
            2 -> Pair(0f, -factor * upDist)             // Up (-Y)
            3 -> Pair(0f, factor * downDist)            // Down (+Y)
            else -> Pair(0f, 0f)
        }

        val translatedPoints = stroke.points.map { p ->
            Offset(p.x + dx, p.y + dy)
        }
        return stroke.copy(points = translatedPoints)
    }

    private fun randomRoamStroke(
        stroke: DrawnStroke,
        timeMs: Long,
        groupCenter: Offset?,
        groupBounds: Rect?,
        canvasSize: Size?
    ): DrawnStroke {
        if (stroke.points.isEmpty()) return stroke

        val (cx, cy) = if (groupCenter != null) {
            Pair(groupCenter.x, groupCenter.y)
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
            Pair((minX + maxX) / 2f, (minY + maxY) / 2f)
        }

        val bounds = groupBounds ?: run {
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
            Rect(minX, minY, maxX, maxY)
        }

        val canvasW = if (canvasSize != null && canvasSize.width > 100f) canvasSize.width else 1080f
        val canvasH = if (canvasSize != null && canvasSize.height > 100f) canvasSize.height else 1920f

        val pad = 40f
        val halfW = (bounds.width / 2f).coerceAtLeast(20f)
        val halfH = (bounds.height / 2f).coerceAtLeast(20f)

        val minCenterX = halfW + pad
        val maxCenterX = (canvasW - halfW - pad).coerceAtLeast(minCenterX)
        val minCenterY = halfH + pad
        val maxCenterY = (canvasH - halfH - pad).coerceAtLeast(minCenterY)

        val midX = (minCenterX + maxCenterX) / 2f
        val midY = (minCenterY + maxCenterY) / 2f
        val spanX = (maxCenterX - minCenterX) / 2f
        val spanY = (maxCenterY - minCenterY) / 2f

        // Multi-frequency incommensurate sinusoids generate smooth, non-repeating organic wandering
        val timeSec = (timeMs % 1000000L) / 1000f
        val normX = 0.52f * sin(timeSec * 0.47f + 1.2f) +
                    0.33f * sin(timeSec * 0.79f + 3.4f) +
                    0.15f * sin(timeSec * 1.23f + 0.7f)
        val normY = 0.52f * sin(timeSec * 0.41f + 2.6f) +
                    0.33f * sin(timeSec * 0.67f + 0.9f) +
                    0.15f * sin(timeSec * 1.09f + 4.1f)

        val effSpanX = if (spanX > 60f) spanX else canvasW * 0.15f
        val effSpanY = if (spanY > 60f) spanY else canvasH * 0.15f

        val targetCenterX = (if (spanX > 60f) midX else cx) + normX * effSpanX
        val targetCenterY = (if (spanY > 60f) midY else cy) + normY * effSpanY

        val dx = targetCenterX - cx
        val dy = targetCenterY - cy

        val roamedPoints = stroke.points.map { p ->
            Offset(p.x + dx, p.y + dy)
        }
        return stroke.copy(points = roamedPoints)
    }
}
