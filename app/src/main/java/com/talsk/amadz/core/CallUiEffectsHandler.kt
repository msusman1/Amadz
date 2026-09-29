package com.talsk.amadz.core

import android.content.Context
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.ui.ongoingCall.CallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface CallUiEffects {
    fun showIncoming(phone: String)
    fun showOutgoing(phone: String)
    fun showOngoing(phone: String, durationSeconds: Int)
    fun stopCallUi()
    suspend fun showMissedCall(phone: String)
}

@Singleton
class CallUiEffectsHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationController: NotificationController
) : CallUiEffects {

    override fun showIncoming(phone: String) {
        CallForegroundService.showIncoming(context, phone)
    }

    override fun showOutgoing(phone: String) {
        CallActivity.start(context, phone)
        CallForegroundService.showOutgoing(context, phone)
    }

    override fun showOngoing(phone: String, durationSeconds: Int) {
        CallForegroundService.showOngoing(context, phone, durationSeconds)
    }

    override fun stopCallUi() {
        CallForegroundService.stop(context)
    }

    override suspend fun showMissedCall(phone: String) {
        notificationController.showMissedCallNotification(phone)
    }
}
