package com.example.drawingo.util

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.model.DrawnStroke
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Ultra-fast, 3-Pass Hardware-Accelerated Spray Paint / Airbrush renderer.
 * Batches thousands of spray droplets into 3 native `drawPoints` GPU calls for locked 60 FPS.
 */
object SprayRenderer {

    private class SprayCache(
        val buf1: FloatArray,
        val count1: Int,
        val buf2: FloatArray,
        val count2: Int,
        val buf3: FloatArray,
        val count3: Int,
        val pointCount: Int,
        val strokeWidth: Float,
        val scale: Float,
        val offsetX: Float,
        val offsetY: Float
    )

    fun draw(
        canvas: Canvas,
        stroke: DrawnStroke,
        scale: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        alphaMultiplier: Float = 1f
    ) {
        val points = stroke.points
        if (points.isEmpty()) return

        val baseWidth = (stroke.strokeWidth * scale).coerceAtLeast(8f)
        val argbColor = stroke.color.toArgb()
        val baseAlpha = (stroke.alpha * alphaMultiplier * 255).toInt().coerceIn(10, 255)

        val existingCache = stroke.renderCache as? SprayCache
        val cache = if (
            existingCache != null &&
            existingCache.pointCount == points.size &&
            existingCache.strokeWidth == stroke.strokeWidth &&
            existingCache.scale == scale &&
            existingCache.offsetX == offsetX &&
            existingCache.offsetY == offsetY
        ) {
            existingCache
        } else {
            val sprayRadius = baseWidth * 1.6f // Increased throw area
            // Spatial downsampling stride: ensure step points are separated by at least minStepDist
            val minStepDistSq = (baseWidth * 0.25f).coerceAtLeast(8f).let { it * it }
            val sampledPoints = ArrayList<Offset>(points.size)
            sampledPoints.add(points.first())
            var lastP = points.first()

            for (i in 1 until points.size) {
                val p = points[i]
                val dx = p.x - lastP.x
                val dy = p.y - lastP.y
                if (dx * dx + dy * dy >= minStepDistSq || i == points.lastIndex) {
                    sampledPoints.add(p)
                    lastP = p
                }
            }

            val numSteps = sampledPoints.size
            val maxLayer1PerStep = 15
            val maxLayer2PerStep = 25
            val maxLayer3PerStep = 40

            val buf1 = FloatArray(numSteps * maxLayer1PerStep * 2)
            val buf2 = FloatArray(numSteps * maxLayer2PerStep * 2)
            val buf3 = FloatArray(numSteps * maxLayer3PerStep * 2)

            var count1 = 0
            var count2 = 0
            var count3 = 0

            for ((idx, p) in sampledPoints.withIndex()) {
                val cx = p.x * scale + offsetX
                val cy = p.y * scale + offsetY
                val pressure = stroke.pressures.getOrNull(idx) ?: 1.0f
                val currentRadius = sprayRadius * (0.65f + pressure * 0.45f)

                // Seed deterministic random per step
                val random = Random(stroke.id xor (idx.toLong() * 1000003L))

                // Layer 1: Core Droplets (Denser inner region)
                repeat(maxLayer1PerStep) {
                    val distanceRatio = sqrt(random.nextFloat()) * currentRadius * 0.5f
                    val angle = random.nextFloat() * 2f * Math.PI.toFloat()
                    if (count1 < buf1.size - 1) {
                        buf1[count1++] = cx + distanceRatio * cos(angle)
                        buf1[count1++] = cy + distanceRatio * sin(angle)
                    }
                }

                // Layer 2: Medium Droplets (Mid-range spread)
                repeat(maxLayer2PerStep) {
                    val distanceRatio = sqrt(random.nextFloat()) * currentRadius * 0.8f
                    val angle = random.nextFloat() * 2f * Math.PI.toFloat()
                    if (count2 < buf2.size - 1) {
                        buf2[count2++] = cx + distanceRatio * cos(angle)
                        buf2[count2++] = cy + distanceRatio * sin(angle)
                    }
                }

                // Layer 3: Fine Mist (Outer halo)
                repeat(maxLayer3PerStep) {
                    val distanceRatio = sqrt(random.nextFloat()) * currentRadius
                    val angle = random.nextFloat() * 2f * Math.PI.toFloat()
                    if (count3 < buf3.size - 1) {
                        buf3[count3++] = cx + distanceRatio * cos(angle)
                        buf3[count3++] = cy + distanceRatio * sin(angle)
                    }
                }
            }

            val newCache = SprayCache(
                buf1 = buf1,
                count1 = count1,
                buf2 = buf2,
                count2 = count2,
                buf3 = buf3,
                count3 = count3,
                pointCount = points.size,
                strokeWidth = stroke.strokeWidth,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY
            )
            stroke.renderCache = newCache
            newCache
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = argbColor
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }

        // Pass 1: Fine Mist (Outer halo)
        if (cache.count3 > 0) {
            paint.strokeWidth = (baseWidth * 0.035f).coerceAtLeast(1.0f * scale)
            paint.alpha = (baseAlpha * 0.35f).toInt().coerceIn(5, 255)
            canvas.drawPoints(cache.buf3, 0, cache.count3, paint)
        }

        // Pass 2: Medium Droplets (Mid-range body)
        if (cache.count2 > 0) {
            paint.strokeWidth = (baseWidth * 0.065f).coerceAtLeast(1.5f * scale)
            paint.alpha = (baseAlpha * 0.50f).toInt().coerceIn(5, 255)
            canvas.drawPoints(cache.buf2, 0, cache.count2, paint)
        }

        // Pass 3: Core Droplets (Rich inner core)
        if (cache.count1 > 0) {
            paint.strokeWidth = (baseWidth * 0.12f).coerceAtLeast(2.5f * scale)
            paint.alpha = (baseAlpha * 0.65f).toInt().coerceIn(5, 255)
            canvas.drawPoints(cache.buf1, 0, cache.count1, paint)
        }
    }
}
