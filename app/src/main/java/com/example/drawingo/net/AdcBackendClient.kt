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
import java.io.File
import java.io.FileOutputStream
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

    /** Generates a short Veo clip from the drawing and saves it only in app-private cache. */
    suspend fun generateVideo(
        context: Context,
        bitmap: Bitmap,
        prompt: String,
        interactionId: String = UUID.randomUUID().toString()
    ): File? = withContext(Dispatchers.IO) {
        val baseUrl = getCandidateUrls().first()
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 82, baos)
        val imageBytes = baos.toByteArray()
        if (imageBytes.size > 5 * 1024 * 1024) return@withContext null
        val request = JSONObject()
            .put("imageBase64", Base64.encodeToString(imageBytes, Base64.NO_WRAP))
            .put("mimeType", "image/jpeg")
            .put("prompt", prompt.take(800))
        GeminiInteractionLogger.record(context, interactionId, "veo_video_request", JSONObject()
            .put("url", "$baseUrl/generateVideo")
            .put("method", "POST")
            .put("body", request)
            .put("prompt", prompt.take(800))
            .put("mimeType", "image/jpeg")
            .put("imageBytes", imageBytes.size))

        try {
            val submit = postJson("$baseUrl/generateVideo", request, 60_000)
            if (submit.first !in 200..299) {
                GeminiInteractionLogger.record(context, interactionId, "veo_video_submit_error", JSONObject()
                    .put("httpStatus", submit.first).put("body", submit.second.take(2000)))
                return@withContext null
            }
            val submitResponse = JSONObject(submit.second)
            val operationId = submitResponse.optString("operationId")
            GeminiInteractionLogger.record(context, interactionId, "veo_video_submitted", JSONObject()
                .put("httpStatus", submit.first)
                .put("operationId", operationId)
                .put("model", submitResponse.optString("model")))
            if (operationId.isBlank()) return@withContext null

            val startedAt = System.currentTimeMillis()
            while (System.currentTimeMillis() - startedAt < 5 * 60_000L) {
                kotlinx.coroutines.delay(10_000)
                val status = postJson("$baseUrl/videoStatus", JSONObject().put("operationId", operationId), 90_000)
                if (status.first == 202) {
                    GeminiInteractionLogger.record(context, interactionId, "veo_video_poll", JSONObject()
                        .put("httpStatus", status.first)
                        .put("body", status.second.take(1000)))
                    continue
                }
                if (status.first !in 200..299) {
                    GeminiInteractionLogger.record(context, interactionId, "veo_video_response_error", JSONObject()
                        .put("httpStatus", status.first).put("body", status.second.take(2000)))
                    return@withContext null
                }
                val response = JSONObject(status.second)
                val videoBase64 = response.getString("videoBase64")
                if (videoBase64.length > 60 * 1024 * 1024) return@withContext null
                val videoBytes = Base64.decode(videoBase64, Base64.DEFAULT)
                if (videoBytes.size > 45 * 1024 * 1024) return@withContext null
                val model = response.optString("model", "veo-3.1-lite-generate-001")
                GeminiInteractionLogger.record(context, interactionId, "veo_video_response", JSONObject()
                    .put("httpStatus", status.first).put("status", response.optString("status"))
                    .put("model", model).put("mimeType", response.optString("mimeType"))
                    .put("videoBase64Characters", videoBase64.length))
                val videoFile = File(context.cacheDir, "drawingo-${UUID.randomUUID()}.mp4")
                FileOutputStream(videoFile).use { it.write(videoBytes) }
                GeminiInteractionLogger.record(context, interactionId, "veo_video_complete", JSONObject()
                    .put("model", model)
                    .put("videoBytes", videoBytes.size)
                    .put("cacheFile", videoFile.name))
                return@withContext videoFile
            }
            GeminiInteractionLogger.record(context, interactionId, "veo_video_timeout", JSONObject())
            null
        } catch (error: Exception) {
            GeminiInteractionLogger.record(context, interactionId, "veo_video_error", JSONObject().put("message", error.toString()))
            null
        }
    }

    private fun postJson(urlValue: String, payload: JSONObject, readTimeoutMs: Int): Pair<Int, String> {
        val connection = URL(urlValue).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.doOutput = true
        connection.connectTimeout = 15_000
        connection.readTimeout = readTimeoutMs
        connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() }.orEmpty()
        connection.disconnect()
        return code to body
    }

}
