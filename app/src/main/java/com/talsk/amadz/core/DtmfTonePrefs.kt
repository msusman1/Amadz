package com.talsk.amadz.core

import android.content.Context

object DtmfTonePrefs {
    const val PREFS_NAME = "amadz_prefs"
    const val KEY_DTMF_TONE_ENABLED = "dtmf_tone_enabled"

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DTMF_TONE_ENABLED, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DTMF_TONE_ENABLED, enabled).apply()
    }
}
