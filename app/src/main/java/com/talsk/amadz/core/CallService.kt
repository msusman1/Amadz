package com.talsk.amadz.core

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.talsk.amadz.domain.CallServiceAudioDelegate
import com.talsk.amadz.domain.CallOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "CallService"

@AndroidEntryPoint
class CallService : InCallService(), CallServiceAudioDelegate {
    @Inject
    lateinit var callOrchestrator: CallOrchestrator


    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        callOrchestrator.setCallServiceAudioDelegate(this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callOrchestrator.onCallAdded(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callOrchestrator.onCallRemoved(call)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        callOrchestrator.onDestroy()
        super.onDestroy()
    }

    override fun setMicMuted(muted: Boolean) {
        this.setMuted(muted)
    }

    override fun setSpeaker(enabled: Boolean) {
        val route = if (enabled) {
            CallAudioState.ROUTE_SPEAKER
        } else {
            CallAudioState.ROUTE_EARPIECE
        }
        this.setAudioRoute(route)
    }
}
