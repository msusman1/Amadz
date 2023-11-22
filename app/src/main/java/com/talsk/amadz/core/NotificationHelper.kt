package com.talsk.amadz.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import androidx.core.app.NotificationManagerCompat
import com.talsk.amadz.R
import com.talsk.amadz.data.ContactImageRepository
import com.talsk.amadz.data.ContactsRepository
import com.talsk.amadz.ui.ongoingCall.CallActivity


/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */
private const val INCOMING_CALL_NOTIFICATION_ID = 123

class NotificationHelper(private val context: Context) {
    private val callChannelId = "AMADZ_CALL_NOTIFICATION_ID"
    private val callChannelName = "AMADZ_CALL_NOTIFICATION"
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)
  private val contactsRepository = ContactsRepository(context)
  private val contactImageRepository = ContactImageRepository(context)
    private var ringtone: Ringtone? = null

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                  callChannelId,
                  callChannelName,
                  NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Incoming call notification";
            val mgr = NotificationManagerCompat.from(context)
            mgr.createNotificationChannel(channel)
        }

        /* val ringtoneUri =
             RingtoneManager.getActualDefaultRingtoneUri(this.context, RingtoneManager.TYPE_RINGTONE)
         channel.setSound(
             ringtoneUri,
             AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                 .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
         )
         channel.setVibrationEnabled(true)*/

    }

    fun displayIncomingCallNotification(phone: String) {

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClass(context, CallActivity::class.java)
        intent.putExtra("phone", phone)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, callChannelId)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setContentIntent(pendingIntent)
        builder.setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.profile_pic)
        builder.setLargeIcon(largeIcon)
        val contactData = contactsRepository.getContactData(phone)
        builder.setContentText(phone)
        if (contactData != null) {
            builder.setContentText(contactData.name)
            val contactPic = contactImageRepository.loadContactImage(contactData.image)
            if (contactPic != null) {
                builder.setLargeIcon(contactPic)
            }
        }
        builder.setFullScreenIntent(pendingIntent, true)
        builder.setSmallIcon(R.drawable.app_logo_short_notification)
        builder.setContentTitle("Incoming Call")

        /*
        val acceptPendingIntent = PendingIntent.getService(
                    context,
                    0,
                    Intent(context, CallAcceptService::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

         val declinePendingIntent = PendingIntent.getService(
                    context,
                    0,
                    Intent(context, CallDeclineService::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )


                builder.addAction(R.drawable.baseline_check_24, "Accept", acceptPendingIntent)
                builder.addAction(R.drawable.outline_clear_24, "Decline", declinePendingIntent)
        */
        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, builder.build())
    }

    fun cancelIncommingCallNotification() {
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }

    fun playCallRingTone() {
        val notification = RingtoneManager.getActualDefaultRingtoneUri(this.context,RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(context, notification)
        ringtone?.play()
    }

    fun stepCallRingTone() {
        ringtone?.stop()
    }
}