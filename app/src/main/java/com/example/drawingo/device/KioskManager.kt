package com.example.drawingo.device

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.drawingo.BuildConfig
import com.example.drawingo.net.AdcBackendClient

/**
 * Manages optional Kiosk Mode preferences and device locking capabilities for Drawingo.
 */
object KioskManager {

    private const val PREFS_NAME = "drawingo_prefs"
    private const val KEY_KIOSK_ENABLED = "key_kiosk_enabled"
    private const val KEY_GEMINI_API_KEY = "key_gemini_api_key"
    private const val KEY_BACKEND_URL = "key_backend_url"
    private const val TAG = "KioskManager"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isKioskModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_KIOSK_ENABLED, false)
    }

    fun setKioskModeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_KIOSK_ENABLED, enabled).apply()
    }

    fun getGeminiApiKey(context: Context): String {
        val userKey = getPrefs(context).getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (userKey.isNotBlank()) return userKey
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun setGeminiApiKey(context: Context, apiKey: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
    }

    fun getBackendUrl(context: Context): String {
        val url = getPrefs(context).getString(KEY_BACKEND_URL, "") ?: ""
        if (url.isNotBlank()) {
            AdcBackendClient.customBackendUrl = url
        }
        return url
    }

    fun setBackendUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_BACKEND_URL, url).apply()
        AdcBackendClient.customBackendUrl = url
    }

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
