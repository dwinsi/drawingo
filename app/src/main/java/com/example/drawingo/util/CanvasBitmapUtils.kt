package com.example.drawingo.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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

            paint.color = stroke.color.toArgb()
            paint.strokeWidth = (stroke.strokeWidth * scale).coerceIn(4f, 80f)
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

            canvas.drawPath(path, paint)
        }

        return bitmap
    }
}
