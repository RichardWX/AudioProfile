package com.rjw.audioprofile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rjw.audioprofile.activity.MainActivity

class AudioProfileListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when(intent.action) {
                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_USER_UNLOCKED -> try {
                    val serviceIntent = Intent(context, AudioProfileService::class.java)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    MainActivity.updateTile(context)
                } catch(e: Throwable) {
                    // Do nothing.
                }
            }
        } catch(e: Exception) {
            // Do nothing.
        }
    }
}