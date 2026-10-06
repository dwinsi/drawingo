package com.example.drawingo.model

enum class SketchCategory(val displayName: String, val emoji: String) {
    ALL("All", "🌈"),
    CELESTIAL("Celestial", "🌟"),
    SEA_ANIMALS("Sea Animals", "🌊"),
    WILD_ANIMALS("Wild Animals", "🦁"),
    OTHER("More", "🎨");

    companion object {
        fun fromString(value: String?): SketchCategory {
            if (value == null) return ALL
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}

/**
 * Model representing a stock line-art sketch for toddler coloring.
 */
data class StockSketch(
    val id: String,
    val title: String,
    val category: SketchCategory,
    val emoji: String = "🎨",
    val difficulty: String = "EASY",
    val tags: List<String> = emptyList(),
    val imageUrl: String,
    val thumbnailUrl: String? = null,
    val assetPath: String? = null,
    val createdAt: String? = null
)
