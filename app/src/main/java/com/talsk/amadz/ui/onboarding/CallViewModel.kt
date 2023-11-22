package com.talsk.amadz.ui.onboarding

import android.content.Context
import android.telecom.Call
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.talsk.amadz.App
import com.talsk.amadz.core.CallManager
import com.talsk.amadz.data.ContactData
import com.talsk.amadz.data.ContactsRepository
import com.talsk.amadz.util.secondsToReadableTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Timer
import java.util.TimerTask

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
class CallViewModel(phone: String, context: Context) : ViewModel() {
    private val _callState: MutableStateFlow<CallUiState> = MutableStateFlow(CallUiState.Initial)
    val callState: StateFlow<CallUiState> get() = _callState
    private val contactsRepository = ContactsRepository(context)
    private val _callTime: MutableStateFlow<Int> = MutableStateFlow(0)
    val callTime: StateFlow<String>
        get() = _callTime.map {
            secondsToReadableTime(it)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    var contactData = contactsRepository.getContactData(phone)?: ContactData (-1, "Unknown", phone, null,isFavourite = false)
    private var callOnHold = false
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

            Call.STATE_DISCONNECTED -> {
                job?.cancel()
                _callState.value = CallUiState.CallDisconnected

            }
        }
    }


    private var job: Job? = null
    private val timer = Timer("some timer")
    private fun startTimer() {

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (callOnHold.not()) {
                    _callTime.update { it + 1 }
                }
            }
        }, 0, 1000)


        /*  job = viewModelScope.launch {
              while (isActive) {
                  if (callOnHold.not()) {
                      _callTime.update { it + 1 }
                  }
                  delay(1000)
              }
          }*/
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
        callOnHold = hold
        CallManager.setCallOnHold(hold)

    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        timer.cancel()
        CallManager.unRegisterCallBack()
    }


}
class CallViewModelFactory(private val phone: String, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallViewModel(phone, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}