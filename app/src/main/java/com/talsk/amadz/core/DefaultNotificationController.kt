package com.talsk.amadz.core

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.talsk.amadz.MainActivity
import com.talsk.amadz.R
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import com.talsk.amadz.domain.repo.ContactRepository
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
    private val contactRepository: ContactRepository,
    private val contactPhotoProvider: ContactPhotoProvider
) : NotificationController {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val contactUiCache = mutableMapOf<String, ContactUi>()



    init {
        createNotificationChannel()
    }

    // ----------------------------
    // Public API (NOW SUSPEND)
    // ----------------------------


    override suspend fun buildIncomingCallNotification(phone: String): Notification {
        val contact = loadContactUi(phone)

        return baseCallBuilder(
            title = "Incoming Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_MAX
        ).apply {
            setAutoCancel(false)
            setOngoing(true)
            setCategory(NotificationCompat.CATEGORY_CALL)
            setFullScreenIntent(callActivityIntent(phone), true)
            addAction(
                R.drawable.baseline_check_24,
                "Accept",
                callActionIntent(CallActionReceiver.ACTION_ACCEPT)
            )
            addAction(
                R.drawable.outline_clear_24,
                "Decline",
                callActionIntent(CallActionReceiver.ACTION_DECLINE)
            )
        }.build()
    }

    override suspend fun buildOutgoingCallNotification(phone: String): Notification {
        val contact = loadContactUi(phone)
        return baseCallBuilder(
            title = "Outgoing Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_HIGH
        ).apply {
            setAutoCancel(false)
            setOngoing(true)
            setCategory(NotificationCompat.CATEGORY_CALL)
            addAction(
                R.drawable.outline_clear_24,
                "Hang up",
                callActionIntent(CallActionReceiver.ACTION_DECLINE)
            )
        }.build()
    }

    override suspend fun buildOngoingCallNotification(
        phone: String,
        durationSeconds: Int
    ): Notification {
        val contact = loadContactUi(phone)
        return baseCallBuilder(
            title = "Ongoing Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_LOW
        ).apply {
            setAutoCancel(false)
            setOngoing(true)
            setCategory(NotificationCompat.CATEGORY_CALL)
            setUsesChronometer(true)
            setWhen(System.currentTimeMillis() - durationSeconds * 1000L)
            addAction(
                R.drawable.outline_clear_24,
                "Hang up",
                callActionIntent(CallActionReceiver.ACTION_DECLINE)
            )
        }.build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun displayIncomingCallNotification(phone: String) {
        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, buildIncomingCallNotification(phone))
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun displayOngoingCallNotification(phone: String) {
        notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, buildOngoingCallNotification(phone, 0))
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
        contactUiCache[phone]?.let { return it }
        val contact = contactRepository.getContactByPhone(phone)
        val contactBitmap = contact?.image?.let { contactPhotoProvider.getContactPhotoBitmap(it) }
        val ui = if (contact != null) {
            ContactUi(
                title = contact.name,
                subtitle = contact.phone,
                avatar = contactBitmap
            )
        } else {
            ContactUi(
                title = "Unknown",
                subtitle = phone,
                avatar = defaultAvatar()
            )
        }
        contactUiCache[phone] = ui
        return ui
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
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            this.action = action
        }
        val requestCode = action.hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun defaultAvatar(): Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.profile_pic)
}
