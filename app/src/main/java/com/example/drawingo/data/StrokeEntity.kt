package com.example.drawingo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strokes")
data class StrokeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val layerId: Long,
    val frameIndex: Int = 0,
    val tool: String,
    val colorArgb: Int,
    val strokeWidth: Float,
    val alpha: Float,
    val pointsJson: String,
    val orderIndex: Int
)
