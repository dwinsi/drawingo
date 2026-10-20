package com.example.drawingo.model

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

/**
 * Scene categories for drawing-to-animation transformations.
 */
enum class AnimationSceneType {
    OCEAN_LEAP,   // Dolphin, Fish, Sea Turtle, Octopus, Sea Creature
    SKY_FLIGHT,   // Bird, Butterfly, Bee, Airplane, Feather
    SPACE_LAUNCH, // Rocket, Car, Spaceship, Star, Comet
    LAND_SAFARI,  // Lion, Elephant, Bear, Dinosaur, Cat, Dog, Animal
    ABSTRACT_FLOW // Generic sketches and abstract forms
}

enum class ParticleType {
    WATER_SPLASH,
    BUBBLE,
    STAR_DUST,
    SMOKE_PUFF,
    RAINBOW_SPARKLE,
    CLOUD,
    STAR
}

enum class AnimationCategory(
    val id: String,
    val title: String,
    val icon: String,
    val defaultParticleType: ParticleType = ParticleType.STAR_DUST,
    val defaultBackgroundColor: String = "#F0F4FF"
) {
    FLOAT("FLOAT", "Float", "☁️", ParticleType.STAR_DUST, "#F0F4FF"),
    ROTATE("ROTATE", "Rotate", "🔄", ParticleType.RAINBOW_SPARKLE, "#F7F0FF"),
    SPIN("SPIN", "Spin", "💫", ParticleType.STAR, "#0D1117"),
    FLIP("FLIP", "Flip", "🪞", ParticleType.CLOUD, "#E8F4FD"),
    TRANSLATE("TRANSLATE", "Translate", "↔️", ParticleType.SMOKE_PUFF, "#F0FFF4"),
    BOUNCE("BOUNCE", "Bounce", "🏀", ParticleType.BUBBLE, "#FFF8F0"),
    PULSE("PULSE", "Pulse", "💓", ParticleType.RAINBOW_SPARKLE, "#FFF0F5"),
    WIGGLE("WIGGLE", "Wiggle", "〰️", ParticleType.STAR_DUST, "#F5F0FF")
}

data class DynamicSceneConfig(
    val backgroundColor: String,
    val particleType: String,
    val animationStyle: String
)

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float = 1.0f,
    var life: Float = 1.0f, // 1.0 down to 0.0
    val maxLifeMs: Long = 800L,
    val spawnTimeMs: Long = System.currentTimeMillis(),
    val type: ParticleType = ParticleType.RAINBOW_SPARKLE
)

data class AnimatedDrawingEntity(
    val strokes: List<DrawnStroke>,
    val bounds: Rect,
    var scale: Float = 1.0f,
    var rotation: Float = 0f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    val dynamicSceneConfig: DynamicSceneConfig? = null
)

data class AnimationSceneResult(
    val sceneType: AnimationSceneType,
    val subjectName: String
)
