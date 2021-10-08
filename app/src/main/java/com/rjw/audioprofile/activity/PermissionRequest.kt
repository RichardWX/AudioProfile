package com.rjw.audioprofile.activity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.rjw.audioprofile.R

class PermissionRequest : AudioActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setWindowRatios(0.6f, 0.5f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        colourControls()
    }

    override fun onResume() {
        super.onResume()
        // If we already have the permission, just leave.
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        if(nm == null || nm.isNotificationPolicyAccessGranted) {
            finish()
        }
    }

    fun onClickRequestPermission(v: View?) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivityForResult(intent, REQUEST_PERMISSIONS)
    }

    fun onClickOK(v: View?) {
        finish()
    }

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }
}