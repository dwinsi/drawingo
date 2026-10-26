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
 * Renders Compose DrawnStrokes onto an in-memory Android Bitmap for local exports and previews.
 */
object CanvasBitmapUtils {

    fun createBitmapFromStrokes(
        strokes: List<DrawnStroke>,
        screenWidth: Int,
        screenHeight: Int,
        backgroundImagePath: String? = null
    ): Bitmap {
        // We render the export exactly at the device screen dimensions so 1:1 mapping works.
        // We can scale it down at the very end if we want, but doing it 1:1 ensures coordinates perfectly match.
        val width = screenWidth
        val height = screenHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background paper color
        val bgColor = 0xFFFFFFFF.toInt()
        canvas.drawColor(bgColor)
        
        // Draw background image if present
        if (backgroundImagePath != null) {
            val bgBitmap = android.graphics.BitmapFactory.decodeFile(backgroundImagePath)
            if (bgBitmap != null) {
                // Exact same scaling logic as DrawingCanvas to ensure perfect visual match
                val scale = minOf(width.toFloat() / bgBitmap.width, height.toFloat() / bgBitmap.height)
                val drawWidth = bgBitmap.width * scale
                val drawHeight = bgBitmap.height * scale
                val left = (width - drawWidth) / 2f
                val top = (height - drawHeight) / 2f
                
                val srcRect = android.graphics.Rect(0, 0, bgBitmap.width, bgBitmap.height)
                val dstRect = android.graphics.Rect(left.toInt(), top.toInt(), (left + drawWidth).toInt(), (top + drawHeight).toInt())
                val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
                bgBitmap.recycle()
            }
        }

        if (strokes.isEmpty()) {
            return bitmap
        }

        // We DO NOT auto-crop if we want strokes and background to match 1:1. 
        // We just draw the strokes exactly where they are on the screen.
        val scale = 1f
        val offsetX = 0f
        val offsetY = 0f

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (stroke in strokes.filter { it.tool == DrawingTool.FILL } + strokes.filter { it.tool != DrawingTool.FILL }) {
            if (stroke.points.size < 2) continue

            val color = stroke.color.toArgb()
            val baseWidth = (stroke.strokeWidth * scale).coerceIn(4f, 200f)
            paint.color = color
            paint.alpha = (stroke.alpha * 255).toInt().coerceIn(0, 255)

            if (stroke.tool == DrawingTool.ERASER) {
                paint.color = bgColor
            }

            val tool = stroke.tool

            if (tool == DrawingTool.FILL) {
                val fillPath = Path().apply {
                    moveTo(stroke.points.first().x, stroke.points.first().y)
                    stroke.points.drop(1).forEach { lineTo(it.x, it.y) }
                    close()
                }
                paint.style = Paint.Style.FILL
                canvas.drawPath(fillPath, paint)
                paint.style = Paint.Style.STROKE
                continue
            }

            // Variable width logic for stylus pressure
            if ((tool == DrawingTool.PEN || tool == DrawingTool.BRUSH) && stroke.pressures.size == stroke.points.size) {
                var previousX = stroke.points[0].x * scale + offsetX
                var previousY = stroke.points[0].y * scale + offsetY
                var previousPressure = stroke.pressures[0]
                
                var lastDrawX = previousX
                var lastDrawY = previousY
                
                for (i in 1 until stroke.points.size) {
                    val currentX = stroke.points[i].x * scale + offsetX
                    val currentY = stroke.points[i].y * scale + offsetY
                    val currentPressure = stroke.pressures[i]
                    
                    val midX = (previousX + currentX) / 2f
                    val midY = (previousY + currentY) / 2f
                    
                    val avgWidth = baseWidth * (0.2f + ((previousPressure + currentPressure) / 2f) * 1.5f)
                    paint.strokeWidth = avgWidth
                    
                    val segmentPath = Path()
                    segmentPath.moveTo(lastDrawX, lastDrawY)
                    segmentPath.quadTo(previousX, previousY, midX, midY)
                    canvas.drawPath(segmentPath, paint)
                    
                    lastDrawX = midX
                    lastDrawY = midY
                    previousX = currentX
                    previousY = currentY
                    previousPressure = currentPressure
                }
                
                val finalPath = Path()
                finalPath.moveTo(lastDrawX, lastDrawY)
                finalPath.lineTo(previousX, previousY)
                paint.strokeWidth = baseWidth * (0.2f + previousPressure * 1.5f)
                canvas.drawPath(finalPath, paint)
                continue
            }

            paint.strokeWidth = baseWidth

            if (stroke.tool == DrawingTool.TEXT) {
                val text = stroke.text
                if (text != null && stroke.points.isNotEmpty()) {
                    val p0 = stroke.points[0]
                    val cx = p0.x * scale + offsetX
                    val cy = p0.y * scale + offsetY

                    var rotationAngle = 0f
                    var scaleFactor = 1f

                    if (stroke.points.size >= 2) {
                        val p1 = stroke.points[1]
                        val dx = (p1.x - p0.x) * scale
                        val dy = (p1.y - p0.y) * scale
                        val currentRadius = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        val defaultRadius = (stroke.strokeWidth * scale * text.length * 0.32f).coerceAtLeast(24f * scale)
                        if (defaultRadius > 0f && currentRadius > 0f) {
                            scaleFactor = currentRadius / defaultRadius
                            rotationAngle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        }
                    }

                    val effectiveTextSize = stroke.strokeWidth * scale * scaleFactor
                    paint.style = Paint.Style.FILL
                    paint.textSize = effectiveTextSize
                    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
                    paint.textAlign = Paint.Align.CENTER
                    paint.color = stroke.color.toArgb()
                    paint.alpha = (stroke.alpha * 255).toInt().coerceIn(0, 255)

                    canvas.save()
                    canvas.translate(cx, cy)
                    if (rotationAngle != 0f) {
                        canvas.rotate(rotationAngle)
                    }
                    canvas.drawText(text, 0f, (effectiveTextSize / 3f), paint)
                    canvas.restore()
                }
                continue
            }

            val path = Path()
            val first = stroke.points[0]
            val firstX = first.x * scale + offsetX
            val firstY = first.y * scale + offsetY
            path.moveTo(firstX, firstY)

            var previousX = firstX
            var previousY = firstY

            for (i in 1 until stroke.points.size) {
                val pt = stroke.points[i]
                val px = pt.x * scale + offsetX
                val py = pt.y * scale + offsetY
                
                val midX = (previousX + px) / 2f
                val midY = (previousY + py) / 2f
                path.quadTo(previousX, previousY, midX, midY)
                
                previousX = px
                previousY = py
            }
            path.lineTo(previousX, previousY)

            when (stroke.tool) {
                DrawingTool.WATERCOLOR -> {
                    WatercolorRenderer.draw(canvas, stroke, scale, offsetX, offsetY)
                }
                DrawingTool.CRAYON -> {
                    CrayonRenderer.drawCrayonStroke(canvas, stroke, scale, offsetX, offsetY)
                }
                DrawingTool.SPRAY -> {
                    SprayRenderer.draw(canvas, stroke, scale, offsetX, offsetY)
                }
                else -> canvas.drawPath(path, paint)
            }
        }

        return bitmap
    }
}
