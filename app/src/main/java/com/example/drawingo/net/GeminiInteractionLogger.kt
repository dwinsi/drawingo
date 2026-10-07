package com.example.drawingo.net

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.time.Instant

/** Stores complete Gemini HTTP exchanges in app-private storage for local diagnostics. */
object GeminiInteractionLogger {
    private const val TAG = "GeminiInteractionLog"
    private const val FILE_NAME = "interactions.ndjson"

    @Synchronized
    fun record(context: Context, interactionId: String, phase: String, details: JSONObject) {
        try {
            val directory = File(context.applicationContext.filesDir, "gemini_logs")
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(TAG, "Could not create private log directory")
                return
            }
            val event = JSONObject()
                .put("timestamp", Instant.now().toString())
                .put("interactionId", interactionId)
                .put("phase", phase)
                .put("details", details)
            File(directory, FILE_NAME).appendText(event.toString() + "\n", Charsets.UTF_8)
        } catch (error: Exception) {
            // Diagnostics must never prevent an animation or Gemini request from completing.
            Log.e(TAG, "Failed to write local Gemini interaction log", error)
        }
    }

    fun logFile(context: Context): File = File(context.applicationContext.filesDir, "gemini_logs/$FILE_NAME")

    @Synchronized
    fun readLogs(context: Context): List<JSONObject> {
        val file = logFile(context)
        if (!file.exists()) return emptyList()
        return try {
            file.readLines(Charsets.UTF_8).filter { it.isNotBlank() }.map { JSONObject(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun clearLogs(context: Context) {
        val file = logFile(context)
        if (file.exists()) file.delete()
    }
}
