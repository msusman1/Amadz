package com.talsk.amadz.core

import android.app.NotificationManager
import android.app.Service
import android.content.Intent

import android.os.IBinder


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
class CallAcceptService : Service() {
    private var notificationManager: NotificationManager? = null
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        CallManager.answer()

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}

class CallDeclineService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        CallManager.hangup()
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}