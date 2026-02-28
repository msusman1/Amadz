package com.talsk.amadz.domain

interface CallServiceAudioDelegate {
    fun setMicMuted(muted: Boolean)
    fun setSpeaker(enabled: Boolean)
}