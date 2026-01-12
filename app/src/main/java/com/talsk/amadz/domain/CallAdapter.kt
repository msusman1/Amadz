package com.talsk.amadz.domain

import android.telecom.Call
import com.talsk.amadz.domain.entity.CallState
import kotlinx.coroutines.flow.StateFlow

interface CallAdapter {
    val callState: StateFlow<CallState>
    fun dispatch(callAction: CallAction)

    fun attachAudioController(audioController: CallAudioController)
    fun detachAudioController()

    fun bindCall(call: Call)
    fun unBindCall(call: Call)

}