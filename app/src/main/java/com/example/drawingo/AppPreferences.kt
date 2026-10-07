package com.example.drawingo

import android.content.Context
import com.example.drawingo.net.AdcBackendClient

/** Local preferences for optional cloud animation and its backend endpoint. */
object AppPreferences {
    private const val PREFS_NAME = "drawingo_prefs"
    private const val KEY_CLOUD_AI_ALLOWED = "key_cloud_ai_allowed"
    private const val KEY_BACKEND_URL = "key_backend_url"
    private const val LEGACY_KEY_GEMINI_API_KEY = "key_gemini_api_key"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun removeLegacyClientApiKey(context: Context) {
        prefs(context).edit().remove(LEGACY_KEY_GEMINI_API_KEY).apply()
    }

    fun isCloudAiAllowed(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CLOUD_AI_ALLOWED, false)

    fun setCloudAiAllowed(context: Context, allowed: Boolean) {
        prefs(context).edit().putBoolean(KEY_CLOUD_AI_ALLOWED, allowed).apply()
    }

    fun getBackendUrl(context: Context): String {
        val url = prefs(context).getString(KEY_BACKEND_URL, "") ?: ""
        if (AdcBackendClient.isSecureBackendUrl(url)) {
            AdcBackendClient.customBackendUrl = url
            return url
        }
        return ""
    }

    fun setBackendUrl(context: Context, url: String) {
        val normalized = url.trim().trimEnd('/')
        require(normalized.isEmpty() || AdcBackendClient.isSecureBackendUrl(normalized)) {
            "Backend URL must use HTTPS."
        }
        prefs(context).edit().putString(KEY_BACKEND_URL, normalized).apply()
        AdcBackendClient.customBackendUrl = normalized
    }
}
