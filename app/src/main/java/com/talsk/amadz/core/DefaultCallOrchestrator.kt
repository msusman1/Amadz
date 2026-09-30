package com.talsk.amadz.core

import android.telecom.Call
import android.telecom.Call.Callback
import android.telecom.DisconnectCause
import android.telecom.VideoProfile
import android.util.Log
import com.talsk.amadz.App
import com.talsk.amadz.di.ApplicationScope
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallOrchestrator
import com.talsk.amadz.domain.CallServiceAudioDelegate
import com.talsk.amadz.domain.entity.CallDirection
import com.talsk.amadz.domain.entity.CallState
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import com.talsk.amadz.domain.repo.SimInfoProvider
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
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "DefaultCallOrchestrator"

@Singleton
class DefaultCallOrchestrator @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository,
    private val callUiEffects: CallUiEffects,
    private val simInfoProvider: SimInfoProvider,
    @ApplicationScope private val appScope: CoroutineScope
) : CallOrchestrator {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()
    private var sessionScope: CoroutineScope? = null
    private var currentCall: Call? = null
    private var timerJob: Job? = null
    private var callServiceAudioDelegate: CallServiceAudioDelegate? = null

    private var currentCallInitialState: Int? = null
    private var currentCallSimError: CallState.SimError? = null
    private var currentCallWasDeclined = false
    private var micMuted: Boolean = false
    private var speakerOn: Boolean = false


    private val telecomCallback = object : Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            Log.d(TAG, "onStateChanged: state=$state")
            if (call === currentCall) {
                handleCallState(call, state)
            }
        }
    }

    override fun onAction(callAction: CallAction) {
        when (callAction) {
            CallAction.Answer -> currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
            CallAction.Hangup -> {
                if (currentCall?.stateCompat == Call.STATE_RINGING) {
                    currentCallWasDeclined = true
                }
                currentCall?.disconnect()
            }
            is CallAction.Hold -> if (callAction.enabled) currentCall?.hold() else currentCall?.unhold()
            is CallAction.Mute -> {
                micMuted = callAction.enabled
                callServiceAudioDelegate?.setMicMuted(callAction.enabled)
                _callState.update { state ->
                    if (state is CallState.Active) state.copy(isMuted = callAction.enabled) else state
                }
                refreshOngoingNotification()
            }

            is CallAction.Speaker -> {
                speakerOn = callAction.enabled
                callServiceAudioDelegate?.setSpeaker(callAction.enabled)
                _callState.update { state ->
                    when (state) {
                        is CallState.Active -> state.copy(isSpeakerOn = callAction.enabled)
                        is CallState.Ringing -> state.copy(isSpeakerOn = callAction.enabled)
                        else -> state
                    }
                }
                refreshOngoingNotification()
            }

            is CallAction.StartDialTone -> currentCall?.playDtmfTone(callAction.char)
            CallAction.StopDialTone -> currentCall?.stopDtmfTone()
        }
    }

    private fun refreshOngoingNotification() {
        val activeCall = _callState.value as? CallState.Active ?: return
        val phone = currentCall?.callerPhone().orEmpty()
        if (phone.isNotBlank()) {
            callUiEffects.showOngoing(phone, activeCall.duration)
        }
    }

    override fun setCallServiceAudioDelegate(audioController: CallServiceAudioDelegate) {
        this.callServiceAudioDelegate = audioController
    }

    private fun startNewSessionScope() {
        cancelSessionScope()
        sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
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
    }

    private fun resetSessionState() {
        currentCall = null
        currentCallInitialState = null
        currentCallSimError = null
        currentCallWasDeclined = false
        micMuted = false
        speakerOn = false
    }

    private fun handleCallState(call: Call, state: Int) {
        if (currentCallSimError != null && state != Call.STATE_ACTIVE) {
            if (state == Call.STATE_DISCONNECTED) {
                callUiEffects.stopCallUi()
                timerJob?.cancel()
                timerJob = null
            }
            return
        }
        if (state == Call.STATE_ACTIVE) {
            currentCallSimError = null
        }

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
                _callState.value = CallState.Ringing(
                    direction = CallDirection.OUTGOING,
                    isSpeakerOn = speakerOn
                )
            }

            Call.STATE_RINGING -> {
                _callState.value = CallState.Ringing(
                    direction = CallDirection.INCOMING,
                    isSpeakerOn = speakerOn
                )
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
                delay(1_000.milliseconds)
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
        startNewSessionScope()
        currentCall?.unregisterCallback(telecomCallback)
        currentCall = call
        currentCallInitialState = call.stateCompat
        currentCallWasDeclined = false
        val isOutgoing = call.stateCompat == Call.STATE_CONNECTING ||
                call.stateCompat == Call.STATE_DIALING
        currentCallSimError = if (isOutgoing) simInfoProvider.checkSimState() else null
        currentCallSimError?.let { _callState.value = it }
        handleCallState(call, call.stateCompat)
        call.registerCallback(telecomCallback)

        val isIncomingRinging = call.stateCompat == Call.STATE_RINGING && !isOutgoing
        val phone = call.callerPhone()
        when {
            isOutgoing -> callUiEffects.showOutgoing(phone)
            isIncomingRinging -> {
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
        if (call !== currentCall) {
            call.unregisterCallback(telecomCallback)
            return
        }
        call.unregisterCallback(telecomCallback)
        callUiEffects.stopCallUi()

        val phone = call.callerPhone()
        val wasIncomingRingingAtStart = currentCallInitialState == Call.STATE_RINGING
        val wasNeverConnected = call.details.connectTimeMillis == 0L
        val isDisconnected = call.stateCompat == Call.STATE_DISCONNECTED
        val disconnectCause = call.details.disconnectCause?.code
        val wasRejected = disconnectCause == DisconnectCause.REJECTED ||
                disconnectCause == DisconnectCause.LOCAL
        val wasDeclined = currentCallWasDeclined

        cancelSessionScope()
        val callSimError = currentCallSimError
        resetSessionState()
        when {
            callSimError != null -> _callState.value = callSimError
            isDisconnected -> _callState.value = CallState.CallDisconnected
            else -> _callState.value = CallState.Idle
        }

        if (wasIncomingRingingAtStart && wasNeverConnected && isDisconnected &&
            !wasDeclined && !wasRejected
        ) {
            appScope.launch {
                callUiEffects.showMissedCall(phone)
            }
        }
    }

    override fun onDestroy() {
        callUiEffects.stopCallUi()
        callServiceAudioDelegate = null
        cancelSessionScope()
        resetSessionState()
        if (_callState.value != CallState.CallDisconnected && _callState.value !is CallState.SimError) {
            _callState.value = CallState.Idle
        }
    }
}
