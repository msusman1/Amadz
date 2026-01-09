package com.talsk.amadz.core

import android.app.Activity
import android.app.KeyguardManager
import android.os.Build
import android.view.WindowManager

object LockScreenController {

    fun enable(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true)
            activity.setTurnScreenOn(true)
            activity.getSystemService(KeyguardManager::class.java)
                .requestDismissKeyguard(activity, null)
        } else {
            activity.window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
