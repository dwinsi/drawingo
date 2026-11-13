package com.example.drawingo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DrawingDaoUnitTest {

    private lateinit var dao: DrawingDao

    private class FakeDrawingDao : DrawingDao {
        private val projects = mutableMapOf<Long, DrawingProject>()
        private val layers = mutableMapOf<Long, MutableList<LayerEntity>>()
        private val strokes = mutableMapOf<Long, MutableList<StrokeEntity>>()
        private var idCounter = 1L

        override fun getAllProjects(): Flow<List<DrawingProject>> {
            return flowOf(projects.values.sortedByDescending { it.updatedAt })
        }

        override suspend fun getMostRecentProject(): DrawingProject? {
            return projects.values.maxByOrNull { it.updatedAt }
        }

        override suspend fun getProjectById(id: Long): DrawingProject? {
            return projects[id]
        }

        override suspend fun insertProject(project: DrawingProject): Long {
            val id = if (project.id == 0L) idCounter++ else project.id
            val saved = project.copy(id = id)
            projects[id] = saved
            return id
        }

        override suspend fun updateProject(project: DrawingProject) {
            projects[project.id] = project
        }

        override suspend fun deleteProject(id: Long) {
            projects.remove(id)
            layers.remove(id)
            strokes.remove(id)
        }

        override suspend fun getLayersForProject(projectId: Long): List<LayerEntity> {
            return layers[projectId] ?: emptyList()
        }

        override suspend fun insertLayers(layers: List<LayerEntity>): List<Long> {
            if (layers.isEmpty()) return emptyList()
            val projectId = layers.first().projectId
            val list = this.layers.getOrPut(projectId) { mutableListOf() }
            val generatedIds = mutableListOf<Long>()
            for (layer in layers) {
                val id = idCounter++
                list.add(layer.copy(id = id))
                generatedIds.add(id)
            }
            return generatedIds
        }

        override suspend fun deleteLayersForProject(projectId: Long) {
            layers.remove(projectId)
        }

        override suspend fun getStrokesForProject(projectId: Long): List<StrokeEntity> {
            return strokes[projectId] ?: emptyList()
        }

        override suspend fun insertStrokes(strokes: List<StrokeEntity>) {
            if (strokes.isEmpty()) return
            val projectId = strokes.first().projectId
            val list = this.strokes.getOrPut(projectId) { mutableListOf() }
            for (stroke in strokes) {
                list.add(stroke.copy(id = idCounter++))
            }
        }

        override suspend fun deleteStrokesForProject(projectId: Long) {
            strokes.remove(projectId)
        }
    }

    @Before
    fun setUp() {
        dao = FakeDrawingDao()
    }

    @Test
    fun `saveProjectContent persists project layers and strokes atomically`() = runBlocking {
        val project = DrawingProject(
            name = "Test Project",
            updatedAt = System.currentTimeMillis()
        )
        val layers = listOf(
            LayerEntity(projectId = 0, name = "Layer 1", orderIndex = 0),
            LayerEntity(projectId = 0, name = "Layer 2", orderIndex = 1)
        )
        val strokes = listOf(
            StrokeEntity(
                projectId = 0,
                layerId = 0,
                tool = "PEN",
                colorArgb = 0xFF000000.toInt(),
                strokeWidth = 10f,
                alpha = 1.0f,
                pointsJson = "[{\"x\":0.0,\"y\":0.0}]",
                orderIndex = 0
            ),
            StrokeEntity(
                projectId = 0,
                layerId = 1,
                tool = "SPRAY",
                colorArgb = 0xFFFF0000.toInt(),
                strokeWidth = 20f,
                alpha = 0.8f,
                pointsJson = "[{\"x\":50.0,\"y\":50.0}]",
                orderIndex = 0
            )
        )

        val projectId = dao.saveProjectContent(project, layers, strokes)
        assertTrue(projectId > 0)

        val retrievedProject = dao.getProjectById(projectId)
        assertNotNull(retrievedProject)
        assertEquals("Test Project", retrievedProject?.name)

        val savedLayers = dao.getLayersForProject(projectId)
        assertEquals(2, savedLayers.size)
        assertEquals("Layer 1", savedLayers[0].name)
        assertEquals("Layer 2", savedLayers[1].name)

        val savedStrokes = dao.getStrokesForProject(projectId)
        assertEquals(2, savedStrokes.size)
        assertEquals("PEN", savedStrokes[0].tool)
        assertEquals("SPRAY", savedStrokes[1].tool)
    }

    @Test
    fun `deleteProject removes project layers and strokes`() = runBlocking {
        val project = DrawingProject(name = "To Delete", updatedAt = System.currentTimeMillis())
        val projectId = dao.insertProject(project)

        assertNotNull(dao.getProjectById(projectId))

        dao.deleteProject(projectId)
        assertNull(dao.getProjectById(projectId))
        assertTrue(dao.getLayersForProject(projectId).isEmpty())
        assertTrue(dao.getStrokesForProject(projectId).isEmpty())
    }
}
