package com.example.drawingo.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawnStroke
import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeMapperTest {

    @Test
    fun `toEntity and fromEntity preserves points pressures and tool properties`() {
        val originalStroke = DrawnStroke(
            id = 42L,
            color = Color.Red,
            strokeWidth = 12.5f,
            alpha = 0.9f,
            tool = DrawingTool.BRUSH,
            points = listOf(Offset(100f, 200f), Offset(150f, 250f)),
            pressures = listOf(0.5f, 0.8f),
            text = null
        )

        val entity = StrokeMapper.toEntity(originalStroke, projectId = 1L, layerId = 2L, frameIndex = 0, orderIndex = 0)
        assertEquals("BRUSH", entity.tool)

        val restoredStroke = StrokeMapper.fromEntity(entity, strokeId = 42L)
        assertEquals(originalStroke.tool, restoredStroke.tool)
        assertEquals(originalStroke.strokeWidth, restoredStroke.strokeWidth, 0.01f)
        assertEquals(originalStroke.alpha, restoredStroke.alpha, 0.01f)
        assertEquals(originalStroke.points.size, restoredStroke.points.size)
        assertEquals(originalStroke.points[0].x, restoredStroke.points[0].x, 0.1f)
        assertEquals(originalStroke.points[0].y, restoredStroke.points[0].y, 0.1f)
    }
}
