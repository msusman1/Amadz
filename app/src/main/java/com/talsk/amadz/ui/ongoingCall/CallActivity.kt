package com.talsk.amadz.ui.ongoingCall

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
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
    var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val currentActivity = LocalContext.current as? CallActivity
            val uiState by vm.callState.collectAsState()
            val callTime by vm.callTime.collectAsState()
            if (uiState is CallUiState.CallDisconnected && alertDialog == null) {
                currentActivity?.finishAndRemoveTask()
            }
            CallScreen(
                contact = vm.contactData,
                uiState = uiState,
                callTime = callTime,
                onIncomingAccept = vm::accept,
                onIncomingDecline = vm::decline,
                onEnd = vm::end,
                setCallOnHold = vm::setCallOnHold,
                setCallMute = { sendBroadcast(Intent(if (it) CALL_ACTION_MUTE else CALL_ACTION_UN_MUTE)) },
                setSpeakerOn = { sendBroadcast(Intent(if (it) CALL_ACTION_SPEAKER_ON else CALL_ACTION_SPEAKER_OFF)) },
                startTone = vm::startTone,
                stopTone = vm::stopTone
            )
        }
        notificationManager = getSystemService(NotificationManager::class.java)
        clearNotification()
        addLockScreenFlag()
        checkAirPlanAndSimStats()
    }


    private fun checkAirPlanAndSimStats() {
        val telephonyManager = getSystemService(TelephonyManager::class.java)
        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            alertDialog =
                AlertDialog.Builder(this).setMessage(telephonyManager.simState.ToSimstateReadable())
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        if (vm.callState.value is CallUiState.CallDisconnected) {
                            finishAndRemoveTask()
                        }
                        alertDialog = null
                    }
                    .show()
        }
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

    override fun onStop() {
        super.onStop()
        App.needDataReload = true
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

fun Int.ToSimstateReadable(): String {
    return when (this) {
        TelephonyManager.SIM_STATE_ABSENT -> "No Sim available"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card Io error"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Sim card restricted"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
        TelephonyManager.SIM_STATE_NOT_READY -> "Sim not ready"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "PERM disabled"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
        TelephonyManager.SIM_STATE_READY -> "Sim is ready"
        else -> "Unknown"
    }


}

