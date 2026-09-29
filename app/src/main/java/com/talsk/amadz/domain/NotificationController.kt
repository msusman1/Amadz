package com.talsk.amadz.domain

import android.app.Notification
import com.talsk.amadz.core.CallNotificationType

interface NotificationController {

    fun buildForegroundNotification(phone: String, type: CallNotificationType): Notification
    suspend fun buildCallNotification( phone: String, type: CallNotificationType, durationSeconds: Int = 0 ): Notification
    suspend fun showMissedCallNotification(phone: String)
}

