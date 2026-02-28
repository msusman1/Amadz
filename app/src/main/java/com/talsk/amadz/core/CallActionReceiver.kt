package com.talsk.amadz.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callAdapter: CallAdapter

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_ACCEPT -> callAdapter.dispatch(CallAction.Answer)
            ACTION_DECLINE -> callAdapter.dispatch(CallAction.Hangup)
        }
    }

    companion object {
        const val ACTION_ACCEPT = "com.talsk.amadz.call.ACTION_ACCEPT"
        const val ACTION_DECLINE = "com.talsk.amadz.call.ACTION_DECLINE"
    }
}
