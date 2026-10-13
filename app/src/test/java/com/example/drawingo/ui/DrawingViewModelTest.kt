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
        assertEquals(DrawingViewModel.QuickMagicPreset.NONE, viewModel.activePreset.value)

        viewModel.setQuickMagicPreset(DrawingViewModel.QuickMagicPreset.TRANSLATE)
        assertEquals(DrawingViewModel.QuickMagicPreset.TRANSLATE, viewModel.activePreset.value)

        viewModel.setQuickMagicPreset(DrawingViewModel.QuickMagicPreset.RANDOM)
        assertEquals(DrawingViewModel.QuickMagicPreset.RANDOM, viewModel.activePreset.value)

        viewModel.createNewProject()
        assertEquals(DrawingViewModel.QuickMagicPreset.NONE, viewModel.activePreset.value)
    }
}

