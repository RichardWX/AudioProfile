package com.rjw.audioprofile.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.utils.AudioProfileList
import java.util.Calendar

@Suppress("DEPRECATION")
class AudioProfileService : Service() {
    private var mSsid = ""

    private val mReceiver = object : BroadcastReceiver() {
        /**
         * Deal with incoming intents.
         * @param context The application context.
         * @param intent  The incoming intent.
         */
        override fun onReceive(context: Context?, intent: Intent?) {
            if(context != null && intent != null) {
                // Wifi has been connected or disconnected, change the audio profile.
                val wm = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                if(wm.wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    var ssid = wm.connectionInfo.ssid
                    if(ssid.isEmpty() || ssid == UNKNOWN_SSID) {
                        if(mSsid.isNotEmpty()) {
                            mSsid = ""
                            val profile = AudioProfileList.exitWifiProfile
                            if(profile != -1) {
                                // Check whether the profile has been locked - if so, don't change it.
                                val now = Calendar.getInstance().timeInMillis
                                val switch = if(AudioProfileList.profileLocked) {
                                    now - AudioProfileList.profileLockStartTime > AudioProfileList.lockProfileTime * 60000
                                } else {
                                    true
                                }
                                if(switch) {
                                    AudioProfileList.currentProfile = profile
                                    AudioProfileList.applyProfile(context)
                                    MainActivity.updateTile(context)
                                }
                            }
                        }
                    } else {
                        if(ssid[0] == '\"') {
                            ssid = ssid.substring(1, ssid.length - 1)
                        }
                        mSsid = ssid
                        val profile = AudioProfileList.enterWifiProfile
                        if(profile != -1) {
                            // Check whether the profile has been locked - if so, don't change it.
                            val now = Calendar.getInstance().timeInMillis
                            val switch = if(AudioProfileList.profileLocked) {
                                now - AudioProfileList.profileLockStartTime > AudioProfileList.lockProfileTime * 60000
                            } else {
                                true
                            }
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
        AudioProfileList.initialise(this)
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        ContextCompat.registerReceiver(this, mReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        Notifications.createNotificationChannel(this)
        Notifications.showServiceNotification(this, getString(R.string.notification_service), pendingIntent)
    }

    /**
     * Destroy the service by unregistering the intent listener.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
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

    companion object {
        const val UNKNOWN_SSID = "<unknown ssid>"
    }
}