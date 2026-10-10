package com.example.drawingo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawing_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<DrawingProject>>

    @Query("SELECT * FROM drawing_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): DrawingProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: DrawingProject): Long

    @Update
    suspend fun updateProject(project: DrawingProject)

    @Query("DELETE FROM drawing_projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("SELECT * FROM layers WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getLayersForProject(projectId: Long): List<LayerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayers(layers: List<LayerEntity>): List<Long>

    @Query("DELETE FROM layers WHERE projectId = :projectId")
    suspend fun deleteLayersForProject(projectId: Long)

    @Query("SELECT * FROM strokes WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getStrokesForProject(projectId: Long): List<StrokeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrokes(strokes: List<StrokeEntity>)

    @Query("DELETE FROM strokes WHERE projectId = :projectId")
    suspend fun deleteStrokesForProject(projectId: Long)
}
