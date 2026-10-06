package com.example.drawingo.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Creature & Nature element types that can magically spawn when toddlers draw or tap.
 */
enum class CreatureCategory {
    SEA_CREATURE,
    WILD_ANIMAL,
    BIRD,
    BUTTERFLY,
    FLOWER,
    DOODLE_FACE
}

enum class CreatureType(val category: CreatureCategory) {
    // Sea animals
    OCTOPUS(CreatureCategory.SEA_CREATURE),
    JELLYFISH(CreatureCategory.SEA_CREATURE),
    CLOWN_FISH(CreatureCategory.SEA_CREATURE),
    STARFISH(CreatureCategory.SEA_CREATURE),

    // Wild animals
    LION(CreatureCategory.WILD_ANIMAL),
    BABY_BEAR(CreatureCategory.WILD_ANIMAL),
    ELEPHANT(CreatureCategory.WILD_ANIMAL),

    // Birds
    LITTLE_BIRD(CreatureCategory.BIRD),
    PENGUIN(CreatureCategory.BIRD),

    // Butterflies
    BUTTERFLY(CreatureCategory.BUTTERFLY),

    // Flowers
    BLOOMING_FLOWER(CreatureCategory.FLOWER),
    SUNFLOWER(CreatureCategory.FLOWER)
}

/**
 * Represents an animated magical companion spawned on the canvas.
 */
data class MagicCompanion(
    val id: Long,
    val type: CreatureType,
    val position: Offset,
    val size: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val spawnTimestamp: Long = System.currentTimeMillis()
)
