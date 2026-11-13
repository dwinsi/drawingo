fun main() {
    val bmpWidth = 6
    val bmpHeight = 3
    val pixels = byteArrayOf(
        -1, -1, -1, -1, -1, -1,
        -1,  1, -1,  1,  1, -1,
        -1, -1, -1, -1, -1, -1
    )
    // Here we simulate the second row having a hole at x=2.
    // x=1 is 1. Left is -1. Outer boundary.
    // x=3 is 1. Left is -1. Hole boundary.
    
    val traceDx = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
    val traceDy = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)
    
    for (y in 0 until bmpHeight) {
        for (x in 0 until bmpWidth) {
            val idx = y * bmpWidth + x
            if (pixels[idx] == 1.toByte() && (x == 0 || (pixels[idx - 1] != 1.toByte() && pixels[idx - 1] != 2.toByte()))) {
                println("Found boundary start at x=$x, y=$y")
                
                var cx = x
                var cy = y
                var dir = 7
                var count = 0
                do {
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
                    count++
                } while ((cx != x || cy != y) && count < 20)
                println("Traced $count pixels")
            }
        }
    }
}
