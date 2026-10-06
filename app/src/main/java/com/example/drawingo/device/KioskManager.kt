package com.example.drawingo.device

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.drawingo.net.AdcBackendClient

/**
 * Manages optional Kiosk Mode preferences and device locking capabilities for Drawingo.
 */
object KioskManager {

    private const val PREFS_NAME = "drawingo_prefs"
    private const val KEY_KIOSK_ENABLED = "key_kiosk_enabled"
    private const val KEY_CLOUD_AI_ALLOWED = "key_cloud_ai_allowed"
    private const val LEGACY_KEY_GEMINI_API_KEY = "key_gemini_api_key"
    private const val KEY_BACKEND_URL = "key_backend_url"
    private const val KEY_SCREEN_TIME_LIMIT = "key_screen_time_limit"
    private const val KEY_SCREEN_TIME_DAY = "key_screen_time_day"
    private const val KEY_SCREEN_TIME_USED_MS = "key_screen_time_used_ms"
    private const val TAG = "KioskManager"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun removeLegacyClientApiKey(context: Context) {
        getPrefs(context).edit().remove(LEGACY_KEY_GEMINI_API_KEY).apply()
    }

    fun isKioskModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_KIOSK_ENABLED, false)
    }

    fun setKioskModeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_KIOSK_ENABLED, enabled).apply()
    }

    fun isCloudAiAllowed(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CLOUD_AI_ALLOWED, false)
    }

    fun setCloudAiAllowed(context: Context, allowed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CLOUD_AI_ALLOWED, allowed).apply()
    }

    fun getBackendUrl(context: Context): String {
        val url = getPrefs(context).getString(KEY_BACKEND_URL, "") ?: ""
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
        getPrefs(context).edit().putString(KEY_BACKEND_URL, normalized).apply()
        AdcBackendClient.customBackendUrl = normalized
    }

    fun getScreenTimeLimit(context: Context): Int {
        // Default to 30 minutes
        return getPrefs(context).getInt(KEY_SCREEN_TIME_LIMIT, 30)
    }

    fun setScreenTimeLimit(context: Context, limitMins: Int) {
        getPrefs(context).edit().putInt(KEY_SCREEN_TIME_LIMIT, limitMins.coerceIn(15, 120)).apply()
    }

    fun getRemainingScreenTimeMs(context: Context): Long {
        val prefs = getPrefs(context)
        val today = todayKey()
        if (prefs.getString(KEY_SCREEN_TIME_DAY, null) != today) {
            prefs.edit().putString(KEY_SCREEN_TIME_DAY, today).putLong(KEY_SCREEN_TIME_USED_MS, 0L).apply()
        }
        val limitMs = getScreenTimeLimit(context).coerceIn(1, 24 * 60) * 60_000L
        return (limitMs - prefs.getLong(KEY_SCREEN_TIME_USED_MS, 0L)).coerceAtLeast(0L)
    }

    fun recordScreenTime(context: Context, durationMs: Long) {
        if (durationMs <= 0L) return
        val prefs = getPrefs(context)
        val today = todayKey()
        val used = if (prefs.getString(KEY_SCREEN_TIME_DAY, null) == today) {
            prefs.getLong(KEY_SCREEN_TIME_USED_MS, 0L)
        } else 0L
        prefs.edit()
            .putString(KEY_SCREEN_TIME_DAY, today)
            .putLong(KEY_SCREEN_TIME_USED_MS, used + durationMs)
            .apply()
    }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    fun configureDeviceOwnerIfPresent(context: Context) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val adminComponent = ComponentName(context, DrawingoDeviceAdminReceiver::class.java)
            if (dpm != null && dpm.isDeviceOwnerApp(context.packageName)) {
                dpm.setLockTaskPackages(adminComponent, arrayOf(context.packageName))
                Log.i(TAG, "Configured lock task packages for Device Owner")
            }
        } catch (e: Exception) {
            Log.w(TAG, "DevicePolicyManager setup note: ${e.message}")
        }
    }

    fun applyKioskState(activity: Activity) {
        if (isKioskModeEnabled(activity)) {
            configureDeviceOwnerIfPresent(activity)
            try {
                activity.startLockTask()
                Log.d(TAG, "startLockTask() called for optional Kiosk mode")
            } catch (e: Exception) {
                Log.w(TAG, "startLockTask() error: ${e.message}")
            }
        } else {
            try {
                activity.stopLockTask()
                Log.d(TAG, "stopLockTask() called as Kiosk mode is disabled")
            } catch (e: Exception) {
                // Normal if not in lock task
            }
        }
    }
}
