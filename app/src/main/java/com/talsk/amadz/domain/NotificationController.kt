package com.talsk.amadz.domain


interface NotificationController {
    suspend fun displayIncomingCallNotification(phone: String)
    suspend fun displayOngoingCallNotification(phone: String)
    fun cancelIncomingCallNotification()
    fun cancelOngoingCallNotification()
    fun playCallRingTone()
    fun stepCallRingTone()
    suspend fun showMissedCallNotification(phone: String)
}