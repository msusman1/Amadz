package com.talsk.amadz.core

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.talsk.amadz.MainActivity
import com.talsk.amadz.R
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.repos.ContactRepository
import com.talsk.amadz.ui.ongoingCall.CallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val CALL_CHANNEL_ID = "AMADZ_CALL_NOTIFICATION_ID"
private const val CALL_CHANNEL_NAME = "AMADZ_CALL_NOTIFICATION"

private const val INCOMING_CALL_NOTIFICATION_ID = 123
private const val ONGOING_CALL_NOTIFICATION_ID = 124
private const val MISSED_CALL_NOTIFICATION_ID = 125

data class ContactUi(
    val title: String,
    val subtitle: String?,
    val avatar: Bitmap?
)


class DefaultNotificationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository
) : NotificationController {

    private val notificationManager = NotificationManagerCompat.from(context)

    private val ringtone: Ringtone? by lazy {
        RingtoneManager.getRingtone(
            context,
            RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE
            )
        )
    }

    init {
        createNotificationChannel()
    }

    // ----------------------------
    // Public API (NOW SUSPEND)
    // ----------------------------


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun displayIncomingCallNotification(phone: String) {
        val contact = loadContactUi(phone)

        val builder = baseCallBuilder(
            title = "Incoming Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_MAX
        ).apply {
            setFullScreenIntent(callActivityIntent(phone), true)
            addAction(
                R.drawable.baseline_check_24,
                "Accept",
                callActionIntent(CallActionService.ACTION_ACCEPT)
            )
            addAction(
                R.drawable.outline_clear_24,
                "Decline",
                callActionIntent(CallActionService.ACTION_DECLINE)
            )
        }

        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, builder.build())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun displayOngoingCallNotification(phone: String) {
        val contact = loadContactUi(phone)

        val builder = baseCallBuilder(
            title = "Ongoing Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_LOW
        )

        notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, builder.build())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun showMissedCallNotification(phone: String) {
        val contact = loadContactUi(phone)

        val builder = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_short_notification)
            .setContentTitle("Missed Call")
            .setContentText(contact.title)
            .setSubText(contact.subtitle)
            .setLargeIcon(contact.avatar)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent())

        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, builder.build())
    }

    override fun cancelIncomingCallNotification() {
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }

    override fun cancelOngoingCallNotification() {
        notificationManager.cancel(ONGOING_CALL_NOTIFICATION_ID)
    }

    override fun playCallRingTone() {
        if (ringtone?.isPlaying == false) {
            ringtone?.play()
        }
    }

    override fun stepCallRingTone() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
    }

    // ----------------------------
    // Internal Helpers
    // ----------------------------

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CALL_CHANNEL_ID,
            CALL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Amadz Call Notifications"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun loadContactUi(phone: String): ContactUi {
        val contact = contactRepository.getContactByPhone(phone)
        return if (contact != null) {
            ContactUi(
                title = contact.name,
                subtitle = contact.companyName,
                avatar = contact.imageBitmap
            )
        } else {
            ContactUi(
                title = phone,
                subtitle = null,
                avatar = defaultAvatar()
            )
        }
    }

    private fun baseCallBuilder(
        title: String,
        content: String,
        subText: String?,
        largeIcon: Bitmap?,
        priority: Int
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_short_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(subText)
            .setLargeIcon(largeIcon)
            .setOngoing(true)
            .setPriority(priority)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(callActivityIntent())
    }

    private fun callActivityIntent(phone: String? = null): PendingIntent {
        val intent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            phone?.let { putExtra("phone", it) }
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun mainActivityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun callActionIntent(action: String): PendingIntent {
        val intent = Intent(context, CallActionService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun defaultAvatar(): Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.profile_pic)
}
