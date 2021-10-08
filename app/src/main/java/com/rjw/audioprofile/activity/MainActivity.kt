package com.rjw.audioprofile.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.service.quicksettings.TileService
import android.view.View
import android.widget.*
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
    private lateinit var binding: ActivityMainBinding
    private val mRadioProfile = arrayOfNulls<RadioButton>(AudioProfileList.NO_PROFILES)
    private val mImageProfile = arrayOfNulls<ImageView>(AudioProfileList.NO_PROFILES)
    private val mTextProfile = arrayOfNulls<TextView>(AudioProfileList.NO_PROFILES)
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.bind(view!!)
        val bindingContent = ContentMainBinding.bind(binding.layoutMain)
        mThis = this

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
        val prefs = getSharedPreferences(TAG, MODE_PRIVATE)
        configColour = prefs.getInt(PREF_APPLICATION_COLOUR, getColor(R.color.colourConfig))

        // Set the profile names and icons.
        for(profile in 0 until AudioProfileList.NO_PROFILES) {
            val audioProfile = AudioProfileList.getProfile(profile)
            mRadioProfile[profile]!!.buttonDrawable!!.setColorFilter(configColour, Mode.SRC_ATOP)
            mTextProfile[profile]!!.text = audioProfile.name
            mImageProfile[profile]!!.setImageDrawable(AudioProfileList.getIcon(audioProfile.icon))
        }

        // Select the current profile.
        selectRadio(AudioProfileList.currentProfile)

        // Check we have the required permissions.
        val intent = Intent(this, PermissionRequest::class.java)
        startActivityForResult(intent, REQUEST_PERMISSIONS)
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), 0)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }
        }
        colourControls()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_PERMISSIONS -> updateTile(this)
            REQUEST_AUDIO_PROFILE -> if(resultCode == RESULT_OK && data != null) {
                AudioProfileList.saveProfiles(this)
                val modified = data.getIntExtra(ProfileConfiguration.AUDIO_PROFILE, 0)
                mTextProfile[modified]!!.text = AudioProfileList.getProfile(modified).name
                mImageProfile[modified]!!.setImageDrawable(AudioProfileList.getIcon(AudioProfileList.getProfile(modified).icon))
                if(AudioProfileList.currentProfile == modified) {
                    selectRadio(AudioProfileList.currentProfile)
                }
            }
            ACTIVITY_SETTINGS -> {
                if(data != null) {
                    val themeColour = data.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, configColour)
                    if(themeColour != configColour) {
                        configColour = themeColour
                        setAppColour(configColour)
                    }
                }
                val intent = Intent(this, javaClass)
                startActivity(intent)
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun onClickAudioProfile(v: View) {
        when(v.id) {
            R.id.radioProfile0, R.id.imageProfile0, R.id.textProfile0 -> selectRadio(0)
            R.id.radioProfile1, R.id.imageProfile1, R.id.textProfile1 -> selectRadio(1)
            R.id.radioProfile2, R.id.imageProfile2, R.id.textProfile2 -> selectRadio(2)
            R.id.radioProfile3, R.id.imageProfile3, R.id.textProfile3 -> selectRadio(3)
        }
    }

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

    fun onClickSettings(v: View?) {
        val intent = Intent(this, Settings::class.java)
        startActivityForResult(intent, ACTIVITY_SETTINGS)
    }

    fun onClickClose(v: View?) {
        finish()
    }

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
        AudioProfileList.applyProfile(this)
        updateTile(this)
    }

    private fun startService() {
        val serviceIntent = Intent(mThis, AudioProfileService::class.java)
        try {
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE)
            startService(serviceIntent)
        } catch(e: Exception) {
            Alerts.toast(e.toString())
        }
    }

    // Do nothing.
    private val isServiceRunning: Boolean
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
        const val ACTIVITY_SETTINGS = 3
        const val ACTIVITY_SELECT_THEME_COLOUR = 4
        private const val CHANNEL_ID = "AudioProfileChannelId"
        private val CHANNEL_NAME: CharSequence = "AudioProfile"
        private const val CHANNEL_DESCRIPTION = "AudioProfile"
        private const val SERVICE_NOTIFICATION_ID = 100
        const val PREF_APPLICATION_COLOUR = "ApplicationColour"
        private var mThis: MainActivity? = null
        var profiles: AudioProfileList? = null
            private set
        var configColour = 0
            private set
        private var mNm: NotificationManager? = null
        private var mNotificationBuilder: Notification.Builder? = null

        val instance: MainActivity?
            get() {
                if(mThis == null) {
                    MainActivity()
                }
                return mThis
            }

        fun updateTile(context: Context?) {
            try {
                TileService.requestListeningState(context, ComponentName(context!!, QuickPanel::class.java))
            } catch(e: Exception) {
            }
        }

        val whiteColour: Int
            get() = mThis!!.getColor(R.color.colourWhiteText)

        fun createNotificationChannel() {
            try {
                mNm = mThis!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
                if(mNm != null) {
                    val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
                    channel.description = CHANNEL_DESCRIPTION
                    channel.setShowBadge(false)
                    channel.enableLights(false)
                    channel.setSound(null, null)
                    mNm!!.createNotificationChannel(channel)
                }
            } catch(e: Exception) {
                Alerts.toast("Creating notification channel: ${e.javaClass.name}\n${e.message}")
            }
        }

        fun showServiceNotification(service: Service?, msg: String?, pendingIntent: PendingIntent?) {
            try {
                if(service != null) {
                    mNotificationBuilder = Notification.Builder(service)
                        .setChannelId(CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification)
                        .setContentTitle(msg)
                        .setContentIntent(pendingIntent)
                    service.startForeground(SERVICE_NOTIFICATION_ID, mNotificationBuilder!!.build())
                    updateNotification()
                }
            } catch(e: Exception) {
                Alerts.toast("Creating notification: ${e.javaClass.name}\n${e.message}")
            }
        }

        fun updateNotification() {
            try {
                if(mNm == null) {
                    mNm = mThis!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
                }
                if(mNm != null && mThis != null && mNotificationBuilder != null) {
                    mNotificationBuilder!!.setContentText(
                        String.format(
                            mThis!!.getString(R.string.notification_profile),
                            AudioProfileList.getProfile(AudioProfileList.currentProfile).name
                        )
                    )
                    mNm!!.notify(SERVICE_NOTIFICATION_ID, mNotificationBuilder!!.build())
                }
            } catch(e: Exception) {
                Alerts.toast("Updating notification: ${e.javaClass.name}\n${e.message}")
            }
        }

        fun setAppColour(colour: Int) {
            configColour = colour
            val prefs = mThis!!.getSharedPreferences(TAG, MODE_PRIVATE)
            prefs.edit().putInt(PREF_APPLICATION_COLOUR, configColour).apply()
        }
    }
}