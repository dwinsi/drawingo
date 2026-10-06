package com.example.drawingo.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

enum class DrawingTool {
    LASSO,
    ERASER,
    PEN,
    HIGHLIGHTER,
    BRUSH
}

enum class CanvasMode {
    DRAWINGO,
    TODDLER_MAGIC
}

enum class CanvasPaperStyle {
    PURE_WHITE,
    BLUE_GRID,
    COSMIC_NIGHT,
    WARM_CREAM
}

/**
 * Drawingo's four-row palette, arranged in hue families down each column.
 */
object DrawingoPalette {
    val grid = listOf(
        // Row 1: Main colors, always visible in the dock
        listOf(
            Color(0xFF000000), // Black
            Color(0xFFFF2A2A), // Red
            Color(0xFFFF8000), // Orange
            Color(0xFFFFD600), // Yellow
            Color(0xFF10B981), // Green
            Color(0xFF0066FF), // Blue
            Color(0xFF9333EA), // Purple
            Color(0xFFFF007F)  // Pink
        ),
        // Row 2: Deep shades, aligned by hue with row 1
        listOf(
            Color(0xFF334155), // Charcoal
            Color(0xFF990000), // Deep Crimson
            Color(0xFFC2410C), // Deep Burnt Orange
            Color(0xFFF59E0B), // Amber
            Color(0xFF047857), // Forest Green
            Color(0xFF1E3A8A), // Midnight Navy
            Color(0xFF581C87), // Deep Purple
            Color(0xFFF43F5E)  // Electric Rose
        ),
        // Row 3: Bright accents, aligned by hue
        listOf(
            Color(0xFF64748B), // Slate Grey
            Color(0xFFFB7185), // Coral
            Color(0xFFF97316), // Tangerine
            Color(0xFFFFD166), // Sunshine Gold
            Color(0xFF65A30D), // Lime Green
            Color(0xFF38BDF8), // Sky Blue
            Color(0xFF7E22CE), // Plum
            Color(0xFFD946EF)  // Fuchsia
        ),
        // Row 4: Soft shades, aligned by hue
        listOf(
            Color(0xFFFFFFFF), // White
            Color(0xFFFFB3BA), // Blush Pink
            Color(0xFFFFDFBA), // Soft Peach
            Color(0xFFFFFFBA), // Lemon Pastel
            Color(0xFFBAFFC9), // Mint Green
            Color(0xFFBAE1FF), // Periwinkle
            Color(0xFFE8DFF5), // Soft Lavender
            Color(0xFFFBCFE8)  // Rose Pink
        )
    )

    // Sized for tactile kid usability (6px up to 92px)
    val strokeSizes = listOf(6f, 12f, 20f, 32f, 48f, 68f, 92f)
}

/**
 * Rich, high-chroma electric neon colors tailored specifically for toddlers
 * to produce maximum visual delight against a canvas.
 */
object NeonPalette {
    val colors = listOf(
        Color(0xFF00F0FF), // Electric Cyan
        Color(0xFF39FF14), // Acid Lime
        Color(0xFFFF007F), // Hot Magenta
        Color(0xFFFFD600), // Sunshine Gold
        Color(0xFFFF5722), // Sizzling Orange
        Color(0xFFB026FF), // Neon Violet
        Color(0xFF00FFCC), // Cyber Mint
        Color(0xFFFF1744), // Radiant Red
        Color(0xFF64DD17), // Vivid Apple Green
        Color(0xFFFF9100)  // Fluorescent Amber
    )

    fun getColor(index: Int): Color {
        return colors[(index % colors.size + colors.size) % colors.size]
    }
}

/**
 * Represents a single continuous stroke drawn on the canvas.
 */
data class DrawnStroke(
    val id: Long,
    val color: Color,
    val strokeWidth: Float = 20f,
    val alpha: Float = 1.0f,
    val tool: DrawingTool = DrawingTool.PEN,
    val points: List<Offset> = emptyList(),
    val boundingBox: Rect = calculateBounds(points)
) {
    companion object {
        fun calculateBounds(points: List<Offset>): Rect {
            if (points.isEmpty()) return Rect.Zero
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE

            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
            return Rect(minX, minY, maxX, maxY)
        }
    }
}

/**
 * Represents animated "Googly Eyes" placed on or near a stroke's bounding box.
 */
data class GooglyEyePair(
    val id: Long,
    val leftCenter: Offset,
    val rightCenter: Offset,
    val radius: Float,
    val pupilRadius: Float,
    val leftPupilOffset: Offset,
    val rightPupilOffset: Offset,
    val hasSmile: Boolean = true,
    val spawnTimestamp: Long = System.currentTimeMillis()
)
