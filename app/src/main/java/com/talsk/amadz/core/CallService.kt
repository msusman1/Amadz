package com.talsk.amadz.core

import android.app.KeyguardManager
import android.os.PowerManager
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.talsk.amadz.domain.CallAdapter
import com.talsk.amadz.domain.CallAudioController
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.ui.ongoingCall.CallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/172023.
 */


@AndroidEntryPoint
class CallService : InCallService() {
    val TAG = "CallService"

    @Inject
    lateinit var callAdapter: CallAdapter

    @Inject
    lateinit var notificationController: NotificationController
    lateinit var powerManager: PowerManager
    lateinit var keyguardManager: KeyguardManager
    private val scope = MainScope()
    private lateinit var audioController: CallAudioController

    override fun onCreate() {
        Log.d(TAG, "onCreate: ")
        super.onCreate()
        powerManager = this.getSystemService(PowerManager::class.java)
        keyguardManager = this.getSystemService(KeyguardManager::class.java)
        audioController = TelecomAudioController(this)
        callAdapter.attachAudioController(audioController)

    }


    override fun onCallAdded(call: Call) {
        Log.d(TAG, "onCallAdded() called with: call = $call")
        super.onCallAdded(call)
        callAdapter.bindCall(call)
        callStates[call] = call.state

        val isOutgoing = call.state == Call.STATE_CONNECTING || call.state == Call.STATE_DIALING
        val isIncomingRinging = call.state == Call.STATE_RINGING && !isOutgoing

        when {
            // Outgoing call → directly open CallActivity
            isOutgoing -> {
                CallActivity.start(this, call.callPhone())
            }

            // Incoming call → show full-screen intent notification
            isIncomingRinging -> {
                scope.launch {
                    notificationController.playCallRingTone()
                    notificationController.displayIncomingCallNotification(call.callPhone())
                }
            }
        }
    }

    private val callStates = mutableMapOf<Call, Int>()
    override fun onCallRemoved(call: Call) {
        Log.d(TAG, "onCallRemoved: $call")
        super.onCallRemoved(call)
        callAdapter.unBindCall(call)
        val previousState = callStates.remove(call)

        if (previousState == Call.STATE_RINGING &&
            call.state == Call.STATE_DISCONNECTED &&
            call.details.connectTimeMillis == 0L
        ) {
            scope.launch {
                notificationController.showMissedCallNotification(call.callPhone())
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        scope.cancel()
        callAdapter.detachAudioController()
        super.onDestroy()
    }
}
