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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
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

    private var sessionScope: CoroutineScope? = null
    private var sessionJob: Job? = null
    private var currentCall: Call? = null
    private var timerJob: Job? = null
    private var callServiceAudioDelegate: CallServiceAudioDelegate? = null

    private var currentCallInitialState: Int? = null
    private var micMuted: Boolean = false
    private var speakerOn: Boolean = false

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
            Log.d(TAG, "onStateChanged: state=$state")
            mapCallState(call, state)
        }
    }

    override fun onAction(callAction: CallAction) {
        when (callAction) {
            CallAction.Answer -> currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
            CallAction.Hangup -> currentCall?.disconnect()
            is CallAction.Hold -> if (callAction.enabled) currentCall?.hold() else currentCall?.unhold()
            is CallAction.Mute -> {
                micMuted = callAction.enabled
                callServiceAudioDelegate?.setMicMuted(callAction.enabled)
                _callState.update { state ->
                    if (state is CallState.Active) state.copy(isMuted = callAction.enabled) else state
                }
            }

            is CallAction.Speaker -> {
                speakerOn = callAction.enabled
                callServiceAudioDelegate?.setSpeaker(callAction.enabled)
                _callState.update { state ->
                    if (state is CallState.Active) state.copy(isSpeakerOn = callAction.enabled) else state
                }
            }

            is CallAction.StartDialTone -> currentCall?.playDtmfTone(callAction.char)
            CallAction.StopDialTone -> currentCall?.stopDtmfTone()
        }
    }

    override fun setCallServiceAudioDelegate(audioController: CallServiceAudioDelegate) {
        this.callServiceAudioDelegate = audioController
    }

    private fun startNewSessionScope() {
        cancelSessionScope()
        val job = SupervisorJob()
        sessionJob = job
        sessionScope = CoroutineScope(job + Dispatchers.Main.immediate)
    }

    private fun sessionScopeOrCreate(): CoroutineScope {
        sessionScope?.let { return it }
        startNewSessionScope()
        return sessionScope!!
    }

    private fun cancelSessionScope() {
        timerJob?.cancel()
        timerJob = null
        sessionScope?.cancel()
        sessionScope = null
        sessionJob = null
    }

    private fun resetSessionState() {
        currentCall = null
        currentCallInitialState = null
        micMuted = false
        speakerOn = false
    }

    private fun mapCallState(call: Call, state: Int) {
        val phone = call.callerPhone()
        when (state) {
            Call.STATE_ACTIVE -> {
                _callState.value = CallState.Active(
                    duration = 0,
                    isMuted = micMuted,
                    isSpeakerOn = speakerOn,
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
        timerJob = sessionScopeOrCreate().launch {
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

    override fun onCallAdded(call: Call) {
        Log.d(TAG, "onCallAdded: $call")
        checkSimState()
        App.needCallLogRefresh = true
        startNewSessionScope()

        currentCall?.unregisterCallback(telecomCallback)

        currentCall = call
        currentCallInitialState = call.stateCompat
        mapCallState(call, call.stateCompat)
        call.registerCallback(telecomCallback)

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

        val wasIncomingRingingAtStart = currentCallInitialState == Call.STATE_RINGING
        val wasNeverConnected = call.details.connectTimeMillis == 0L
        val isDisconnected = call.stateCompat == Call.STATE_DISCONNECTED
        if (wasIncomingRingingAtStart && wasNeverConnected && isDisconnected) {
            sessionScopeOrCreate().launch {
                callUiEffects.showMissedCall(call.callerPhone())
                cancelSessionScope()
                resetSessionState()
                _callState.value = CallState.Idle
            }
        } else {
            cancelSessionScope()
            resetSessionState()
            _callState.value = CallState.Idle
        }
    }

    override fun onDestroy() {
        callUiEffects.stopCallUi()
        callServiceAudioDelegate = null
        cancelSessionScope()
        resetSessionState()
        _callState.value = CallState.Idle
    }
}
