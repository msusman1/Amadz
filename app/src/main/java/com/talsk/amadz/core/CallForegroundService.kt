package com.talsk.amadz.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.app.ServiceCompat
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.RingToneController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CallForegroundService"

@AndroidEntryPoint
class CallForegroundService : Service() {

    @Inject
    lateinit var notificationController: NotificationController

    @Inject
    lateinit var ringToneController: RingToneController

    private val scope = MainScope()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        val phone = intent.getStringExtra(EXTRA_PHONE).orEmpty()
        val duration = intent.getIntExtra(EXTRA_DURATION_SECONDS, 0)

        scope.launch {
            when (action) {
                ACTION_SHOW_INCOMING -> {
                    if (phone.isBlank()) return@launch
                    ringToneController.playCallRingTone()
                    val notification = notificationController.buildIncomingCallNotification(phone)
                    startCallForeground(notification)
                }

                ACTION_SHOW_OUTGOING -> {
                    if (phone.isBlank()) return@launch
                    ringToneController.stepCallRingTone()
                    val notification = notificationController.buildOutgoingCallNotification(phone)
                    startCallForeground(notification)
                }

                ACTION_SHOW_ONGOING -> {
                    if (phone.isBlank()) return@launch
                    ringToneController.stepCallRingTone()
                    val notification = notificationController.buildOngoingCallNotification(phone, duration)
                    startCallForeground(notification)
                }

                ACTION_STOP_CALL_UI -> stopServiceInternal()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        ringToneController.stepCallRingTone()
        scope.cancel()
        super.onDestroy()
    }

    private fun startCallForeground(notification: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ONGOING_CALL_NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(ONGOING_CALL_NOTIFICATION_ID, notification)
        }
    }

    private fun stopServiceInternal() {
        Log.d(TAG, "Stopping call foreground service")
        ringToneController.stepCallRingTone()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val ONGOING_CALL_NOTIFICATION_ID = 124

        private const val EXTRA_PHONE = "extra_phone"
        private const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"

        private const val ACTION_SHOW_INCOMING = "com.talsk.amadz.call.SHOW_INCOMING"
        private const val ACTION_SHOW_OUTGOING = "com.talsk.amadz.call.SHOW_OUTGOING"
        private const val ACTION_SHOW_ONGOING = "com.talsk.amadz.call.SHOW_ONGOING"
        private const val ACTION_STOP_CALL_UI = "com.talsk.amadz.call.STOP_UI"

        fun showIncoming(context: Context, phone: String) {
            context.startCallForegroundService(
                Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_SHOW_INCOMING
                    putExtra(EXTRA_PHONE, phone)
                }
            )
        }

        fun showOutgoing(context: Context, phone: String) {
            context.startCallForegroundService(
                Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_SHOW_OUTGOING
                    putExtra(EXTRA_PHONE, phone)
                }
            )
        }

        fun showOngoing(context: Context, phone: String, durationSeconds: Int) {
            context.startCallForegroundService(
                Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_SHOW_ONGOING
                    putExtra(EXTRA_PHONE, phone)
                    putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
                }
            )
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_STOP_CALL_UI
                }
            )
        }

        private fun Context.startCallForegroundService(intent: Intent) {
            ContextCompat.startForegroundService(this, intent)
        }
    }
}


