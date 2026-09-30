package com.talsk.amadz.domain

import android.app.Notification
import com.talsk.amadz.core.CallNotificationType

interface NotificationController {

    fun buildForegroundNotification(
        phone: String,
        type: CallNotificationType,
        isMuted: Boolean = false,
        isSpeakerOn: Boolean = false
    ): Notification

    suspend fun buildCallNotification(
        phone: String,
        type: CallNotificationType,
        durationSeconds: Int = 0,
        isMuted: Boolean = false,
        isSpeakerOn: Boolean = false
    ): Notification

    suspend fun showMissedCallNotification(phone: String)
}
