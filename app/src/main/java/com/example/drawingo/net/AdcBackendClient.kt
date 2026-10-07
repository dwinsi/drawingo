package com.example.drawingo.net

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.drawingo.model.AnimationSceneResult
import com.example.drawingo.model.AnimationSceneType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Android client connecting Drawingo to the GCP Cloud Function ADC Backend
 * (GCP Project: project-2154682a-9280-4a32-a72).
 */
object AdcBackendClient {

    private const val TAG = "AdcBackendClient"
    const val CLOUD_RUN_URL = "https://drawingo-backend-357002186662.us-central1.run.app"
    var customBackendUrl: String = ""

    private fun getCandidateUrls(): List<String> {
        if (isSecureBackendUrl(customBackendUrl)) {
            // An explicit server choice must not silently fall through to another host.
            return listOf(customBackendUrl.trimEnd('/'))
        }
        return listOf(CLOUD_RUN_URL)
    }

    fun isSecureBackendUrl(value: String): Boolean {
        val uri = Uri.parse(value)
        return uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() && uri.userInfo == null &&
            uri.fragment == null && uri.query == null &&
            (uri.path.isNullOrEmpty() || uri.path == "/")
    }

    suspend fun analyzeDrawing(context: Context, bitmap: Bitmap, interactionId: String = UUID.randomUUID().toString()): AnimationSceneResult? = withContext(Dispatchers.IO) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos)
        val base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

        val jsonPayload = JSONObject().apply {
            put("interactionId", interactionId)
            put("imageBase64", base64Image)
            put("mimeType", "image/png")
        }
        GeminiInteractionLogger.record(
            context,
            interactionId,
            "analyze_request",
            JSONObject()
                .put("url", "${getCandidateUrls().first()}/analyzeDrawing")
                .put("method", "POST")
                .put("contentType", "application/json; charset=UTF-8")
                .put("body", jsonPayload)
        )
        val encodedPayload = jsonPayload.toString().toByteArray(Charsets.UTF_8)

        for (baseUrl in getCandidateUrls()) {
            try {
                val requestStartedAt = System.currentTimeMillis()
                val url = URL("$baseUrl/analyzeDrawing")
                val connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 15000

                connection.outputStream.use { os ->
                    os.write(encodedPayload)
                }

                val responseCode = connection.responseCode
                val elapsedMs = System.currentTimeMillis() - requestStartedAt
                val responseStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val responseStr = responseStream?.use {
                    BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText()
                }.orEmpty()
                GeminiInteractionLogger.record(
                    context,
                    interactionId,
                    "analyze_response",
                    JSONObject()
                        .put("httpStatus", responseCode)
                        .put("contentType", connection.contentType)
                        .put("elapsedMs", elapsedMs)
                        .put("body", responseStr)
                )

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val json = JSONObject(responseStr)
                    val sceneStr = json.optString("sceneType", "ABSTRACT_FLOW")
                    val sceneType = when (sceneStr.uppercase()) {
                        "OCEAN_LEAP" -> AnimationSceneType.OCEAN_LEAP
                        "SKY_FLIGHT" -> AnimationSceneType.SKY_FLIGHT
                        "SPACE_LAUNCH" -> AnimationSceneType.SPACE_LAUNCH
                        "LAND_SAFARI" -> AnimationSceneType.LAND_SAFARI
                        else -> AnimationSceneType.ABSTRACT_FLOW
                    }
                    val subjectName = json.optString("subjectName", "Doodle")
                    return@withContext AnimationSceneResult(
                        sceneType = sceneType,
                        subjectName = subjectName
                    )
                }
            } catch (e: Exception) {
                GeminiInteractionLogger.record(
                    context,
                    interactionId,
                    "analyze_error",
                    JSONObject().put("message", e.toString())
                )
                Log.d(TAG, "Backend URL $baseUrl unreachable: ${e.message}")
            }
        }
        return@withContext null
    }

}
