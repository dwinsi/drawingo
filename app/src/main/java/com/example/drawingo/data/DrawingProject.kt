package com.example.drawingo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawing_projects")
data class DrawingProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)
