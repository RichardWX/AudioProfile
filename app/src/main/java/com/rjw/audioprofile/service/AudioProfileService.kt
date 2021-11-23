package com.rjw.audioprofile.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.IBinder
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.utils.AudioProfileList
import java.util.*

class  AudioProfileService : Service() {
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(context != null && intent != null) {
                val action = intent.action
                if(action == WifiManager.NETWORK_STATE_CHANGED_ACTION || action == WifiManager.NETWORK_IDS_CHANGED_ACTION || action == WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) {
                    // Wifi has been connected or disconnected, change the audio profile.
                    val wm = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    if(wm.wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        val ssid = wm.connectionInfo.ssid
                        if(ssid.isEmpty() || ssid == UNKNOWN_SSID) {
                            val profile = AudioProfileList.exitWifiProfile
                            if(profile != -1) {
                                val now = Calendar.getInstance().timeInMillis
                                val switch = if(AudioProfileList.lockProfileTime == -1) true else
                                    now - AudioProfileList.lastProfileSwitchTime > AudioProfileList.lockProfileTime * 60000
                                if(switch) {
                                    AudioProfileList.currentProfile = profile
                                    AudioProfileList.applyProfile(context)
                                    MainActivity.updateTile(context)
                                }
                            }
                        } else {
                            val profile = AudioProfileList.enterWifiProfile
                            if(profile != -1) {
                                val now = Calendar.getInstance().timeInMillis
                                val switch = if(AudioProfileList.lockProfileTime == -1) true else
                                    now - AudioProfileList.lastProfileSwitchTime > AudioProfileList.lockProfileTime * 60000
                                if(switch) {
                                    AudioProfileList.currentProfile = profile
                                    AudioProfileList.applyProfile(context)
                                    MainActivity.updateTile(context)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Get the profile list updated.
        AudioProfileList.initialise(this)
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
        registerReceiver(mReceiver, filter)
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        Notifications.createNotificationChannel(this)
        Notifications.showServiceNotification(this, getString(R.string.notification_service), pendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    companion object {
        const val UNKNOWN_SSID = "<unknown ssid>"
    }
}