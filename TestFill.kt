import java.nio.ByteBuffer

fun main() {
    val b = ByteArray(10)
    val bb = ByteBuffer.allocateDirect(10)
    bb.get(b)
    println(b.size)
}
