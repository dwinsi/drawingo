package com.example.drawingo.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.drawingo.model.AnimationSceneResult
import com.example.drawingo.model.AnimationSceneType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Handles Google Gemini AI multimodal recognition of toddler drawings
 * and classifies artwork into animated scenes with nursery rhymes (Ages 1–8).
 */
object GeminiMagicManager {

    private const val TAG = "GeminiMagicManager"
    private const val MODEL_NAME = "gemini-2.0-flash"

    private val FallbackScenes = listOf(
        AnimationSceneResult(
            sceneType = AnimationSceneType.OCEAN_LEAP,
            subjectName = "Dolphin",
            rhymeText = "✨ Splash splash! Look at the dolphin leap so high!\nJumping over ocean waves into the blue sky! 🐬🌊"
        ),
        AnimationSceneResult(
            sceneType = AnimationSceneType.OCEAN_LEAP,
            subjectName = "Little Fish",
            rhymeText = "🐠 Machhli jal ki rani hai, jeevan uska paani hai!\nSplish splash swimming in the blue sea so shiny! 🐠"
        ),
        AnimationSceneResult(
            sceneType = AnimationSceneType.SKY_FLIGHT,
            subjectName = "Little Bird",
            rhymeText = "🐦 Flying high, flying free, little bird in the sky!\nUp up up above the green green tree so high! ☁️"
        ),
        AnimationSceneResult(
            sceneType = AnimationSceneType.SKY_FLIGHT,
            subjectName = "Butterfly",
            rhymeText = "🦋 Titli udi bus par chadhi, phoolon ke sang nachi!\nFluttering wings so bright and pretty! 🌸"
        ),
        AnimationSceneResult(
            sceneType = AnimationSceneType.SPACE_LAUNCH,
            subjectName = "Rocket",
            rhymeText = "🚀 Zoom zoom vroom! Rocket flying to the stars!\nSaying hello to the moon and Mars! ⭐"
        ),
        AnimationSceneResult(
            sceneType = AnimationSceneType.LAND_SAFARI,
            subjectName = "Happy Lion",
            rhymeText = "🦁 Roar roar! The happy lion dances on the ground!\nThe cheerful lion in the jungle we found! 🦁"
        ),
        AnimationSceneResult(
            sceneType = AnimationSceneType.MAGIC_DANCE,
            subjectName = "Magic Doodle",
            rhymeText = "✨ Chanda mama door ke, naye dost aaye door ke!\nYour magic drawing is sparkling with joy! 🎨"
        )
    )

    suspend fun analyzeDrawingForAnimation(
        bitmap: Bitmap,
        apiKey: String
    ): AnimationSceneResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.i(TAG, "Gemini API key is blank. Returning curated fallback animation scene.")
            return@withContext getRandomFallbackScene()
        }

        try {
            Log.i(TAG, "Connecting to live Gemini AI model ($MODEL_NAME)...")
            val generativeModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey
            )

            val prompt = """
                You are a warm, magical AI friend for kids aged 1 to 8 years (fans of Like Nastya, Peppa Pig, ChuChu TV, Cocomelon).
                Look at this child's drawing or scribbles and analyze what it is!
                
                Respond in EXACTLY this format:
                SCENE: [OCEAN_LEAP or SKY_FLIGHT or SPACE_LAUNCH or LAND_SAFARI or MAGIC_DANCE]
                SUBJECT: [Short 1-3 word name, e.g., Dolphin, Bird, Rocket, Car, Lion, Flower, Doodle]
                RHYME: [2 to 4 line super catchy, rhythmic nursery rhyme in English or Hindi/Hinglish with sound effects and emojis!]
                
                SCENE GUIDELINES:
                - Use OCEAN_LEAP for dolphins, fish, sea turtles, octopuses, boats, water creatures.
                - Use SKY_FLIGHT for birds, butterflies, bees, airplanes, clouds, flying creatures.
                - Use SPACE_LAUNCH for rockets, cars, spaceships, stars, comets, fast vehicles.
                - Use LAND_SAFARI for lions, bears, elephants, dinosaurs, dogs, cats, land animals.
                - Use MAGIC_DANCE for general doodles, scribbles, flowers, shapes, suns.
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val responseText = response.text?.trim()

            if (!responseText.isNullOrBlank()) {
                Log.i(TAG, "Live Gemini AI response received successfully!")
                parseGeminiResponse(responseText)
            } else {
                getRandomFallbackScene()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating animation scene from Gemini API: ${e.message}", e)
            getRandomFallbackScene()
        }
    }

    private fun parseGeminiResponse(text: String): AnimationSceneResult {
        var sceneType = AnimationSceneType.MAGIC_DANCE
        var subjectName = "Magic Drawing"
        var rhymeText = text

        val lines = text.lines()
        val rhymeLines = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("SCENE:", ignoreCase = true)) {
                val valStr = trimmed.substringAfter(":").trim().uppercase()
                sceneType = when {
                    valStr.contains("OCEAN") || valStr.contains("DOLPHIN") || valStr.contains("FISH") -> AnimationSceneType.OCEAN_LEAP
                    valStr.contains("SKY") || valStr.contains("BIRD") || valStr.contains("FLIGHT") -> AnimationSceneType.SKY_FLIGHT
                    valStr.contains("SPACE") || valStr.contains("ROCKET") || valStr.contains("CAR") -> AnimationSceneType.SPACE_LAUNCH
                    valStr.contains("LAND") || valStr.contains("SAFARI") || valStr.contains("ANIMAL") -> AnimationSceneType.LAND_SAFARI
                    else -> AnimationSceneType.MAGIC_DANCE
                }
            } else if (trimmed.startsWith("SUBJECT:", ignoreCase = true)) {
                subjectName = trimmed.substringAfter(":").trim()
            } else if (trimmed.startsWith("RHYME:", ignoreCase = true)) {
                rhymeLines.add(trimmed.substringAfter(":").trim())
            } else if (trimmed.isNotEmpty() && !trimmed.contains("SCENE:") && !trimmed.contains("SUBJECT:")) {
                rhymeLines.add(trimmed)
            }
        }

        if (rhymeLines.isNotEmpty()) {
            rhymeText = rhymeLines.joinToString("\n")
        }

        return AnimationSceneResult(
            sceneType = sceneType,
            subjectName = subjectName,
            rhymeText = rhymeText
        )
    }

    private fun getRandomFallbackScene(): AnimationSceneResult {
        return FallbackScenes[Random.nextInt(FallbackScenes.size)]
    }
}
