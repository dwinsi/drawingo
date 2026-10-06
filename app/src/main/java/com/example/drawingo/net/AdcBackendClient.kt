package com.example.drawingo.net

import android.content.Context
import android.graphics.Bitmap
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
    var customBackendUrl: String = ""

    private fun getCandidateUrls(): List<String> {
        val list = mutableListOf<String>()
        if (customBackendUrl.isNotBlank()) {
            list.add(customBackendUrl.trimEnd('/'))
        }
        list.add("http://10.0.2.2:8080")
        list.add("http://127.0.0.1:8080")
        list.add("http://localhost:8080")
        return list.distinct()
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
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 4000
                connection.readTimeout = 6000

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

                    Log.i(TAG, "Successfully connected to ADC backend at $baseUrl! Subject: $subjectName")
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
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 4000
                connection.readTimeout = 6000

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
}
