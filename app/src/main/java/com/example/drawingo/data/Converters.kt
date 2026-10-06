package com.example.drawingo.data

import androidx.room.TypeConverter
import com.example.drawingo.model.CanvasPaperStyle

class Converters {
    @TypeConverter
    fun fromCanvasPaperStyle(value: CanvasPaperStyle): String {
        return value.name
    }

    @TypeConverter
    fun toCanvasPaperStyle(value: String): CanvasPaperStyle {
        return try {
            CanvasPaperStyle.valueOf(value)
        } catch (e: Exception) {
            CanvasPaperStyle.PURE_WHITE
        }
    }
}
