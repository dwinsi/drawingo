package com.example.drawingo.animation

import androidx.compose.ui.graphics.Color
import com.example.drawingo.model.Particle
import com.example.drawingo.model.ParticleType
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Low-latency particle physics engine for Drawingo animations.
 * Handles water splashes, rising bubbles, rocket smoke trails, and star sparkles.
 */
object ParticleEngine {

    private val particleIdGen = AtomicLong(1L)

    private val WaterSplashColors = listOf(
        Color(0xFF00F0FF),
        Color(0xFF80D8FF),
        Color(0xFF00E5FF),
        Color(0xFFFFFFFF)
    )

    private val StarSparkleColors = listOf(
        Color(0xFFFFD600),
        Color(0xFFFFEA00),
        Color(0xFFFF007F),
        Color(0xFF00F0FF),
        Color(0xFF39FF14)
    )

    fun createWaterSplash(x: Float, y: Float, count: Int = 18): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = -Math.PI.toFloat() * (0.15f + Random.nextFloat() * 0.70f)
            val speed = 8f + Random.nextFloat() * 16f
            val color = WaterSplashColors[Random.nextInt(WaterSplashColors.size)]

            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x + (Random.nextFloat() - 0.5f) * 20f,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = 8f + Random.nextFloat() * 14f,
                    type = ParticleType.WATER_SPLASH,
                    maxLifeMs = (500 + Random.nextInt(400)).toLong()
                )
            )
        }
        return list
    }

    fun createBubbles(x: Float, y: Float, count: Int = 8): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x + (Random.nextFloat() - 0.5f) * 40f,
                    y = y + Random.nextFloat() * 20f,
                    vx = (Random.nextFloat() - 0.5f) * 2f,
                    vy = -(2f + Random.nextFloat() * 4f),
                    color = Color(0x9900E5FF),
                    size = 10f + Random.nextFloat() * 18f,
                    type = ParticleType.BUBBLE,
                    maxLifeMs = (1200 + Random.nextInt(600)).toLong()
                )
            )
        }
        return list
    }

    fun createStarDust(x: Float, y: Float, count: Int = 12): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.28f
            val speed = 3f + Random.nextFloat() * 8f
            val color = StarSparkleColors[Random.nextInt(StarSparkleColors.size)]

            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = 6f + Random.nextFloat() * 12f,
                    type = ParticleType.STAR_DUST,
                    maxLifeMs = (600 + Random.nextInt(400)).toLong()
                )
            )
        }
        return list
    }

    fun createSmokePuffs(x: Float, y: Float, count: Int = 6): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.28f
            val speed = 2f + Random.nextFloat() * 5f
            val isFire = Random.nextBoolean()
            val color = if (isFire) Color(0xFFFF6D00) else Color(0xCCECEFF1)

            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x + (Random.nextFloat() - 0.5f) * 12f,
                    y = y + (Random.nextFloat() - 0.5f) * 12f,
                    vx = cos(angle) * speed - 4f,
                    vy = sin(angle) * speed + 2f,
                    color = color,
                    size = 12f + Random.nextFloat() * 20f,
                    type = ParticleType.SMOKE_PUFF,
                    maxLifeMs = (700 + Random.nextInt(400)).toLong()
                )
            )
        }
        return list
    }

    fun createClouds(x: Float, y: Float, count: Int = 1): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x,
                    y = y,
                    vx = -0.5f - Random.nextFloat() * 1.5f, // Drift left
                    vy = (Random.nextFloat() - 0.5f) * 0.2f,
                    color = Color(0xF2FFFFFF),
                    size = 40f + Random.nextFloat() * 60f,
                    type = ParticleType.CLOUD,
                    maxLifeMs = (4000 + Random.nextInt(2000)).toLong()
                )
            )
        }
        return list
    }

    fun createStars(x: Float, y: Float, count: Int = 2): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x,
                    y = y,
                    vx = 0f,
                    vy = 3f + Random.nextFloat() * 8f, // Fall straight down quickly
                    color = Color.White,
                    size = 2f + Random.nextFloat() * 4f,
                    type = ParticleType.STAR,
                    maxLifeMs = (1500 + Random.nextInt(1000)).toLong()
                )
            )
        }
        return list
    }

    fun createRainbowSparkles(x: Float, y: Float, count: Int = 4): List<Particle> {
        val list = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 6.28f
            val speed = 1f + Random.nextFloat() * 3f
            // Pick a random vibrant color
            val color = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color(0xFFFF00FF)).random()

            list.add(
                Particle(
                    id = particleIdGen.getAndIncrement(),
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    size = 4f + Random.nextFloat() * 8f,
                    type = ParticleType.RAINBOW_SPARKLE,
                    maxLifeMs = (800 + Random.nextInt(500)).toLong()
                )
            )
        }
        return list
    }

    fun updateParticles(particles: List<Particle>, deltaMs: Long): List<Particle> {
        val deltaSec = deltaMs / 1000f
        val alive = mutableListOf<Particle>()

        for (p in particles) {
            val age = System.currentTimeMillis() - p.spawnTimeMs
            val lifeRatio = 1.0f - (age.toFloat() / p.maxLifeMs)

            if (lifeRatio > 0f) {
                p.life = lifeRatio
                p.alpha = lifeRatio.coerceIn(0f, 1f)

                p.x += p.vx
                p.y += p.vy

                when (p.type) {
                    ParticleType.WATER_SPLASH -> {
                        p.vy += 0.42f // Gravity
                    }
                    ParticleType.BUBBLE -> {
                        p.vy -= 0.05f // Floating upward buoyancy
                        p.vx += sin(age * 0.01f).toFloat() * 0.3f
                    }
                    ParticleType.STAR_DUST -> {
                        p.vx *= 0.95f
                        p.vy *= 0.95f
                    }
                    ParticleType.SMOKE_PUFF -> {
                        p.size += 0.8f // Expansion
                        p.vx *= 0.92f
                    }
                    ParticleType.RAINBOW_SPARKLE -> {
                        p.vy += 0.1f
                    }
                    ParticleType.CLOUD -> {
                        p.size += 0.1f // Very slow expansion
                    }
                    ParticleType.STAR -> {
                        // Just fall
                    }
                }

                alive.add(p)
            }
        }
        return alive
    }
}
