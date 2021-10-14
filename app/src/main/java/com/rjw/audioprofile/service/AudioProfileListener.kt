package com.rjw.audioprofile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rjw.audioprofile.activity.MainActivity

class AudioProfileListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if(context != null && intent != null) {
                when(intent.action) {
                    Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_USER_UNLOCKED -> try {
                        val serviceIntent = Intent(context, AudioProfileService::class.java)
                        context.startForegroundService(serviceIntent)
                        MainActivity.updateTile(context)
                    } catch(e: Throwable) {
                        // Do nothing.
                    }
                }
            }
        } catch(e: Exception) {
            // Do nothing.
        }
    }
}