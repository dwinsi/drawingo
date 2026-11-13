Let's implement `floodFillVector` in `ShapeFillUtils.kt`.
It'll completely bypass the error-prone looping geometry math.

```kotlin
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
                if (pixels[idx] == 1.toByte() && (x == 0 || pixels[idx - 1] == 0.toByte())) {
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
