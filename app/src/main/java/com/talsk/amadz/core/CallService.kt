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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/172023.
 */

const val TAG = "CallService"

@AndroidEntryPoint
class CallService : InCallService() {
    @Inject
    lateinit var callAdapter: CallAdapter

    @Inject
    lateinit var notificationController: NotificationController
    lateinit var powerManager: PowerManager
    lateinit var keyguardManager: KeyguardManager
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var audioController: CallAudioController

    override fun onCreate() {
        super.onCreate()
        powerManager = this.getSystemService(PowerManager::class.java)
        keyguardManager = this.getSystemService(KeyguardManager::class.java)
        audioController = TelecomAudioController(this)
        callAdapter.attachAudioController(audioController)

    }

    override fun onCallAdded(call: Call) {
        Log.d(TAG, "onCallAdded() called with: call = $call")
        super.onCallAdded(call)


        val isRinging = call.state == Call.STATE_RINGING
        val isOutgoing = call.state == Call.STATE_CONNECTING || call.state == Call.STATE_DIALING

        val shouldShowCallUI = when {
            isOutgoing -> true
            isRinging -> {
                // Only start Activity if screen is off or locked (Heads-up notification would handle the rest)
                !powerManager.isInteractive || keyguardManager.isKeyguardLocked
            }

            else -> false
        }
        if (shouldShowCallUI) {
            CallActivity.start(this, call.callPhone())
        }
        callAdapter.bindCall(call)


    }

    override fun onCallRemoved(call: Call) {
        Log.d(TAG, "onCallRemoved: $call")
        super.onCallRemoved(call)
        callAdapter.unBindCall(call)
        if (call.details.connectTimeMillis == 0L) {
            scope.launch {
                notificationController.showMissedCallNotification(call.callPhone())
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        callAdapter.detachAudioController()
        super.onDestroy()
    }
}
