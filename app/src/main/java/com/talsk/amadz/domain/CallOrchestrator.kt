package com.talsk.amadz.domain

import android.telecom.Call
import com.talsk.amadz.domain.entity.CallState
import kotlinx.coroutines.flow.StateFlow

interface CallOrchestrator {
    val callState: StateFlow<CallState>

    fun onAction(callAction: CallAction)
    fun setCallServiceAudioDelegate(audioController: CallServiceAudioDelegate)
    fun onCallAdded(call: Call)
    fun onCallRemoved(call: Call)
    fun onDestroy()
}
