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
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.talsk.amadz.MainActivity
import com.talsk.amadz.R
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.ongoingCall.CallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val INCOMING_CALL_CHANNEL_ID = "AMADZ_INCOMING_CALL_NOTIFICATION_ID"
private const val INCOMING_CALL_CHANNEL_NAME = "AMADZ_INCOMING_CALL_NOTIFICATION"
private const val ONGOING_CALL_CHANNEL_ID = "AMADZ_ONGOING_CALL_NOTIFICATION_ID"
private const val ONGOING_CALL_CHANNEL_NAME = "AMADZ_ONGOING_CALL_NOTIFICATION"

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

        val builder = baseCallBuilder(
            title = "Incoming Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_MAX,
            phone = phone,
            channelId = INCOMING_CALL_CHANNEL_ID
        ).apply {
            setAutoCancel(false)
            setOngoing(true)
            setCategory(NotificationCompat.CATEGORY_CALL)
            setFullScreenIntent(callActivityIntent(phone), true)
            setDeleteIntent(callActionIntent(CallActionReceiver.ACTION_DECLINE))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person = contact.toPerson()
            builder.setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    person,
                    callActionIntent(CallActionReceiver.ACTION_DECLINE),
                    callActionIntent(CallActionReceiver.ACTION_ACCEPT)
                )
            )
        } else {
            builder.addAction(
                R.drawable.baseline_check_24,
                "Accept",
                callActionIntent(CallActionReceiver.ACTION_ACCEPT)
            )
            builder.addAction(
                R.drawable.outline_clear_24,
                "Decline",
                callActionIntent(CallActionReceiver.ACTION_DECLINE)
            )
        }

        return builder.build()
    }

    override suspend fun buildOutgoingCallNotification(phone: String): Notification {
        val contact = loadContactUi(phone)
        val builder = baseCallBuilder(
            title = "Outgoing Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_LOW,
            phone = phone,
            channelId = ONGOING_CALL_CHANNEL_ID
        ).apply {
            setAutoCancel(false)
            setOngoing(true)
            setOnlyAlertOnce(true)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            setSilent(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person = contact.toPerson()
            builder.setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    person,
                    callActionIntent(CallActionReceiver.ACTION_DECLINE)
                )
            )
        } else {
            builder.addAction(
                R.drawable.outline_clear_24,
                "Hang up",
                callActionIntent(CallActionReceiver.ACTION_DECLINE)
            )
        }

        return builder.build()
    }

    override suspend fun buildOngoingCallNotification(
        phone: String,
        durationSeconds: Int
    ): Notification {
        val contact = loadContactUi(phone)
        val builder = baseCallBuilder(
            title = "Ongoing Call",
            content = contact.title,
            subText = contact.subtitle,
            largeIcon = contact.avatar,
            priority = NotificationCompat.PRIORITY_LOW,
            phone = phone,
            channelId = ONGOING_CALL_CHANNEL_ID
        ).apply {
            setAutoCancel(false)
            setOngoing(true)
            setOnlyAlertOnce(true)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
            setSilent(true)
            setUsesChronometer(true)
            setWhen(System.currentTimeMillis() - durationSeconds * 1000L)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person = contact.toPerson()
            builder.setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    person,
                    callActionIntent(CallActionReceiver.ACTION_DECLINE)
                )
            )
        } else {
            builder.addAction(
                R.drawable.outline_clear_24,
                "Hang up",
                callActionIntent(CallActionReceiver.ACTION_DECLINE)
            )
        }

        return builder.build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun showMissedCallNotification(phone: String) {
        val contact = loadContactUi(phone)

        val builder = NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_short_notification)
            .setContentTitle("Missed Call")
            .setContentText(contact.title)
            .setSubText(contact.subtitle)
            .setLargeIcon(contact.avatar)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent())

        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, builder.build())
    }

    // ----------------------------
    // Internal Helpers
    // ----------------------------

    private fun createNotificationChannel() {
        val incomingChannel = NotificationChannel(
            INCOMING_CALL_CHANNEL_ID,
            INCOMING_CALL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Amadz Incoming Call Notifications"
        }

        val ongoingChannel = NotificationChannel(
            ONGOING_CALL_CHANNEL_ID,
            ONGOING_CALL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Amadz Ongoing and Outgoing Call Notifications"
            setSound(null, null)
            enableVibration(false)
            vibrationPattern = longArrayOf(0L)
        }

        notificationManager.createNotificationChannel(incomingChannel)
        notificationManager.createNotificationChannel(ongoingChannel)
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
        priority: Int,
        phone: String,
        channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo_short_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(subText)
            .setLargeIcon(largeIcon)
            .setOngoing(true)
            .setPriority(priority)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(callActivityIntent(phone))
    }

    private fun callActivityIntent(phone: String? = null): PendingIntent {
        val intent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            phone?.let { putExtra("phone", it) }
        }
        val requestCode = phone?.hashCode() ?: 0
        return PendingIntent.getActivity(
            context,
            requestCode,
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

    private fun ContactUi.toPerson(): Person {
        return Person.Builder()
            .setName(title)
            .setImportant(true)
            .build()
    }
}
