package com.example.drawingo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.drawingo.model.CanvasPaperStyle

@Entity(tableName = "drawings")
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val strokesJson: String, // Serialize List<DrawnStroke> to JSON
    val paperStyle: CanvasPaperStyle,
    val magicCreatureJson: String?, // Serialize MagicCompanion to JSON
    val createdAtTimestamp: Long
)
