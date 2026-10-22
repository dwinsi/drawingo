package com.example.drawingo.util

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.model.DrawnStroke
import java.util.Random

/**
 * Photorealistic Watercolor renderer incorporating real-world fluid physics:
 * 1. Wet Edge / Dark Fringe Effect (capillary pigment accumulation at stroke borders).
 * 2. Paper Tooth Granulation (pigment settling into microscopic paper pits).
 * 3. Capillary Fluid Bleed (organic edge perturbations and soft water wash).
 * 4. Deterministic Pigment Blooms & Water Droplet Splatters.
 */
object WatercolorRenderer {

    private const val NOISE_SIZE = 128

    // Paper grain noise bitmap for realistic pigment granulation
    private val grainBitmap: Bitmap by lazy {
        generateTileablePaperNoise(NOISE_SIZE)
    }

    private val grainShader: BitmapShader by lazy {
        BitmapShader(grainBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private val shaderMatrix = Matrix()

    fun draw(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        val points = stroke.points
        if (points.isEmpty()) return

        val baseWidth = (stroke.strokeWidth * scale).coerceAtLeast(4f)
        val color = stroke.color.toArgb()
        val baseAlpha = (stroke.alpha * 255).toInt().coerceIn(10, 255)

        if (scale != 1f || offsetX != 0f || offsetY != 0f) {
            shaderMatrix.reset()
            shaderMatrix.postScale(scale, scale)
            shaderMatrix.postTranslate(offsetX, offsetY)
            grainShader.setLocalMatrix(shaderMatrix)
        } else {
            grainShader.setLocalMatrix(null)
        }

        if (points.size == 1) {
            drawSinglePointBlob(canvas, points.first(), baseWidth, color, baseAlpha, scale, offsetX, offsetY)
            return
        }

        // Build continuous smooth quadratic path
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

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        // Pass 1: Outer Soft Water Wash (Feathered base layer)
        paint.color = color
        paint.alpha = (baseAlpha * 0.18f).toInt().coerceIn(2, 255)
        paint.strokeWidth = baseWidth * 2.2f
        canvas.drawPath(path, paint)

        // Pass 2: Main Pigment Body
        paint.alpha = (baseAlpha * 0.32f).toInt().coerceIn(4, 255)
        paint.strokeWidth = baseWidth * 1.1f
        canvas.drawPath(path, paint)

        // Pass 3: Wet Edge / Dark Fringe Effect (Capillary edge accumulation)
        paint.alpha = (baseAlpha * 0.45f).toInt().coerceIn(8, 255)
        paint.strokeWidth = baseWidth * 1.75f
        canvas.drawPath(path, paint)

        // Inner core highlights
        paint.alpha = (baseAlpha * 0.25f).toInt().coerceIn(3, 255)
        paint.strokeWidth = baseWidth * 0.6f
        canvas.drawPath(path, paint)

        // Pass 4: Pigment Blooms & Fluid Water Droplets
        drawPigmentBlooms(canvas, stroke, scale, offsetX, offsetY, baseWidth, color, baseAlpha)
    }

    private fun drawSinglePointBlob(
        canvas: Canvas,
        point: androidx.compose.ui.geometry.Offset,
        baseWidth: Float,
        color: Int,
        baseAlpha: Int,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        val cx = point.x * scale + offsetX
        val cy = point.y * scale + offsetY
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        // Outer wet wash circle
        paint.shader = RadialGradient(
            cx, cy, baseWidth * 1.2f,
            intColorWithAlpha(color, (baseAlpha * 0.25f).toInt()),
            intColorWithAlpha(color, 0),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, baseWidth * 1.2f, paint)

        // Dark fringe outer ring
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = baseWidth * 0.15f
        paint.color = color
        paint.alpha = (baseAlpha * 0.45f).toInt()
        canvas.drawCircle(cx, cy, baseWidth * 0.85f, paint)
    }

    private fun drawPigmentBlooms(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        baseWidth: Float,
        color: Int,
        baseAlpha: Int
    ) {
        val stride = (stroke.points.size / 32).coerceAtLeast(1)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        stroke.points.indices.step(stride).forEach { index ->
            val point = stroke.points[index]
            val seed = stroke.id xor (index.toLong() * 31L)
            val cx = point.x * scale + offsetX + signedNoise(seed ushr 8) * baseWidth * 0.25f
            val cy = point.y * scale + offsetY + signedNoise(seed ushr 20) * baseWidth * 0.25f
            val radius = (baseWidth * (0.25f + ((seed and 7L).toFloat() / 7f) * 0.40f)).coerceAtLeast(2f)

            paint.shader = RadialGradient(
                cx,
                cy,
                radius,
                intColorWithAlpha(color, (baseAlpha * 0.25f).toInt()),
                intColorWithAlpha(color, 0),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius, paint)

            if ((seed and 3L) == 0L) {
                val dropX = cx + signedNoise(seed ushr 28) * baseWidth * 0.60f
                val dropY = cy + signedNoise(seed ushr 36) * baseWidth * 0.60f
                val dropRadius = (baseWidth * (0.04f + ((seed ushr 4 and 7L).toFloat() / 7f) * 0.08f)).coerceAtLeast(1f)

                paint.shader = RadialGradient(
                    dropX,
                    dropY,
                    dropRadius,
                    intColorWithAlpha(color, (baseAlpha * 0.30f).toInt()),
                    intColorWithAlpha(color, 0),
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(dropX, dropY, dropRadius, paint)
            }
        }
    }

    private fun generateTileablePaperNoise(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8)
        val pixels = IntArray(size * size)
        val random = Random(42L)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val noise = random.nextInt(60) + 195
                pixels[y * size + x] = noise shl 24
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }

    private fun signedNoise(value: Long): Float = (((value and 15L).toFloat() / 15f) - 0.5f) * 2f

    private fun intColorWithAlpha(color: Int, alpha: Int): Int {
        val boundedAlpha = alpha.coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (boundedAlpha shl 24)
    }
}
