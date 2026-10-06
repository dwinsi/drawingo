package com.example.drawingo.ui

import androidx.compose.ui.geometry.Offset
import com.example.drawingo.model.CanvasMode
import com.example.drawingo.model.CreatureCategory
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawingoPalette
import com.example.drawingo.model.NeonPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
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
    fun `drawingo mode persists strokes and supports undo and redo`() {
        viewModel.setCanvasMode(CanvasMode.DRAWINGO)
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

    @Test
    fun `toddler magic mode touch down cycles vibrant neon colors`() {
        viewModel.setCanvasMode(CanvasMode.TODDLER_MAGIC)
        val color0 = NeonPalette.getColor(0)
        val color1 = NeonPalette.getColor(1)

        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(100f, 100f))
        val stroke1 = viewModel.activeStrokes.value[1L]
        assertNotNull(stroke1)
        assertEquals(color0, stroke1?.color)

        viewModel.onPointerUp(pointerId = 1L)

        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(200f, 200f))
        val stroke2 = viewModel.activeStrokes.value[1L]
        assertNotNull(stroke2)
        assertEquals(color1, stroke2?.color)
        assertNotEquals(stroke1?.color, stroke2?.color)
    }

    @Test
    fun `toddler magic mode stroke vanishes on release and spawns character`() {
        viewModel.setCanvasMode(CanvasMode.TODDLER_MAGIC)
        val p1 = Offset(50f, 100f)
        val p2 = Offset(150f, 300f)

        viewModel.onPointerDown(pointerId = 1L, screenPosition = p1)
        viewModel.onPointerMove(pointerId = 1L, screenPosition = p2)

        assertEquals(1, viewModel.activeStrokes.value.size)

        viewModel.onPointerUp(pointerId = 1L)

        assertEquals(0, viewModel.activeStrokes.value.size)
        assertEquals(0, viewModel.completedStrokes.value.size)
        assertEquals(1, viewModel.magicCompanions.value.size)
    }

    @Test
    fun `tapping canvas in toddler magic mode spawns magical companions`() {
        viewModel.setCanvasMode(CanvasMode.TODDLER_MAGIC)
        for (i in 1..12) {
            val pos = Offset(50f * i, 50f * i)
            viewModel.onPointerDown(pointerId = i.toLong(), screenPosition = pos)
            viewModel.onPointerUp(pointerId = i.toLong())
        }

        val companions = viewModel.magicCompanions.value
        assertEquals(12, companions.size)

        val categories = companions.map { it.type.category }.toSet()
        assertTrue("Contains Sea Creatures", categories.contains(CreatureCategory.SEA_CREATURE))
        assertTrue("Contains Wild Animals", categories.contains(CreatureCategory.WILD_ANIMAL))
        assertTrue("Contains Birds", categories.contains(CreatureCategory.BIRD))
        assertTrue("Contains Butterflies", categories.contains(CreatureCategory.BUTTERFLY))
        assertTrue("Contains Flowers", categories.contains(CreatureCategory.FLOWER))
    }

    @Test
    fun `wipe transition smoothly clears all canvas paths, eyes, and magic companions`() = runTest {
        viewModel.setCanvasMode(CanvasMode.DRAWINGO)
        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(100f, 100f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(200f, 200f))
        viewModel.onPointerUp(pointerId = 1L)

        assertTrue(viewModel.completedStrokes.value.isNotEmpty())

        viewModel.triggerWipeAndClear()

        advanceTimeBy(1300L)

        assertEquals(0, viewModel.completedStrokes.value.size)
        assertEquals(0, viewModel.googlyEyes.value.size)
        assertEquals(0, viewModel.magicCompanions.value.size)
        assertFalse(viewModel.isWiping.value)
        assertEquals(0f, viewModel.wipeProgress.value, 0.01f)
    }
}
