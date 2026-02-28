package com.talsk.amadz.core

import android.content.Context
import android.telecom.Call
import android.telecom.Call.Callback
import android.telecom.VideoProfile
import android.telephony.TelephonyManager
import android.util.Log
import com.talsk.amadz.App
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallOrchestrator
import com.talsk.amadz.domain.CallServiceAudioDelegate
import com.talsk.amadz.domain.entity.CallDirection
import com.talsk.amadz.domain.entity.CallState
import com.talsk.amadz.domain.repo.BlockedNumberRepository
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
import javax.inject.Singleton

private const val TAG = "DefaultCallOrchestrator"

@Singleton
class DefaultCallOrchestrator @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository,
    private val callUiEffects: CallUiEffects,
    @ApplicationContext context: Context,
) : CallOrchestrator {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()
    private val scope = MainScope()
    private var currentCall: Call? = null
    private var timerJob: Job? = null
    private var callServiceAudioDelegate: CallServiceAudioDelegate? = null

    private val telephonyManager = context.getSystemService(TelephonyManager::class.java)


    private fun checkSimState() {
        val simState = telephonyManager.simState
        if (simState != TelephonyManager.SIM_STATE_READY) {
            _callState.value = CallState.SimError(
                simState = simState,
                message = simState.toSimStateReadable()
            )
        }
    }

    private val telecomCallback = object : Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            Log.d(TAG, "onStateChanged: state:${state}}")
            mapCallState(call, state)
        }
    }

    override fun onAction(callAction: CallAction) {
        when (callAction) {
            CallAction.Answer -> currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
            CallAction.Hangup -> currentCall?.disconnect()
            is CallAction.Hold -> if (callAction.enabled) currentCall?.hold() else currentCall?.unhold()
            is CallAction.Mute -> callServiceAudioDelegate?.setMicMuted(callAction.enabled)
            is CallAction.Speaker -> callServiceAudioDelegate?.setSpeaker(callAction.enabled)
            is CallAction.StartDialTone -> currentCall?.playDtmfTone(callAction.char)
            CallAction.StopDialTone -> currentCall?.stopDtmfTone()
        }
    }


    override fun setCallServiceAudioDelegate(audioController: CallServiceAudioDelegate) {
        this.callServiceAudioDelegate = audioController
    }


    private fun mapCallState(call: Call, state: Int) {


        val phone = call.callerPhone()
        when (state) {
            Call.STATE_ACTIVE -> {
                _callState.value = CallState.Active(
                    duration = 0,
                    isMuted = false,
                    isSpeakerOn = false,
                    isOnHold = false
                )
                startTimer()
                callUiEffects.showOngoing(phone, 0)
            }

            Call.STATE_CONNECTING -> {
                _callState.value = CallState.Connecting
            }

            Call.STATE_DIALING -> {
                _callState.value = CallState.Ringing(CallDirection.OUTGOING)
                callUiEffects.showOutgoing(call.callerPhone())
            }

            Call.STATE_RINGING -> {
                _callState.value = CallState.Ringing(CallDirection.INCOMING)
            }

            Call.STATE_HOLDING -> {
                _callState.value = CallState.OnHold
            }

            Call.STATE_DISCONNECTED -> {
                _callState.value = CallState.CallDisconnected
                callUiEffects.stopCallUi()
                timerJob?.cancel()
                timerJob = null
            }
        }
    }


    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1_000)
                val newDuration = ((_callState.value as? CallState.Active)?.duration ?: 0) + 1
                currentCall?.callerPhone()?.let {
                    callUiEffects.showOngoing(it, newDuration)
                }
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


    override fun onCallAdded(call: Call) {
        Log.d(TAG, "onCallAdded: $call")
        checkSimState()
        App.needCallLogRefresh = true
        currentCall?.unregisterCallback(telecomCallback)
        mapCallState(call, call.stateCompat)
        currentCall = call
        currentCall?.registerCallback(telecomCallback)
        val isOutgoing =
            call.stateCompat == Call.STATE_CONNECTING || call.stateCompat == Call.STATE_DIALING
        val isIncomingRinging = call.stateCompat == Call.STATE_RINGING && !isOutgoing

        when {
            isOutgoing -> {
                val phone = call.callerPhone()
                callUiEffects.launchCallScreen(phone)
                callUiEffects.showOutgoing(phone)
            }

            isIncomingRinging -> {
                val phone = call.callerPhone()
                if (blockedNumberRepository.isBlocked(phone)) {
                    onAction(CallAction.Hangup)
                    callUiEffects.stopCallUi()
                    return
                }
                callUiEffects.showIncoming(phone)
            }
        }
    }

    override fun onCallRemoved(call: Call) {
        Log.d(TAG, "onCallRemoved: $call")
        currentCall?.unregisterCallback(telecomCallback)
        callUiEffects.stopCallUi()
        currentCall = null
        timerJob?.cancel()
        timerJob = null
    }

    override fun onDestroy() {
        callUiEffects.stopCallUi()
        this.callServiceAudioDelegate = null
        scope.cancel()
    }
}
