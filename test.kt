import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawTransform

fun test(t: DrawTransform) {
    t.rotate(degrees = 1f, pivot = Offset.Zero)
}
