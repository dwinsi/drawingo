package com.example.drawingo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.drawingo.R
import kotlin.random.Random

/**
 * Manages audio feedback using SoundPool for ultra-low-latency sound playback.
 * Handles bubble pops, gentle chimes for butterflies/flowers, and playful animal squeaks.
 */
class SoundManager(context: Context) {

    private val soundPool: SoundPool
    private var popSoundId: Int = 0
    private var chimeSoundId: Int = 0
    private var squeakSoundId: Int = 0
    private val loadedSoundIds = mutableSetOf<Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
                Log.d(TAG, "Sound loaded successfully: sampleId=$sampleId")
            } else {
                Log.e(TAG, "Failed to load sound, sampleId=$sampleId status=$status")
            }
        }

        try {
            val appCtx = context.applicationContext
            popSoundId = soundPool.load(appCtx, R.raw.bubble_pop, 1)
            chimeSoundId = soundPool.load(appCtx, R.raw.chime_twinkle, 1)
            squeakSoundId = soundPool.load(appCtx, R.raw.squeak_pop, 1)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound resources", e)
        }
    }

    /**
     * Plays the bubble pop sound effect with micro-variations in pitch.
     */
    fun playBubblePop() {
        playSound(popSoundId, 0.94f, 0.24f)
    }

    /**
     * Plays a pleasant gentle sparkle chime (for butterflies, flowers, birds).
     */
    fun playChimeTwinkle() {
        playSound(chimeSoundId, 0.96f, 0.18f)
    }

    /**
     * Plays a cute cartoon squeak (for wild animals & sea creatures).
     */
    fun playSqueak() {
        playSound(squeakSoundId, 0.95f, 0.20f)
    }

    private fun playSound(soundId: Int, baseRate: Float, rateRange: Float) {
        if (!loadedSoundIds.contains(soundId) || soundId == 0) return
        try {
            val rate = baseRate + Random.nextFloat() * rateRange
            val volume = 0.95f
            soundPool.play(soundId, volume, volume, 1, 0, rate)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing soundId=$soundId", e)
        }
    }

    fun release() {
        try {
            soundPool.release()
            loadedSoundIds.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundPool", e)
        }
    }

    companion object {
        private const val TAG = "SoundManager"
    }
}
