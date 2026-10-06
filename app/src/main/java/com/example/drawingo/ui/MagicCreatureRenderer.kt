package com.example.drawingo.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.drawingo.model.CreatureType
import com.example.drawingo.model.MagicCompanion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure Compose Canvas renderer for magical companion creatures:
 * - Sea Animals (Octopus, Jellyfish, Clownfish, Starfish)
 * - Wild Animals (Lion, Baby Bear, Elephant)
 * - Birds (Little Bird, Penguin)
 * - Butterflies (Fluttering Neon Butterfly)
 * - Flowers (Blooming Flower, Sunflower)
 */
object MagicCreatureRenderer {

    private val OutlineColor = Color(0xFF14151C)
    private val WhiteColor = Color(0xFFFFFFFF)
    private val CheekColor = Color(0xE6FF6090)
    private val NoseColor = Color(0xFF1E1B4B)

    fun drawCompanion(
        drawScope: DrawScope,
        companion: MagicCompanion,
        currentTimeMs: Long
    ) {
        val age = (currentTimeMs - companion.spawnTimestamp).coerceAtLeast(0L)
        val entranceScale = if (age < 380L) {
            val t = age / 380f
            1.0f + (sin(t * PI).toFloat() * 0.25f) * (1f - t)
        } else {
            1.0f
        }

        val baseSize = companion.size * entranceScale
        if (baseSize <= 2f) return

        when (companion.type) {
            CreatureType.OCTOPUS -> drawOctopus(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.JELLYFISH -> drawJellyfish(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.CLOWN_FISH -> drawClownfish(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.STARFISH -> drawStarfish(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.LION -> drawLion(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BABY_BEAR -> drawBabyBear(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.ELEPHANT -> drawElephant(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.LITTLE_BIRD -> drawLittleBird(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.PENGUIN -> drawPenguin(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BUTTERFLY -> drawButterfly(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BLOOMING_FLOWER -> drawBloomingFlower(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.SUNFLOWER -> drawSunflower(drawScope, companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
        }
    }

    private fun drawOctopus(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.45f
        val headCenter = Offset(pos.x, pos.y - r * 0.2f)

        val tentacleCount = 6
        for (i in 0 until tentacleCount) {
            val tFrac = (i.toFloat() / (tentacleCount - 1) - 0.5f) * 2f
            val startX = headCenter.x + tFrac * r * 0.8f
            val startY = headCenter.y + r * 0.6f
            val wave = sin(age * 0.008f + i * 1.2f).toFloat() * r * 0.25f

            val tentaclePath = Path().apply {
                moveTo(startX, startY)
                quadraticTo(startX + wave, startY + r * 0.6f, startX + tFrac * r * 0.4f, startY + r * 1.1f)
            }
            drawScope.drawPath(
                path = tentaclePath,
                color = prim,
                style = Stroke(width = r * 0.25f, cap = StrokeCap.Round)
            )
            drawScope.drawPath(
                path = tentaclePath,
                color = OutlineColor,
                style = Stroke(width = r * 0.08f, cap = StrokeCap.Round)
            )
        }

        drawScope.drawCircle(color = prim, radius = r, center = headCenter)
        drawScope.drawCircle(color = OutlineColor, radius = r, center = headCenter, style = Stroke(width = r * 0.1f))

        drawCuteFace(drawScope, headCenter, r * 0.32f)
    }

    private fun drawJellyfish(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.48f
        val bellCenter = Offset(pos.x, pos.y - r * 0.3f)

        for (i in -2..2) {
            val startX = bellCenter.x + i * r * 0.35f
            val startY = bellCenter.y + r * 0.3f
            val wave = sin(age * 0.009f + i * 1.5f).toFloat() * r * 0.2f

            val tPath = Path().apply {
                moveTo(startX, startY)
                quadraticTo(startX + wave, startY + r * 0.7f, startX - wave * 0.5f, startY + r * 1.3f)
            }
            drawScope.drawPath(path = tPath, color = sec, style = Stroke(width = r * 0.12f, cap = StrokeCap.Round))
        }

        val bellPath = Path().apply {
            moveTo(bellCenter.x - r, bellCenter.y)
            quadraticTo(bellCenter.x - r, bellCenter.y - r * 1.1f, bellCenter.x, bellCenter.y - r * 1.1f)
            quadraticTo(bellCenter.x + r, bellCenter.y - r * 1.1f, bellCenter.x + r, bellCenter.y)
            quadraticTo(bellCenter.x, bellCenter.y + r * 0.2f, bellCenter.x - r, bellCenter.y)
        }
        drawScope.drawPath(path = bellPath, color = prim)
        drawScope.drawPath(path = bellPath, color = OutlineColor, style = Stroke(width = r * 0.1f))

        drawCuteFace(drawScope, Offset(bellCenter.x, bellCenter.y - r * 0.35f), r * 0.3f)
    }

    private fun drawClownfish(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.45f
        val tailWave = sin(age * 0.012f).toFloat() * r * 0.2f

        val tailPath = Path().apply {
            moveTo(pos.x - r * 0.8f, pos.y)
            lineTo(pos.x - r * 1.4f, pos.y - r * 0.5f + tailWave)
            lineTo(pos.x - r * 1.2f, pos.y + tailWave * 0.5f)
            lineTo(pos.x - r * 1.4f, pos.y + r * 0.5f + tailWave)
            close()
        }
        drawScope.drawPath(path = tailPath, color = prim)
        drawScope.drawPath(path = tailPath, color = OutlineColor, style = Stroke(width = r * 0.08f))

        drawScope.drawOval(
            color = prim,
            topLeft = Offset(pos.x - r * 0.9f, pos.y - r * 0.55f),
            size = Size(r * 1.8f, r * 1.1f)
        )
        drawScope.drawOval(
            color = OutlineColor,
            topLeft = Offset(pos.x - r * 0.9f, pos.y - r * 0.55f),
            size = Size(r * 1.8f, r * 1.1f),
            style = Stroke(width = r * 0.09f)
        )

        drawScope.drawRoundRect(
            color = WhiteColor,
            topLeft = Offset(pos.x - r * 0.1f, pos.y - r * 0.52f),
            size = Size(r * 0.28f, r * 1.04f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.1f)
        )

        drawCuteSingleEye(drawScope, Offset(pos.x + r * 0.45f, pos.y - r * 0.15f), r * 0.26f)
        drawSmile(drawScope, Offset(pos.x + r * 0.65f, pos.y + r * 0.12f), r * 0.18f)
    }

    private fun drawStarfish(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val outerR = size * 0.48f
        val innerR = outerR * 0.42f
        val path = Path()

        for (i in 0 until 10) {
            val angle = i * PI / 5 - PI / 2
            val currR = if (i % 2 == 0) outerR else innerR
            val x = pos.x + cos(angle).toFloat() * currR
            val y = pos.y + sin(angle).toFloat() * currR
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawScope.drawPath(path = path, color = prim)
        drawScope.drawPath(path = path, color = OutlineColor, style = Stroke(width = outerR * 0.1f, join = StrokeJoin.Round))

        drawCuteFace(drawScope, pos, outerR * 0.26f)
    }

    private fun drawLion(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.38f

        val manePetals = 12
        for (i in 0 until manePetals) {
            val angle = i * 2 * PI / manePetals
            val cx = pos.x + cos(angle).toFloat() * r * 0.95f
            val cy = pos.y + sin(angle).toFloat() * r * 0.95f
            drawScope.drawCircle(color = sec, radius = r * 0.42f, center = Offset(cx, cy))
        }

        drawScope.drawCircle(color = prim, radius = r, center = pos)
        drawScope.drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.1f))

        drawScope.drawCircle(color = prim, radius = r * 0.32f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = prim, radius = r * 0.32f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))

        drawCuteFace(drawScope, pos, r * 0.32f, isAnimal = true)
    }

    private fun drawBabyBear(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f

        drawScope.drawCircle(color = prim, radius = r * 0.35f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = CheekColor, radius = r * 0.20f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = OutlineColor, radius = r * 0.35f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f), style = Stroke(width = r * 0.09f))

        drawScope.drawCircle(color = prim, radius = r * 0.35f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = CheekColor, radius = r * 0.20f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))
        drawScope.drawCircle(color = OutlineColor, radius = r * 0.35f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f), style = Stroke(width = r * 0.09f))

        drawScope.drawCircle(color = prim, radius = r, center = pos)
        drawScope.drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.1f))

        drawScope.drawOval(
            color = WhiteColor,
            topLeft = Offset(pos.x - r * 0.4f, pos.y + r * 0.05f),
            size = Size(r * 0.8f, r * 0.55f)
        )
        drawCuteFace(drawScope, pos, r * 0.32f, isAnimal = true)
    }

    private fun drawElephant(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f

        drawScope.drawCircle(color = sec, radius = r * 0.65f, center = Offset(pos.x - r * 0.85f, pos.y))
        drawScope.drawCircle(color = CheekColor, radius = r * 0.35f, center = Offset(pos.x - r * 0.85f, pos.y))
        drawScope.drawCircle(color = OutlineColor, radius = r * 0.65f, center = Offset(pos.x - r * 0.85f, pos.y), style = Stroke(width = r * 0.09f))

        drawScope.drawCircle(color = sec, radius = r * 0.65f, center = Offset(pos.x + r * 0.85f, pos.y))
        drawScope.drawCircle(color = CheekColor, radius = r * 0.35f, center = Offset(pos.x + r * 0.85f, pos.y))
        drawScope.drawCircle(color = OutlineColor, radius = r * 0.65f, center = Offset(pos.x + r * 0.85f, pos.y), style = Stroke(width = r * 0.09f))

        drawScope.drawCircle(color = prim, radius = r, center = pos)
        drawScope.drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.1f))

        drawCuteEyesOnly(drawScope, Offset(pos.x, pos.y - r * 0.15f), r * 0.28f)

        val trunkPath = Path().apply {
            moveTo(pos.x, pos.y + r * 0.15f)
            quadraticTo(pos.x, pos.y + r * 0.9f, pos.x + r * 0.4f, pos.y + r * 0.75f)
            quadraticTo(pos.x + r * 0.5f, pos.y + r * 0.55f, pos.x + r * 0.3f, pos.y + r * 0.52f)
        }
        drawScope.drawPath(path = trunkPath, color = prim, style = Stroke(width = r * 0.28f, cap = StrokeCap.Round))
        drawScope.drawPath(path = trunkPath, color = OutlineColor, style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))

        drawScope.drawCircle(color = Color(0xFF00F5FF), radius = r * 0.12f, center = Offset(pos.x + r * 0.55f, pos.y + r * 0.35f))
    }

    private fun drawLittleBird(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.44f
        val wingWave = sin(age * 0.015f).toFloat() * r * 0.2f

        val tailPath = Path().apply {
            moveTo(pos.x - r * 0.8f, pos.y)
            lineTo(pos.x - r * 1.3f, pos.y - r * 0.3f)
            lineTo(pos.x - r * 1.1f, pos.y + r * 0.1f)
            close()
        }
        drawScope.drawPath(path = tailPath, color = sec)

        drawScope.drawCircle(color = prim, radius = r, center = pos)
        drawScope.drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        drawScope.drawOval(
            color = sec,
            topLeft = Offset(pos.x - r * 0.5f, pos.y - r * 0.1f + wingWave),
            size = Size(r * 0.7f, r * 0.45f)
        )

        drawCuteSingleEye(drawScope, Offset(pos.x + r * 0.35f, pos.y - r * 0.2f), r * 0.25f)

        val beakPath = Path().apply {
            moveTo(pos.x + r * 0.85f, pos.y - r * 0.15f)
            lineTo(pos.x + r * 1.35f, pos.y)
            lineTo(pos.x + r * 0.85f, pos.y + r * 0.12f)
            close()
        }
        drawScope.drawPath(path = beakPath, color = Color(0xFFFF9100))
        drawScope.drawPath(path = beakPath, color = OutlineColor, style = Stroke(width = r * 0.07f))
    }

    private fun drawPenguin(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.45f

        drawScope.drawOval(color = Color(0xFFFF9100), topLeft = Offset(pos.x - r * 0.6f, pos.y + r * 0.75f), size = Size(r * 0.5f, r * 0.28f))
        drawScope.drawOval(color = Color(0xFFFF9100), topLeft = Offset(pos.x + r * 0.1f, pos.y + r * 0.75f), size = Size(r * 0.5f, r * 0.28f))

        drawScope.drawOval(color = Color(0xFF1E1B2E), topLeft = Offset(pos.x - r * 0.75f, pos.y - r * 0.9f), size = Size(r * 1.5f, r * 1.8f))

        val flipperWave = sin(age * 0.012f).toFloat() * r * 0.15f
        drawScope.drawOval(color = Color(0xFF1E1B2E), topLeft = Offset(pos.x - r * 1.15f, pos.y - r * 0.2f + flipperWave), size = Size(r * 0.4f, r * 0.8f))
        drawScope.drawOval(color = Color(0xFF1E1B2E), topLeft = Offset(pos.x + r * 0.75f, pos.y - r * 0.2f - flipperWave), size = Size(r * 0.4f, r * 0.8f))

        drawScope.drawOval(color = WhiteColor, topLeft = Offset(pos.x - r * 0.52f, pos.y - r * 0.65f), size = Size(r * 1.04f, r * 1.45f))

        drawCuteEyesOnly(drawScope, Offset(pos.x, pos.y - r * 0.35f), r * 0.24f)
        val beak = Path().apply {
            moveTo(pos.x - r * 0.18f, pos.y - r * 0.18f)
            lineTo(pos.x + r * 0.18f, pos.y - r * 0.18f)
            lineTo(pos.x, pos.y - r * 0.02f)
            close()
        }
        drawScope.drawPath(path = beak, color = Color(0xFFFF9100))
    }

    private fun drawButterfly(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.45f
        val flap = (sin(age * 0.016f).toFloat() * 0.35f + 0.65f)

        drawScope.drawOval(
            color = prim,
            topLeft = Offset(pos.x - r * 1.1f * flap, pos.y - r * 0.95f),
            size = Size(r * 1.05f * flap, r * 0.95f)
        )
        drawScope.drawOval(
            color = prim,
            topLeft = Offset(pos.x + r * 0.05f, pos.y - r * 0.95f),
            size = Size(r * 1.05f * flap, r * 0.95f)
        )

        drawScope.drawOval(
            color = sec,
            topLeft = Offset(pos.x - r * 0.85f * flap, pos.y - r * 0.1f),
            size = Size(r * 0.8f * flap, r * 0.8f)
        )
        drawScope.drawOval(
            color = sec,
            topLeft = Offset(pos.x + r * 0.05f, pos.y - r * 0.1f),
            size = Size(r * 0.8f * flap, r * 0.8f)
        )

        drawScope.drawCircle(color = WhiteColor, radius = r * 0.22f * flap, center = Offset(pos.x - r * 0.55f * flap, pos.y - r * 0.5f))
        drawScope.drawCircle(color = WhiteColor, radius = r * 0.22f * flap, center = Offset(pos.x + r * 0.55f * flap, pos.y - r * 0.5f))

        drawScope.drawOval(color = OutlineColor, topLeft = Offset(pos.x - r * 0.12f, pos.y - r * 0.7f), size = Size(r * 0.24f, r * 1.3f))

        val antLeft = Path().apply {
            moveTo(pos.x - r * 0.05f, pos.y - r * 0.65f)
            quadraticTo(pos.x - r * 0.4f, pos.y - r * 1.1f, pos.x - r * 0.35f, pos.y - r * 1.25f)
        }
        val antRight = Path().apply {
            moveTo(pos.x + r * 0.05f, pos.y - r * 0.65f)
            quadraticTo(pos.x + r * 0.4f, pos.y - r * 1.1f, pos.x + r * 0.35f, pos.y - r * 1.25f)
        }
        drawScope.drawPath(path = antLeft, color = OutlineColor, style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))
        drawScope.drawPath(path = antRight, color = OutlineColor, style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))

        drawScope.drawCircle(color = Color(0xFFFFEA00), radius = r * 0.1f, center = Offset(pos.x - r * 0.35f, pos.y - r * 1.25f))
        drawScope.drawCircle(color = Color(0xFFFFEA00), radius = r * 0.1f, center = Offset(pos.x + r * 0.35f, pos.y - r * 1.25f))
    }

    private fun drawBloomingFlower(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f
        val petalCount = 8

        for (i in 0 until petalCount) {
            val angle = i * 2 * PI / petalCount
            val px = pos.x + cos(angle).toFloat() * r * 0.72f
            val py = pos.y + sin(angle).toFloat() * r * 0.72f
            drawScope.drawCircle(color = prim, radius = r * 0.48f, center = Offset(px, py))
            drawScope.drawCircle(color = OutlineColor, radius = r * 0.48f, center = Offset(px, py), style = Stroke(width = r * 0.08f))
        }

        drawScope.drawCircle(color = sec, radius = r * 0.62f, center = pos)
        drawScope.drawCircle(color = OutlineColor, radius = r * 0.62f, center = pos, style = Stroke(width = r * 0.09f))

        drawCuteFace(drawScope, pos, r * 0.28f)
    }

    private fun drawSunflower(drawScope: DrawScope, pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.45f
        val petalCount = 14

        for (i in 0 until petalCount) {
            val angle = i * 2 * PI / petalCount
            val px = pos.x + cos(angle).toFloat() * r * 0.8f
            val py = pos.y + sin(angle).toFloat() * r * 0.8f
            drawScope.drawCircle(color = Color(0xFFFFEA00), radius = r * 0.34f, center = Offset(px, py))
        }

        drawScope.drawCircle(color = Color(0xFFFF9100), radius = r * 0.65f, center = pos)
        drawScope.drawCircle(color = OutlineColor, radius = r * 0.65f, center = pos, style = Stroke(width = r * 0.09f))

        drawCuteFace(drawScope, pos, r * 0.30f)
    }

    private fun drawCuteFace(drawScope: DrawScope, center: Offset, eyeRadius: Float, isAnimal: Boolean = false) {
        drawCuteEyesOnly(drawScope, Offset(center.x, center.y - eyeRadius * 0.4f), eyeRadius)

        val mouthY = center.y + eyeRadius * 0.7f
        if (isAnimal) {
            drawScope.drawCircle(color = NoseColor, radius = eyeRadius * 0.32f, center = Offset(center.x, center.y + eyeRadius * 0.15f))
        }
        drawSmile(drawScope, Offset(center.x, mouthY), eyeRadius * 0.8f)

        val cheekDist = eyeRadius * 1.5f
        drawScope.drawCircle(color = CheekColor, radius = eyeRadius * 0.38f, center = Offset(center.x - cheekDist, mouthY - eyeRadius * 0.2f))
        drawScope.drawCircle(color = CheekColor, radius = eyeRadius * 0.38f, center = Offset(center.x + cheekDist, mouthY - eyeRadius * 0.2f))
    }

    private fun drawCuteEyesOnly(drawScope: DrawScope, center: Offset, radius: Float) {
        val dist = radius * 1.15f
        drawCuteSingleEye(drawScope, Offset(center.x - dist, center.y), radius)
        drawCuteSingleEye(drawScope, Offset(center.x + dist, center.y), radius)
    }

    private fun drawCuteSingleEye(drawScope: DrawScope, center: Offset, radius: Float) {
        drawScope.drawCircle(color = WhiteColor, radius = radius, center = center)
        drawScope.drawCircle(color = OutlineColor, radius = radius, center = center, style = Stroke(width = radius * 0.15f))
        val pupilCenter = Offset(center.x + radius * 0.2f, center.y - radius * 0.1f)
        drawScope.drawCircle(color = OutlineColor, radius = radius * 0.52f, center = pupilCenter)
        drawScope.drawCircle(color = WhiteColor, radius = radius * 0.22f, center = Offset(pupilCenter.x + radius * 0.15f, pupilCenter.y - radius * 0.15f))
    }

    private fun drawSmile(drawScope: DrawScope, center: Offset, width: Float) {
        val mouth = Path().apply {
            moveTo(center.x - width * 0.6f, center.y)
            quadraticTo(center.x, center.y + width * 0.5f, center.x + width * 0.6f, center.y)
        }
        drawScope.drawPath(path = mouth, color = OutlineColor, style = Stroke(width = (width * 0.18f).coerceIn(2.5f, 5.5f), cap = StrokeCap.Round))
    }
}
