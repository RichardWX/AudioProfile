package com.rjw.audioprofile.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rjw.audioprofile.R;
import com.rjw.audioprofile.activity.AudioActivity;

public class PermissionRequest extends AudioActivity {
    public final static int REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.setWindowRatios(0.6f, 0.5f);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        colourControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If we already have the permission, just leave.
        final NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(nm == null || nm.isNotificationPolicyAccessGranted()) {
            finish();
        }
    }

    public void onClickRequestPermission(final View v) {
        final Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivityForResult(intent, REQUEST_PERMISSIONS);
    }

    public void onClickOK(final View v) {
        finish();
    }
}
