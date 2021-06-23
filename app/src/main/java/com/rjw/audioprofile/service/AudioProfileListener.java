package com.rjw.audioprofile.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.rjw.audioprofile.activity.MainActivity;

public class AudioProfileListener extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            switch(intent.getAction()) {
                case Intent.ACTION_BOOT_COMPLETED:
                case Intent.ACTION_LOCKED_BOOT_COMPLETED:
                case Intent.ACTION_USER_UNLOCKED:
                    try {
                        final Intent serviceIntent = new Intent(context, AudioProfileService.class);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent);
                        } else {
                            context.startService(serviceIntent);
                        }
						MainActivity.updateTile(context);
                    } catch(Throwable e) {
                        // Do nothing.
                    }
                    break;
            }
        } catch(Exception e) {
            // Do nothing.
        }
    }
}
