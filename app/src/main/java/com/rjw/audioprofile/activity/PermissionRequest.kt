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
    private lateinit var binding: ActivityPermissionBinding

    /**
     * Create the activity.
     * @param savedInstanceState The state information for the activity.
     */
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

    /**
     * Check the permissions when the activity resumes.
     */
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

    /**
     * Point the user at the Do not disturb settings.
     * @param v The view in question.
     */
    fun onClickRequestPermissionDND(v: View?) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivityForResult(intent, REQUEST_PERMISSION_RESPONSE)
    }

    /**
     * Point the user at the Battery settings.
     * @param v The view in question.
     */
    fun onClickRequestPermissionBattery(v: View?) {
        val intentOptimization = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        startActivityForResult(intentOptimization, REQUEST_PERMISSION_RESPONSE)
    }

    /**
     * Point the user at the Battery optimization settings.
     * @param v The view in question.
     */
    fun onClickRequestNeverSleep(v: View?) {
        try {
            val intentSleeping = Intent()
            intentSleeping.component = ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")
            startActivityForResult(intentSleeping, REQUEST_PERMISSION_RESPONSE)
        } catch(e: Exception) {
            // Try something else...
            try {
                val intentSleeping = Intent()
                intentSleeping.component = ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")
                startActivityForResult(intentSleeping, REQUEST_PERMISSION_RESPONSE)
            } catch(e: Exception) {
                // Do nothing, just skip.
            }
        }
    }

    /**
     * Close the activity.
     * @param v The view in question.
     */
    fun onClickClose(v: View?) {
        finish()
    }

    companion object {
        const val REQUEST_PERMISSION_RESPONSE = 1
    }
}