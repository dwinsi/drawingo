package com.example.drawingo.audio

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Manages local disk caching of synthesized MP3 voice files
 * in context.cacheDir/voice_cache for instant replay and zero latency.
 */
object AudioCacheManager {

    private const val TAG = "AudioCacheManager"
    private const val CACHE_DIR_NAME = "voice_cache"

    private fun getCacheDirectory(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun generateHashKey(text: String, lang: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest("$lang:$text".toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            (lang + text).hashCode().toString()
        }
    }

    fun getCachedAudioFile(context: Context, text: String, lang: String): File? {
        val key = generateHashKey(text, lang)
        val file = File(getCacheDirectory(context), "$key.mp3")
        return if (file.exists() && file.length() > 0) {
            Log.d(TAG, "Cache hit for voice key: $key")
            file
        } else {
            null
        }
    }

    fun saveAudioToCache(context: Context, text: String, lang: String, bytes: ByteArray): File? {
        return try {
            val key = generateHashKey(text, lang)
            val file = File(getCacheDirectory(context), "$key.mp3")
            FileOutputStream(file).use { fos ->
                fos.write(bytes)
            }
            Log.d(TAG, "Successfully cached audio file: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving audio to cache", e)
            null
        }
    }
}
