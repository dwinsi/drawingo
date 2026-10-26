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

import com.example.drawingo.data.DrawingDao
import com.example.drawingo.data.DrawingProject
import com.example.drawingo.data.LayerEntity
import com.example.drawingo.data.StrokeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class DrawingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DrawingViewModel

    private val fakeDao = object : DrawingDao {
        override fun getAllProjects(): Flow<List<DrawingProject>> = flowOf(emptyList())
        override suspend fun getProjectById(id: Long): DrawingProject? = null
        override suspend fun getMostRecentProject(): DrawingProject? = null
        override suspend fun insertProject(project: DrawingProject): Long = 1L
        override suspend fun updateProject(project: DrawingProject) {}
        override suspend fun deleteProject(id: Long) {}
        override suspend fun getLayersForProject(projectId: Long): List<LayerEntity> = emptyList()
        override suspend fun insertLayers(layers: List<LayerEntity>): List<Long> = emptyList()
        override suspend fun deleteLayersForProject(projectId: Long) {}
        override suspend fun getStrokesForProject(projectId: Long): List<StrokeEntity> = emptyList()
        override suspend fun insertStrokes(strokes: List<StrokeEntity>) {}
        override suspend fun deleteStrokesForProject(projectId: Long) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DrawingViewModel(fakeDao)
        viewModel.createNewProject()
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

        val strokeCount = { viewModel.layers.value.sumOf { layer -> layer.frames.sumOf { it.strokes.size } } }

        assertEquals(1, strokeCount())
        assertTrue(viewModel.canUndo.value)
        assertFalse(viewModel.canRedo.value)

        viewModel.onPointerDown(pointerId = 2L, screenPosition = Offset(200f, 300f))
        viewModel.onPointerUp(pointerId = 2L)

        assertEquals(2, strokeCount())

        viewModel.undo()
        assertEquals(1, strokeCount())
        assertTrue(viewModel.canRedo.value)

        viewModel.redo()
        assertEquals(2, strokeCount())
        assertFalse(viewModel.canRedo.value)

        viewModel.clearCanvas()
        assertEquals(0, strokeCount())
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
    fun `quick magic preset can be updated to translate and random`() {
        assertEquals(emptySet<DrawingViewModel.QuickMagicPreset>(), viewModel.activePresets.value)

        viewModel.toggleQuickMagicPreset(DrawingViewModel.QuickMagicPreset.TRANSLATE)
        assertTrue(viewModel.activePresets.value.contains(DrawingViewModel.QuickMagicPreset.TRANSLATE))

        viewModel.toggleQuickMagicPreset(DrawingViewModel.QuickMagicPreset.RANDOM)
        assertTrue(viewModel.activePresets.value.contains(DrawingViewModel.QuickMagicPreset.RANDOM))
        assertTrue(viewModel.activePresets.value.contains(DrawingViewModel.QuickMagicPreset.TRANSLATE))

        viewModel.createNewProject()
        assertEquals(emptySet<DrawingViewModel.QuickMagicPreset>(), viewModel.activePresets.value)
    }

    @Test
    fun `fill tool fills enclosed shape when tapping anywhere inside or on the drawn object`() {
        // Draw a square shape: (100,100) -> (300,100) -> (300,300) -> (100,300) -> (100,105)
        viewModel.setTool(DrawingTool.PEN)
        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(100f, 100f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(300f, 100f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(300f, 300f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(100f, 300f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(100f, 105f))
        viewModel.onPointerUp(pointerId = 1L)

        val totalStrokes = { viewModel.layers.value.sumOf { layer -> layer.frames.sumOf { it.strokes.size } } }
        assertEquals(1, totalStrokes())

        // Select Fill tool and tap in the CENTER of the drawn object (200, 200)
        viewModel.setTool(DrawingTool.FILL)
        viewModel.onPointerDown(pointerId = 2L, screenPosition = Offset(200f, 200f))

        assertEquals(2, totalStrokes())
        val fillStroke = viewModel.layers.value[0].frames[0].strokes.last()
        assertEquals(DrawingTool.FILL, fillStroke.tool)

        // Undo fill
        viewModel.undo()
        assertEquals(1, totalStrokes())

        // Tap on the BOUNDARY of the drawn object (300, 200)
        viewModel.onPointerDown(pointerId = 3L, screenPosition = Offset(300f, 200f))
        assertEquals(2, totalStrokes())
        assertEquals(DrawingTool.FILL, viewModel.layers.value[0].frames[0].strokes.last().tool)
    }

    @Test
    fun `spray paint tool creates strokes with spray tool properties`() {
        viewModel.setTool(DrawingTool.SPRAY)
        assertEquals(DrawingTool.SPRAY, viewModel.selectedTool.value)

        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(50f, 50f))
        viewModel.onPointerMove(pointerId = 1L, screenPosition = Offset(100f, 100f))
        viewModel.onPointerUp(pointerId = 1L)

        val stroke = viewModel.layers.value[0].frames[0].strokes.last()
        assertEquals(DrawingTool.SPRAY, stroke.tool)
        assertEquals(0.75f, stroke.alpha)
    }

    @Test
    fun `text stroke supports selection moving scaling and rotation`() {
        viewModel.setTool(DrawingTool.TEXT)
        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(200f, 200f))
        viewModel.addTextStroke("Hello World")

        val textStroke = viewModel.layers.value[0].frames[0].strokes.last()
        assertEquals(DrawingTool.TEXT, textStroke.tool)
        assertEquals("Hello World", textStroke.text)
        assertTrue(textStroke.points.size >= 2)

        val handlePos = textStroke.points[1]
        // Simulate dragging the scale and rotate handle
        viewModel.updateTextStrokeTransform(textStroke.id, Offset(handlePos.x + 50f, handlePos.y + 50f))

        val updatedStroke = viewModel.layers.value[0].frames[0].strokes.last()
        assertEquals(Offset(handlePos.x + 50f, handlePos.y + 50f), updatedStroke.points[1])
        assertTrue(updatedStroke.boundingBox.width > textStroke.boundingBox.width)
    }

    @Test
    fun `switching tool clears any in flight active strokes`() {
        viewModel.setTool(DrawingTool.PEN)
        viewModel.onPointerDown(pointerId = 1L, screenPosition = Offset(10f, 10f))
        assertEquals(1, viewModel.activeStrokes.value.size)

        // User switches tool mid-flight or after an incomplete touch
        viewModel.setTool(DrawingTool.SPRAY)
        assertEquals(DrawingTool.SPRAY, viewModel.selectedTool.value)
        assertTrue(viewModel.activeStrokes.value.isEmpty())
    }
}




