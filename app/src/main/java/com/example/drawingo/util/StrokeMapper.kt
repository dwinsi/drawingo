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
    
    fun toEntity(stroke: DrawnStroke, projectId: Long, layerId: Long, frameIndex: Int, orderIndex: Int): StrokeEntity {
        val wrapper = StrokeDataWrapper(stroke.points, stroke.pressures)
        val bundledJson = gson.toJson(wrapper)
        
        return StrokeEntity(
            projectId = projectId,
            layerId = layerId,
            frameIndex = frameIndex,
            tool = stroke.tool.name,
            colorArgb = stroke.color.toArgb(),
            strokeWidth = stroke.strokeWidth,
            alpha = stroke.alpha,
            pointsJson = bundledJson,
            orderIndex = orderIndex
        )
    }

    fun fromEntity(entity: StrokeEntity, strokeId: Long): DrawnStroke {
        val tool = try {
            DrawingTool.valueOf(entity.tool)
        } catch (e: Exception) {
            DrawingTool.PEN
        }
        
        var points: List<Offset> = emptyList()
        var pressures: List<Float> = emptyList()
        
        try {
            val wrapperType = object : TypeToken<StrokeDataWrapper>() {}.type
            val wrapper: StrokeDataWrapper? = gson.fromJson(entity.pointsJson, wrapperType)
            if (wrapper != null) {
                points = wrapper.points
                pressures = wrapper.pressures
            }
        } catch (e: Exception) {
            // Fallback for old schema
            val pointsType = object : TypeToken<List<Offset>>() {}.type
            points = gson.fromJson(entity.pointsJson, pointsType) ?: emptyList()
        }
        
        return DrawnStroke(
            id = strokeId,
            color = Color(entity.colorArgb),
            strokeWidth = entity.strokeWidth,
            alpha = entity.alpha,
            tool = tool,
            points = points,
            pressures = pressures
        )
    }
}

data class StrokeDataWrapper(
    val points: List<Offset>,
    val pressures: List<Float>
)
