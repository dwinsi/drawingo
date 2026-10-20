package com.example.drawingo.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickMagicRendererTest {

    private fun createSampleStroke(): DrawnStroke {
        return DrawnStroke(
            id = 1L,
            points = listOf(
                Offset(100f, 100f),
                Offset(150f, 100f),
                Offset(150f, 150f),
                Offset(100f, 150f)
            ),
            color = Color.Black,
            strokeWidth = 5f,
            tool = DrawingTool.PEN
        )
    }

    @Test
    fun `translate preset moves forward, backward, up, and down across phases`() {
        val stroke = createSampleStroke()
        val canvasSize = Size(1000f, 1000f)
        val bounds = Rect(100f, 100f, 150f, 150f)

        // Phase 0: Forward (+X), at peak (progress = 0.5 within phase 0 -> t = 0.5 * 1500 = 750ms)
        val forwardStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.TRANSLATE),
            timeMs = 750L,
            groupBounds = bounds,
            canvasSize = canvasSize
        )
        assertNotNull(forwardStroke)
        val deltaXForward = forwardStroke!!.points[0].x - stroke.points[0].x
        val deltaYForward = forwardStroke.points[0].y - stroke.points[0].y
        assertTrue("Expected forward movement in +X", deltaXForward > 50f)
        assertEquals("Expected no Y displacement in forward phase", 0f, deltaYForward, 0.01f)

        // Phase 1: Backward (-X), at peak (t = 1500 + 750 = 2250ms)
        val backwardStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.TRANSLATE),
            timeMs = 2250L,
            groupBounds = bounds,
            canvasSize = canvasSize
        )
        assertNotNull(backwardStroke)
        val deltaXBackward = backwardStroke!!.points[0].x - stroke.points[0].x
        val deltaYBackward = backwardStroke.points[0].y - stroke.points[0].y
        assertTrue("Expected backward movement in -X", deltaXBackward < -50f)
        assertEquals("Expected no Y displacement in backward phase", 0f, deltaYBackward, 0.01f)

        // Phase 2: Up (-Y), at peak (t = 3000 + 750 = 3750ms)
        val upStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.TRANSLATE),
            timeMs = 3750L,
            groupBounds = bounds,
            canvasSize = canvasSize
        )
        assertNotNull(upStroke)
        val deltaXUp = upStroke!!.points[0].x - stroke.points[0].x
        val deltaYUp = upStroke.points[0].y - stroke.points[0].y
        assertEquals("Expected no X displacement in up phase", 0f, deltaXUp, 0.01f)
        assertTrue("Expected up movement in -Y", deltaYUp < -50f)

        // Phase 3: Down (+Y), at peak (t = 4500 + 750 = 5250ms)
        val downStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.TRANSLATE),
            timeMs = 5250L,
            groupBounds = bounds,
            canvasSize = canvasSize
        )
        assertNotNull(downStroke)
        val deltaXDown = downStroke!!.points[0].x - stroke.points[0].x
        val deltaYDown = downStroke.points[0].y - stroke.points[0].y
        assertEquals("Expected no X displacement in down phase", 0f, deltaXDown, 0.01f)
        assertTrue("Expected down movement in +Y", deltaYDown > 50f)
    }

    @Test
    fun `random roam moves artwork across screen and stays safely within canvas bounds`() {
        val stroke = createSampleStroke()
        val canvasSize = Size(1000f, 1000f)
        val bounds = Rect(100f, 100f, 150f, 150f)
        val center = Offset(125f, 125f)

        var minSeenX = Float.POSITIVE_INFINITY
        var maxSeenX = Float.NEGATIVE_INFINITY
        var minSeenY = Float.POSITIVE_INFINITY
        var maxSeenY = Float.NEGATIVE_INFINITY

        // Sample across 30 seconds to verify roaming across different quadrants of the screen
        for (second in 0..30) {
            val roamed = QuickMagicRenderer.processMagicStroke(
                stroke = stroke,
                presets = setOf(QuickMagicPreset.RANDOM),
                timeMs = second * 1000L,
                groupCenter = center,
                groupBounds = bounds,
                canvasSize = canvasSize
            )
            assertNotNull(roamed)
            roamed!!.points.forEach { p ->
                if (p.x < minSeenX) minSeenX = p.x
                if (p.x > maxSeenX) maxSeenX = p.x
                if (p.y < minSeenY) minSeenY = p.y
                if (p.y > maxSeenY) maxSeenY = p.y
            }
        }

        // Verify that the stroke explored a wide area of the canvas
        assertTrue("Expected artwork to move across wide X span", (maxSeenX - minSeenX) > 300f)
        assertTrue("Expected artwork to move across wide Y span", (maxSeenY - minSeenY) > 300f)

        // Verify bounds safety: artwork must never jump off screen
        assertTrue("Artwork should stay within left margin", minSeenX >= 0f)
        assertTrue("Artwork should stay within right margin", maxSeenX <= canvasSize.width)
        assertTrue("Artwork should stay within top margin", minSeenY >= 0f)
        assertTrue("Artwork should stay within bottom margin", maxSeenY <= canvasSize.height)
    }
}
