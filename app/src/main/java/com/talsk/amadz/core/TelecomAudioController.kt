package com.talsk.amadz.core

import android.telecom.CallAudioState
import android.telecom.InCallService
import com.talsk.amadz.domain.CallAudioController

class TelecomAudioController(
    private val service: InCallService
) : CallAudioController {

    override fun setMuted(muted: Boolean) {
        service.setMuted(muted)
    }

    override fun setSpeaker(enabled: Boolean) {
        val route = if (enabled) {
            CallAudioState.ROUTE_SPEAKER
        } else {
            CallAudioState.ROUTE_EARPIECE
        }
        service.setAudioRoute(route)
    }
}
