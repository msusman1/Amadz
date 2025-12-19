package com.talsk.amadz

import android.app.Application
import com.talsk.amadz.core.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
@HiltAndroidApp
class App : Application() {
    lateinit var notificationHelper: NotificationHelper

    companion object {
        lateinit var instance: App
        var needDataReload: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationHelper = NotificationHelper(this)
    }
}