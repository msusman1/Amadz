package com.talsk.amadz.core

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.talsk.amadz.App
import com.talsk.amadz.ui.ongoingCall.CallActivity


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/172023.
 */
const val CALL_ACTION_MUTE = "action_mute"
const val CALL_ACTION_UN_MUTE = "action_un_mute"
const val CALL_ACTION_SPEAKER_ON = "action_speaker_on"
const val CALL_ACTION_SPEAKER_OFF = "action_speaker_off"

class CallService : InCallService() {
    val TAG = "CallService"
    private lateinit var powerManager: PowerManager
    private lateinit var keyguardManager: KeyguardManager
    override fun onCreate() {
        powerManager = getSystemService(PowerManager::class.java)
        keyguardManager = getSystemService(KeyguardManager::class.java)
        val intentFilter = IntentFilter()
        intentFilter.addAction(CALL_ACTION_MUTE)
        intentFilter.addAction(CALL_ACTION_UN_MUTE)
        intentFilter.addAction(CALL_ACTION_SPEAKER_ON)
        intentFilter.addAction(CALL_ACTION_SPEAKER_OFF)
        registerReceiver(callActionReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callActionReceiver)
    }

    override fun onCallAdded(call: Call) {
        Log.d(TAG, "onCallAdded() called with: call = $call")
        super.onCallAdded(call)
        CallManager.updateCall(call)
        if (call.state == Call.STATE_RINGING) {
            if (powerManager.isInteractive && keyguardManager.isKeyguardLocked.not()) {
                App.instance.notificationHelper.displayIncomingCallNotification(call.callPhone())
                App.instance.notificationHelper.playCallRingTone()

            } else {
                App.instance.notificationHelper.playCallRingTone()
                CallActivity.start(this,call.callPhone())
            }
        } else if (call.state == Call.STATE_CONNECTING || call.state == Call.STATE_DIALING) {
            CallActivity.start(this,call.callPhone())
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.updateCall(null)
    }


    private val callActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, intent: Intent) {
            when (intent.action) {
                CALL_ACTION_MUTE -> setMuted(true)
                CALL_ACTION_UN_MUTE -> setMuted(false)
                CALL_ACTION_SPEAKER_ON -> setAudioRoute(CallAudioState.ROUTE_SPEAKER)
                CALL_ACTION_SPEAKER_OFF -> setAudioRoute(CallAudioState.ROUTE_EARPIECE)
                else -> {}
            }
        }
    }
}
