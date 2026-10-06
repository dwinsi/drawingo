package com.example.drawingo.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.drawingo.model.AnimationSceneResult
import com.example.drawingo.model.AnimationSceneType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.random.Random

/**
 * Handles Google Gemini AI multimodal recognition of toddler drawings
 * and classifies artwork into animated scenes using a True Parallel Multi-Agent System.
 */
object GeminiMagicManager {

    private const val TAG = "GeminiMagicManager"
    private const val MODEL_NAME = "gemini-2.5-flash" // Recommended fast model for structured output


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
            Log.i(TAG, "Starting Multi-Agent Orchestration with $MODEL_NAME...")
            
            // Shared config for strict JSON output
            val jsonConfig = generationConfig {
                responseMimeType = "application/json"
            }

            // Step 1: The Vision Agent (Sequential First Step)
            val visionModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey,
                generationConfig = jsonConfig
            )

            val visionPrompt = """
                Analyze this child's drawing.
                Return ONLY a JSON object with:
                - "subject": Short 1-3 word name (e.g. Dolphin, Bird, Rocket, Car, Lion, Flower, Doodle).
                - "colors": List of main colors used.
                - "vibe": A descriptive word of the emotion (e.g. happy, fast, calm, silly).
                - "action": What the subject might be doing.
            """.trimIndent()

            val visionResponse = visionModel.generateContent(
                content {
                    image(bitmap)
                    text(visionPrompt)
                }
            )
            val visionJsonText = visionResponse.text?.trim() ?: throw Exception("Vision Agent returned null")
            Log.i(TAG, "Vision Agent Output: ${visionJsonText}")

            // Step 2: The Creative Team (Parallel Fan-Out)
            coroutineScope {
                // Agent 2: Storyteller
                val storyJob = async {
                    val model = GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey, generationConfig = jsonConfig)
                    val prompt = """
                        You are a warm, magical AI friend for kids aged 1 to 8 years.
                        Based on this context from a drawing: $visionJsonText
                        Write a 2 to 4 line super catchy, rhythmic nursery rhyme in English or Hindi/Hinglish with sound effects and emojis!
                        Return ONLY a JSON object with:
                        - "rhymeText": The generated rhyme.
                    """.trimIndent()
                    model.generateContent(prompt).text?.trim() ?: "{}"
                }

                // Agent 3: Animator
                val animatorJob = async {
                    val model = GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey, generationConfig = jsonConfig)
                    val prompt = """
                        Based on this context from a drawing: $visionJsonText
                        Select the best animation scene, particle density, and a matching hex color.
                        Valid scenes: OCEAN_LEAP, SKY_FLIGHT, SPACE_LAUNCH, LAND_SAFARI, MAGIC_DANCE.
                        Valid particle density: LOW, MEDIUM, HIGH.
                        Return ONLY a JSON object with:
                        - "sceneType": One of the valid scenes.
                        - "particleDensity": One of the valid densities.
                        - "magicColorHex": A 6-character hex color code (e.g. #FF0000).
                    """.trimIndent()
                    model.generateContent(prompt).text?.trim() ?: "{}"
                }

                // Agent 4: Musician/Audio
                val audioJob = async {
                    val model = GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey, generationConfig = jsonConfig)
                    val prompt = """
                        Based on this context from a drawing: $visionJsonText
                        Determine the best voice style and music tempo for this scene.
                        Valid voice styles: ENERGETIC, SOOTHING, SILLY.
                        Valid music tempos: FAST, SLOW, WALTZ.
                        Return ONLY a JSON object with:
                        - "voiceStyle": One of the valid styles.
                        - "musicTempo": One of the valid tempos.
                    """.trimIndent()
                    model.generateContent(prompt).text?.trim() ?: "{}"
                }

                // Step 3: Data Aggregation
                val storyJsonText = storyJob.await()
                val animatorJsonText = animatorJob.await()
                val audioJsonText = audioJob.await()

                Log.i(TAG, "Story Agent: $storyJsonText")
                Log.i(TAG, "Animator Agent: $animatorJsonText")
                Log.i(TAG, "Audio Agent: $audioJsonText")

                // Parse and Merge
                val visionJson = JSONObject(visionJsonText)
                val storyJson = JSONObject(storyJsonText)
                val animatorJson = JSONObject(animatorJsonText)
                val audioJson = JSONObject(audioJsonText)

                val sceneTypeStr = animatorJson.optString("sceneType", "MAGIC_DANCE")
                val sceneType = try {
                    AnimationSceneType.valueOf(sceneTypeStr)
                } catch (e: Exception) {
                    AnimationSceneType.MAGIC_DANCE
                }

                AnimationSceneResult(
                    sceneType = sceneType,
                    subjectName = visionJson.optString("subject", "Magic Drawing"),
                    rhymeText = storyJson.optString("rhymeText", "✨ Magic is happening! 🎨"),
                    voiceStyle = audioJson.optString("voiceStyle", "ENERGETIC"),
                    musicTempo = audioJson.optString("musicTempo", "FAST"),
                    particleDensity = animatorJson.optString("particleDensity", "MEDIUM"),
                    magicColorHex = animatorJson.optString("magicColorHex", "#FFFFFF")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating animation scene from Multi-Agent system: ${e.message}", e)
            getRandomFallbackScene()
        }
    }

    private fun getRandomFallbackScene(): AnimationSceneResult {
        return FallbackScenes[Random.nextInt(FallbackScenes.size)]
    }
}
