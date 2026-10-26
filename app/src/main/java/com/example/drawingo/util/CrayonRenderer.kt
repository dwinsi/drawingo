package com.example.drawingo.util

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.model.DrawnStroke
import java.util.Random
import kotlin.math.hypot

/**
 * High-fidelity Crayon renderer implementing physical wax and paper-tooth simulation:
 * 1. Seamless tileable grayscale noise texture acting as paper grain.
 * 2. Canvas-anchored BitmapShader so overlapping strokes hit identical paper pits and peaks.
 * 3. Offscreen stroke layer (saveLayer) to avoid self-intersection darkening and allow opaque wax rendering.
 * 4. PorterDuff.Mode.DST_IN mask with ColorMatrix (a' = k * a + b) scaling coverage by pressure and velocity.
 * 5. 4 parallel sub-strokes with normal-offset jitter for striated waxy edges.
 * 6. Subtle HSV color micro-variations across strands.
 */
object CrayonRenderer {

    private const val NOISE_SIZE = 128
    private const val NUM_STRANDS = 4

    // Pre-generated tileable paper grain noise bitmap
    private val grainBitmap: Bitmap by lazy {
        generateTileableNoiseBitmap(NOISE_SIZE)
    }

    private val grainShader: BitmapShader by lazy {
        BitmapShader(grainBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private val dstInXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    private val shaderMatrix = Matrix()

    /**
     * Renders a crayon stroke onto the given canvas.
     *
     * @param canvas Target Android graphics canvas (native canvas).
     * @param stroke The drawn stroke data.
     * @param scale Coordinate scale factor (default 1f).
     * @param offsetX Coordinate X offset (default 0f).
     * @param offsetY Coordinate Y offset (default 0f).
     */
    private class CrayonCache(
        val layerBounds: RectF,
        val strandPaths: List<Path>,
        val strandPaints: List<Paint>,
        val maskPaint: Paint,
        val dabPaint: Paint?,
        val dabCenter: Pair<Float, Float>?,
        val dabRadius: Float,
        val pointCount: Int,
        val strokeWidth: Float,
        val color: Int,
        val scale: Float,
        val offsetX: Float,
        val offsetY: Float
    )

    fun drawCrayonStroke(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        alphaMultiplier: Float = 1f
    ) {
        val points = stroke.points
        if (points.isEmpty()) return

        val baseWidth = stroke.strokeWidth * scale
        val baseArgb = stroke.color.toArgb()

        // Anchor grain shader to canvas space (scaling/translating if export coordinates differ)
        if (scale != 1f || offsetX != 0f || offsetY != 0f) {
            shaderMatrix.reset()
            shaderMatrix.postScale(scale, scale)
            shaderMatrix.postTranslate(offsetX, offsetY)
            grainShader.setLocalMatrix(shaderMatrix)
        } else {
            grainShader.setLocalMatrix(null)
        }

        val existingCache = stroke.renderCache as? CrayonCache
        val cache = if (
            existingCache != null &&
            existingCache.pointCount == points.size &&
            existingCache.strokeWidth == stroke.strokeWidth &&
            existingCache.color == baseArgb &&
            existingCache.scale == scale &&
            existingCache.offsetX == offsetX &&
            existingCache.offsetY == offsetY
        ) {
            existingCache
        } else {
            // Calculate stroke bounding box with generous padding
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            var maxY = -Float.MAX_VALUE

            for (p in points) {
                val px = p.x * scale + offsetX
                val py = p.y * scale + offsetY
                if (px < minX) minX = px
                if (px > maxX) maxX = px
                if (py < minY) minY = py
                if (py > maxY) maxY = py
            }

            val padding = baseWidth * 2.5f + 16f
            val layerBounds = RectF(
                minX - padding,
                minY - padding,
                maxX + padding,
                maxY + padding
            )

            // 1. Calculate effective coverage from pressure and velocity
            val avgPressure = if (stroke.pressures.isNotEmpty()) {
                stroke.pressures.average().toFloat()
            } else {
                0.5f
            }

            var totalDist = 0f
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                totalDist += hypot(p1.x - p0.x, p1.y - p0.y)
            }
            val avgStep = if (points.size > 1) totalDist / (points.size - 1) else 8f
            val speedFactor = ((avgStep - 4f) / 30f).coerceIn(0f, 1f)

            // Light pressure + fast move = low coverage (peaks only)
            // Heavy pressure + slow move = high coverage (fills pits)
            val pEff = (avgPressure * 0.72f + (1f - speedFactor) * 0.28f).coerceIn(0.12f, 0.96f)

            // ColorMatrix mapping: a' = k * a + b
            val k = 0.85f + 1.45f * (1.0f - pEff)
            val b = (pEff - 0.40f) * 1.55f * 255f

            val colorMatrix = ColorMatrix(
                floatArrayOf(
                    0f, 0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f, 0f,
                    0f, 0f, 0f, k, b
                )
            )

            val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = grainShader
                colorFilter = ColorMatrixColorFilter(colorMatrix)
                xfermode = dstInXfermode
            }

            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(baseArgb, hsv)

            if (points.size == 1) {
                val p = points[0]
                val cx = p.x * scale + offsetX
                val cy = p.y * scale + offsetY
                val dabPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = baseArgb or (0xFF shl 24)
                }
                val newCache = CrayonCache(
                    layerBounds = layerBounds,
                    strandPaths = emptyList(),
                    strandPaints = emptyList(),
                    maskPaint = maskPaint,
                    dabPaint = dabPaint,
                    dabCenter = Pair(cx, cy),
                    dabRadius = baseWidth * 0.5f,
                    pointCount = points.size,
                    strokeWidth = stroke.strokeWidth,
                    color = baseArgb,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY
                )
                stroke.renderCache = newCache
                newCache
            } else {
                val strandFractions = floatArrayOf(-0.35f, -0.12f, 0.12f, 0.35f)
                val strandPaths = mutableListOf<Path>()
                val strandPaints = mutableListOf<Paint>()

                for (s in 0 until NUM_STRANDS) {
                    val strandPath = Path()
                    val strandWidth = (baseWidth * (0.36f + (s % 2) * 0.08f)).coerceAtLeast(1.2f)
                    val baseFraction = strandFractions[s]

                    val strandSeed = stroke.id xor (s.toLong() * 37L)
                    val hueShift = (((strandSeed and 15L).toFloat() / 15f) - 0.5f) * 4f
                    val satMult = 1f + (((strandSeed ushr 4 and 15L).toFloat() / 15f) - 0.5f) * 0.08f
                    val valMult = 1f + (((strandSeed ushr 8 and 15L).toFloat() / 15f) - 0.5f) * 0.14f

                    val strandHsv = floatArrayOf(
                        (hsv[0] + hueShift).mod(360f),
                        (hsv[1] * satMult).coerceIn(0f, 1f),
                        (hsv[2] * valMult).coerceIn(0f, 1f)
                    )
                    val strandColor = android.graphics.Color.HSVToColor(255, strandHsv)

                    val strandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        this.strokeWidth = strandWidth
                        this.color = strandColor
                    }

                    var prevX = 0f
                    var prevY = 0f

                    for (i in points.indices) {
                        val p = points[i]
                        val (nx, ny) = getPointNormal(points, i)

                        val hash = ((stroke.id xor (i.toLong() * 73L + s.toLong() * 197L)) * 0x45D9F3BL).toInt()
                        val jitter = ((hash and 0xFF) / 255f - 0.5f) * 0.22f
                        val totalOffset = (baseFraction + jitter) * baseWidth

                        val px = (p.x * scale + offsetX) + nx * totalOffset
                        val py = (p.y * scale + offsetY) + ny * totalOffset

                        if (i == 0) {
                            strandPath.moveTo(px, py)
                            prevX = px
                            prevY = py
                        } else {
                            val midX = (prevX + px) * 0.5f
                            val midY = (prevY + py) * 0.5f
                            strandPath.quadTo(prevX, prevY, midX, midY)
                            prevX = px
                            prevY = py
                        }
                    }
                    strandPath.lineTo(prevX, prevY)
                    strandPaths.add(strandPath)
                    strandPaints.add(strandPaint)
                }

                val newCache = CrayonCache(
                    layerBounds = layerBounds,
                    strandPaths = strandPaths,
                    strandPaints = strandPaints,
                    maskPaint = maskPaint,
                    dabPaint = null,
                    dabCenter = null,
                    dabRadius = 0f,
                    pointCount = points.size,
                    strokeWidth = stroke.strokeWidth,
                    color = baseArgb,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY
                )
                stroke.renderCache = newCache
                newCache
            }
        }

        val layerPaint = if (alphaMultiplier < 1f) {
            Paint().apply { alpha = (alphaMultiplier * 255).toInt().coerceIn(0, 255) }
        } else null

        val saveCount = canvas.saveLayer(cache.layerBounds, layerPaint)

        if (cache.dabPaint != null && cache.dabCenter != null) {
            canvas.drawCircle(cache.dabCenter.first, cache.dabCenter.second, cache.dabRadius, cache.dabPaint)
        } else {
            for (i in cache.strandPaths.indices) {
                canvas.drawPath(cache.strandPaths[i], cache.strandPaints[i])
            }
        }

        // Multiply stroke layer with paper grain mask (DST_IN)
        canvas.drawRect(cache.layerBounds, cache.maskPaint)

        // Composite back onto canvas
        canvas.restoreToCount(saveCount)
    }

    private fun getPointNormal(points: List<androidx.compose.ui.geometry.Offset>, i: Int): Pair<Float, Float> {
        val dx: Float
        val dy: Float
        if (i == 0) {
            dx = points[1].x - points[0].x
            dy = points[1].y - points[0].y
        } else if (i == points.size - 1) {
            dx = points[i].x - points[i - 1].x
            dy = points[i].y - points[i - 1].y
        } else {
            dx = points[i + 1].x - points[i - 1].x
            dy = points[i + 1].y - points[i - 1].y
        }
        val len = hypot(dx, dy)
        return if (len > 0.001f) {
            Pair(-dy / len, dx / len)
        } else {
            Pair(0f, 1f)
        }
    }

    /**
     * Generates a toroidal 2D noise texture that tiles seamlessly along both axes.
     * Alpha and RGB values represent tooth peak height (0 = pit, 255 = peak).
     */
    private fun generateTileableNoiseBitmap(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(133742L)

        val g1Size = 16
        val grid1 = FloatArray(g1Size * g1Size) { random.nextFloat() }

        val g2Size = 32
        val grid2 = FloatArray(g2Size * g2Size) { random.nextFloat() }

        val pixels = IntArray(size * size)

        fun sampleGrid(grid: FloatArray, gSize: Int, x: Float, y: Float): Float {
            val gx = (x * gSize / size).mod(gSize.toFloat())
            val gy = (y * gSize / size).mod(gSize.toFloat())
            val x0 = gx.toInt()
            val y0 = gy.toInt()
            val x1 = (x0 + 1) % gSize
            val y1 = (y0 + 1) % gSize
            val tx = gx - x0
            val ty = gy - y0

            // Quintic smoothstep: 6t^5 - 15t^4 + 10t^3
            val sx = tx * tx * tx * (tx * (tx * 6f - 15f) + 10f)
            val sy = ty * ty * ty * (ty * (ty * 6f - 15f) + 10f)

            val v00 = grid[y0 * gSize + x0]
            val v10 = grid[y0 * gSize + x1]
            val v01 = grid[y1 * gSize + x0]
            val v11 = grid[y1 * gSize + x1]

            val top = v00 + sx * (v10 - v00)
            val bottom = v01 + sx * (v11 - v01)
            return top + sy * (bottom - top)
        }

        for (y in 0 until size) {
            for (x in 0 until size) {
                val n1 = sampleGrid(grid1, g1Size, x.toFloat(), y.toFloat())
                val n2 = sampleGrid(grid2, g2Size, x.toFloat(), y.toFloat())
                val fine = (random.nextFloat() - 0.5f) * 0.12f
                val composite = (n1 * 0.64f + n2 * 0.36f + fine).coerceIn(0f, 1f)
                val alphaByte = (composite * 255f).toInt().coerceIn(0, 255)
                pixels[y * size + x] = (alphaByte shl 24) or (alphaByte shl 16) or (alphaByte shl 8) or alphaByte
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }
}
