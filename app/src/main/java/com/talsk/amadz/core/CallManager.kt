package com.talsk.amadz.core

import android.telecom.Call
import android.telecom.Call.Callback
import android.telecom.VideoProfile
import android.util.Log
import com.talsk.amadz.App
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
object CallManager {
    const val TAG = "CallManager"
    private val callback = object : Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            if (newState == Call.STATE_DISCONNECTED) {
                App.instance.notificationHelper.cancelIncommingCallNotification()
                App.instance.notificationHelper.stepCallRingTone()
            }
            Log.d(TAG, "onStateChanged() called with:  newState = $newState , call = $call,")
            uiCallback?.onStateChanged(call, newState)
        }
    }

    private var uiCallback: Callback? = null

    fun registerCallBack(callback: Callback) {
        uiCallback = callback
    }

    fun unRegisterCallBack() {
        uiCallback = null
    }

    var sCall: Call? = null
    fun updateCall(call: Call?) {
        if (call == null) {
            sCall?.unregisterCallback(callback)
        } else {
            call.registerCallback(callback)
        }
        sCall = call
    }


    fun answer() {
        sCall?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        sCall?.disconnect()
    }


    fun setCallOnHold(hold: Boolean) {
        if (hold) {
            sCall?.hold()
        } else {
            sCall?.unhold()
        }

    }

}