package com.talsk.amadz.ui.onboarding

import android.content.Context
import android.telecom.Call
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.App
import com.talsk.amadz.core.CallManager
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.ContactsRepositoryImpl
import com.talsk.amadz.domain.repos.ContactRepository
import com.talsk.amadz.util.secondsToReadableTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */

@HiltViewModel
class CallViewModel @Inject constructor(
    private val contactsRepository: ContactRepository,
    phone: String,
) : ViewModel() {
    private val _callState: MutableStateFlow<CallUiState> = MutableStateFlow(CallUiState.Initial)
    val callState: StateFlow<CallUiState> get() = _callState
    private val _callTime: MutableStateFlow<Int> = MutableStateFlow(0)
    val callTime: StateFlow<String>
        get() = _callTime.map {
            secondsToReadableTime(it)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    var contactData = contactsRepository.getContactByPhone(phone) ?: ContactData(
        id = -1,
        name = "Unknown",
        companyName = "",
        phone = phone,
        image = null,
        isFavourite = false
    )

    private val ongoingCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            callBackToUiState(call, state)

        }
    }

    init {
        CallManager.registerCallBack(ongoingCallback)
        CallManager.sCall?.let { call ->
            callBackToUiState(call, call.state)
        }
    }

    private fun callBackToUiState(call: Call, state: Int) {

        when (state) {
            Call.STATE_ACTIVE -> { //call connected
                _callState.value = CallUiState.InCall
                startTimer()
            }


            Call.STATE_CONNECTING, Call.STATE_DIALING -> {
                _callState.value = CallUiState.OutgoingCall
            }

            Call.STATE_RINGING -> { //for incoming
                _callState.value = CallUiState.InComingCall
            }

            Call.STATE_HOLDING -> {
                _callState.value = CallUiState.OnHold
            }

            Call.STATE_DISCONNECTED -> {
                _callState.value = CallUiState.CallDisconnected
                timer?.cancel()
                timer = null
            }
        }
    }


    private var timer: Timer? = null
    private fun startTimer() {
        if (timer == null) {
            timer = Timer("some timer")
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (callState.value is CallUiState.InCall) {
                        _callTime.update { it + 1 }
                    }
                }
            }, 0, 1000)
        }

    }

    fun accept() {
        App.instance.notificationHelper.stepCallRingTone()
        CallManager.answer()
    }

    fun decline() {
        App.instance.notificationHelper.stepCallRingTone()
        CallManager.hangup()
    }

    fun end() {
        CallManager.hangup()
    }

    fun setCallOnHold(hold: Boolean) {
        CallManager.setCallOnHold(hold)

    }

    fun startTone(char: Char) {
        CallManager.playDtmfTone(char)
    }

    fun stopTone() {
        CallManager.stopDtmfTone()
    }


    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
        CallManager.unRegisterCallBack()
    }


}

class CallViewModelFactory(private val phone: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallViewModel(phone) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}