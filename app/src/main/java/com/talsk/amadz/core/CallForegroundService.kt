package com.talsk.amadz.core

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.app.ServiceCompat
import com.talsk.amadz.domain.NotificationController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CallForegroundService"

@AndroidEntryPoint
class CallForegroundService : Service() {

    @Inject
    lateinit var notificationController: NotificationController

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)
    private var notificationGeneration = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_INCOMING -> handleCall(
                intent = intent,
                type = CallNotificationType.INCOMING
            )

            ACTION_SHOW_OUTGOING -> handleCall(
                intent = intent,
                type = CallNotificationType.OUTGOING
            )

            ACTION_SHOW_ONGOING -> handleCall(intent = intent, type = CallNotificationType.ONGOING)
            ACTION_STOP_CALL_UI -> stopServiceInternal()
            else -> Log.w(TAG, "Unknown service action: ${intent?.action}")
        }
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun handleCall(intent: Intent, type: CallNotificationType) {
        val phone = intent.getStringExtra(EXTRA_PHONE).orEmpty()
        if (phone.isBlank()) {
            Log.w(TAG, "Ignoring $type notification: phone is blank")
            return
        }
        val durationSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 0)

        // This notification must be created synchronously/cheaply so the * service can enter the foreground immediately.
        val generation = ++notificationGeneration
        val fastNotification = notificationController.buildForegroundNotification(
            phone = phone,
            type = type
        )
        startCallForeground(fastNotification) /* * Contact/photo lookup happens asynchronously. */
        serviceScope.launch {
            val richNotification = runCatching {
                notificationController.buildCallNotification(phone, type, durationSeconds)
            }.getOrElse { throwable ->
                Log.w(TAG, "Failed to build rich $type notification", throwable)
                return@launch
            }
            /* * Ignore an old lookup if another call notification request * arrived while this one was running. */
            if (generation != notificationGeneration) {
                return@launch
            }
            NotificationManagerCompat.from(this@CallForegroundService)
                .notify(ACTIVE_CALL_NOTIFICATION_ID, richNotification)
        }
    }

    private fun startCallForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ACTIVE_CALL_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(ACTIVE_CALL_NOTIFICATION_ID, notification)
        }
    }

    private fun stopServiceInternal() {
        Log.d(TAG, "Stopping call foreground service")
        notificationGeneration++
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        notificationGeneration++
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTIVE_CALL_NOTIFICATION_ID = 124

        private const val EXTRA_PHONE = "extra_phone"
        private const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"

        private const val ACTION_SHOW_INCOMING = "com.talsk.amadz.call.SHOW_INCOMING"
        private const val ACTION_SHOW_OUTGOING = "com.talsk.amadz.call.SHOW_OUTGOING"
        private const val ACTION_SHOW_ONGOING = "com.talsk.amadz.call.SHOW_ONGOING"
        private const val ACTION_STOP_CALL_UI = "com.talsk.amadz.call.STOP_UI"

        fun showIncoming(context: Context, phone: String) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_SHOW_INCOMING
                putExtra(EXTRA_PHONE, phone)
            }
            context.startCallForegroundService(intent)
        }

        fun showOutgoing(context: Context, phone: String) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_SHOW_OUTGOING
                putExtra(EXTRA_PHONE, phone)
            }
            context.startCallForegroundService(intent)
        }

        private fun Context.startCallForegroundService(intent: Intent) {
            ContextCompat.startForegroundService(this, intent)
        }


        fun showOngoing(context: Context, phone: String, durationSeconds: Int) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_SHOW_ONGOING
                putExtra(EXTRA_PHONE, phone)
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
            }
            context.startCallForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_STOP_CALL_UI
            }
            context.startService(intent)
        }
    }
}
