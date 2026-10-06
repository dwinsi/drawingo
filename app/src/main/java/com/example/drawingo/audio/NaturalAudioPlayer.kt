package com.example.drawingo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import java.io.File

/**
 * Native Audio Player for Drawingo to play high-fidelity
 * MP3 natural human voice rhymes.
 */
class NaturalAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null

    fun playAudioFile(file: File, onCompletion: () -> Unit = {}) {
        if (!file.exists() || file.length() <= 0) return

        try {
            stop()
            currentFile = file

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    Log.d(TAG, "Finished playing natural voice audio")
                    onCompletion()
                }
                prepare()
                start()
            }
            mediaPlayer = player
            Log.d(TAG, "Started playing natural voice file: ${file.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing natural audio file: ${e.message}", e)
        }
    }

    fun replay() {
        val file = currentFile
        if (file != null && file.exists()) {
            playAudioFile(file)
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaPlayer", e)
        }
    }

    fun release() {
        try {
            stop()
            currentFile = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing NaturalAudioPlayer", e)
        }
    }

    companion object {
        private const val TAG = "NaturalAudioPlayer"
    }
}
