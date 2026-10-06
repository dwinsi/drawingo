import re

with open("/Users/ashwinsingh/Drawingo/app/src/main/java/com/example/drawingo/ui/MagicCreatureRenderer.kt", "r") as f:
    code = f.read()

# 1. Change function definitions
# private fun drawSmilingSun(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long)
code = re.sub(r"private fun (\w+)\(drawScope: DrawScope, ", r"private fun DrawScope.\1(", code)
code = re.sub(r"private fun (\w+)\(drawScope: DrawScope\)", r"private fun DrawScope.\1()", code)

# 2. Change function calls in drawCompanion
# CreatureType.SMILING_SUN -> drawSmilingSun(drawScope, companion.position, ...)
code = re.sub(r"-> (\w+)\(drawScope, ", r"-> drawScope.\1(", code)

# 3. Change function calls in helper methods
# drawCuteFace(drawScope, pos, r * 0.30f)
code = re.sub(r"(\w+)\(drawScope, ", r"\1(", code)

# 4. Remove drawScope. prefix everywhere else inside the class
code = code.replace("drawScope.withTransform", "withTransform")
code = code.replace("drawScope.drawPath", "drawPath")
code = code.replace("drawScope.drawCircle", "drawCircle")
code = code.replace("drawScope.drawOval", "drawOval")
code = code.replace("drawScope.drawRoundRect", "drawRoundRect")
code = code.replace("drawScope.drawLine", "drawLine")

# Wait, in drawCompanion, there are no drawScope.draw... calls right?
# Just in case, let's make sure we only removed drawScope. inside the file where it makes sense.
# Actually, inside drawCompanion we did `drawScope.drawSmilingSun(...)`.
# Let's write the file out.
with open("/Users/ashwinsingh/Drawingo/app/src/main/java/com/example/drawingo/ui/MagicCreatureRenderer.kt", "w") as f:
    f.write(code)
