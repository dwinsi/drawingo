import kotlin.reflect.full.memberFunctions
fun main() {
    val clazz = Class.forName("androidx.compose.ui.graphics.drawscope.DrawTransform")
    clazz.methods.forEach { println(it.name + " " + it.parameterTypes.map { it.name }) }
}
