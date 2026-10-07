package com.example.drawingo.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawnStroke

/**
 * Renders Compose DrawnStrokes onto an in-memory Android Bitmap
 * to send to Google Gemini AI.
 */
object CanvasBitmapUtils {

    fun createBitmapFromStrokes(
        strokes: List<DrawnStroke>,
        width: Int = 800,
        height: Int = 800
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background paper color
        val bgColor = 0xFFFFFFFF.toInt()
        canvas.drawColor(bgColor)

        if (strokes.isEmpty()) {
            return bitmap
        }

        // Determine bounds of strokes to scale bitmap efficiently
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (stroke in strokes) {
            for (p in stroke.points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
        }

        val strokeWidthRange = (maxX - minX).coerceAtLeast(100f)
        val strokeHeightRange = (maxY - minY).coerceAtLeast(100f)

        val padding = 40f
        val scaleX = (width - padding * 2) / strokeWidthRange
        val scaleY = (height - padding * 2) / strokeHeightRange
        val scale = minOf(scaleX, scaleY).coerceIn(0.2f, 3.0f)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (stroke in strokes) {
            if (stroke.points.size < 2) continue

            val color = stroke.color.toArgb()
            val baseWidth = (stroke.strokeWidth * scale).coerceIn(4f, 80f)
            paint.color = color
            paint.strokeWidth = baseWidth
            paint.alpha = (stroke.alpha * 255).toInt().coerceIn(0, 255)

            if (stroke.tool == DrawingTool.ERASER) {
                paint.color = bgColor
            }

            val path = Path()
            val first = stroke.points[0]
            val firstX = (first.x - minX) * scale + padding
            val firstY = (first.y - minY) * scale + padding
            path.moveTo(firstX, firstY)

            for (i in 1 until stroke.points.size) {
                val pt = stroke.points[i]
                val px = (pt.x - minX) * scale + padding
                val py = (pt.y - minY) * scale + padding
                path.lineTo(px, py)
            }

            when (stroke.tool) {
                DrawingTool.WATERCOLOR -> {
                    paint.alpha = (stroke.alpha * 0.24f * 255).toInt().coerceIn(0, 255)
                    paint.strokeWidth = baseWidth * 1.7f
                    canvas.drawPath(path, paint)
                    paint.alpha = (stroke.alpha * 0.32f * 255).toInt().coerceIn(0, 255)
                    paint.strokeWidth = baseWidth * 1.12f
                    canvas.drawPath(path, paint)
                    drawWatercolorBlooms(canvas, stroke, minX, minY, scale, padding, color)
                }
                DrawingTool.CRAYON -> {
                    paint.alpha = (stroke.alpha * 0.76f * 255).toInt().coerceIn(0, 255)
                    paint.strokeWidth = baseWidth
                    canvas.drawPath(path, paint)
                    drawCrayonTexture(canvas, stroke, minX, minY, scale, padding, color)
                }
                else -> canvas.drawPath(path, paint)
            }
        }

        return bitmap
    }

    private fun drawWatercolorBlooms(
        canvas: Canvas, stroke: DrawnStroke, minX: Float, minY: Float,
        scale: Float, padding: Float, color: Int
    ) {
        val stride = (stroke.points.size / 32).coerceAtLeast(1)
        val bloomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        stroke.points.indices.step(stride).forEach { index ->
            val point = stroke.points[index]
            val seed = stroke.id xor (index.toLong() * 31L)
            val offset = (((seed ushr 8) and 15).toFloat() - 7.5f) * stroke.strokeWidth * 0.09f * scale
            val radius = (stroke.strokeWidth * scale * (0.22f + ((seed and 7).toFloat() * 0.025f))).coerceAtLeast(1f)
            val centerX = (point.x - minX) * scale + padding + offset
            val centerY = (point.y - minY) * scale + padding - offset
            val pigment = (stroke.alpha * 0.24f * 255).toInt().coerceIn(0, 255)
            bloomPaint.shader = RadialGradient(
                centerX, centerY, radius,
                color.withAlpha(pigment), color.withAlpha(0), Shader.TileMode.CLAMP
            )
            canvas.drawCircle(centerX, centerY, radius, bloomPaint)
        }
    }

    private fun drawCrayonTexture(
        canvas: Canvas, stroke: DrawnStroke, minX: Float, minY: Float,
        scale: Float, padding: Float, color: Int
    ) {
        val stride = (stroke.points.size / 72).coerceAtLeast(1)
        val fleckPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        stroke.points.indices.step(stride).forEach { index ->
            val point = stroke.points[index]
            val seed = stroke.id xor (index.toLong() * 0x45D9F3BL)
            val side = if ((seed and 1L) == 0L) -1f else 1f
            val across = side * stroke.strokeWidth * scale * (0.2f + ((seed ushr 4 and 7).toFloat() * 0.045f))
            val radius = (stroke.strokeWidth * scale * 0.055f).coerceAtLeast(0.8f)
            val x = (point.x - minX) * scale + padding
            val y = (point.y - minY) * scale + padding
            fleckPaint.color = 0xA8FFFFFF.toInt()
            canvas.drawCircle(x + across, y + side * radius, radius, fleckPaint)
            if (index % 3 == 0) {
                fleckPaint.color = color.withAlpha((stroke.alpha * 0.82f * 255).toInt().coerceIn(0, 255))
                canvas.drawCircle(x - across * 0.55f, y - side * radius, radius * 0.85f, fleckPaint)
            }
        }
    }

    private fun Int.withAlpha(alpha: Int): Int = (this and 0x00FFFFFF) or (alpha shl 24)
}
