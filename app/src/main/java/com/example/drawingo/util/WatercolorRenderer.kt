package com.example.drawingo.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.model.DrawnStroke

/** Shared watercolor rendering for the live canvas, tool preview, and exported bitmap. */
object WatercolorRenderer {
    fun draw(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        val points = stroke.points
        if (points.isEmpty()) return

        val baseWidth = stroke.strokeWidth * scale
        val color = stroke.color.toArgb()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        if (points.size == 1) {
            val point = points.first()
            val centerX = point.x * scale + offsetX
            val centerY = point.y * scale + offsetY
            paint.style = Paint.Style.FILL
            paint.alpha = (stroke.alpha * 0.22f * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(centerX, centerY, baseWidth * 0.8f, paint)
        } else {
            val path = Path()
            var previousX = points.first().x * scale + offsetX
            var previousY = points.first().y * scale + offsetY
            path.moveTo(previousX, previousY)
            for (index in 1 until points.size) {
                val point = points[index]
                val x = point.x * scale + offsetX
                val y = point.y * scale + offsetY
                val midpointX = (previousX + x) * 0.5f
                val midpointY = (previousY + y) * 0.5f
                path.quadTo(previousX, previousY, midpointX, midpointY)
                previousX = x
                previousY = y
            }
            path.lineTo(previousX, previousY)

            paint.alpha = (stroke.alpha * 0.28f * 255).toInt().coerceIn(0, 255)
            paint.strokeWidth = baseWidth * 1.9f
            canvas.drawPath(path, paint)
            paint.alpha = (stroke.alpha * 0.24f * 255).toInt().coerceIn(0, 255)
            paint.strokeWidth = baseWidth * 0.95f
            canvas.drawPath(path, paint)
        }

        drawPigmentBlooms(canvas, stroke, scale, offsetX, offsetY, baseWidth, color)
    }

    private fun drawPigmentBlooms(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        baseWidth: Float,
        color: Int
    ) {
        val stride = (stroke.points.size / 36).coerceAtLeast(1)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        stroke.points.indices.step(stride).forEach { index ->
            val point = stroke.points[index]
            val seed = stroke.id xor (index.toLong() * 31L)
            val centerX = point.x * scale + offsetX + signedNoise(seed ushr 8) * baseWidth * 0.22f
            val centerY = point.y * scale + offsetY + signedNoise(seed ushr 20) * baseWidth * 0.22f
            val radius = (baseWidth * (0.2f + ((seed and 7L).toFloat() / 7f) * 0.34f)).coerceAtLeast(1f)
            val alpha = (stroke.alpha * (0.18f + ((seed ushr 12 and 15L).toFloat() / 15f) * 0.20f) * 255)
                .toInt().coerceIn(0, 255)
            paint.shader = RadialGradient(
                centerX,
                centerY,
                radius,
                color.withAlpha(alpha),
                color.withAlpha(0),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(centerX, centerY, radius, paint)

            if ((seed and 3L) == 0L) {
                val dropX = centerX + signedNoise(seed ushr 28) * baseWidth * 0.55f
                val dropY = centerY + signedNoise(seed ushr 36) * baseWidth * 0.55f
                val dropRadius = (baseWidth * (0.035f + ((seed ushr 4 and 7L).toFloat() / 7f) * 0.07f))
                    .coerceAtLeast(0.5f)
                paint.shader = RadialGradient(
                    dropX,
                    dropY,
                    dropRadius,
                    color.withAlpha((stroke.alpha * 0.24f * 255).toInt().coerceIn(0, 255)),
                    color.withAlpha(0),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(dropX, dropY, dropRadius, paint)
            }
        }
    }

    private fun signedNoise(value: Long): Float = (((value and 15L).toFloat() / 15f) - 0.5f) * 2f

    private fun Int.withAlpha(alpha: Int): Int = (this and 0x00FFFFFF) or (alpha shl 24)
}
