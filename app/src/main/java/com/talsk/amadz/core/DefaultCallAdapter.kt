package com.talsk.amadz.core

import android.telecom.Call
import android.telecom.Call.Callback
import android.telecom.VideoProfile
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.CallAudioController
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.ui.onboarding.CallDirection
import com.talsk.amadz.ui.onboarding.CallState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultCallAdapter @Inject constructor(
    private val notificationController: NotificationController,
) : CallAdapter {
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var currentCall: Call? = null
    private var timerJob: Job? = null
    private var audioController: CallAudioController? = null

    private val telecomCallback = object : Callback() {
        override fun onStateChanged(call: Call, state: Int) {

            if (state == Call.STATE_ACTIVE) {
                scope.launch {
                    notificationController.displayOngoingCallNotification(call.callPhone())
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
                        0,
                        isMuted = false,
                        isSpeakerOn = false,
                        isOnHold = false
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
        currentCall?.unregisterCallback(telecomCallback)
        this.currentCall = call
        currentCall?.registerCallback(telecomCallback)
        _callState.value = CallState.Idle
    }

    override fun unBindCall(call: Call) {
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