package com.rjw.audioprofile.activity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import com.rjw.audioprofile.R
import android.content.ComponentName
import android.os.Build
import com.rjw.audioprofile.databinding.ActivityPermissionBinding


class PermissionRequest : AudioActivity() {
    lateinit var binding: ActivityPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.setWindowRatios(0.9f, 0.5f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
        binding = ActivityPermissionBinding.bind(view!!)
        if(Build.MANUFACTURER.compareTo("Samsung", true) == 0) {
            binding.textNeverSleep.visibility = View.VISIBLE
            binding.textNeverSleepInstructions.visibility = View.VISIBLE
            binding.buttonNeverSleep.visibility = View.VISIBLE
            binding.textBattery.visibility = View.GONE
            binding.buttonBattery.visibility = View.GONE
        }
        colourControls()
    }

    override fun onResume() {
        super.onResume()
        // If we already have the permission, just leave.
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        val pm = getSystemService(POWER_SERVICE) as PowerManager?
        if(nm == null || pm == null) {
            finish()
        }
        if(nm!!.isNotificationPolicyAccessGranted && pm!!.isIgnoringBatteryOptimizations(packageName)) {
            finish()
        }
    }

    fun onClickRequestPermissionDND(v: View?) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivityForResult(intent, REQUEST_PERMISSION_RESPONSE)
    }

    fun onClickRequestPermissionBattery(v: View?) {
        var intentOptimization = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        startActivityForResult(intentOptimization, REQUEST_PERMISSION_RESPONSE)
    }

    fun onClickRequestNeverSleep(v: View?) {
        try {
            val intentSleeping = Intent()
            intentSleeping.component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")
            startActivityForResult(intentSleeping, REQUEST_PERMISSION_RESPONSE)
        } catch (e: Exception) {
            // Try something else...
            try {
                val intentSleeping = Intent()
                intentSleeping.component = ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")
                startActivityForResult(intentSleeping, REQUEST_PERMISSION_RESPONSE)
            } catch (e: Exception) {
                // Do nothing, just skip.
            }
        }
    }

    fun onClickClose(v: View?) {
        finish()
    }

    companion object {
        final val REQUEST_PERMISSION_RESPONSE = 1
    }
}