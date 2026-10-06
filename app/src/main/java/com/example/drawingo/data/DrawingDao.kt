package com.example.drawingo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawings ORDER BY createdAtTimestamp DESC")
    suspend fun getAllDrawings(): List<DrawingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrawing(drawing: DrawingEntity)

    @Delete
    suspend fun deleteDrawing(drawing: DrawingEntity)
}
