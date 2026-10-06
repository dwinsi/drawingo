package com.example.drawingo.net

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.drawingo.audio.AudioCacheManager
import com.example.drawingo.model.AnimationSceneResult
import com.example.drawingo.model.AnimationSceneType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android client connecting Drawingo to the GCP Cloud Function ADC Backend
 * (GCP Project: project-2154682a-9280-4a32-a72).
 */
object AdcBackendClient {

    private const val TAG = "AdcBackendClient"
    const val CLOUD_RUN_URL = "https://drawingo-backend-357002186662.us-central1.run.app"
    var customBackendUrl: String = ""

    private fun getCandidateUrls(): List<String> {
        val list = mutableListOf<String>()
        if (isSecureBackendUrl(customBackendUrl)) {
            list.add(customBackendUrl.trimEnd('/'))
        }
        list.add(CLOUD_RUN_URL)
        return list.distinct()
    }

    fun isSecureBackendUrl(value: String): Boolean {
        val uri = Uri.parse(value)
        return uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() && uri.userInfo == null &&
            uri.fragment == null && uri.query == null &&
            (uri.path.isNullOrEmpty() || uri.path == "/")
    }

    suspend fun analyzeDrawing(bitmap: Bitmap): AnimationSceneResult? = withContext(Dispatchers.IO) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

        val jsonPayload = JSONObject().apply {
            put("imageBase64", base64Image)
            put("mimeType", "image/png")
        }.toString().toByteArray(Charsets.UTF_8)

        for (baseUrl in getCandidateUrls()) {
            try {
                val url = URL("$baseUrl/analyzeDrawing")
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 15000

                connection.outputStream.use { os ->
                    os.write(jsonPayload)
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                    val responseStr = reader.readText()
                    reader.close()

                    val json = JSONObject(responseStr)
                    val sceneStr = json.optString("sceneType", "MAGIC_DANCE")
                    val sceneType = when (sceneStr.uppercase()) {
                        "OCEAN_LEAP" -> AnimationSceneType.OCEAN_LEAP
                        "SKY_FLIGHT" -> AnimationSceneType.SKY_FLIGHT
                        "SPACE_LAUNCH" -> AnimationSceneType.SPACE_LAUNCH
                        "LAND_SAFARI" -> AnimationSceneType.LAND_SAFARI
                        else -> AnimationSceneType.MAGIC_DANCE
                    }
                    val subjectName = json.optString("subjectName", "Magic Drawing")
                    val rhymeText = json.optString("rhymeText", "")

                    return@withContext AnimationSceneResult(
                        sceneType = sceneType,
                        subjectName = subjectName,
                        rhymeText = rhymeText
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "Backend URL $baseUrl unreachable: ${e.message}")
            }
        }
        return@withContext null
    }

    suspend fun synthesizeSpeech(context: Context, text: String): File? = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext null

        val jsonPayload = JSONObject().apply {
            put("text", text)
        }.toString().toByteArray(Charsets.UTF_8)

        for (baseUrl in getCandidateUrls()) {
            try {
                val url = URL("$baseUrl/synthesizeSpeech")
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 15000

                connection.outputStream.use { os ->
                    os.write(jsonPayload)
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                    val responseStr = reader.readText()
                    reader.close()

                    val json = JSONObject(responseStr)
                    val audioBase64 = json.optString("audioBase64", "")
                    val langCode = json.optString("languageCode", "en-US")

                    if (audioBase64.isNotBlank()) {
                        val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
                        Log.i(TAG, "Successfully received natural voice audio from ADC backend at $baseUrl")
                        return@withContext AudioCacheManager.saveAudioToCache(context, text, langCode, audioBytes)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Backend URL $baseUrl speech unreachable: ${e.message}")
            }
        }
        return@withContext null
    }

    /**
     * Fetches the dynamic stock sketches catalog from the backend.
     */
    suspend fun getStockSketches(category: String? = null): List<com.example.drawingo.model.StockSketch>? = withContext(Dispatchers.IO) {
        val query = if (category != null && category.isNotBlank() && category != "ALL") "?category=$category" else ""

        for (baseUrl in getCandidateUrls()) {
            try {
                val url = URL("$baseUrl/sketches$query")
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 10000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                    val responseStr = reader.readText()
                    reader.close()

                    val json = JSONObject(responseStr)
                    val array = json.optJSONArray("sketches") ?: continue
                    val list = mutableListOf<com.example.drawingo.model.StockSketch>()

                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val tagsList = mutableListOf<String>()
                        val tagsArray = item.optJSONArray("tags")
                        if (tagsArray != null) {
                            for (t in 0 until tagsArray.length()) {
                                tagsList.add(tagsArray.getString(t))
                            }
                        }

                        list.add(
                            com.example.drawingo.model.StockSketch(
                                id = item.optString("id", "sketch_$i"),
                                title = item.optString("title", "Sketch"),
                                category = com.example.drawingo.model.SketchCategory.fromString(item.optString("category")),
                                emoji = item.optString("emoji", "🎨"),
                                difficulty = item.optString("difficulty", "EASY"),
                                tags = tagsList,
                                imageUrl = item.optString("imageUrl", ""),
                                thumbnailUrl = if (item.has("thumbnailUrl") && !item.isNull("thumbnailUrl")) item.getString("thumbnailUrl") else null,
                                assetPath = if (item.has("assetPath") && !item.isNull("assetPath")) item.getString("assetPath") else null,
                                createdAt = if (item.has("createdAt") && !item.isNull("createdAt")) item.getString("createdAt") else null
                            )
                        )
                    }
                    Log.i(TAG, "Successfully fetched ${list.size} sketches from $baseUrl")
                    return@withContext list
                }
            } catch (e: Exception) {
                Log.d(TAG, "Backend URL $baseUrl sketches unreachable: ${e.message}")
            }
        }
        return@withContext null
    }
}
