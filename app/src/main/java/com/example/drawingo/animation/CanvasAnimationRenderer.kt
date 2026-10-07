package com.example.drawingo.animation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.drawingo.model.AnimatedDrawingEntity
import com.example.drawingo.model.AnimationSceneType
import com.example.drawingo.model.Particle
import com.example.drawingo.model.ParticleType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure Compose Canvas Animation Renderer for Drawingo.
 * Animates the user's actual drawn artwork across five dynamic environments:
 * Ocean Leap, Sky Flight, Space Launch, Land Safari, and Abstract Flow.
 */
object CanvasAnimationRenderer {

    private val OceanColorTop = Color(0xFF00E5FF)
    private val OceanColorDeep = Color(0xFF00363A)
    private val WaveColorFront = Color(0xFF00F0FF)
    private val WaveColorBack = Color(0x9900838F)

    private val SkyColorTop = Color(0xFF80D8FF)
    private val SkyColorBottom = Color(0xFFE0F7FA)
    private val CloudColor = Color(0xF2FFFFFF)

    private val SpaceColorTop = Color(0xFF0A0C18)
    private val SpaceColorBottom = Color(0xFF1A1C38)

    private val HillColorBack = Color(0xFF4CAF50)
    private val HillColorFront = Color(0xFF81C784)

    fun renderScene(
        drawScope: DrawScope,
        sceneType: AnimationSceneType,
        entity: AnimatedDrawingEntity?,
        particles: List<Particle>,
        progress: Float, // 0.0f to 1.0f
        currentTimeMs: Long
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Render animated environment background
        when (sceneType) {
            AnimationSceneType.OCEAN_LEAP -> drawOceanBackground(drawScope, width, height, currentTimeMs)
            AnimationSceneType.SKY_FLIGHT -> drawSkyBackground(drawScope, width, height, currentTimeMs)
            AnimationSceneType.SPACE_LAUNCH -> drawSpaceBackground(drawScope, width, height, currentTimeMs)
            AnimationSceneType.LAND_SAFARI -> drawLandBackground(drawScope, width, height)
            AnimationSceneType.ABSTRACT_FLOW -> drawAbstractBackground(drawScope, width, height, currentTimeMs)
        }

        // 2. Render particle physics
        drawParticles(drawScope, particles)

        // 3. Render animated drawn artwork
        if (entity != null && entity.strokes.isNotEmpty()) {
            drawAnimatedArtwork(drawScope, entity, sceneType, progress, width, height, currentTimeMs)
        }
    }

    // ==========================================
    // BACKGROUND SCENE RENDERERS
    // ==========================================

    private fun drawOceanBackground(drawScope: DrawScope, w: Float, h: Float, timeMs: Long) {
        val oceanGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0F7FA), OceanColorTop, OceanColorDeep),
            startY = 0f,
            endY = h
        )
        drawScope.drawRect(brush = oceanGrad, size = Size(w, h))

        val waterLine = h * 0.65f

        // Back Wave
        val backWavePath = Path().apply {
            moveTo(0f, waterLine)
            var x = 0f
            while (x <= w + 20f) {
                val y = waterLine + sin((x * 0.012f) + timeMs * 0.003f).toFloat() * 25f
                lineTo(x, y)
                x += 20f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawScope.drawPath(path = backWavePath, color = WaveColorBack)

        // Front Wave
        val frontWavePath = Path().apply {
            moveTo(0f, waterLine + 15f)
            var x = 0f
            while (x <= w + 20f) {
                val y = waterLine + 15f + sin((x * 0.018f) - timeMs * 0.004f).toFloat() * 30f
                lineTo(x, y)
                x += 20f
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawScope.drawPath(path = frontWavePath, color = WaveColorFront.copy(alpha = 0.85f))
    }

    private fun drawSkyBackground(drawScope: DrawScope, w: Float, h: Float, timeMs: Long) {
        val skyGrad = Brush.verticalGradient(
            colors = listOf(SkyColorTop, SkyColorBottom),
            startY = 0f,
            endY = h
        )
        drawScope.drawRect(brush = skyGrad, size = Size(w, h))

        // Rainbow Arc
        drawScope.drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color(0xFFFF007F), Color(0xFFFFD600), Color(0xFF39FF14), Color(0xFF00F0FF), Color(0xFFB026FF))
            ),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.2f),
            size = Size(w * 0.8f, h * 0.8f),
            style = Stroke(width = 24f)
        )

        // Drifting Fluffy Clouds
        val cloudOffset1 = (timeMs * 0.03f) % (w + 200f) - 100f
        drawCloud(drawScope, Offset(cloudOffset1, h * 0.25f), 70f)
        val cloudOffset2 = ((timeMs * 0.02f) + w * 0.5f) % (w + 200f) - 100f
        drawCloud(drawScope, Offset(cloudOffset2, h * 0.15f), 90f)
    }

    private fun drawSpaceBackground(drawScope: DrawScope, w: Float, h: Float, timeMs: Long) {
        val spaceGrad = Brush.verticalGradient(
            colors = listOf(SpaceColorTop, SpaceColorBottom),
            startY = 0f,
            endY = h
        )
        drawScope.drawRect(brush = spaceGrad, size = Size(w, h))

        // Glowing Moon
        drawScope.drawCircle(color = Color(0xFFFFF9C4), radius = 60f, center = Offset(w * 0.82f, h * 0.2f))
        drawScope.drawCircle(color = Color(0x44FFF9C4), radius = 80f, center = Offset(w * 0.82f, h * 0.2f))

        // Twinkling stars
        val starCount = 20
        for (i in 0 until starCount) {
            val sx = (i * 137.5f) % w
            val sy = (i * 97.3f) % (h * 0.7f)
            val alpha = (sin(timeMs * 0.005f + i).toFloat() * 0.4f + 0.6f).coerceIn(0.2f, 1f)
            drawScope.drawCircle(color = Color.White.copy(alpha = alpha), radius = (i % 3 + 2).toFloat(), center = Offset(sx, sy))
        }
    }

    private fun drawLandBackground(drawScope: DrawScope, w: Float, h: Float) {
        val skyGrad = Brush.verticalGradient(
            colors = listOf(Color(0xFFB2EBF2), Color(0xFFE8F5E9)),
            startY = 0f,
            endY = h
        )
        drawScope.drawRect(brush = skyGrad, size = Size(w, h))

        // Back Hill
        val hillBack = Path().apply {
            moveTo(-50f, h * 0.7f)
            quadraticTo(w * 0.3f, h * 0.5f, w * 0.7f, h * 0.65f)
            quadraticTo(w * 0.9f, h * 0.7f, w + 50f, h * 0.6f)
            lineTo(w + 50f, h)
            lineTo(-50f, h)
            close()
        }
        drawScope.drawPath(path = hillBack, color = HillColorBack)

        // Front Hill
        val hillFront = Path().apply {
            moveTo(-50f, h * 0.75f)
            quadraticTo(w * 0.5f, h * 0.6f, w + 50f, h * 0.72f)
            lineTo(w + 50f, h)
            lineTo(-50f, h)
            close()
        }
        drawScope.drawPath(path = hillFront, color = HillColorFront)
    }

    private fun drawAbstractBackground(drawScope: DrawScope, w: Float, h: Float, timeMs: Long) {
        val abstractGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF), Color(0xFFFDEB71), Color(0xFFBAFFC9), Color(0xFFBAE1FF)),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
        drawScope.drawRect(brush = abstractGradient, size = Size(w, h))

        // Draw animated floating shapes (Triangles, Circles, Squares, Stars)
        val shapeCount = 20
        for (i in 0 until shapeCount) {
            val speed = 0.05f + (i % 5) * 0.02f
            val sy = (h + 200f) - ((timeMs * speed + i * 150f) % (h + 400f))
            val sx = (i * 317f) % w + sin(timeMs * 0.002f + i).toFloat() * 50f
            val size = 30f + (i % 4) * 25f
            val rot = (timeMs * 0.06f * (if (i % 2 == 0) 1f else -1f) + i * 45f) % 360f

            val alpha = (sin(timeMs * 0.003f + i).toFloat() * 0.3f + 0.5f).coerceIn(0.2f, 0.8f)

            drawScope.withTransform({
                translate(sx, sy)
                rotate(rot, pivot = Offset.Zero)
            }) {
                val color = when (i % 5) {
                    0 -> Color(0xFFFF007F) // Hot Magenta
                    1 -> Color(0xFF00F0FF) // Cyber Aqua
                    2 -> Color(0xFF39FF14) // Acid Lime
                    3 -> Color(0xFFFFD600) // Sunshine Gold
                    else -> Color(0xFF9333EA) // Royal Violet
                }.copy(alpha = alpha)

                when (i % 4) {
                    0 -> { // Circle
                        drawCircle(color = color, radius = size / 2f)
                    }
                    1 -> { // Triangle
                        val path = Path().apply {
                            moveTo(0f, -size / 1.5f)
                            lineTo(size / 1.5f, size / 1.5f)
                            lineTo(-size / 1.5f, size / 1.5f)
                            close()
                        }
                        drawPath(path = path, color = color)
                    }
                    2 -> { // Square
                        drawRect(
                            color = color,
                            topLeft = Offset(-size / 2f, -size / 2f),
                            size = Size(size, size)
                        )
                    }
                    3 -> { // Star
                        val path = Path()
                        val outerR = size / 1.2f
                        val innerR = outerR * 0.4f
                        for (j in 0 until 10) {
                            val angle = j * PI / 5 - PI / 2
                            val r = if (j % 2 == 0) outerR else innerR
                            val px = cos(angle).toFloat() * r
                            val py = sin(angle).toFloat() * r
                            if (j == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        drawPath(path = path, color = color)
                    }
                }
            }
        }
    }

    private fun drawCloud(drawScope: DrawScope, center: Offset, size: Float) {
        drawScope.drawCircle(color = CloudColor, radius = size * 0.5f, center = center)
        drawScope.drawCircle(color = CloudColor, radius = size * 0.4f, center = Offset(center.x - size * 0.4f, center.y + size * 0.1f))
        drawScope.drawCircle(color = CloudColor, radius = size * 0.4f, center = Offset(center.x + size * 0.4f, center.y + size * 0.1f))
        drawScope.drawRoundRect(
            color = CloudColor,
            topLeft = Offset(center.x - size * 0.6f, center.y),
            size = Size(size * 1.2f, size * 0.4f),
            cornerRadius = CornerRadius(size * 0.2f)
        )
    }

    // ==========================================
    // DRAWN ARTWORK TRAJECTORY ANIMATION
    // ==========================================

    private fun drawAnimatedArtwork(
        drawScope: DrawScope,
        entity: AnimatedDrawingEntity,
        sceneType: AnimationSceneType,
        progress: Float,
        w: Float,
        h: Float,
        timeMs: Long
    ) {
        val bounds = entity.bounds
        val drawnWidth = bounds.width.coerceAtLeast(40f)
        val drawnHeight = bounds.height.coerceAtLeast(40f)
        val drawnCenterX = bounds.center.x
        val drawnCenterY = bounds.center.y

        val targetDim = (minOf(w, h) * 0.35f).coerceAtLeast(120f)
        val baseScale = targetDim / maxOf(drawnWidth, drawnHeight)

        val phase = progress * 2f * PI.toFloat()
        val drift = sin(phase).toFloat()
        val bounce = kotlin.math.abs(sin(phase * 2f)).toFloat()
        val posX: Float
        val posY: Float
        var scaleX = baseScale
        var scaleY = baseScale

        when (sceneType) {
            AnimationSceneType.OCEAN_LEAP -> {
                posX = w * 0.5f + drift * w * 0.16f
                posY = h * 0.53f - bounce * h * 0.12f
                val squash = bounce * 0.06f
                scaleX = baseScale * (1f + squash)
                scaleY = baseScale * (1f - squash)
            }
            AnimationSceneType.SKY_FLIGHT -> {
                posX = w * 0.5f + drift * w * 0.18f
                posY = h * 0.37f + sin(phase * 2f).toFloat() * h * 0.07f
            }
            AnimationSceneType.SPACE_LAUNCH -> {
                posX = w * 0.5f + drift * w * 0.12f
                posY = h * 0.54f - (0.5f + 0.5f * sin(phase - PI.toFloat() / 2f)) * h * 0.12f
                val pulse = sin(phase).toFloat() * 0.035f
                scaleX = baseScale * (1f + pulse)
                scaleY = baseScale * (1f + pulse)
            }
            AnimationSceneType.LAND_SAFARI -> {
                posX = w * 0.5f + drift * w * 0.12f
                posY = h * 0.58f - bounce * h * 0.055f
                val squash = bounce * 0.1f
                scaleX = baseScale * (1f + squash)
                scaleY = baseScale * (1f - squash)
            }
            AnimationSceneType.ABSTRACT_FLOW -> {
                posX = w * 0.5f + cos(phase).toFloat() * w * 0.1f
                posY = h * 0.5f + sin(phase).toFloat() * h * 0.08f
                val pulse = sin(timeMs * 0.002f).toFloat() * 0.035f
                scaleX = baseScale * (1f + pulse)
                scaleY = baseScale * (1f + pulse)
            }
        }

        drawScope.withTransform({
            translate(posX, posY)
            scale(scaleX, scaleY, pivot = Offset.Zero)
            translate(-drawnCenterX, -drawnCenterY)
        }) {
            for (stroke in entity.strokes) {
                if (stroke.points.size < 2) continue
                val path = Path().apply {
                    moveTo(stroke.points[0].x, stroke.points[0].y)
                    for (i in 1 until stroke.points.size) {
                        lineTo(stroke.points[i].x, stroke.points[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = stroke.color.copy(alpha = stroke.alpha),
                    style = Stroke(
                        width = stroke.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }

    private fun drawParticles(drawScope: DrawScope, particles: List<Particle>) {
        for (p in particles) {
            val alphaColor = p.color.copy(alpha = (p.alpha * p.color.alpha).coerceIn(0f, 1f))
            when (p.type) {
                ParticleType.WATER_SPLASH -> {
                    drawScope.drawCircle(color = alphaColor, radius = p.size * p.life, center = Offset(p.x, p.y))
                }
                ParticleType.BUBBLE -> {
                    drawScope.drawCircle(color = alphaColor, radius = p.size, center = Offset(p.x, p.y))
                    drawScope.drawCircle(color = Color.White.copy(alpha = p.alpha * 0.7f), radius = p.size * 0.28f, center = Offset(p.x - p.size * 0.25f, p.y - p.size * 0.25f))
                }
                ParticleType.STAR_DUST -> {
                    val s = p.size * p.life
                    drawScope.drawRect(color = alphaColor, topLeft = Offset(p.x - s / 2f, p.y - s / 2f), size = Size(s, s))
                }
                ParticleType.SMOKE_PUFF -> {
                    drawScope.drawCircle(color = alphaColor, radius = p.size, center = Offset(p.x, p.y))
                }
                ParticleType.RAINBOW_SPARKLE -> {
                    drawScope.drawCircle(color = alphaColor, radius = p.size * p.life, center = Offset(p.x, p.y))
                }
            }
        }
    }
}
