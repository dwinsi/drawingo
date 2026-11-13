package com.example.drawingo.util

import androidx.compose.ui.geometry.Offset
import com.example.drawingo.model.DrawnStroke

data class ContourCandidate(
    val layerId: Long,
    val points: List<Offset>,
    val strokeWidth: Float,
    val area: Float
)

object ShapeFillUtils {

    /** Finds closed contours formed by single or multiple strokes. */
    fun findClosedContours(strokes: List<Pair<Long, DrawnStroke>>): List<ContourCandidate> {
        val candidates = mutableListOf<ContourCandidate>()

        // 1. Single-stroke loop detection
        for ((layerId, stroke) in strokes) {
            val pts = stroke.points
            if (pts.size < 4) continue

            val pathLength = pts.zipWithNext().sumOf { (a, b) ->
                kotlin.math.sqrt(distanceSquared(a, b).toDouble())
            }.toFloat()

            val closureTolerance = maxOf(48f, stroke.strokeWidth * 2.5f, pathLength * 0.25f).coerceIn(40f, 300f)
            val toleranceSq = closureTolerance * closureTolerance

            var bestPolygon: List<Offset>? = null
            var bestLength = -1

            for (j in pts.indices.reversed()) {
                if (j < 3) break
                for (i in 0..j - 3) {
                    val span = j - i
                    if (span <= bestLength) continue
                    if (distanceSquared(pts[i], pts[j]) <= toleranceSq) {
                        val closed = pts.subList(i, j + 1).toMutableList()
                        closed.add(pts[i])
                        bestPolygon = closed
                        bestLength = span
                    }
                }
            }

            if (bestPolygon != null && bestPolygon.size >= 4) {
                val area = polygonArea(bestPolygon)
                if (area > 50f) {
                    candidates.add(ContourCandidate(layerId, bestPolygon, stroke.strokeWidth, area))
                }
            }
        }

        // 2. Multi-stroke loop detection
        for (startIndex in strokes.indices.reversed()) {
            val (startLayerId, startStroke) = strokes[startIndex]
            val chain = startStroke.points.toMutableList()
            val used = mutableSetOf(startIndex)
            var layerId = startLayerId
            var maxStrokeWidth = startStroke.strokeWidth
            var closedPolygon: List<Offset>? = null

            while (used.size <= strokes.size) {
                val pathLength = chain.zipWithNext().sumOf { (a, b) ->
                    kotlin.math.sqrt(distanceSquared(a, b).toDouble())
                }.toFloat()

                val closureTolerance = maxOf(48f, maxStrokeWidth * 2.5f, pathLength * 0.25f).coerceIn(40f, 300f)
                val toleranceSq = closureTolerance * closureTolerance

                var closureIndex = -1
                if (chain.size >= 4) {
                    for (k in 0..chain.size - 4) {
                        if (distanceSquared(chain.last(), chain[k]) <= toleranceSq) {
                            closureIndex = k
                            break
                        }
                    }
                }

                if (closureIndex >= 0) {
                    val closed = chain.subList(closureIndex, chain.size).toMutableList()
                    closed.add(chain[closureIndex])
                    closedPolygon = closed
                    break
                }

                var nearestIndex = -1
                var nearestAtStart = false
                var nearestDistance = Float.MAX_VALUE
                val endPos = chain.last()

                for (candidateIndex in strokes.indices.reversed()) {
                    if (candidateIndex in used) continue
                    val (candidateLayerId, candidate) = strokes[candidateIndex]
                    val first = candidate.points.first()
                    val last = candidate.points.last()
                    val tol = maxOf(36f, maxOf(maxStrokeWidth, candidate.strokeWidth) * 2f)
                    val distanceToFirst = distanceSquared(endPos, first)
                    val distanceToLast = distanceSquared(endPos, last)
                    val candidateDistance = minOf(distanceToFirst, distanceToLast)
                    if (candidateDistance <= tol * tol && candidateDistance < nearestDistance) {
                        nearestIndex = candidateIndex
                        nearestAtStart = distanceToFirst <= distanceToLast
                        nearestDistance = candidateDistance
                        layerId = candidateLayerId
                    }
                }

                if (nearestIndex < 0) break

                val candidateStroke = strokes[nearestIndex].second
                maxStrokeWidth = maxOf(maxStrokeWidth, candidateStroke.strokeWidth)
                val nextPoints = candidateStroke.points.let { points ->
                    if (nearestAtStart) points else points.asReversed()
                }
                chain.addAll(nextPoints.drop(1))
                used += nearestIndex
            }

            if (closedPolygon != null && closedPolygon.size >= 4) {
                // Only add if it actually combined multiple strokes to prevent duplicating single-stroke detections
                if (used.size > 1) {
                    val area = polygonArea(closedPolygon)
                    if (area > 50f) {
                        candidates.add(ContourCandidate(layerId, closedPolygon, maxStrokeWidth, area))
                    }
                }
            }
        }

        return candidates
    }

    fun polygonArea(points: List<Offset>): Float {
        if (points.size < 3) return 0f
        var area = 0f
        var j = points.lastIndex
        for (i in points.indices) {
            area += (points[j].x + points[i].x) * (points[j].y - points[i].y)
            j = i
        }
        return kotlin.math.abs(area / 2f)
    }

    fun distanceSquared(first: Offset, second: Offset): Float {
        val dx = first.x - second.x
        val dy = first.y - second.y
        return dx * dx + dy * dy
    }

    fun floodFillVector(
        strokes: List<DrawnStroke>,
        point: Offset,
        width: Int,
        height: Int
    ): List<Offset>? {
        if (width <= 0 || height <= 0) return null

        val maxDim = 512f
        val scale = minOf(1f, maxDim / maxOf(width, height))
        val bmpWidth = (width * scale).toInt().coerceAtLeast(1)
        val bmpHeight = (height * scale).toInt().coerceAtLeast(1)

        val bitmap = android.graphics.Bitmap.createBitmap(bmpWidth, bmpHeight, android.graphics.Bitmap.Config.ALPHA_8)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
            color = android.graphics.Color.BLACK
        }

        // Draw boundaries
        for (stroke in strokes) {
            paint.strokeWidth = stroke.strokeWidth * scale
            val path = android.graphics.Path()
            var isNew = true
            for (p in stroke.points) {
                if (p == Offset.Unspecified || p.x.isNaN() || p.y.isNaN()) {
                    isNew = true
                } else {
                    if (isNew) {
                        path.moveTo(p.x * scale, p.y * scale)
                        isNew = false
                    } else {
                        path.lineTo(p.x * scale, p.y * scale)
                    }
                }
            }
            canvas.drawPath(path, paint)
        }

        val pixels = ByteArray(bmpWidth * bmpHeight)
        val byteBuffer = java.nio.ByteBuffer.allocateDirect(pixels.size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        byteBuffer.rewind()
        byteBuffer.get(pixels)

        val startX = (point.x * scale).toInt().coerceIn(0, bmpWidth - 1)
        val startY = (point.y * scale).toInt().coerceIn(0, bmpHeight - 1)

        // Check if user clicked on a stroke boundary (alpha channel is non-zero, mapping to -1 in signed byte)
        if (pixels[startY * bmpWidth + startX] != 0.toByte()) return null

        val queue = IntArray(bmpWidth * bmpHeight * 2)
        var head = 0
        var tail = 0
        queue[tail++] = startX
        queue[tail++] = startY
        pixels[startY * bmpWidth + startX] = 1.toByte()

        val dx = intArrayOf(-1, 1, 0, 0)
        val dy = intArrayOf(0, 0, -1, 1)

        while (head < tail) {
            val x = queue[head++]
            val y = queue[head++]

            for (i in 0 until 4) {
                val nx = x + dx[i]
                val ny = y + dy[i]
                if (nx in 0 until bmpWidth && ny in 0 until bmpHeight) {
                    val idx = ny * bmpWidth + nx
                    if (pixels[idx] == 0.toByte()) {
                        pixels[idx] = 1.toByte()
                        queue[tail++] = nx
                        queue[tail++] = ny
                    }
                }
            }
        }

        val allPoints = mutableListOf<Offset>()
        val traceDx = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
        val traceDy = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)

        for (y in 0 until bmpHeight) {
            for (x in 0 until bmpWidth) {
                val idx = y * bmpWidth + x
                if (pixels[idx] == 1.toByte() && (x == 0 || (pixels[idx - 1] != 1.toByte() && pixels[idx - 1] != 2.toByte()))) {
                    val contour = mutableListOf<Offset>()
                    var cx = x
                    var cy = y
                    var dir = 7

                    do {
                        contour.add(Offset(cx / scale, cy / scale))
                        pixels[cy * bmpWidth + cx] = 2.toByte()

                        var found = false
                        val startDir = (dir + 6) % 8
                        for (i in 0 until 8) {
                            val nextDir = (startDir + i) % 8
                            val nx = cx + traceDx[nextDir]
                            val ny = cy + traceDy[nextDir]
                            if (nx in 0 until bmpWidth && ny in 0 until bmpHeight) {
                                val pVal = pixels[ny * bmpWidth + nx]
                                if (pVal == 1.toByte() || pVal == 2.toByte()) {
                                    cx = nx
                                    cy = ny
                                    dir = nextDir
                                    found = true
                                    break
                                }
                            }
                        }
                        if (!found) break
                    } while (cx != x || cy != y)

                    if (contour.size > 2) {
                        val simplified = simplify(contour, 1.5f / scale)
                        if (allPoints.isNotEmpty()) allPoints.add(Offset.Unspecified)
                        allPoints.addAll(simplified)
                    }
                }
            }
        }
        return if (allPoints.isEmpty()) null else allPoints
    }

    private fun simplify(points: List<Offset>, tolerance: Float): List<Offset> {
        if (points.size <= 2) return points
        val sqTolerance = tolerance * tolerance
        val result = mutableListOf<Offset>()
        result.add(points.first())
        var lastAdded = points.first()
        for (i in 1 until points.size - 1) {
            val dx = lastAdded.x - points[i].x
            val dy = lastAdded.y - points[i].y
            if (dx * dx + dy * dy > sqTolerance) {
                result.add(points[i])
                lastAdded = points[i]
            }
        }
        result.add(points.last())
        return result
    }
}
