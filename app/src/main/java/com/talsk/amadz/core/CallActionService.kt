package com.talsk.amadz.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.talsk.amadz.domain.CallAction
import com.talsk.amadz.domain.CallAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
@AndroidEntryPoint
class CallActionService : Service() {
    @Inject
    lateinit var callAdapter: CallAdapter

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_ACCEPT -> callAdapter.dispatch(CallAction.Answer)
            ACTION_DECLINE -> callAdapter.dispatch(CallAction.Hangup)
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_ACCEPT = "call_action_accept"
        const val ACTION_DECLINE = "call_action_decline"
    }
}
