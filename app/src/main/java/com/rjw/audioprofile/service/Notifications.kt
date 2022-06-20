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

    /**
     * Create the notification channel for displaying notifications.
     * @param context The application context.
     */
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

    /**
     * Show the notification to allow the service to run in the background.
     * @param service       The service being started.
     * @param msg           The message to display in the notification.
     * @param pendingIntent The intent to be sent when the notification is clicked on.
     */
    fun showServiceNotification(service: Service?, msg: String?, pendingIntent: PendingIntent?) {
        try {
            if(service != null) {
                mNotificationBuilder = Notification.Builder(service)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(AudioProfileList.getIconResource(AudioProfileList.getProfile(AudioProfileList.currentProfile).icon))
                    .setContentTitle(msg)
                    .setContentIntent(pendingIntent)
                service.startForeground(SERVICE_NOTIFICATION_ID, mNotificationBuilder!!.build())
                updateNotification(service)
            }
        } catch(e: Exception) {
            Alerts.toast("Creating notification: ${e.javaClass.name}\n${e.message}")
        }
    }

    /**
     * Update the current notification with a new text and icon.
     * @param context The application context.
     */
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
                mNotificationBuilder!!.setSmallIcon(AudioProfileList.getIconResource(AudioProfileList.getProfile(AudioProfileList.currentProfile).icon))
                mNm!!.notify(SERVICE_NOTIFICATION_ID, mNotificationBuilder!!.build())
            }
        } catch(e: Exception) {
            Alerts.toast("Updating notification: ${e.javaClass.name}\n${e.message}")
        }
    }
}