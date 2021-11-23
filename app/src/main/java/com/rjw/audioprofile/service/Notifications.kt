package com.rjw.audioprofile.service

import android.app.*
import android.content.Context
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.Alerts
import com.rjw.audioprofile.utils.AudioProfileList

object Notifications {
    private const val CHANNEL_ID = "AudioProfileChannelId"
    private val CHANNEL_NAME: CharSequence = "AudioProfile"
    private const val CHANNEL_DESCRIPTION = "AudioProfile"
    private const val SERVICE_NOTIFICATION_ID = 100

    private var mNm: NotificationManager? = null
    private var mNotificationBuilder: Notification.Builder? = null

    fun createNotificationChannel(context: Context) {
        try {
            mNm = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager?
            if(mNm != null) {
                val channel = NotificationChannel(CHANNEL_ID,   CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
                channel.description = CHANNEL_DESCRIPTION
                channel.setShowBadge(false)
                channel.enableLights(false)
                channel.setSound(null, null)
                mNm!!.createNotificationChannel(channel)
            }
        } catch(e: Exception) {
            Alerts.toast("Creating notification channel: ${e.javaClass.name}\n${e.message}")
        }
    }

    fun showServiceNotification(service: Service?, msg: String?, pendingIntent: PendingIntent?) {
        try {
            if(service != null) {
                mNotificationBuilder = Notification.Builder(service)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle(msg)
                    .setContentIntent(pendingIntent)
                service.startForeground(SERVICE_NOTIFICATION_ID, mNotificationBuilder!!.build())
                updateNotification(service)
            }
        } catch(e: Exception) {
            Alerts.toast("Creating notification: ${e.javaClass.name}\n${e.message}")
        }
    }

    fun updateNotification(context: Context) {
        try {
            if(mNm == null) {
                mNm = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager?
            }
            if(mNm != null && mNotificationBuilder != null) {
                mNotificationBuilder!!.setContentText(
                    String.format(
                        context.getString(R.string.notification_profile),
                        AudioProfileList.getProfile(AudioProfileList.currentProfile).name
                    )
                )
                mNm!!.notify(SERVICE_NOTIFICATION_ID, mNotificationBuilder!!.build())
            }
        } catch(e: Exception) {
            Alerts.toast("Updating notification: ${e.javaClass.name}\n${e.message}")
        }
    }

}