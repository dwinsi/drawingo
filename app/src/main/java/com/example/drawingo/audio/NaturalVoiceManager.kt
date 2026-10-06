package com.example.drawingo.audio

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Natural Human Voice Synthesis Manager for Drawingo.
 * Fetches high-fidelity, natural human voice MP3 audio for English and Hindi rhymes.
 */
object NaturalVoiceManager {

    private const val TAG = "NaturalVoiceManager"
    private const val CLOUD_TTS_ENDPOINT = "https://texttospeech.googleapis.com/v1/text:synthesize"

    suspend fun fetchNaturalVoiceAudio(
        context: Context,
        text: String,
        apiKey: String
    ): File? = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext null

        // Clean emojis or special symbols for smooth natural speech
        val cleanText = text.replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27FF]"), "").trim()
        if (cleanText.isBlank()) return@withContext null

        val isHindi = cleanText.any { it in '\u0900'..'\u097F' } ||
                cleanText.contains("chanda", ignoreCase = true) ||
                cleanText.contains("titli", ignoreCase = true) ||
                cleanText.contains("pyari", ignoreCase = true) ||
                cleanText.contains("dost", ignoreCase = true) ||
                cleanText.contains("machhli", ignoreCase = true)

        val langCode = if (isHindi) "hi-IN" else "en-US"
        val shortLang = if (isHindi) "hi" else "en"

        // 1. Check local disk cache
        val cachedFile = AudioCacheManager.getCachedAudioFile(context, cleanText, langCode)
        if (cachedFile != null) {
            return@withContext cachedFile
        }

        // 2. Try Google Cloud TTS REST API if API Key is available
        if (apiKey.isNotBlank()) {
            val cloudFile = fetchFromCloudTts(context, cleanText, apiKey, langCode, isHindi)
            if (cloudFile != null) {
                return@withContext cloudFile
            }
        }

        // 3. High-Quality Natural Voice Endpoint (Requires NO API key, returns natural human MP3 audio)
        val naturalFile = fetchFromPublicNaturalVoice(context, cleanText, shortLang, langCode)
        if (naturalFile != null) {
            return@withContext naturalFile
        }

        return@withContext null
    }

    private fun fetchFromCloudTts(
        context: Context,
        text: String,
        apiKey: String,
        langCode: String,
        isHindi: Boolean
    ): File? {
        try {
            val voiceName = if (isHindi) "hi-IN-Neural2-A" else "en-US-Journey-F"
            val url = URL("$CLOUD_TTS_ENDPOINT?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val jsonPayload = JSONObject().apply {
                put("input", JSONObject().put("text", text))
                put("voice", JSONObject().apply {
                    put("languageCode", langCode)
                    put("name", voiceName)
                })
                put("audioConfig", JSONObject().apply {
                    put("audioEncoding", "MP3")
                    put("speakingRate", 0.92)
                    put("pitch", 1.2)
                })
            }

            connection.outputStream.use { os ->
                os.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val responseStr = reader.readText()
                reader.close()

                val jsonResponse = JSONObject(responseStr)
                val audioContentBase64 = jsonResponse.optString("audioContent", "")

                if (audioContentBase64.isNotBlank()) {
                    val audioBytes = Base64.decode(audioContentBase64, Base64.DEFAULT)
                    return AudioCacheManager.saveAudioToCache(context, text, langCode, audioBytes)
                }
            } else {
                Log.w(TAG, "Cloud TTS returned response code: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cloud TTS call failed, trying public natural voice stream: ${e.message}")
        }
        return null
    }

    private fun fetchFromPublicNaturalVoice(
        context: Context,
        text: String,
        shortLang: String,
        fullLangCode: String
    ): File? {
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val urlString = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encodedText&tl=$shortLang&client=tw-ob"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val baos = ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    input.copyTo(baos)
                }
                val audioBytes = baos.toByteArray()
                if (audioBytes.isNotEmpty()) {
                    Log.i(TAG, "Successfully fetched natural voice audio (${audioBytes.size} bytes)")
                    return AudioCacheManager.saveAudioToCache(context, text, fullLangCode, audioBytes)
                }
            } else {
                Log.w(TAG, "Public natural voice returned response code: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching public natural voice audio", e)
        }
        return null
    }
}
