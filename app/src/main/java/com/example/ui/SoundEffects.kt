package com.example.ui

import android.media.AudioManager
import android.media.ToneGenerator

object SoundEffects {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 95)
        } catch (e: Exception) {
            toneGen = null
        }
    }

    fun playAppleSound() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_0, 70)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun playStoneSound() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 120)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun playStartSound() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_A, 150)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun playGameOverSound() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
