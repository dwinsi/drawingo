package com.example.drawingo.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Speech synthesis manager for Drawingo to read nursery rhymes
 * aloud for kids aged 1–8 in English & Hindi.
 */
class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localeHindi = Locale("hi", "IN")
            val result = tts?.setLanguage(localeHindi)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Hindi language not supported, falling back to English")
                tts?.setLanguage(Locale.ENGLISH)
            }
            tts?.setPitch(1.15f) // Slightly higher energetic child-friendly pitch
            tts?.setSpeechRate(0.92f) // Slightly slower rate for clarity for toddlers

            isInitialized = true
            Log.d(TAG, "TextToSpeech initialized successfully")
        } else {
            Log.e(TAG, "TextToSpeech initialization failed status=$status")
        }
    }

    fun speakRhyme(text: String) {
        if (!isInitialized || text.isBlank()) return

        try {
            tts?.stop()

            val localeHindi = Locale("hi", "IN")
            val hiResult = tts?.setLanguage(localeHindi)
            if (hiResult == TextToSpeech.LANG_MISSING_DATA || hiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }

            tts?.setPitch(1.15f)
            tts?.setSpeechRate(0.92f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DRAWINGO_RHYME_UTTERANCE")
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text with TTS", e)
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing TTS", e)
        }
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
