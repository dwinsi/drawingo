package com.example.drawingo.util

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.model.DrawnStroke
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-fidelity Spray Paint / Airbrush renderer that generates realistic particle scattering along stroke paths.
 */
object SprayRenderer {

    fun draw(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        val points = stroke.points
        if (points.isEmpty()) return

        val baseRadius = (stroke.strokeWidth * scale * 0.85f).coerceAtLeast(6f)
        val argbColor = stroke.color.toArgb()
        val baseAlpha = (stroke.alpha * 255).toInt().coerceIn(10, 255)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = argbColor
            style = Paint.Style.FILL
        }

        for (i in points.indices) {
            val curr = points[i]
            val currX = curr.x * scale + offsetX
            val currY = curr.y * scale + offsetY
            val pressure = stroke.pressures.getOrNull(i) ?: 1.0f

            val stepRadius = baseRadius * (0.6f + pressure * 0.5f)
            val density = (stepRadius * 1.8f * pressure).toInt().coerceIn(12, 120)

            // Seed deterministic random generator per point
            val random = Random(stroke.id xor (i.toLong() * 1000003L))

            repeat(density) {
                val distanceRatio = sqrt(random.nextFloat()) * stepRadius
                val angle = random.nextFloat() * 2f * Math.PI.toFloat()

                val px = currX + distanceRatio * cos(angle)
                val py = currY + distanceRatio * sin(angle)

                val dotRadius = (random.nextFloat() * 2.2f + 0.8f) * (1f - (distanceRatio / stepRadius) * 0.4f) * scale
                val edgeFade = (1f - (distanceRatio / stepRadius) * 0.5f).coerceIn(0.2f, 1f)
                paint.alpha = (baseAlpha * edgeFade * (0.35f + random.nextFloat() * 0.4f)).toInt().coerceIn(5, 255)

                canvas.drawCircle(px, py, dotRadius, paint)
            }
        }
    }
}
