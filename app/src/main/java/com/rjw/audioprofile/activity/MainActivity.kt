package com.rjw.audioprofile.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivityMainBinding
import com.rjw.audioprofile.databinding.ContentMainBinding
import com.rjw.audioprofile.service.AudioProfileService
import com.rjw.audioprofile.service.Notifications
import com.rjw.audioprofile.service.QuickPanel
import com.rjw.audioprofile.utils.Alerts
import com.rjw.audioprofile.utils.AudioProfileList
import com.rjw.audioprofile.utils.CLEAR_LOG
import com.rjw.audioprofile.utils.MinutesAdapter
import com.rjw.audioprofile.utils.Mode
import com.rjw.audioprofile.utils.ProfileAdapter
import com.rjw.audioprofile.utils.TAG
import com.rjw.audioprofile.utils.setColorFilter
import java.util.Calendar

@Suppress("DEPRECATION")
class MainActivity : AudioActivity() {
    private val REQUEST_PERMISSION_RESPONSE = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingContent: ContentMainBinding
    private val lockTimings = arrayOf(1, 2, 5, 10, 20, 30, 60, 90, 120)
    private val radioProfile by lazy { arrayOfNulls<RadioButton>(AudioProfileList.noProfiles) }
    private val imageProfile by lazy { arrayOfNulls<ImageView>(AudioProfileList.noProfiles) }
    private val textProfile by lazy { arrayOfNulls<TextView>(AudioProfileList.noProfiles) }
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    private var profileLockChanged = false
    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        updateTile()
    }
    private val requestAudioProfile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == RESULT_OK) {
            result.data?.let { data ->
                AudioProfileList.saveProfiles(this)
                val modified = data.getIntExtra(ProfileConfiguration.AUDIO_PROFILE, 0)
                textProfile[modified]?.text = AudioProfileList.getProfile(modified).name
                imageProfile[modified]?.setImageDrawable(AudioProfileList.getIcon(AudioProfileList.getProfile(modified).icon))
                updateControls()
            }
        }
    }
    private var askedPermissions = false

    /**
     * Create the activity.
     * @param savedInstanceState The state information for the activity.
     */
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        windowRatio = floatArrayOf(0.9f, 0.7f)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(view)
        bindingContent = ContentMainBinding.bind(binding.layoutMain)

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler { _, e -> e.printStackTrace() }

        // Load the settings.
        AudioProfileList.initialise(this)
        radioProfile[0] = bindingContent.radioProfile0
        radioProfile[1] = bindingContent.radioProfile1
        radioProfile[2] = bindingContent.radioProfile2
        radioProfile[3] = bindingContent.radioProfile3
        imageProfile[0] = bindingContent.imageProfile0
        imageProfile[1] = bindingContent.imageProfile1
        imageProfile[2] = bindingContent.imageProfile2
        imageProfile[3] = bindingContent.imageProfile3
        textProfile[0] = bindingContent.textProfile0
        textProfile[1] = bindingContent.textProfile1
        textProfile[2] = bindingContent.textProfile2
        textProfile[3] = bindingContent.textProfile3

        if(!isServiceRunning) {
            startService()
        }

        // Check we have the required additional permissions.
        val doNotDisturb = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted
        val batteryOptimizations = (getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)
        val notifications =
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        if(!doNotDisturb || !notifications || !batteryOptimizations) {
            val intent = Intent(this, PermissionRequest::class.java)
            requestPermissions.launch(intent)
        }

        // Set the profile names and icons.
        for(profile in 0 until AudioProfileList.noProfiles) {
            val audioProfile = AudioProfileList.getProfile(profile)
            radioProfile[profile]?.buttonDrawable?.setColorFilter(configColour, Mode.SRC_ATOP)
            textProfile[profile]?.text = audioProfile.name
            imageProfile[profile]?.setImageDrawable(AudioProfileList.getIcon(audioProfile.icon))
        }

        // Set up the lock adapters.
        val profileLockTime = AudioProfileList.lockProfileTime
        val lockAdapter = MinutesAdapter(this, lockTimings)
        bindingContent.spinnerLockProfile.adapter = lockAdapter
        bindingContent.spinnerLockProfile.background.setColorFilter(configColour, Mode.SRC_ATOP)
        bindingContent.checkboxLockProfile.buttonDrawable?.setColorFilter(configColour, Mode.SRC_ATOP)

        // Set up the profile lock time.
        var profileLocked = AudioProfileList.profileLocked
        if(profileLocked and (Calendar.getInstance().timeInMillis > AudioProfileList.profileLockStartTime + profileLockTime * 60000)) {
            profileLocked = false
        }
        bindingContent.checkboxLockProfile.isChecked = profileLocked

        // Set the handler for the lock time spinner.
        AdapterView.OnItemClickListener { _, _, _, _ -> profileLockChanged = true }

        // Set up the control operations.
        bindingContent.about.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        bindingContent.radioProfile0.setOnClickListener { setAudioProfile(0) }
        bindingContent.imageProfile0.setOnClickListener { setAudioProfile(0) }
        bindingContent.textProfile0.setOnClickListener { setAudioProfile(0) }
        bindingContent.rowProfile0.setOnClickListener { setAudioProfile(0) }
        bindingContent.buttonConfigureProfile0.setOnClickListener { configureAudioProfile(0) }
        bindingContent.radioProfile1.setOnClickListener { setAudioProfile(1) }
        bindingContent.imageProfile1.setOnClickListener { setAudioProfile(1) }
        bindingContent.textProfile1.setOnClickListener { setAudioProfile(1) }
        bindingContent.rowProfile1.setOnClickListener { setAudioProfile(1) }
        bindingContent.buttonConfigureProfile1.setOnClickListener { configureAudioProfile(1) }
        bindingContent.radioProfile2.setOnClickListener { setAudioProfile(2) }
        bindingContent.imageProfile2.setOnClickListener { setAudioProfile(2) }
        bindingContent.textProfile2.setOnClickListener { setAudioProfile(2) }
        bindingContent.rowProfile2.setOnClickListener { setAudioProfile(2) }
        bindingContent.buttonConfigureProfile2.setOnClickListener { configureAudioProfile(2) }
        bindingContent.radioProfile3.setOnClickListener { setAudioProfile(3) }
        bindingContent.imageProfile3.setOnClickListener { setAudioProfile(3) }
        bindingContent.textProfile3.setOnClickListener { setAudioProfile(3) }
        bindingContent.rowProfile3.setOnClickListener { setAudioProfile(3) }
        bindingContent.buttonConfigureProfile3.setOnClickListener { configureAudioProfile(3) }
        bindingContent.checkboxLockProfile.setOnClickListener {
            AudioProfileList.profileLocked = bindingContent.checkboxLockProfile.isChecked
            profileLockChanged = true
            updateTile()
        }
        bindingContent.textSettings.setOnClickListener {
            bindingContent.layoutSettings.visibility = if(bindingContent.layoutSettings.isVisible) View.GONE else View.VISIBLE
        }
        bindingContent.imageSettings.setOnClickListener {
            bindingContent.layoutSettings.visibility = if(bindingContent.layoutSettings.isVisible) View.GONE else View.VISIBLE
        }
        bindingContent.checkboxEnterWifiDefault.setOnClickListener {
            AudioProfileList.enterWifiProfile =
                if(bindingContent.checkboxEnterWifiDefault.isChecked) -1 else bindingContent.spinnerEnterWifi.selectedItemPosition
            updateControls()
        }
        bindingContent.spinnerEnterWifi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                AudioProfileList.enterWifiProfile = bindingContent.spinnerEnterWifi.selectedItemPosition
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        bindingContent.checkboxExitWifiDefault.setOnClickListener {
            AudioProfileList.exitWifiProfile =
                if(bindingContent.checkboxExitWifiDefault.isChecked) -1 else bindingContent.spinnerExitWifi.selectedItemPosition
            updateControls()
        }
        bindingContent.spinnerExitWifi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                AudioProfileList.exitWifiProfile = bindingContent.spinnerExitWifi.selectedItemPosition
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        bindingContent.buttonClose.setOnClickListener {
            AudioProfileList.enterWifiProfile =
                if(bindingContent.checkboxEnterWifiDefault.isChecked) -1 else bindingContent.spinnerEnterWifi.selectedItemPosition
            AudioProfileList.exitWifiProfile =
                if(bindingContent.checkboxExitWifiDefault.isChecked) -1 else bindingContent.spinnerExitWifi.selectedItemPosition
            AudioProfileList.lockProfileTime =
                bindingContent.spinnerLockProfile.getItemAtPosition(bindingContent.spinnerLockProfile.selectedItemPosition) as Int
            if(profileLockChanged) {
                AudioProfileList.profileLocked = bindingContent.checkboxLockProfile.isChecked
                AudioProfileList.profileLockStartTime = Calendar.getInstance().timeInMillis
            }
            finish()
        }

        colourControls()
        if(savedInstanceState == null) {
            setAudioProfile(AudioProfileList.currentProfile)
        }
        if(intent.hasExtra(CLEAR_LOG)) {
            Alerts.clearLog()
        }
    }

    /**
     * Resume the activity.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        // Request standard application permissions.
        if(!askedPermissions) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_RESPONSE)
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        REQUEST_PERMISSION_RESPONSE
                    )
                }
            }
            askedPermissions = true
        }
    }

    /**
     * Deal with permission requests.
     * @param requestCode  The code for the permission request.
     * @param permissions  The permissions being requested.
     * @param grantResults The results for each permission.
     */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Select the radio button and the current profile.
     * @param profile The profile to be set.
     */
    private fun setAudioProfile(profile: Int) {
        AudioProfileList.currentProfile = profile
        when(profile) {
            0 -> {
                radioProfile[0]?.isChecked = true
                radioProfile[1]?.isChecked = false
                radioProfile[2]?.isChecked = false
                radioProfile[3]?.isChecked = false
            }
            1 -> {
                radioProfile[0]?.isChecked = false
                radioProfile[1]?.isChecked = true
                radioProfile[2]?.isChecked = false
                radioProfile[3]?.isChecked = false
            }
            2 -> {
                radioProfile[0]?.isChecked = false
                radioProfile[1]?.isChecked = false
                radioProfile[2]?.isChecked = true
                radioProfile[3]?.isChecked = false
            }
            3 -> {
                radioProfile[0]?.isChecked = false
                radioProfile[1]?.isChecked = false
                radioProfile[2]?.isChecked = false
                radioProfile[3]?.isChecked = true
            }
        }
        AudioProfileList.currentProfile = profile
        AudioProfileList.profileLocked = false
    }

    /**
     * Update the specified audio profile.
     * @param profile The profile to be configured.
     */
    private fun configureAudioProfile(profile: Int) {
        val intent = Intent(this, ProfileConfiguration::class.java)
        intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, profile)
        requestAudioProfile.launch(intent)
    }

    /**
     * Update the controls to reflect the current settings.
     */
    private fun updateControls() {
        // Now update the spinners for the entry and exit events.
        val adapterLockTime = bindingContent.spinnerLockProfile.adapter
        for(item in 0 until adapterLockTime.count) {
            if(adapterLockTime.getItem(item) == AudioProfileList.lockProfileTime) {
                bindingContent.spinnerLockProfile.setSelection(item)
                break
            }
        }
        bindingContent.checkboxLockProfile.isChecked = AudioProfileList.profileLocked
        bindingContent.spinnerEnterWifi.visibility = if(bindingContent.checkboxEnterWifiDefault.isChecked) View.INVISIBLE else View.VISIBLE
        bindingContent.spinnerExitWifi.visibility = if(bindingContent.checkboxExitWifiDefault.isChecked) View.INVISIBLE else View.VISIBLE
        bindingContent.spinnerLockProfile.visibility = if(bindingContent.checkboxLockProfile.isChecked) View.VISIBLE else View.INVISIBLE

        val adapter = ProfileAdapter(this, AudioProfileList.getProfiles().toTypedArray())
        bindingContent.spinnerEnterWifi.adapter = adapter
        bindingContent.checkboxEnterWifiDefault.buttonDrawable?.setColorFilter(configColour, Mode.SRC_ATOP)
        bindingContent.spinnerExitWifi.adapter = adapter
        bindingContent.checkboxExitWifiDefault.buttonDrawable?.setColorFilter(configColour, Mode.SRC_ATOP)
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
            bindService(serviceIntent, connection, BIND_AUTO_CREATE)
            startService(serviceIntent)
        } catch(_: Exception) {
            // Do nothing...
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
            } catch(_: Exception) {
                // Do nothing.
            }
            return false
        }

    @SuppressLint("StaticFieldLeak")
    companion object {
        private var mThis: MainActivity? = null

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
        fun updateTile() {
            try {
                mThis?.let { instance ->
                    instance.updateControls()
                    TileService.requestListeningState(instance, ComponentName(instance.applicationContext, QuickPanel::class.java))
                    Notifications.updateNotification(instance)
                }
            } catch(_: Exception) {
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
                        mThis?.getColor(R.color.colourConfig) ?: 0
                    }
                    else -> {
                        val wm = WallpaperManager.getInstance(mThis)
                        wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb() ?: 0
                    }
                }
            }

        val secondaryColour: Int
            /**
             * Get the current application secondary colour.
             */
            get() {
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        mThis?.getColor(R.color.colourAccent) ?: 0
                    }
                    else -> {
                        val wm = WallpaperManager.getInstance(mThis)
                        wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.secondaryColor?.toArgb() ?: 0
                    }
                }
            }

        val whiteColour: Int
            /**
             * Get the white colour.
             */
            get() = mThis?.getColor(R.color.colourWhiteText) ?: 0xFFFFFF
    }

    init {
        mThis = this
    }
}