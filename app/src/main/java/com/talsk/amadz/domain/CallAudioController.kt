package com.talsk.amadz.domain

interface CallAudioController {
    fun setMuted(muted: Boolean)
    fun setSpeaker(enabled: Boolean)
}