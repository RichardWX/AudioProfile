@file:Suppress("DEPRECATION")

package com.rjw.audioprofile.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.utils.AudioProfileList

object Notifications {
    private const val CHANNEL_ID = "AudioProfileChannelId"
    private val CHANNEL_NAME: CharSequence = "Ongoing"
    private const val SERVICE_NOTIFICATION_ID = 100

    private var notificationManager: NotificationManager? = null
    private var notificationBuilder: Notification.Builder? = null

    /**
     * Create the notification channel for displaying notifications.
     * @param context The application context.
     */
    fun createNotificationChannel(context: Context) {
        runCatching {
            notificationManager = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.let { notificationManager ->
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
                channel.setShowBadge(false)
                channel.enableLights(false)
                channel.setSound(null, null)
                channel.description = context.getString(R.string.notification_description)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Show the notification to allow the service to run in the background.
     * @param service       The service being started.
     * @param msg           The message to display in the notification.
     * @param pendingIntent The intent to be sent when the notification is clicked on.
     */
    fun showServiceNotification(service: Service?, msg: String?, pendingIntent: PendingIntent?) {
        runCatching {
            if(service != null) {
                notificationBuilder = Notification.Builder(service)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(AudioProfileList.getIconResource(AudioProfileList.getProfile(AudioProfileList.currentProfile).icon))
                    .setContentTitle(msg)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                notificationBuilder?.let { builder ->
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        service.startForeground(SERVICE_NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                    } else {
                        service.startForeground(SERVICE_NOTIFICATION_ID, builder.build())
                    }
                    updateNotification(service)
                }
            }
        }
    }

    /**
     * Update the current notification with a new text and icon.
     * @param context The application context.
     * @param rebuild If true, rebuild the notification builder to ensure icon changes are applied.
     */
    fun updateNotification(context: Context?, rebuild: Boolean = false) {
        context?.let { context ->
            runCatching {
                if(notificationManager == null) {
                    notificationManager = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager?
                }
                notificationBuilder?.let { builder ->
                    builder.setContentText(
                        String.format(
                            context.getString(R.string.notification_profile),
                            AudioProfileList.getProfile(AudioProfileList.currentProfile).name,
                            if(AudioProfileList.profileLocked) context.getString(R.string.locked) else ""
                        )
                    )
                    builder.setSmallIcon(AudioProfileList.getIconResource(AudioProfileList.getProfile(AudioProfileList.currentProfile).icon))
                    notificationManager?.notify(SERVICE_NOTIFICATION_ID, builder.build())
                }
            }
        }
    }
}