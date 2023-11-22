package com.talsk.amadz.ui.ongoingCall

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.talsk.amadz.App
import com.talsk.amadz.core.CALL_ACTION_MUTE
import com.talsk.amadz.core.CALL_ACTION_SPEAKER_OFF
import com.talsk.amadz.core.CALL_ACTION_SPEAKER_ON
import com.talsk.amadz.core.CALL_ACTION_UN_MUTE
import com.talsk.amadz.ui.onboarding.CallUiState
import com.talsk.amadz.ui.onboarding.CallViewModel
import com.talsk.amadz.ui.onboarding.CallViewModelFactory

class CallActivity : ComponentActivity() {
    val phone: String by lazy { intent.getStringExtra("phone") ?: "" }
    private val vm: CallViewModel by viewModels(factoryProducer = {
        CallViewModelFactory(
            phone,
            applicationContext
        )
    })
    lateinit var notificationManager: NotificationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val currentActivity = LocalContext.current as? CallActivity
            val uiState by vm.callState.collectAsState()
            val callTime by vm.callTime.collectAsState()
            if (uiState is CallUiState.CallDisconnected) {
                currentActivity?.finishAndRemoveTask()
            }
            CallScreen(
                contact=vm.contactData,
                uiState = uiState,
                callTime = callTime,
                onIncomingAccept = vm::accept,
                onIncomingDecline = vm::decline,
                onEnd = vm::end,
                setCallOnHold = vm::setCallOnHold,
                setCallMute = { sendBroadcast(Intent(if (it) CALL_ACTION_MUTE else CALL_ACTION_UN_MUTE)) },
                setSpeakerOn = { sendBroadcast(Intent(if (it) CALL_ACTION_SPEAKER_ON else CALL_ACTION_SPEAKER_OFF)) },
            )
        }
        notificationManager = getSystemService(NotificationManager::class.java)
        clearNotification()
        addLockScreenFlag()

    }

    private fun clearNotification() {
        App.instance.notificationHelper.cancelIncommingCallNotification()
    }


    private fun addLockScreenFlag() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

    }


    companion object {
        fun start(context: Context, phone: String) {
            val intent = Intent(context, CallActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("phone", phone)
            context.startActivity(intent)

        }
    }
}

