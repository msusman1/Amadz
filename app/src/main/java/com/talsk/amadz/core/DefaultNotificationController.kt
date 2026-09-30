package com.talsk.amadz.core

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.LruCache
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.talsk.amadz.MainActivity
import com.talsk.amadz.R
import com.talsk.amadz.domain.NotificationController
import com.talsk.amadz.domain.repo.ContactPhotoBitmapProvider
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.ongoingCall.CallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class CallNotificationType {
    INCOMING,
    OUTGOING,
    ONGOING
}


data class ContactUi(
    val title: String,
    val subtitle: String?,
    val avatar: Bitmap?
)

@Singleton
class DefaultNotificationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val contactPhotoBitmapProvider: ContactPhotoBitmapProvider
) : NotificationController {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val contactUiCache = LruCache<String, ContactUi>(30)

    private val defaultAvatar: Bitmap by lazy(LazyThreadSafetyMode.NONE) {
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.profile_pic
        )
    }

    init {
        createNotificationChannels()
    }

    override fun buildForegroundNotification(
        phone: String,
        type: CallNotificationType,
        isMuted: Boolean,
        isSpeakerOn: Boolean
    ): Notification {
        val contact: ContactUi = contactUiCache.get(phone) ?: ContactUi(
            title = "Unknown",
            subtitle = phone,
            avatar = null
        )

        return buildCallNotification(
            phone = phone,
            type = type,
            contact = contact,
            useRichStyle = false,
            durationSeconds = null,
            isMuted = isMuted,
            isSpeakerOn = isSpeakerOn
        )

    }

    override suspend fun buildCallNotification(
        phone: String,
        type: CallNotificationType,
        durationSeconds: Int,
        isMuted: Boolean,
        isSpeakerOn: Boolean
    ): Notification {
        val contact = loadContactUi(phone)
        return buildCallNotification(
            phone = phone,
            type = type,
            contact = contact,
            useRichStyle = true,
            durationSeconds = durationSeconds,
            isMuted = isMuted,
            isSpeakerOn = isSpeakerOn
        )
    }

    private fun buildCallNotification(
        phone: String,
        type: CallNotificationType,
        contact: ContactUi,
        useRichStyle: Boolean,
        durationSeconds: Int?,
        isMuted: Boolean,
        isSpeakerOn: Boolean
    ): Notification {
        val builder = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(type.title)
            .setContentText(contact.displayDetails)
            .setSubText(contact.subtitle)
            .setLargeIcon(contact.avatar ?: defaultAvatar)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(type.priority)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(callActivityIntent(phone))
        when (type) {
            CallNotificationType.INCOMING -> {
                configureIncomingNotification(
                    builder = builder,
                    phone = phone,
                    contact = contact,
                    useRichStyle = useRichStyle
                )
            }

            CallNotificationType.OUTGOING -> {
                configureOngoingNotification(
                    builder = builder,
                    phone = phone,
                    contact = contact,
                    useRichStyle = useRichStyle,
                    durationSeconds = durationSeconds,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    showAudioActions = false
                )
            }

            CallNotificationType.ONGOING -> {
                configureOngoingNotification(
                    builder = builder,
                    phone = phone,
                    contact = contact,
                    useRichStyle = useRichStyle,
                    durationSeconds = durationSeconds,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    showAudioActions = true,
                    showChronometer = true
                )
            }
        }
        return builder.build()
    }

    private fun configureIncomingNotification(
        builder: NotificationCompat.Builder,
        phone: String,
        contact: ContactUi,
        useRichStyle: Boolean
    ) {
        builder.setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(callActivityIntent(phone), true)
            .setDeleteIntent(
                callActionIntent(
                    action = CallActionReceiver.ACTION_DECLINE,
                    phone = phone
                )
            )
        if (useRichStyle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    contact.toPerson(),
                    callActionIntent(CallActionReceiver.ACTION_DECLINE, phone),
                    callActionIntent(CallActionReceiver.ACTION_ACCEPT, phone)
                )
            )
        } else {
            builder.addAction(
                R.drawable.baseline_check_24,
                "Accept",
                callActionIntent(CallActionReceiver.ACTION_ACCEPT, phone)
            )
            builder.addAction(
                R.drawable.outline_clear_24,
                "Decline",
                callActionIntent(CallActionReceiver.ACTION_DECLINE, phone)
            )
        }
    }

    private fun configureOngoingNotification(
        builder: NotificationCompat.Builder,
        phone: String,
        contact: ContactUi,
        useRichStyle: Boolean,
        durationSeconds: Int?,
        isMuted: Boolean,
        isSpeakerOn: Boolean,
        showAudioActions: Boolean,
        showChronometer: Boolean = false
    ) {
        builder.setCategory(NotificationCompat.CATEGORY_CALL).setSilent(true)
            .setOnlyAlertOnce(true)
        if (showChronometer) {
            builder.setUsesChronometer(true)
                .setWhen(
                    System.currentTimeMillis() - (durationSeconds ?: 0).coerceAtLeast(0) * 1_000L
                )
        }
        if (useRichStyle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    contact.toPerson(),
                    callActionIntent(CallActionReceiver.ACTION_DECLINE, phone)
                )
            )
        } else {
            builder.addAction(
                R.drawable.outline_clear_24,
                "Hang up",
                callActionIntent(CallActionReceiver.ACTION_DECLINE, phone)
            )
        }
        if (showAudioActions) {
            builder.addAction(
                if (isSpeakerOn) {
                    R.drawable.outline_volume_off_24
                } else {
                    R.drawable.outline_volume_up_24
                },
                "Speaker",
                callActionIntent(
                    action = CallActionReceiver.ACTION_SPEAKER,
                    phone = phone,
                    enabled = !isSpeakerOn
                )
            )
            builder.addAction(
                if (isMuted) {
                    R.drawable.outline_mic_24
                } else {
                    R.drawable.outline_mic_off_24
                },
                if (isMuted) "Unmute" else "Mute",
                callActionIntent(
                    action = CallActionReceiver.ACTION_MUTE,
                    phone = phone,
                    enabled = !isMuted
                )
            )

        }
    }


    override suspend fun showMissedCallNotification(phone: String) {
        val contact = loadContactUi(phone)
        val notification = NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo).setContentTitle("Missed Call")
            .setContentText(contact.title).setSubText(contact.subtitle)
            .setLargeIcon(contact.avatar ?: defaultAvatar).setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainActivityIntent()).build()
        runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, notification)
            }

        }
    }


    private suspend fun loadContactUi(phone: String): ContactUi {
        contactUiCache.get(phone)?.let { return it }
        val contact = runCatching { contactRepository.getContactByPhone(phone) }.getOrNull()
        val avatar = contact?.image?.let { image ->
            runCatching { contactPhotoBitmapProvider.getContactPhotoBitmap(image) }.getOrNull()
        }
        val result = if (contact != null) {
            ContactUi(
                title = contact.name.ifBlank { "Unknown" },
                subtitle = contact.phone.ifBlank { phone },
                avatar = avatar
            )
        } else {
            ContactUi(
                title = "Unknown",
                subtitle = phone,
                avatar = null
            )
        }
        contactUiCache.put(phone, result)
        return result
    }


    private fun createNotificationChannels() {
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

    private fun callActivityIntent(phone: String? = null): PendingIntent {
        val intent = Intent(context, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            phone?.let { putExtra(CallActivity.EXTRA_PHONE, it) }
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


    private fun callActionIntent(
        action: String,
        phone: String,
        enabled: Boolean? = null
    ): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            this.action = action
            putExtra(CallActionReceiver.EXTRA_PHONE, phone)
            enabled?.let { putExtra(CallActionReceiver.EXTRA_ENABLED, it) }
        }
        val requestCode = "$action:$phone:$enabled".hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ContactUi.toPerson(): Person {
        return Person.Builder()
            .setName(title)
            .setImportant(true)
            .build()
    }

    private val CallNotificationType.title: String
        get() = when (this) {
            CallNotificationType.INCOMING -> "Incoming Call"
            CallNotificationType.OUTGOING -> "Outgoing Call"
            CallNotificationType.ONGOING -> "Ongoing Call"
        }

    private val ContactUi.displayDetails: String
        get() = subtitle?.takeIf(String::isNotBlank) ?: title
    private val CallNotificationType.priority: Int
        get() =
            when (this) {
                CallNotificationType.INCOMING -> NotificationCompat.PRIORITY_MAX
                CallNotificationType.OUTGOING, CallNotificationType.ONGOING -> NotificationCompat.PRIORITY_LOW
            }
    private val CallNotificationType.channelId: String
        get() =
            when (this) {
                CallNotificationType.INCOMING -> INCOMING_CALL_CHANNEL_ID
                CallNotificationType.OUTGOING, CallNotificationType.ONGOING -> ONGOING_CALL_CHANNEL_ID
            }


    companion object {
        private const val INCOMING_CALL_CHANNEL_ID = "AMADZ_INCOMING_CALL_NOTIFICATION_ID"
        private const val INCOMING_CALL_CHANNEL_NAME = "AMADZ_INCOMING_CALL_NOTIFICATION"
        private const val ONGOING_CALL_CHANNEL_ID = "AMADZ_ONGOING_CALL_NOTIFICATION_ID"
        private const val ONGOING_CALL_CHANNEL_NAME = "AMADZ_ONGOING_CALL_NOTIFICATION"
        private const val MISSED_CALL_NOTIFICATION_ID = 125
    }
}
