package com.rjw.audioprofile.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.service.quicksettings.TileService
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import com.rjw.audioprofile.BuildConfig
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivityMainBinding
import com.rjw.audioprofile.databinding.ContentMainBinding
import com.rjw.audioprofile.service.AudioProfileService
import com.rjw.audioprofile.service.QuickPanel
import com.rjw.audioprofile.utils.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AudioActivity() {
    private val REQUEST_PERMISSION_RESPONSE = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingContent: ContentMainBinding
    private val mLockTimings = arrayOf(1, 2, 5, 10, 20, 30, 60, 90, 120)
    private val mRadioProfile = arrayOfNulls<RadioButton>(AudioProfileList.NO_PROFILES)
    private val mImageProfile = arrayOfNulls<ImageView>(AudioProfileList.NO_PROFILES)
    private val mTextProfile = arrayOfNulls<TextView>(AudioProfileList.NO_PROFILES)
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    private var mProfileLockChanged = false

    /**
     * Create the activity.
     * @param savedInstanceState The state information for the activity.
     */
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWindowRatio = floatArrayOf(0.9f, 0.7f)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(view!!)
        bindingContent = ContentMainBinding.bind(binding.layoutMain)

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler { _, e -> e.printStackTrace() }

        // Load the settings.
        profiles = AudioProfileList(this)
        mRadioProfile[0] = bindingContent.radioProfile0
        mRadioProfile[1] = bindingContent.radioProfile1
        mRadioProfile[2] = bindingContent.radioProfile2
        mRadioProfile[3] = bindingContent.radioProfile3
        mImageProfile[0] = bindingContent.imageProfile0
        mImageProfile[1] = bindingContent.imageProfile1
        mImageProfile[2] = bindingContent.imageProfile2
        mImageProfile[3] = bindingContent.imageProfile3
        mTextProfile[0] = bindingContent.textProfile0
        mTextProfile[1] = bindingContent.textProfile1
        mTextProfile[2] = bindingContent.textProfile2
        mTextProfile[3] = bindingContent.textProfile3

        if(!isServiceRunning) {
            startService()
        }

        // Check we have the required permissions.
        val doNotDisturb = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
        val batteryOptimizations = (getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
        val notifications =
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        if(!doNotDisturb || !notifications || !batteryOptimizations) {
            val intent = Intent(this, PermissionRequest::class.java)
            startActivityForResult(intent, REQUEST_PERMISSIONS)
        }
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_RESPONSE)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_PERMISSION_RESPONSE)
        }

        // Set the profile names and icons.
        for(profile in 0 until AudioProfileList.NO_PROFILES) {
            val audioProfile = AudioProfileList.getProfile(profile)
            mRadioProfile[profile]!!.buttonDrawable!!.setColorFilter(configColour, Mode.SRC_ATOP)
            mTextProfile[profile]!!.text = audioProfile.name
            mImageProfile[profile]!!.setImageDrawable(AudioProfileList.getIcon(audioProfile.icon))
        }

        // Set up the lock adapters.
        val profileLockTime = AudioProfileList.lockProfileTime
        val lockAdapter = MinutesAdapter(this, mLockTimings)
        bindingContent.spinnerLockProfile.adapter = lockAdapter
        bindingContent.spinnerLockProfile.background.setColorFilter(configColour, Mode.SRC_ATOP)
        bindingContent.checkboxLockProfile.buttonDrawable!!.setColorFilter(configColour, Mode.SRC_ATOP)

        // Set up the profile lock time.
        var profileLocked = AudioProfileList.profileLocked
        if(profileLocked and (Calendar.getInstance().timeInMillis > AudioProfileList.profileLockStartTime + profileLockTime * 60000)) {
            profileLocked = false
        }
        bindingContent.checkboxLockProfile.isChecked = profileLocked

        // Set the handler for the lock time spinner.
        AdapterView.OnItemClickListener { _, _, _, _ -> mProfileLockChanged = true }

        colourControls()
        updateControls()
    }

    /**
     * Handle the closing of the other activities.
     * @param requestCode The id of the activity that has been closed.
     * @param resultCode  The result of closing the activity.
     * @param data        The data returned from the closing activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_PERMISSIONS -> updateTile(this)
            REQUEST_AUDIO_PROFILE -> if(resultCode == RESULT_OK && data != null) {
                AudioProfileList.saveProfiles(this)
                val modified = data.getIntExtra(ProfileConfiguration.AUDIO_PROFILE, 0)
                mTextProfile[modified]!!.text = AudioProfileList.getProfile(modified).name
                mImageProfile[modified]!!.setImageDrawable(AudioProfileList.getIcon(AudioProfileList.getProfile(modified).icon))
                updateControls()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Set the current profile.
     * @param v The view in question.
     */
    fun onClickAudioProfile(v: View) {
        when(v.id) {
            R.id.radioProfile0, R.id.imageProfile0, R.id.textProfile0 -> selectRadio(0)
            R.id.radioProfile1, R.id.imageProfile1, R.id.textProfile1 -> selectRadio(1)
            R.id.radioProfile2, R.id.imageProfile2, R.id.textProfile2 -> selectRadio(2)
            R.id.radioProfile3, R.id.imageProfile3, R.id.textProfile3 -> selectRadio(3)
        }
    }

    /**
     * Update the specified audio profile.
     * @param v The view in question.
     */
    fun onClickAudioConfiguration(v: View) {
        val intent = Intent(this, ProfileConfiguration::class.java)
        when(v.id) {
            R.id.buttonConfigureProfile0 -> intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 0)
            R.id.buttonConfigureProfile1 -> intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 1)
            R.id.buttonConfigureProfile2 -> intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 2)
            R.id.buttonConfigureProfile3 -> intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 3)
        }
        startActivityForResult(intent, REQUEST_AUDIO_PROFILE)
    }

    /**
     * Display the application about information.
     * @param v The view in question.
     */
    fun onClickAbout(v: View?) {
        val about = StringBuilder()
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            about.append(getString(R.string.about_version)).append(": ").append(pInfo.versionName)
            val buildDate = Calendar.getInstance()
            buildDate.timeInMillis = BuildConfig.TIMESTAMP
            val builtDate = StringBuilder(DateFormat.getDateInstance(DateFormat.SHORT).format(buildDate.time))
            if(BuildConfig.DEBUG) {
                builtDate.append(" ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(buildDate.time))
            }
            about.append("\n\n").append(getString(R.string.about_built)).append(" ").append(builtDate)
            about.append("\n\n").append(String.format(getString(R.string.copyright), SimpleDateFormat("yyyy", Locale.getDefault()).format(buildDate.time)))
            val title = StringBuilder(getString(R.string.about_title)).append(" ").append(getString(R.string.app_name))
            Alerts.alert(title, about)
        } catch(e: Throwable) {
            // Do nothing.
        }
    }

    /**
     * Show or hide the settings controls.
     * @param v The view in question.
     */
    fun onClickSettings(v: View?) {
        bindingContent.layoutSettings.visibility = if(bindingContent.layoutSettings.isVisible) View.GONE else View.VISIBLE
    }

    /**
     * Toggle the enter WiFi update profile setting.
     * @param v The view in question.
     */
    fun onClickEnter(v: View?) {
        AudioProfileList.enterWifiProfile = if(bindingContent.checkboxEnterWifiDefault.isChecked) -1 else bindingContent.spinnerEnterWifi.selectedItemPosition
        updateControls()
    }

    /**
     * Toggle the exit WiFi update profile setting.
     * @param v The view in question.
     */
    fun onClickExit(v: View?) {
        AudioProfileList.exitWifiProfile = if(bindingContent.checkboxExitWifiDefault.isChecked) -1 else bindingContent.spinnerExitWifi.selectedItemPosition
        updateControls()
    }

    /**
     * Toggle whether the profile is locked.
     * @param v The view in question.
     */
    fun onClickLockProfile(v: View?) {
        mProfileLockChanged = true
        updateControls()
    }

    /**
     * Close the activity.
     * @param v The view in question.
     */
    fun onClickClose(v: View?) {
        AudioProfileList.enterWifiProfile = if(bindingContent.checkboxEnterWifiDefault.isChecked) -1 else bindingContent.spinnerEnterWifi.selectedItemPosition
        AudioProfileList.exitWifiProfile = if(bindingContent.checkboxExitWifiDefault.isChecked) -1 else bindingContent.spinnerExitWifi.selectedItemPosition
        AudioProfileList.lockProfileTime = bindingContent.spinnerLockProfile.getItemAtPosition(bindingContent.spinnerLockProfile.selectedItemPosition) as Int
        if(mProfileLockChanged) {
            AudioProfileList.profileLocked = bindingContent.checkboxLockProfile.isChecked
            AudioProfileList.profileLockStartTime = Calendar.getInstance().timeInMillis
            Log.d("AudioProfile", "New lock profile start time = ${AudioProfileList.profileLockStartTime}${if(AudioProfileList.profileLocked) " (locked)" else ""}")
        }
        finish()
    }

    /**
     * Select the radio button and the current profile.
     */
    private fun selectRadio(profile: Int) {
        AudioProfileList.currentProfile = profile
        when(profile) {
            0 -> {
                mRadioProfile[0]!!.isChecked = true
                mRadioProfile[1]!!.isChecked = false
                mRadioProfile[2]!!.isChecked = false
                mRadioProfile[3]!!.isChecked = false
            }
            1 -> {
                mRadioProfile[0]!!.isChecked = false
                mRadioProfile[1]!!.isChecked = true
                mRadioProfile[2]!!.isChecked = false
                mRadioProfile[3]!!.isChecked = false
            }
            2 -> {
                mRadioProfile[0]!!.isChecked = false
                mRadioProfile[1]!!.isChecked = false
                mRadioProfile[2]!!.isChecked = true
                mRadioProfile[3]!!.isChecked = false
            }
            3 -> {
                mRadioProfile[0]!!.isChecked = false
                mRadioProfile[1]!!.isChecked = false
                mRadioProfile[2]!!.isChecked = false
                mRadioProfile[3]!!.isChecked = true
            }
        }
        AudioProfileList.currentProfile = profile
        AudioProfileList.applyProfile(this)
        updateTile(this)
    }

    /**
     * Update the controls to reflect the current settings.
     */
    private fun updateControls() {
        // Select the right profile.
        selectRadio(AudioProfileList.currentProfile)

        // Now update the spinners for the entry and exit events.
        val adapterLockTime = bindingContent.spinnerLockProfile.adapter
        for(item in 0 until adapterLockTime.count) {
            if(adapterLockTime.getItem(item)!! == AudioProfileList.lockProfileTime) {
                bindingContent.spinnerLockProfile.setSelection(item)
                break
            }
        }
        bindingContent.spinnerEnterWifi.visibility = if(bindingContent.checkboxEnterWifiDefault.isChecked) View.INVISIBLE else View.VISIBLE
        bindingContent.spinnerExitWifi.visibility = if(bindingContent.checkboxExitWifiDefault.isChecked) View.INVISIBLE else View.VISIBLE
        bindingContent.spinnerLockProfile.visibility = if(bindingContent.checkboxLockProfile.isChecked) View.VISIBLE else View.INVISIBLE

        val adapter = ProfileAdapter(this, AudioProfileList.getProfiles().toTypedArray())
        bindingContent.spinnerEnterWifi.adapter = adapter
        bindingContent.checkboxEnterWifiDefault.buttonDrawable!!.setColorFilter(configColour, Mode.SRC_ATOP)
        bindingContent.spinnerExitWifi.adapter = adapter
        bindingContent.checkboxExitWifiDefault.buttonDrawable!!.setColorFilter(configColour, Mode.SRC_ATOP)
        var profile = AudioProfileList.enterWifiProfile
        if(profile == -1) {
            bindingContent.checkboxEnterWifiDefault.isChecked = true
        } else {
            bindingContent.spinnerEnterWifi.setSelection(profile)
        }
        profile = AudioProfileList.exitWifiProfile
        if(profile == -1) {
            bindingContent.checkboxExitWifiDefault.isChecked = true
        } else {
            bindingContent.spinnerExitWifi.setSelection(profile)
        }
        colourControls()
    }

    /**
     * Start the audio listener service.
     */
    private fun startService() {
        val serviceIntent = Intent(mThis, AudioProfileService::class.java)
        try {
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch(e: Exception) {
            Alerts.toast(e.toString())
        }
    }

    private val isServiceRunning: Boolean
        /**
         * Return if the service is running.
         */
        get() {
            try {
                val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
                if(am != null) {
                    for(service in am.getRunningServices(Int.MAX_VALUE)) {
                        if(AudioProfileService::class.java.name == service.service.className) {
                            return true
                        }
                    }
                }
            } catch(e: Exception) {
                // Do nothing.
            }
            return false
        }

    companion object {
        const val TAG = "AudioProfile"
        const val REQUEST_PERMISSIONS = 1
        const val REQUEST_AUDIO_PROFILE = 2
        private var mThis: MainActivity? = null
        var profiles: AudioProfileList? = null
            private set

        val instance: MainActivity?
            /**
             * Return an instance of the main activity.
             */
            get() {
                if(mThis == null) {
                    MainActivity()
                }
                return mThis
            }

        /**
         * Update the quick panel tile to reflect the current profile.
         */
        fun updateTile(context: Context?) {
            try {
                TileService.requestListeningState(context, ComponentName(context!!, QuickPanel::class.java))
            } catch(e: Exception) {
                // Do nothing.
            }
        }

        val configColour: Int
            /**
             * Get the current application colour.
             */
            get() {
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        mThis!!.getColor(R.color.colourConfig)
                    }
                    else -> {
                        val wm = WallpaperManager.getInstance(mThis)
                        wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)!!.primaryColor.toArgb()
                    }
                }
            }

        val whiteColour: Int
            /**
             * Get the white colour.
             */
            get() = mThis!!.getColor(R.color.colourWhiteText)
    }

    init {
        mThis = this
    }
}