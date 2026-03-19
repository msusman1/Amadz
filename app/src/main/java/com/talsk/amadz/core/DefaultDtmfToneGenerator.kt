package com.talsk.amadz.core

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.provider.Settings
import com.talsk.amadz.domain.DtmfToneGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

fun Char.toTone(): Int {
    return when (this) {
        '0' -> ToneGenerator.TONE_DTMF_0
        '1' -> ToneGenerator.TONE_DTMF_1
        '2' -> ToneGenerator.TONE_DTMF_2
        '3' -> ToneGenerator.TONE_DTMF_3
        '4' -> ToneGenerator.TONE_DTMF_4
        '5' -> ToneGenerator.TONE_DTMF_5
        '6' -> ToneGenerator.TONE_DTMF_6
        '7' -> ToneGenerator.TONE_DTMF_7
        '8' -> ToneGenerator.TONE_DTMF_8
        '9' -> ToneGenerator.TONE_DTMF_9
        '*' -> ToneGenerator.TONE_DTMF_S
        '#' -> ToneGenerator.TONE_DTMF_P
        else -> ToneGenerator.TONE_PROP_BEEP
    }
}

class DefaultDtmfToneGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) : DtmfToneGenerator {
    private val toneGenerator by lazy {
        ToneGenerator(AudioManager.STREAM_DTMF, 80)
    }

    override fun startTone(digit: Char) {
        if (!shouldPlayTone()) return
        toneGenerator.startTone(digit.toTone())
    }

    override fun stopTone() {
        toneGenerator.stopTone()
    }

    fun release() {
        toneGenerator.release()
    }

    private fun shouldPlayTone(): Boolean {
        if (!DtmfTonePrefs.isEnabled(context)) return false

        val audioManager = context.getSystemService(AudioManager::class.java) ?: return false
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return false
        if (audioManager.getStreamVolume(AudioManager.STREAM_DTMF) == 0) return false

        val resolver = context.contentResolver
        val dtmfEnabled =
            Settings.System.getInt(resolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1
        return dtmfEnabled
    }
}