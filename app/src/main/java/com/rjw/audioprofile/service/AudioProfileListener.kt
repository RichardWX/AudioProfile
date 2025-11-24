package com.rjw.audioprofile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.utils.Alerts

class AudioProfileListener : BroadcastReceiver() {
    /**
     * Process incoming intents.
     * @param context The application context.
     * @param intent  The intent to be handled.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if(context != null && intent != null) {
                when(intent.action) {
                    Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_USER_UNLOCKED -> try {
                        val serviceIntent = Intent(context, AudioProfileService::class.java)
                        context.startService(serviceIntent)
                        Alerts.clearLog()
                        Alerts.log("Device rebooted")
                        MainActivity.updateTile()
                    } catch(_: Throwable) {
                        // Do nothing.
                    }
                }
            }
        } catch(_: Exception) {
            // Do nothing.
        }
    }
}