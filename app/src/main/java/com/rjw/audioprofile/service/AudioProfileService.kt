package com.rjw.audioprofile.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.utils.Alerts
import com.rjw.audioprofile.utils.AudioProfileList
import java.util.Calendar

@Suppress("DEPRECATION")
class AudioProfileService : Service() {
    val UNKNOWN_SSID = "<unknown ssid>"
    val UPDATE_DELAY = 500L
    private var ssid = ""
    private var initialised = false

    private val receiver = object : BroadcastReceiver() {
        /**
         * Deal with incoming intents.
         * @param context The application context.
         * @param intent  The incoming intent.
         */
        override fun onReceive(context: Context?, intent: Intent?) {
            if(context != null && intent != null) {
                val now = Calendar.getInstance().timeInMillis
                when(intent.action) {
                    WifiManager.NETWORK_STATE_CHANGED_ACTION, WifiManager.NETWORK_IDS_CHANGED_ACTION, WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION, ConnectivityManager.CONNECTIVITY_ACTION -> {
                        // Wifi has been connected or disconnected, change the audio profile.
                        val wm = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                        if(wm.wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            var newSsid = wm.connectionInfo.ssid
                            if(newSsid.isEmpty() || newSsid == UNKNOWN_SSID) {
                                Alerts.log("${intent.action?.substringAfterLast(".")} - disconnected")
                                if(ssid.isNotEmpty()) {
                                    Alerts.log("Exit profile being fired.")
                                    ssid = ""
                                    val profile = AudioProfileList.exitWifiProfile
                                    if(profile != -1) {
                                        // Check whether the profile has been locked - if so, don't change it.
                                        val switch = if(AudioProfileList.profileLocked) {
                                            now - AudioProfileList.profileLockStartTime > AudioProfileList.lockProfileTime * 60000
                                        } else {
                                            true
                                        }
                                        if(switch) {
                                            Alerts.log("Switching profile to ${AudioProfileList.getProfile(profile).name}")
                                            AudioProfileList.currentProfile = profile
                                            AudioProfileList.applyProfile(context)
                                            Handler(mainLooper).postDelayed({
                                                Notifications.updateNotification(this@AudioProfileService)
                                            }, UPDATE_DELAY)
                                        }
                                    }
                                }
                            } else {
                                if(newSsid[0] == '\"') {
                                    newSsid = newSsid.substring(1, newSsid.length - 1)
                                }
                                Alerts.log("${intent.action?.substringAfterLast(".")} - newSSid = $newSsid")
                                if(ssid != newSsid) {
                                    ssid = newSsid
                                    Alerts.log("Entry profile being fired for $ssid.")
                                    val profile = AudioProfileList.enterWifiProfile
                                    if(profile != -1) {
                                        // Check whether the profile has been locked - if so, don't change it.
                                        val switch = if(AudioProfileList.profileLocked) {
                                            now - AudioProfileList.profileLockStartTime > AudioProfileList.lockProfileTime * 60000
                                        } else {
                                            true
                                        }
                                        if(switch) {
                                            Alerts.log("Switching profile to ${AudioProfileList.getProfile(profile).name}")
                                            AudioProfileList.currentProfile = profile
                                            AudioProfileList.applyProfile(context)
                                            Handler(mainLooper).postDelayed({
                                                Notifications.updateNotification(this@AudioProfileService)
                                            }, UPDATE_DELAY)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Intent.ACTION_TIME_TICK -> {
                        if(now - AudioProfileList.profileLockStartTime > AudioProfileList.lockProfileTime * 60000 && AudioProfileList.profileLocked) {
                            AudioProfileList.currentProfile = AudioProfileList.previousProfile
                            AudioProfileList.profileLocked = false
                            MainActivity.updateTile()
                        }
                    }
                }
            }
        }
    }

    /**
     * Bind the service.
     * @param intent The intent to start the service.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Create the service and add the intents that will be listened for.
     */
    override fun onCreate() {
        super.onCreate()

        // Get the profile list updated.
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(Intent.ACTION_TIME_TICK)
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        if(!initialised) {
            Notifications.createNotificationChannel(this)
            Notifications.showServiceNotification(this, getString(R.string.notification_service), pendingIntent)
        }
        initialised = true
    }

    /**
     * Destroy the service by unregistering the intent listener.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    /**
     * Set the service as permanently running.
     * @param intent  The intent for starting the service.
     * @param flags   The flags for starting the service.
     * @param startId The id to identify the service.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
}