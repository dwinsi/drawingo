package com.example.drawingo.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawnStroke
import com.example.drawingo.ui.DrawingViewModel.QuickMagicPreset
import org.junit.Assert.assertNotEquals
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
    fun `dancing preset transforms stroke points smoothly`() {
        val stroke = createSampleStroke()
        val bounds = Rect(100f, 100f, 150f, 150f)
        val center = Offset(125f, 125f)

        val animatedStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.DANCING),
            timeMs = 500L,
            groupCenter = center,
            groupBounds = bounds
        )

        assertNotNull(animatedStroke)
        assertNotEquals(stroke.points, animatedStroke!!.points)
    }

    @Test
    fun `dancing preset applies 3D sphere rotation and depth perspective over time`() {
        val stroke = createSampleStroke()
        val bounds = Rect(100f, 100f, 150f, 150f)
        val center = Offset(125f, 125f)

        val frame1 = QuickMagicRenderer.processMagicStroke(stroke, setOf(QuickMagicPreset.DANCING), 200L, center, bounds)
        val frame2 = QuickMagicRenderer.processMagicStroke(stroke, setOf(QuickMagicPreset.DANCING), 800L, center, bounds)

        assertNotNull(frame1)
        assertNotNull(frame2)
        assertNotEquals(frame1!!.points, frame2!!.points)
    }

    @Test
    fun `swimming preset applies floating wave movement`() {
        val stroke = createSampleStroke()

        val animatedStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.SWIMMING),
            timeMs = 1000L
        )

        assertNotNull(animatedStroke)
        assertNotEquals(stroke.points, animatedStroke!!.points)
    }

    @Test
    fun `rainbow preset changes stroke color over time`() {
        val stroke = createSampleStroke()

        val animatedStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.RAINBOW),
            timeMs = 1000L
        )

        assertNotNull(animatedStroke)
        assertNotEquals(stroke.color, animatedStroke!!.color)
    }

    @Test
    fun `twirl preset rotates points around group center`() {
        val stroke = createSampleStroke()
        val center = Offset(125f, 125f)

        val animatedStroke = QuickMagicRenderer.processMagicStroke(
            stroke = stroke,
            presets = setOf(QuickMagicPreset.TWIRL),
            timeMs = 750L,
            groupCenter = center
        )

        assertNotNull(animatedStroke)
        assertNotEquals(stroke.points, animatedStroke!!.points)
    }
}
