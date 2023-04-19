package com.rjw.audioprofile.activity

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.rjw.audioprofile.R
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
        colourControls()
    }

    /**
     * Check the permissions when the activity resumes.
     */
    override fun onResume() {
        super.onResume()
        // If we already have the permission, just leave.
        val doNotDisturb = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
        val batteryOptimizations = (getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
        val notifications =
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        if(doNotDisturb) {
            binding.textDoNotDisturb.visibility = View.GONE
            binding.buttonDoNotDisturb.visibility = View.GONE
        }
        if(batteryOptimizations) {
            binding.textBattery.visibility = View.GONE
            binding.buttonBattery.visibility = View.GONE
        } else {
            if(Build.MANUFACTURER.compareTo("Samsung", true) == 0) {
                binding.textNeverSleep.visibility = View.VISIBLE
                binding.textNeverSleepInstructions.visibility = View.VISIBLE
                binding.buttonNeverSleep.visibility = View.VISIBLE
            }
        }
        if(notifications) {
            binding.textPermissionsInstructions.visibility = View.GONE
            binding.buttonPermissions.visibility = View.GONE
        }
        if(doNotDisturb && batteryOptimizations && notifications) {
            finish()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PERMISSION_RESPONSE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(MainActivity.TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                }
                else -> {
                    // Permission denied.
                }
            }
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
                // Do nothing.
            }
        }
    }

    /**
     * Point the user at the Application settings.
     * @param v The view in question.
     */
    fun onClickRequestPermissions(v: View?) {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_PERMISSION_RESPONSE
        )
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