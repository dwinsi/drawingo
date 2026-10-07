package com.example.drawingo.ui

import androidx.compose.ui.geometry.Offset
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DrawingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DrawingViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `drawing persists strokes and supports undo and redo`() {
        viewModel.setTool(DrawingTool.PEN)
        viewModel.setColor(DrawingoPalette.grid[0][1])

        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(50f, 100f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(150f, 200f))
        viewModel.onPointerUp(pointerId = 1L)

        assertEquals(1, viewModel.completedStrokes.value.size)
        assertTrue(viewModel.canUndo.value)
        assertFalse(viewModel.canRedo.value)

        viewModel.onPointerDown(pointerId = 2L, screenPosition = Offset(200f, 300f))
        viewModel.onPointerUp(pointerId = 2L)

        assertEquals(2, viewModel.completedStrokes.value.size)

        viewModel.undo()
        assertEquals(1, viewModel.completedStrokes.value.size)
        assertTrue(viewModel.canRedo.value)

        viewModel.redo()
        assertEquals(2, viewModel.completedStrokes.value.size)
        assertFalse(viewModel.canRedo.value)

        viewModel.clearCanvas()
        assertEquals(0, viewModel.completedStrokes.value.size)
    }

    @Test
    fun `2-finger pan and zoom updates canvas scale and offset`() {
        assertEquals(1.0f, viewModel.canvasScale.value, 0.01f)
        assertEquals(0.0f, viewModel.canvasOffsetX.value, 0.01f)
        assertEquals(0.0f, viewModel.canvasOffsetY.value, 0.01f)

        viewModel.onPanAndZoom(zoomChange = 1.5f, panChange = Offset(20f, -30f))

        assertEquals(1.5f, viewModel.canvasScale.value, 0.01f)
        assertEquals(20f, viewModel.canvasOffsetX.value, 0.01f)
        assertEquals(-30f, viewModel.canvasOffsetY.value, 0.01f)

        viewModel.resetPanAndZoom()
        assertEquals(1.0f, viewModel.canvasScale.value, 0.01f)
    }
}
