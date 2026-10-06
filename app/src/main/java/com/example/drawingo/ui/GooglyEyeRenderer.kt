package com.example.drawingo.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.drawingo.model.GooglyEyePair
import kotlin.math.PI
import kotlin.math.sin

/**
 * Draws animated Googly Eyes directly onto the Compose Canvas.
 * Includes bouncy spawn animation, glossy specular light reflections, 3D depth,
 * and an optional cheerful smile with blushing cheeks.
 */
object GooglyEyeRenderer {

    private val ScleraColor = Color(0xFFFDFDFD)
    private val ScleraShadowColor = Color(0x33000000)
    private val BorderColor = Color(0xFF14151A)
    private val PupilColor = Color(0xFF181920)
    private val SpecularHighlightColor = Color(0xFFFFFFFF)
    private val SecondaryHighlightColor = Color(0xAAFFFFFF)
    private val CheekBlushColor = Color(0xE6FF6B8B)
    private val TongueColor = Color(0xFFFF5252)

    fun drawGooglyEyePair(
        drawScope: DrawScope,
        eyePair: GooglyEyePair,
        currentTimeMs: Long
    ) {
        val age = (currentTimeMs - eyePair.spawnTimestamp).coerceAtLeast(0L)
        val scale = if (age < 350L) {
            val t = age / 350f
            1.0f + (sin(t * PI).toFloat() * 0.22f) * (1f - t)
        } else {
            1.0f
        }

        val currentRadius = eyePair.radius * scale
        val currentPupilRadius = eyePair.pupilRadius * scale

        drawSingleEye(
            drawScope = drawScope,
            center = eyePair.leftCenter,
            radius = currentRadius,
            pupilRadius = currentPupilRadius,
            pupilOffset = eyePair.leftPupilOffset * scale
        )

        drawSingleEye(
            drawScope = drawScope,
            center = eyePair.rightCenter,
            radius = currentRadius,
            pupilRadius = currentPupilRadius,
            pupilOffset = eyePair.rightPupilOffset * scale
        )

        if (eyePair.hasSmile && currentRadius > 14f) {
            drawMouthAndCheeks(
                drawScope = drawScope,
                leftEye = eyePair.leftCenter,
                rightEye = eyePair.rightCenter,
                eyeRadius = currentRadius,
                scale = scale
            )
        }
    }

    private fun drawSingleEye(
        drawScope: DrawScope,
        center: Offset,
        radius: Float,
        pupilRadius: Float,
        pupilOffset: Offset
    ) {
        if (radius <= 0f) return

        drawScope.drawCircle(
            color = ScleraShadowColor,
            radius = radius + 3f,
            center = Offset(center.x, center.y + 3f)
        )

        drawScope.drawCircle(
            color = ScleraColor,
            radius = radius,
            center = center
        )

        drawScope.drawCircle(
            color = BorderColor,
            radius = radius,
            center = center,
            style = Stroke(width = (radius * 0.12f).coerceIn(2.5f, 6f))
        )

        val pupilCenter = Offset(center.x + pupilOffset.x, center.y + pupilOffset.y)
        drawScope.drawCircle(
            color = PupilColor,
            radius = pupilRadius,
            center = pupilCenter
        )

        val mainSpecCenter = Offset(
            pupilCenter.x + pupilRadius * 0.32f,
            pupilCenter.y - pupilRadius * 0.32f
        )
        drawScope.drawCircle(
            color = SpecularHighlightColor,
            radius = pupilRadius * 0.34f,
            center = mainSpecCenter
        )

        val secondarySpecCenter = Offset(
            pupilCenter.x - pupilRadius * 0.30f,
            pupilCenter.y + pupilRadius * 0.28f
        )
        drawScope.drawCircle(
            color = SecondaryHighlightColor,
            radius = pupilRadius * 0.16f,
            center = secondarySpecCenter
        )
    }

    private fun drawMouthAndCheeks(
        drawScope: DrawScope,
        leftEye: Offset,
        rightEye: Offset,
        eyeRadius: Float,
        scale: Float
    ) {
        val midX = (leftEye.x + rightEye.x) / 2f
        val midY = (leftEye.y + rightEye.y) / 2f
        val mouthY = midY + eyeRadius * 1.25f
        val mouthWidth = (rightEye.x - leftEye.x) * 0.55f

        val cheekRadius = eyeRadius * 0.42f
        drawScope.drawCircle(
            color = CheekBlushColor,
            radius = cheekRadius,
            center = Offset(leftEye.x - eyeRadius * 0.3f, mouthY - eyeRadius * 0.1f)
        )
        drawScope.drawCircle(
            color = CheekBlushColor,
            radius = cheekRadius,
            center = Offset(rightEye.x + eyeRadius * 0.3f, mouthY - eyeRadius * 0.1f)
        )

        val mouthPath = Path().apply {
            moveTo(midX - mouthWidth, mouthY)
            quadraticTo(midX, mouthY + eyeRadius * 0.75f, midX + mouthWidth, mouthY)
        }
        drawScope.drawPath(
            path = mouthPath,
            color = BorderColor,
            style = Stroke(width = (eyeRadius * 0.14f).coerceIn(2.5f, 5.5f), cap = StrokeCap.Round)
        )

        val tonguePath = Path().apply {
            moveTo(midX - mouthWidth * 0.4f, mouthY + eyeRadius * 0.35f)
            quadraticTo(midX, mouthY + eyeRadius * 0.65f, midX + mouthWidth * 0.4f, mouthY + eyeRadius * 0.35f)
        }
        drawScope.drawPath(
            path = tonguePath,
            color = TongueColor,
            style = Stroke(width = (eyeRadius * 0.11f).coerceIn(2f, 4f), cap = StrokeCap.Round)
        )
    }
}
