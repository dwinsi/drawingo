package com.example.drawingo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "layers")
data class LayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val isVisible: Boolean = true,
    val opacity: Float = 1.0f,
    val orderIndex: Int
)
