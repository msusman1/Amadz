package com.talsk.amadz

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.talsk.amadz.core.NotificationHelper

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
class App : Application() {
    lateinit var notificationHelper: NotificationHelper

    companion object {
        var isAppInForeground = false
        lateinit var instance: App
        var needDataReload: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationHelper = NotificationHelper(this)
        registerForgreoundCallBack()
    }

    private fun registerForgreoundCallBack() {

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // Activity created
            }

            override fun onActivityStarted(activity: Activity) {
                // Activity started
                isAppInForeground = true
            }

            override fun onActivityResumed(activity: Activity) {
                // Activity resumed
                isAppInForeground = true
            }

            override fun onActivityPaused(activity: Activity) {
                // Activity paused
                isAppInForeground = false
            }

            override fun onActivityStopped(activity: Activity) {
                // Activity stopped
                isAppInForeground = false
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Activity state saved
            }

            override fun onActivityDestroyed(activity: Activity) {
                // Activity destroyed
            }
        })
    }
}