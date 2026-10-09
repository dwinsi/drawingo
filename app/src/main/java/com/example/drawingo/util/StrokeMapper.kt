package com.example.drawingo.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.drawingo.data.StrokeEntity
import com.example.drawingo.model.DrawingTool
import com.example.drawingo.model.DrawnStroke
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StrokeMapper {
    private val gson = Gson()
    
    fun toEntity(stroke: DrawnStroke, projectId: Long, orderIndex: Int): StrokeEntity {
        val pointsJson = gson.toJson(stroke.points)
        return StrokeEntity(
            projectId = projectId,
            tool = stroke.tool.name,
            colorArgb = stroke.color.toArgb(),
            strokeWidth = stroke.strokeWidth,
            alpha = stroke.alpha,
            pointsJson = pointsJson,
            orderIndex = orderIndex
        )
    }

    fun fromEntity(entity: StrokeEntity, strokeId: Long): DrawnStroke {
        val pointsType = object : TypeToken<List<Offset>>() {}.type
        val points: List<Offset> = gson.fromJson(entity.pointsJson, pointsType) ?: emptyList()
        val tool = try {
            DrawingTool.valueOf(entity.tool)
        } catch (e: Exception) {
            DrawingTool.PEN
        }
        
        return DrawnStroke(
            id = strokeId,
            color = Color(entity.colorArgb),
            strokeWidth = entity.strokeWidth,
            alpha = entity.alpha,
            tool = tool,
            points = points
        )
    }
}
