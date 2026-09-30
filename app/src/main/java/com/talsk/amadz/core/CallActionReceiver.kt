package com.talsk.amadz.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callOrchestrator: CallOrchestrator

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_ACCEPT -> callOrchestrator.onAction(CallAction.Answer)
            ACTION_DECLINE -> callOrchestrator.onAction(CallAction.Hangup)
            ACTION_MUTE -> callOrchestrator.onAction(
                CallAction.Mute(
                    intent.getBooleanExtra(
                        EXTRA_ENABLED,
                        false
                    )
                )
            )

            ACTION_SPEAKER -> callOrchestrator.onAction(
                CallAction.Speaker(
                    intent.getBooleanExtra(
                        EXTRA_ENABLED,
                        false
                    )
                )
            )
        }
    }

    companion object {
        const val ACTION_ACCEPT = "com.talsk.amadz.call.ACTION_ACCEPT"
        const val ACTION_DECLINE = "com.talsk.amadz.call.ACTION_DECLINE"
        const val ACTION_MUTE = "com.talsk.amadz.call.ACTION_MUTE"
        const val ACTION_SPEAKER = "com.talsk.amadz.call.ACTION_SPEAKER"
        const val EXTRA_PHONE = "com.talsk.amadz.call.EXTRA_PHONE"
        const val EXTRA_ENABLED = "com.talsk.amadz.call.EXTRA_ENABLED"
    }
}
