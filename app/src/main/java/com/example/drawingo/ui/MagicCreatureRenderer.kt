package com.example.drawingo.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.drawingo.model.CreatureType
import com.example.drawingo.model.MagicCompanion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure Compose Canvas renderer for magical companion sticker stamps:
 * - Celestial Objects (Smiling Sun, Crescent Moon, Twinkle Star, Planet Saturn, Cosmic Rocket, Shooting Comet)
 * - Sea Animals (Octopus, Jellyfish, Clownfish, Starfish, Baby Whale, Sea Turtle)
 * - Wild Animals (Lion, Baby Bear, Elephant, Playful Monkey, Cute Panda, Giraffe)
 * - Birds & Friends (Little Bird, Penguin, Butterfly, Flowers)
 */
object MagicCreatureRenderer {

    private val OutlineColor = Color(0xFF374151)
    private val WhiteColor = Color(0xFFFFFFFF)
    private val CheekColor = Color(0xE6FF6B9E)
    private val NoseColor = Color(0xFF1F2937)

    fun drawCompanion(
        drawScope: DrawScope,
        companion: MagicCompanion,
        currentTimeMs: Long
    ) {
        val age = (currentTimeMs - companion.spawnTimestamp).coerceAtLeast(0L)
        val entranceScale = if (age < 380L) {
            val t = age / 380f
            1.0f + (sin(t * PI).toFloat() * 0.28f) * (1f - t)
        } else {
            1.0f
        }

        val baseSize = companion.size * entranceScale
        if (baseSize <= 2f) return

        when (companion.type) {
            // Celestial Objects
            CreatureType.SMILING_SUN -> drawScope.drawSmilingSun(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.CRESCENT_MOON -> drawScope.drawCrescentMoon(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.TWINKLE_STAR -> drawScope.drawTwinkleStar(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.PLANET_SATURN -> drawScope.drawPlanetSaturn(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.COSMIC_ROCKET -> drawScope.drawCosmicRocket(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.SHOOTING_COMET -> drawScope.drawShootingComet(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)

            // Sea Animals
            CreatureType.OCTOPUS -> drawScope.drawOctopus(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.JELLYFISH -> drawScope.drawJellyfish(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.CLOWN_FISH -> drawScope.drawClownfish(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.STARFISH -> drawScope.drawStarfish(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BABY_WHALE -> drawScope.drawBabyWhale(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.SEA_TURTLE -> drawScope.drawSeaTurtle(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)

            // Wild Animals
            CreatureType.LION -> drawScope.drawLion(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BABY_BEAR -> drawScope.drawBabyBear(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.ELEPHANT -> drawScope.drawElephant(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.PLAYFUL_MONKEY -> drawScope.drawPlayfulMonkey(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.CUTE_PANDA -> drawScope.drawCutePanda(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.GIRAFFE -> drawScope.drawGiraffe(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)

            // Birds & Classic Friends
            CreatureType.LITTLE_BIRD -> drawScope.drawLittleBird(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.PENGUIN -> drawScope.drawPenguin(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BUTTERFLY -> drawScope.drawButterfly(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.BLOOMING_FLOWER -> drawScope.drawBloomingFlower(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
            CreatureType.SUNFLOWER -> drawScope.drawSunflower(companion.position, baseSize, companion.primaryColor, companion.secondaryColor, age)
        }
    }

    // ==========================================
    // ☀️ CELESTIAL OBJECTS
    // ==========================================

    private fun DrawScope.drawSmilingSun(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.36f
        val rayCount = 10
        val rayPulse = (sin(age * 0.006f).toFloat() * 0.08f) + 1.0f

        // Radiant Sunny Rays
        for (i in 0 until rayCount) {
            val angle = i * 2 * PI / rayCount + (age * 0.0008f)
            val rayBaseDist = r * 1.05f
            val rayTipDist = r * (1.32f * rayPulse)
            val perpAngle = angle + PI / 2

            val baseHalfW = r * 0.22f
            val bx = pos.x + cos(angle).toFloat() * rayBaseDist
            val by = pos.y + sin(angle).toFloat() * rayBaseDist

            val tipX = pos.x + cos(angle).toFloat() * rayTipDist
            val tipY = pos.y + sin(angle).toFloat() * rayTipDist

            val p1X = bx + cos(perpAngle).toFloat() * baseHalfW
            val p1Y = by + sin(perpAngle).toFloat() * baseHalfW
            val p2X = bx - cos(perpAngle).toFloat() * baseHalfW
            val p2Y = by - sin(perpAngle).toFloat() * baseHalfW

            val rayPath = Path().apply {
                moveTo(p1X, p1Y)
                lineTo(tipX, tipY)
                lineTo(p2X, p2Y)
                close()
            }
            drawPath(path = rayPath, color = sec)
            drawPath(path = rayPath, color = OutlineColor, style = Stroke(width = r * 0.08f, join = StrokeJoin.Round))
        }

        // Sun Face Sphere
        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        drawCuteFace(pos, r * 0.30f)
    }

    private fun DrawScope.drawCrescentMoon(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.44f
        val floatOffset = sin(age * 0.005f).toFloat() * r * 0.08f
        val center = Offset(pos.x, pos.y + floatOffset)

        val moonPath = Path().apply {
            moveTo(center.x, center.y - r)
            cubicTo(
                center.x + r * 1.25f, center.y - r * 0.6f,
                center.x + r * 1.25f, center.y + r * 0.6f,
                center.x, center.y + r
            )
            cubicTo(
                center.x + r * 0.45f, center.y + r * 0.45f,
                center.x + r * 0.45f, center.y - r * 0.45f,
                center.x, center.y - r
            )
            close()
        }

        drawPath(path = moonPath, color = prim)
        drawPath(path = moonPath, color = OutlineColor, style = Stroke(width = r * 0.09f, join = StrokeJoin.Round))

        // Cute Smiling Eye & Cheek on the Crescent
        val faceCenter = Offset(center.x + r * 0.52f, center.y)
        drawCuteSingleEye(Offset(faceCenter.x, faceCenter.y - r * 0.15f), r * 0.22f)
        drawSmile(Offset(faceCenter.x - r * 0.05f, faceCenter.y + r * 0.2f), r * 0.22f)
        drawCircle(color = CheekColor, radius = r * 0.15f, center = Offset(faceCenter.x + r * 0.12f, faceCenter.y + r * 0.1f))

        // Twinkling companion stars
        drawSparkleStar(Offset(center.x - r * 0.45f, center.y - r * 0.35f), r * 0.26f, sec, age)
        drawSparkleStar(Offset(center.x - r * 0.35f, center.y + r * 0.45f), r * 0.20f, sec, age + 300L)
    }

    private fun DrawScope.drawTwinkleStar(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val outerR = size * 0.48f
        val innerR = outerR * 0.44f
        val starPulse = 1.0f + (sin(age * 0.007f).toFloat() * 0.06f)
        val path = Path()

        for (i in 0 until 10) {
            val angle = i * PI / 5 - PI / 2
            val currR = (if (i % 2 == 0) outerR else innerR) * starPulse
            val x = pos.x + cos(angle).toFloat() * currR
            val y = pos.y + sin(angle).toFloat() * currR
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(path = path, color = prim)
        drawPath(path = path, color = OutlineColor, style = Stroke(width = outerR * 0.09f, join = StrokeJoin.Round))

        drawCuteFace(pos, outerR * 0.26f)

        // Little sparkle crosses around star tips
        drawSparkleStar(Offset(pos.x + outerR * 0.7f, pos.y - outerR * 0.7f), outerR * 0.22f, sec, age)
        drawSparkleStar(Offset(pos.x - outerR * 0.7f, pos.y + outerR * 0.7f), outerR * 0.18f, sec, age + 400L)
    }

    private fun DrawScope.drawPlanetSaturn(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.32f
        val floatOffset = sin(age * 0.005f).toFloat() * r * 0.1f
        val center = Offset(pos.x, pos.y + floatOffset)

        // Outer Ring - Back Segment
        withTransform({
            rotate(degrees = -24f, pivot = center)
        }) {
            drawOval(
                color = sec,
                topLeft = Offset(center.x - r * 1.6f, center.y - r * 0.45f),
                size = Size(r * 3.2f, r * 0.9f)
            )
            drawOval(
                color = OutlineColor,
                topLeft = Offset(center.x - r * 1.6f, center.y - r * 0.45f),
                size = Size(r * 3.2f, r * 0.9f),
                style = Stroke(width = r * 0.1f)
            )
        }

        // Planet Sphere
        drawCircle(color = prim, radius = r, center = center)
        drawCircle(color = OutlineColor, radius = r, center = center, style = Stroke(width = r * 0.09f))

        // Cute Face
        drawCuteFace(center, r * 0.28f)

        // Front half of the ring
        withTransform({
            rotate(degrees = -24f, pivot = center)
            clipRect(left = center.x - r * 1.8f, top = center.y, right = center.x + r * 1.8f, bottom = center.y + r * 0.9f)
        }) {
            drawOval(
                color = sec,
                topLeft = Offset(center.x - r * 1.6f, center.y - r * 0.45f),
                size = Size(r * 3.2f, r * 0.9f)
            )
            drawOval(
                color = OutlineColor,
                topLeft = Offset(center.x - r * 1.6f, center.y - r * 0.45f),
                size = Size(r * 3.2f, r * 0.9f),
                style = Stroke(width = r * 0.1f)
            )
        }
    }

    private fun DrawScope.drawCosmicRocket(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val w = size * 0.36f
        val h = size * 0.72f
        val shake = sin(age * 0.03f).toFloat() * 2f

        withTransform({
            rotate(degrees = -32f, pivot = pos)
            translate(left = 0f, top = shake)
        }) {
            // Booster Exhaust Flame
            val flameH = h * (0.35f + sin(age * 0.02f).toFloat() * 0.08f)
            val flamePath = Path().apply {
                moveTo(pos.x - w * 0.25f, pos.y + h * 0.38f)
                lineTo(pos.x, pos.y + h * 0.38f + flameH)
                lineTo(pos.x + w * 0.25f, pos.y + h * 0.38f)
                close()
            }
            drawPath(path = flamePath, color = Color(0xFFFF5722))
            drawPath(path = flamePath, color = Color(0xFFFFD600), style = Stroke(width = w * 0.1f, join = StrokeJoin.Round))

            // Side Fins
            val finLeft = Path().apply {
                moveTo(pos.x - w * 0.35f, pos.y + h * 0.1f)
                lineTo(pos.x - w * 0.75f, pos.y + h * 0.38f)
                lineTo(pos.x - w * 0.25f, pos.y + h * 0.35f)
                close()
            }
            val finRight = Path().apply {
                moveTo(pos.x + w * 0.35f, pos.y + h * 0.1f)
                lineTo(pos.x + w * 0.75f, pos.y + h * 0.38f)
                lineTo(pos.x + w * 0.25f, pos.y + h * 0.35f)
                close()
            }
            drawPath(path = finLeft, color = sec)
            drawPath(path = finLeft, color = OutlineColor, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))
            drawPath(path = finRight, color = sec)
            drawPath(path = finRight, color = OutlineColor, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))

            // Fuselage Body
            val bodyPath = Path().apply {
                moveTo(pos.x, pos.y - h * 0.45f)
                cubicTo(pos.x + w * 0.55f, pos.y - h * 0.1f, pos.x + w * 0.45f, pos.y + h * 0.35f, pos.x, pos.y + h * 0.35f)
                cubicTo(pos.x - w * 0.45f, pos.y + h * 0.35f, pos.x - w * 0.55f, pos.y - h * 0.1f, pos.x, pos.y - h * 0.45f)
                close()
            }
            drawPath(path = bodyPath, color = prim)
            drawPath(path = bodyPath, color = OutlineColor, style = Stroke(width = w * 0.09f, join = StrokeJoin.Round))

            // Nose Cone Cap
            val nosePath = Path().apply {
                moveTo(pos.x, pos.y - h * 0.45f)
                lineTo(pos.x + w * 0.32f, pos.y - h * 0.15f)
                lineTo(pos.x - w * 0.32f, pos.y - h * 0.15f)
                close()
            }
            drawPath(path = nosePath, color = sec)

            // Porthole Window with Cute Peeking Eyes
            val windowCenter = Offset(pos.x, pos.y + h * 0.02f)
            val windowR = w * 0.26f
            drawCircle(color = WhiteColor, radius = windowR, center = windowCenter)
            drawCircle(color = OutlineColor, radius = windowR, center = windowCenter, style = Stroke(width = w * 0.08f))
            drawCuteFace(windowCenter, windowR * 0.45f)
        }
    }

    private fun DrawScope.drawShootingComet(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.28f

        withTransform({
            rotate(degrees = -25f, pivot = pos)
        }) {
            // Comet Rainbow Tail Streaks
            val tailW = size * 0.8f
            for (i in -1..1) {
                val trailY = pos.y + i * r * 0.5f
                val tailPath = Path().apply {
                    moveTo(pos.x - r * 0.3f, trailY)
                    quadraticTo(pos.x - tailW * 0.5f, trailY + i * r * 0.3f, pos.x - tailW, trailY + i * r * 0.6f)
                }
                drawPath(
                    path = tailPath,
                    color = if (i == 0) prim else sec,
                    style = Stroke(width = r * 0.35f, cap = StrokeCap.Round)
                )
            }

            // Glowing Star Comet Head
            drawCircle(color = Color(0xFFFFD600), radius = r, center = pos)
            drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.1f))

            drawCuteFace(pos, r * 0.32f)
        }
    }

    // ==========================================
    // 🌊 SEA ANIMALS
    // ==========================================

    private fun DrawScope.drawOctopus(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f
        val headCenter = Offset(pos.x, pos.y - r * 0.25f)

        val tentacleCount = 6
        for (i in 0 until tentacleCount) {
            val tFrac = (i.toFloat() / (tentacleCount - 1) - 0.5f) * 2f
            val startX = headCenter.x + tFrac * r * 0.8f
            val startY = headCenter.y + r * 0.6f
            val wave = sin(age * 0.008f + i * 1.2f).toFloat() * r * 0.25f

            val tentaclePath = Path().apply {
                moveTo(startX, startY)
                quadraticTo(startX + wave, startY + r * 0.6f, startX + tFrac * r * 0.4f, startY + r * 1.15f)
            }
            drawPath(path = tentaclePath, color = prim, style = Stroke(width = r * 0.25f, cap = StrokeCap.Round))
            drawPath(path = tentaclePath, color = OutlineColor, style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))
        }

        drawCircle(color = prim, radius = r, center = headCenter)
        drawCircle(color = OutlineColor, radius = r, center = headCenter, style = Stroke(width = r * 0.09f))

        drawCuteFace(headCenter, r * 0.30f)
    }

    private fun DrawScope.drawJellyfish(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.46f
        val bellCenter = Offset(pos.x, pos.y - r * 0.3f)

        for (i in -2..2) {
            val startX = bellCenter.x + i * r * 0.35f
            val startY = bellCenter.y + r * 0.3f
            val wave = sin(age * 0.009f + i * 1.5f).toFloat() * r * 0.2f

            val tPath = Path().apply {
                moveTo(startX, startY)
                quadraticTo(startX + wave, startY + r * 0.7f, startX - wave * 0.5f, startY + r * 1.35f)
            }
            drawPath(path = tPath, color = sec, style = Stroke(width = r * 0.12f, cap = StrokeCap.Round))
        }

        val bellPath = Path().apply {
            moveTo(bellCenter.x - r, bellCenter.y)
            quadraticTo(bellCenter.x - r, bellCenter.y - r * 1.1f, bellCenter.x, bellCenter.y - r * 1.1f)
            quadraticTo(bellCenter.x + r, bellCenter.y - r * 1.1f, bellCenter.x + r, bellCenter.y)
            quadraticTo(bellCenter.x, bellCenter.y + r * 0.2f, bellCenter.x - r, bellCenter.y)
        }
        drawPath(path = bellPath, color = prim)
        drawPath(path = bellPath, color = OutlineColor, style = Stroke(width = r * 0.09f))

        drawCuteFace(Offset(bellCenter.x, bellCenter.y - r * 0.35f), r * 0.28f)
    }

    private fun DrawScope.drawClownfish(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.44f
        val tailWave = sin(age * 0.012f).toFloat() * r * 0.2f

        val tailPath = Path().apply {
            moveTo(pos.x - r * 0.8f, pos.y)
            lineTo(pos.x - r * 1.4f, pos.y - r * 0.5f + tailWave)
            lineTo(pos.x - r * 1.2f, pos.y + tailWave * 0.5f)
            lineTo(pos.x - r * 1.4f, pos.y + r * 0.5f + tailWave)
            close()
        }
        drawPath(path = tailPath, color = prim)
        drawPath(path = tailPath, color = OutlineColor, style = Stroke(width = r * 0.08f))

        drawOval(color = prim, topLeft = Offset(pos.x - r * 0.9f, pos.y - r * 0.55f), size = Size(r * 1.8f, r * 1.1f))
        drawOval(color = OutlineColor, topLeft = Offset(pos.x - r * 0.9f, pos.y - r * 0.55f), size = Size(r * 1.8f, r * 1.1f), style = Stroke(width = r * 0.09f))

        // White Clownfish Stripe
        drawRoundRect(
            color = WhiteColor,
            topLeft = Offset(pos.x - r * 0.1f, pos.y - r * 0.52f),
            size = Size(r * 0.30f, r * 1.04f),
            cornerRadius = CornerRadius(r * 0.1f)
        )

        drawCuteSingleEye(Offset(pos.x + r * 0.45f, pos.y - r * 0.15f), r * 0.26f)
        drawSmile(Offset(pos.x + r * 0.65f, pos.y + r * 0.12f), r * 0.20f)
    }

    private fun DrawScope.drawStarfish(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val outerR = size * 0.46f
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

        drawPath(path = path, color = prim)
        drawPath(path = path, color = OutlineColor, style = Stroke(width = outerR * 0.09f, join = StrokeJoin.Round))

        drawCuteFace(pos, outerR * 0.26f)
    }

    private fun DrawScope.drawBabyWhale(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f
        val tailWiggle = sin(age * 0.01f).toFloat() * r * 0.18f

        // Water Spout on Top
        val spoutBase = Offset(pos.x - r * 0.1f, pos.y - r * 0.7f)
        for (i in -1..1) {
            val spoutDrop = Offset(spoutBase.x + i * r * 0.35f, spoutBase.y - r * 0.45f + (i * i * r * 0.1f))
            drawCircle(color = Color(0xFF00E5FF), radius = r * 0.14f, center = spoutDrop)
        }

        // Tail Fin
        val tailPath = Path().apply {
            moveTo(pos.x - r * 0.9f, pos.y)
            lineTo(pos.x - r * 1.5f, pos.y - r * 0.5f + tailWiggle)
            lineTo(pos.x - r * 1.3f, pos.y + tailWiggle * 0.5f)
            lineTo(pos.x - r * 1.5f, pos.y + r * 0.5f + tailWiggle)
            close()
        }
        drawPath(path = tailPath, color = prim)
        drawPath(path = tailPath, color = OutlineColor, style = Stroke(width = r * 0.08f, join = StrokeJoin.Round))

        // Chubby Whale Body
        drawOval(
            color = prim,
            topLeft = Offset(pos.x - r * 1.0f, pos.y - r * 0.65f),
            size = Size(r * 2.1f, r * 1.3f)
        )
        drawOval(
            color = OutlineColor,
            topLeft = Offset(pos.x - r * 1.0f, pos.y - r * 0.65f),
            size = Size(r * 2.1f, r * 1.3f),
            style = Stroke(width = r * 0.09f)
        )

        // Cute Whale Eye & Smile
        drawCuteSingleEye(Offset(pos.x + r * 0.55f, pos.y - r * 0.15f), r * 0.24f)
        drawSmile(Offset(pos.x + r * 0.7f, pos.y + r * 0.15f), r * 0.22f)
        drawCircle(color = CheekColor, radius = r * 0.16f, center = Offset(pos.x + r * 0.35f, pos.y + r * 0.1f))
    }

    private fun DrawScope.drawSeaTurtle(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.38f
        val flipperPaddle = sin(age * 0.008f).toFloat() * r * 0.15f

        // 4 Flippers
        drawOval(color = sec, topLeft = Offset(pos.x - r * 1.1f, pos.y - r * 0.9f + flipperPaddle), size = Size(r * 0.7f, r * 0.45f))
        drawOval(color = sec, topLeft = Offset(pos.x + r * 0.4f, pos.y - r * 0.9f - flipperPaddle), size = Size(r * 0.7f, r * 0.45f))
        drawOval(color = sec, topLeft = Offset(pos.x - r * 0.9f, pos.y + r * 0.5f - flipperPaddle), size = Size(r * 0.55f, r * 0.35f))
        drawOval(color = sec, topLeft = Offset(pos.x + r * 0.35f, pos.y + r * 0.5f + flipperPaddle), size = Size(r * 0.55f, r * 0.35f))

        // Turtle Head
        val headCenter = Offset(pos.x, pos.y - r * 0.95f)
        drawCircle(color = sec, radius = r * 0.38f, center = headCenter)
        drawCircle(color = OutlineColor, radius = r * 0.38f, center = headCenter, style = Stroke(width = r * 0.08f))
        drawCuteFace(headCenter, r * 0.22f)

        // Shell
        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        // Shell Center Hexagon / Pattern
        drawCircle(color = sec, radius = r * 0.52f, center = pos)
        drawCircle(color = OutlineColor, radius = r * 0.52f, center = pos, style = Stroke(width = r * 0.07f))
    }

    // ==========================================
    // 🦁 WILD ANIMALS
    // ==========================================

    private fun DrawScope.drawLion(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.36f

        val manePetals = 12
        for (i in 0 until manePetals) {
            val angle = i * 2 * PI / manePetals
            val cx = pos.x + cos(angle).toFloat() * r * 0.95f
            val cy = pos.y + sin(angle).toFloat() * r * 0.95f
            drawCircle(color = sec, radius = r * 0.42f, center = Offset(cx, cy))
        }

        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        // Round Ears
        drawCircle(color = prim, radius = r * 0.32f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = prim, radius = r * 0.32f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))

        drawCuteFace(pos, r * 0.28f, isAnimal = true)
    }

    private fun DrawScope.drawBabyBear(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.40f

        // Ears
        drawCircle(color = prim, radius = r * 0.36f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = sec, radius = r * 0.22f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = OutlineColor, radius = r * 0.36f, center = Offset(pos.x - r * 0.75f, pos.y - r * 0.75f), style = Stroke(width = r * 0.08f))

        drawCircle(color = prim, radius = r * 0.36f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = sec, radius = r * 0.22f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f))
        drawCircle(color = OutlineColor, radius = r * 0.36f, center = Offset(pos.x + r * 0.75f, pos.y - r * 0.75f), style = Stroke(width = r * 0.08f))

        // Head
        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        // Muzzle
        val muzzleCenter = Offset(pos.x, pos.y + r * 0.25f)
        drawOval(color = sec, topLeft = Offset(muzzleCenter.x - r * 0.48f, muzzleCenter.y - r * 0.35f), size = Size(r * 0.96f, r * 0.7f))

        drawCuteFace(pos, r * 0.26f, isAnimal = true)
    }

    private fun DrawScope.drawElephant(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.38f

        // Big Floppy Ears
        val earW = r * 0.85f
        val earH = r * 1.15f
        drawOval(color = sec, topLeft = Offset(pos.x - r * 1.45f, pos.y - r * 0.6f), size = Size(earW, earH))
        drawOval(color = OutlineColor, topLeft = Offset(pos.x - r * 1.45f, pos.y - r * 0.6f), size = Size(earW, earH), style = Stroke(width = r * 0.08f))

        drawOval(color = sec, topLeft = Offset(pos.x + r * 0.6f, pos.y - r * 0.6f), size = Size(earW, earH))
        drawOval(color = OutlineColor, topLeft = Offset(pos.x + r * 0.6f, pos.y - r * 0.6f), size = Size(earW, earH), style = Stroke(width = r * 0.08f))

        // Head
        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        // Curved Elephant Trunk
        val trunkWave = sin(age * 0.008f).toFloat() * r * 0.15f
        val trunkPath = Path().apply {
            moveTo(pos.x, pos.y + r * 0.2f)
            cubicTo(
                pos.x, pos.y + r * 0.7f,
                pos.x + r * 0.5f + trunkWave, pos.y + r * 1.1f,
                pos.x + r * 0.7f + trunkWave, pos.y + r * 0.9f
            )
        }
        drawPath(path = trunkPath, color = prim, style = Stroke(width = r * 0.32f, cap = StrokeCap.Round))
        drawPath(path = trunkPath, color = OutlineColor, style = Stroke(width = r * 0.08f, cap = StrokeCap.Round))

        drawCuteEyesOnly(Offset(pos.x, pos.y - r * 0.25f), r * 0.24f)
        drawCircle(color = CheekColor, radius = r * 0.16f, center = Offset(pos.x - r * 0.55f, pos.y + r * 0.15f))
        drawCircle(color = CheekColor, radius = r * 0.16f, center = Offset(pos.x + r * 0.55f, pos.y + r * 0.15f))
    }

    private fun DrawScope.drawPlayfulMonkey(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.40f

        // Big Monkey Ears
        drawCircle(color = prim, radius = r * 0.38f, center = Offset(pos.x - r * 0.9f, pos.y))
        drawCircle(color = sec, radius = r * 0.22f, center = Offset(pos.x - r * 0.9f, pos.y))
        drawCircle(color = OutlineColor, radius = r * 0.38f, center = Offset(pos.x - r * 0.9f, pos.y), style = Stroke(width = r * 0.08f))

        drawCircle(color = prim, radius = r * 0.38f, center = Offset(pos.x + r * 0.9f, pos.y))
        drawCircle(color = sec, radius = r * 0.22f, center = Offset(pos.x + r * 0.9f, pos.y))
        drawCircle(color = OutlineColor, radius = r * 0.38f, center = Offset(pos.x + r * 0.9f, pos.y), style = Stroke(width = r * 0.08f))

        // Head Base
        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        // Light Mask (Heart / Peaches shape)
        drawOval(color = sec, topLeft = Offset(pos.x - r * 0.65f, pos.y - r * 0.45f), size = Size(r * 1.3f, r * 1.05f))

        drawCuteFace(pos, r * 0.26f, isAnimal = true)
    }

    private fun DrawScope.drawCutePanda(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.40f

        // Black Panda Ears
        drawCircle(color = Color(0xFF1E1B2E), radius = r * 0.34f, center = Offset(pos.x - r * 0.72f, pos.y - r * 0.72f))
        drawCircle(color = Color(0xFF1E1B2E), radius = r * 0.34f, center = Offset(pos.x + r * 0.72f, pos.y - r * 0.72f))

        // White Head
        drawCircle(color = WhiteColor, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        // Characteristic Angled Black Eye Patches
        withTransform({ rotate(degrees = -18f, pivot = Offset(pos.x - r * 0.4f, pos.y - r * 0.12f)) }) {
            drawOval(color = Color(0xFF1E1B2E), topLeft = Offset(pos.x - r * 0.6f, pos.y - r * 0.32f), size = Size(r * 0.45f, r * 0.55f))
        }
        withTransform({ rotate(degrees = 18f, pivot = Offset(pos.x + r * 0.4f, pos.y - r * 0.12f)) }) {
            drawOval(color = Color(0xFF1E1B2E), topLeft = Offset(pos.x + r * 0.15f, pos.y - r * 0.32f), size = Size(r * 0.45f, r * 0.55f))
        }

        // Twinkling White Pupils inside Black Patches
        drawCuteSingleEye(Offset(pos.x - r * 0.38f, pos.y - r * 0.12f), r * 0.16f)
        drawCuteSingleEye(Offset(pos.x + r * 0.38f, pos.y - r * 0.12f), r * 0.16f)

        // Cute Panda Nose & Smile
        drawCircle(color = NoseColor, radius = r * 0.11f, center = Offset(pos.x, pos.y + r * 0.15f))
        drawSmile(Offset(pos.x, pos.y + r * 0.35f), r * 0.28f)

        // Pink Cheeks
        drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x - r * 0.55f, pos.y + r * 0.28f))
        drawCircle(color = CheekColor, radius = r * 0.55f.coerceAtMost(r * 0.18f), center = Offset(pos.x + r * 0.55f, pos.y + r * 0.28f))
    }

    private fun DrawScope.drawGiraffe(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.38f

        // Cute Ossicones (Horns with knobs)
        for (sign in listOf(-1, 1)) {
            val hornBase = Offset(pos.x + sign * r * 0.32f, pos.y - r * 0.85f)
            val hornTip = Offset(pos.x + sign * r * 0.42f, pos.y - r * 1.35f)
            drawLine(color = OutlineColor, start = hornBase, end = hornTip, strokeWidth = r * 0.1f, cap = StrokeCap.Round)
            drawCircle(color = sec, radius = r * 0.16f, center = hornTip)
            drawCircle(color = OutlineColor, radius = r * 0.16f, center = hornTip, style = Stroke(width = r * 0.06f))
        }

        // Cute Side Ears
        drawOval(color = prim, topLeft = Offset(pos.x - r * 1.35f, pos.y - r * 0.75f), size = Size(r * 0.65f, r * 0.35f))
        drawOval(color = prim, topLeft = Offset(pos.x + r * 0.7f, pos.y - r * 0.75f), size = Size(r * 0.65f, r * 0.35f))

        // Giraffe Face
        drawOval(color = prim, topLeft = Offset(pos.x - r * 0.85f, pos.y - r * 0.95f), size = Size(r * 1.7f, r * 1.9f))
        drawOval(color = OutlineColor, topLeft = Offset(pos.x - r * 0.85f, pos.y - r * 0.95f), size = Size(r * 1.7f, r * 1.9f), style = Stroke(width = r * 0.09f))

        // Brown Spots on forehead
        drawCircle(color = sec, radius = r * 0.15f, center = Offset(pos.x - r * 0.35f, pos.y - r * 0.45f))
        drawCircle(color = sec, radius = r * 0.18f, center = Offset(pos.x + r * 0.3f, pos.y - r * 0.35f))

        // Cute Eyes & Friendly Muzzle
        drawCuteEyesOnly(Offset(pos.x, pos.y - r * 0.15f), r * 0.24f)
        val muzzleY = pos.y + r * 0.45f
        drawOval(color = sec, topLeft = Offset(pos.x - r * 0.6f, muzzleY - r * 0.25f), size = Size(r * 1.2f, r * 0.6f))
        drawSmile(Offset(pos.x, muzzleY + r * 0.08f), r * 0.35f)
        drawCircle(color = CheekColor, radius = r * 0.16f, center = Offset(pos.x - r * 0.6f, muzzleY))
        drawCircle(color = CheekColor, radius = r * 0.16f, center = Offset(pos.x + r * 0.6f, muzzleY))
    }

    // ==========================================
    // 🐦 BIRDS & FLOWERS
    // ==========================================

    private fun DrawScope.drawLittleBird(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f
        val wingFlap = sin(age * 0.015f).toFloat() * r * 0.35f

        drawCircle(color = prim, radius = r, center = pos)
        drawCircle(color = OutlineColor, radius = r, center = pos, style = Stroke(width = r * 0.09f))

        val wingPath = Path().apply {
            moveTo(pos.x - r * 0.1f, pos.y)
            quadraticTo(pos.x - r * 0.8f, pos.y - r * 0.4f + wingFlap, pos.x - r * 0.6f, pos.y + r * 0.4f)
            close()
        }
        drawPath(path = wingPath, color = sec)
        drawPath(path = wingPath, color = OutlineColor, style = Stroke(width = r * 0.08f))

        val beakPath = Path().apply {
            moveTo(pos.x + r * 0.85f, pos.y - r * 0.1f)
            lineTo(pos.x + r * 1.35f, pos.y + r * 0.05f)
            lineTo(pos.x + r * 0.85f, pos.y + r * 0.2f)
            close()
        }
        drawPath(path = beakPath, color = Color(0xFFFF9100))
        drawPath(path = beakPath, color = OutlineColor, style = Stroke(width = r * 0.08f))

        drawCuteSingleEye(Offset(pos.x + r * 0.35f, pos.y - r * 0.18f), r * 0.26f)
        drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x + r * 0.25f, pos.y + r * 0.2f))
    }

    private fun DrawScope.drawPenguin(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.44f

        drawOval(color = Color(0xFF1E1B2E), topLeft = Offset(pos.x - r * 0.85f, pos.y - r), size = Size(r * 1.7f, r * 2f))
        drawOval(color = OutlineColor, topLeft = Offset(pos.x - r * 0.85f, pos.y - r), size = Size(r * 1.7f, r * 2f), style = Stroke(width = r * 0.09f))

        drawOval(color = WhiteColor, topLeft = Offset(pos.x - r * 0.55f, pos.y - r * 0.65f), size = Size(r * 1.1f, r * 1.55f))

        val beak = Path().apply {
            moveTo(pos.x - r * 0.2f, pos.y - r * 0.1f)
            lineTo(pos.x, pos.y + r * 0.15f)
            lineTo(pos.x + r * 0.2f, pos.y - r * 0.1f)
            close()
        }
        drawPath(path = beak, color = Color(0xFFFF9100))
        drawPath(path = beak, color = OutlineColor, style = Stroke(width = r * 0.08f))

        drawCuteEyesOnly(Offset(pos.x, pos.y - r * 0.35f), r * 0.22f)
        drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x - r * 0.45f, pos.y))
        drawCircle(color = CheekColor, radius = r * 0.18f, center = Offset(pos.x + r * 0.45f, pos.y))
    }

    private fun DrawScope.drawButterfly(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.44f
        val wingScale = (cos(age * 0.016f).toFloat() * 0.35f).coerceIn(-0.35f, 0.35f) + 1.0f

        for (sign in listOf(-1f, 1f)) {
            val wingTop = Path().apply {
                moveTo(pos.x, pos.y - r * 0.1f)
                cubicTo(
                    pos.x + sign * r * 1.35f * wingScale, pos.y - r * 1.25f,
                    pos.x + sign * r * 1.55f * wingScale, pos.y - r * 0.1f,
                    pos.x, pos.y + r * 0.1f
                )
                close()
            }
            val wingBottom = Path().apply {
                moveTo(pos.x, pos.y + r * 0.1f)
                cubicTo(
                    pos.x + sign * r * 1.2f * wingScale, pos.y + r * 0.4f,
                    pos.x + sign * r * 1.1f * wingScale, pos.y + r * 1.1f,
                    pos.x, pos.y + r * 0.4f
                )
                close()
            }

            drawPath(path = wingTop, color = prim)
            drawPath(path = wingTop, color = OutlineColor, style = Stroke(width = r * 0.08f))
            drawPath(path = wingBottom, color = sec)
            drawPath(path = wingBottom, color = OutlineColor, style = Stroke(width = r * 0.08f))
        }

        drawRoundRect(
            color = OutlineColor,
            topLeft = Offset(pos.x - r * 0.12f, pos.y - r * 0.65f),
            size = Size(r * 0.24f, r * 1.3f),
            cornerRadius = CornerRadius(r * 0.12f)
        )
    }

    private fun DrawScope.drawBloomingFlower(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.42f
        val petalCount = 8

        for (i in 0 until petalCount) {
            val angle = i * 2 * PI / petalCount
            val px = pos.x + cos(angle).toFloat() * r * 0.72f
            val py = pos.y + sin(angle).toFloat() * r * 0.72f
            drawCircle(color = prim, radius = r * 0.48f, center = Offset(px, py))
            drawCircle(color = OutlineColor, radius = r * 0.48f, center = Offset(px, py), style = Stroke(width = r * 0.08f))
        }

        drawCircle(color = sec, radius = r * 0.62f, center = pos)
        drawCircle(color = OutlineColor, radius = r * 0.62f, center = pos, style = Stroke(width = r * 0.09f))

        drawCuteFace(pos, r * 0.28f)
    }

    private fun DrawScope.drawSunflower(pos: Offset, size: Float, prim: Color, sec: Color, age: Long) {
        val r = size * 0.45f
        val petalCount = 14

        for (i in 0 until petalCount) {
            val angle = i * 2 * PI / petalCount
            val px = pos.x + cos(angle).toFloat() * r * 0.8f
            val py = pos.y + sin(angle).toFloat() * r * 0.8f
            drawCircle(color = Color(0xFFFFEA00), radius = r * 0.34f, center = Offset(px, py))
        }

        drawCircle(color = Color(0xFFFF9100), radius = r * 0.65f, center = pos)
        drawCircle(color = OutlineColor, radius = r * 0.65f, center = pos, style = Stroke(width = r * 0.09f))

        drawCuteFace(pos, r * 0.30f)
    }

    // ==========================================
    // 🎨 FACIAL & SPARKLE HELPERS
    // ==========================================

    private fun DrawScope.drawCuteFace(center: Offset, eyeRadius: Float, isAnimal: Boolean = false) {
        drawCuteEyesOnly(Offset(center.x, center.y - eyeRadius * 0.35f), eyeRadius)

        val mouthY = center.y + eyeRadius * 0.7f
        if (isAnimal) {
            drawCircle(color = NoseColor, radius = eyeRadius * 0.32f, center = Offset(center.x, center.y + eyeRadius * 0.15f))
        }
        drawSmile(Offset(center.x, mouthY), eyeRadius * 0.85f)

        val cheekDist = eyeRadius * 1.45f
        drawCircle(color = CheekColor, radius = eyeRadius * 0.38f, center = Offset(center.x - cheekDist, mouthY - eyeRadius * 0.2f))
        drawCircle(color = CheekColor, radius = eyeRadius * 0.38f, center = Offset(center.x + cheekDist, mouthY - eyeRadius * 0.2f))
    }

    private fun DrawScope.drawCuteEyesOnly(center: Offset, radius: Float) {
        val dist = radius * 1.15f
        drawCuteSingleEye(Offset(center.x - dist, center.y), radius)
        drawCuteSingleEye(Offset(center.x + dist, center.y), radius)
    }

    private fun DrawScope.drawCuteSingleEye(center: Offset, radius: Float) {
        drawCircle(color = WhiteColor, radius = radius, center = center)
        drawCircle(color = OutlineColor, radius = radius, center = center, style = Stroke(width = radius * 0.16f))
        val pupilCenter = Offset(center.x + radius * 0.18f, center.y - radius * 0.1f)
        drawCircle(color = OutlineColor, radius = radius * 0.54f, center = pupilCenter)
        drawCircle(color = WhiteColor, radius = radius * 0.24f, center = Offset(pupilCenter.x + radius * 0.14f, pupilCenter.y - radius * 0.14f))
    }

    private fun DrawScope.drawSmile(center: Offset, width: Float) {
        val mouth = Path().apply {
            moveTo(center.x - width * 0.6f, center.y)
            quadraticTo(center.x, center.y + width * 0.55f, center.x + width * 0.6f, center.y)
        }
        drawPath(path = mouth, color = OutlineColor, style = Stroke(width = (width * 0.18f).coerceIn(3.0f, 6.0f), cap = StrokeCap.Round))
    }

    private fun DrawScope.drawSparkleStar(center: Offset, radius: Float, color: Color, age: Long) {
        val pulse = 1.0f + sin(age * 0.01f).toFloat() * 0.2f
        val r = radius * pulse
        val p = Path().apply {
            moveTo(center.x, center.y - r)
            quadraticTo(center.x, center.y, center.x + r, center.y)
            quadraticTo(center.x, center.y, center.x, center.y + r)
            quadraticTo(center.x, center.y, center.x - r, center.y)
            quadraticTo(center.x, center.y, center.x, center.y - r)
            close()
        }
        drawPath(path = p, color = color)
        drawPath(path = p, color = OutlineColor, style = Stroke(width = r * 0.12f, join = StrokeJoin.Round))
    }
}
