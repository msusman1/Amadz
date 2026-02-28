package com.talsk.amadz.domain

import android.app.Notification

interface NotificationController {
    suspend fun buildIncomingCallNotification(phone: String): Notification
    suspend fun buildOutgoingCallNotification(phone: String): Notification
    suspend fun buildOngoingCallNotification(phone: String, durationSeconds: Int): Notification
    suspend fun showMissedCallNotification(phone: String)
}