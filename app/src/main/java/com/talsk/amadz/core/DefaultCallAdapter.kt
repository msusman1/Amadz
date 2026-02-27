package com.talsk.amadz.core

import android.content.Context
import android.telecom.Call
import android.telecom.Call.Callback
import android.telecom.VideoProfile
import android.telephony.TelephonyManager
import android.util.Log
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.CallAudioController
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.entity.CallDirection
import com.talsk.amadz.domain.entity.CallState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


fun Int.toSimStateReadable(): String {
    return when (this) {
        TelephonyManager.SIM_STATE_ABSENT -> "No Sim available"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card Io error"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Sim card restricted"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
        TelephonyManager.SIM_STATE_NOT_READY -> "Sim not ready"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "PERM disabled"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
        TelephonyManager.SIM_STATE_READY -> "Sim is ready"
        else -> "Unknown"
    }
}

class DefaultCallAdapter @Inject constructor(
    private val notificationController: NotificationController,
    @ApplicationContext context: Context,
) : CallAdapter {
    val TAG = "DefaultCallAdapter"

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()
    private val scope = MainScope()
    private var currentCall: Call? = null
    private var timerJob: Job? = null
    private var audioController: CallAudioController? = null

    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)

    private fun checkSimState() {
        val simState = telephonyManager.simState
        if (simState != TelephonyManager.SIM_STATE_READY) {
            _callState.value = CallState.SimError(
                simState = simState, message = simState.toSimStateReadable()
            )
        }
    }

    private val telecomCallback = object : Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            Log.d(
                TAG,
                "onStateChanged: state:${state}, Phone:${call.callerPhone()}, Name :${call.callerName()}"
            )
            mapCallState(call, state)
        }
    }

    override fun dispatch(callAction: CallAction) {
        when (callAction) {
            CallAction.Answer -> {
                currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
            }

            CallAction.Hangup -> {
                currentCall?.disconnect()
            }

            is CallAction.Hold -> {
                if (callAction.enabled) currentCall?.hold() else currentCall?.unhold()
            }

            is CallAction.Mute -> {
                audioController?.setMuted(callAction.enabled)
            }

            is CallAction.Speaker -> {
                audioController?.setSpeaker(callAction.enabled)
            }

            is CallAction.StartDialTone -> {
                currentCall?.playDtmfTone(callAction.char)
            }

            CallAction.StopDialTone -> {
                currentCall?.stopDtmfTone()
            }
        }

    }

    override fun attachAudioController(audioController: CallAudioController) {
        this.audioController = audioController
    }

    override fun detachAudioController() {
        this.audioController = null
    }

    override fun bindCall(call: Call) {
        Log.d(TAG, "bindCall: ${call.details}")
        checkSimState()
        currentCall?.unregisterCallback(telecomCallback)
        mapCallState(call, call.state)
        this.currentCall = call
        currentCall?.registerCallback(telecomCallback)
    }

    private fun mapCallState(call: Call, state: Int) {
        if (state == Call.STATE_ACTIVE) {
            scope.launch {
                notificationController.displayOngoingCallNotification(call.callerPhone())
            }
        }

        if (state == Call.STATE_DISCONNECTED) {
            notificationController.cancelOngoingCallNotification()
            notificationController.cancelIncomingCallNotification()
            notificationController.stepCallRingTone()
        }

        when (state) {
            Call.STATE_ACTIVE -> { //call connected
                _callState.value = CallState.Active(
                    0, isMuted = false, isSpeakerOn = false, isOnHold = false
                )
                startTimer()
                notificationController.stepCallRingTone()
            }


            Call.STATE_CONNECTING -> {
                _callState.value = CallState.Connecting
            }

            Call.STATE_DIALING -> {
                _callState.value = CallState.Ringing(CallDirection.OUTGOING)
            }

            Call.STATE_RINGING -> { //for incoming
                _callState.value = CallState.Ringing(CallDirection.INCOMING)
            }

            Call.STATE_HOLDING -> {
                _callState.value = CallState.OnHold
            }

            Call.STATE_DISCONNECTED -> {
                _callState.value = CallState.CallDisconnected
                notificationController.stepCallRingTone()
                timerJob?.cancel()
                timerJob = null
            }
        }

    }

    override fun unBindCall(call: Call) {
        Log.d(TAG, "unBindCall: ${call.details}")
        currentCall?.unregisterCallback(telecomCallback)
        this.currentCall = null
        timerJob?.cancel()
        timerJob = null
        scope.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1_000)
                _callState.update {
                    if (it is CallState.Active) {
                        it.copy(duration = it.duration + 1)
                    } else {
                        it
                    }
                }
            }
        }
    }

}