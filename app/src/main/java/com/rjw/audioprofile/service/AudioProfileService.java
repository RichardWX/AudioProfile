package com.rjw.audioprofile.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.rjw.audioprofile.R;
import com.rjw.audioprofile.activity.MainActivity;
import com.rjw.audioprofile.utils.AudioProfileList;

public class AudioProfileService extends Service {
    public static final String UNKNOWN_SSID = "<unknown ssid>";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                    action.equals(WifiManager.NETWORK_IDS_CHANGED_ACTION) ||
                    action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                // Wifi has been connected or disconnected, change the audio profile.
                final WifiManager wm = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    final String ssid = wm.getConnectionInfo().getSSID();
                    if(ssid.isEmpty() || ssid.equals(UNKNOWN_SSID)) {
                        final int profile = AudioProfileList.getExitWifiProfile();
                        if(profile != -1) {
                            AudioProfileList.setCurrentProfile(profile);
                            AudioProfileList.applyProfile(context);
                            MainActivity.updateTile(context);
                        }
                        Log.d("AudioProfile", "Action: " + intent.getAction() + ", connected to " + ssid + ", set audio profile to " + profile);
                    } else {
                        final int profile = AudioProfileList.getEnterWifiProfile();
                        if(profile != -1) {
                            AudioProfileList.setCurrentProfile(profile);
                            AudioProfileList.applyProfile(context);
                            MainActivity.updateTile(context);
                        }
                        Log.d("AudioProfile", "Action: " + intent.getAction() + ", connected to " + ssid + ", set audio profile to " + profile);
                    }
                }
            }
        }
    };

    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(mReceiver, filter);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        MainActivity.createNotificationChannel(this);
        MainActivity.showServiceNotification(this, getString(R.string.notification), pendingIntent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}
