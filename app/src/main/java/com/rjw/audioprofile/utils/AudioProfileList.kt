package com.rjw.audioprofile.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.utils.AudioProfileList.Companion.currentProfile
import com.rjw.audioprofile.utils.AudioProfileList.Companion.lockProfileTime
import com.rjw.audioprofile.utils.AudioProfileList.Companion.profileLocked

class AudioProfileList() {
    /**
     * Class constructor.
     * @param name               The profile name.
     * @param icon               The icon to be used for the profile.
     * @param ringtoneVolume     The ringtone volume.
     * @param notificationVolume The notification volume.
     * @param mediaVolume        The media volume.
     * @param systemVolume       The system volume.
     * @param vibrate            True if the device will vibrate when in mute or false if not.
     */
    class AudioProfile(
        name: String?,
        icon: Int,
        ringtoneVolume: Int,
        notificationVolume: Int,
        mediaVolume: Int,
        systemVolume: Int,
        vibrate: Boolean
    ) {
        var name: String? = null
        var icon = 0
        var ringtoneVolume = 0
        var notificationVolume = 0
        var mediaVolume = 0
        var systemVolume = 0
        var vibrate = true

        /**
         * Set the audio profile information.
         * @param name               The profile name.
         * @param icon               The icon to be used for the profile.
         * @param ringtoneVolume     The ringtone volume.
         * @param notificationVolume The notification volume.
         * @param mediaVolume        The media volume.
         * @param systemVolume       The system volume.
         * @param vibrate            True if the device will vibrate when in mute or false if not.
         */
        operator fun set(
            name: String?,
            icon: Int,
            ringtoneVolume: Int,
            notificationVolume: Int,
            mediaVolume: Int,
            systemVolume: Int,
            vibrate: Boolean
        ) {
            this.name = name
            this.icon = icon
            this.ringtoneVolume = ringtoneVolume
            this.notificationVolume = notificationVolume
            this.mediaVolume = mediaVolume
            this.systemVolume = systemVolume
            this.vibrate = vibrate
        }

        /**
         * Class initialisation.
         */
        init {
            set(name, icon, ringtoneVolume, notificationVolume, mediaVolume, systemVolume, vibrate)
        }
    }

    companion object {
        const val PROFILE_DELAY = 500L
        private const val DEFAULT = -1
        var noProfiles = 0
        private var _currentProfile = 0
        private var _enterWifiProfile = -1
        private var _exitWifiProfile = -1
        private var _profileLocked = false
        private var _lockProfileTime = -1
        private var context: Context? = null
        private var prefs: SharedPreferences? = null
        private val profiles = ArrayList<AudioProfile>()
        private var icons = IntArray(0)
        private var _profileLockStartTime = -1L

        /**
         * Initialise the class.
         * @param context The application context.
         */
        fun initialise(context: Context) {
            Companion.context = context.applicationContext
            if(icons.isEmpty()) {
                icons = intArrayOf(
                    R.drawable.icon00,
                    R.drawable.icon01,
                    R.drawable.icon02,
                    R.drawable.icon03,
                    R.drawable.icon04,
                    R.drawable.icon05,
                    R.drawable.icon06,
                    R.drawable.icon07,
                    R.drawable.icon08,
                    R.drawable.icon09,
                    R.drawable.icon10,
                    R.drawable.icon11,
                    R.drawable.icon12,
                    R.drawable.icon13,
                    R.drawable.icon14
                )
            }
            if(profiles.isEmpty()) {
                loadProfiles()
            }
        }

        val length: Int
            /**
             * Get the number of icons.
             */
            get() {
                return icons.size
            }

        /**
         * Get the icon for the profile.
         * @param iconId The profile icon index.
         * @return The drawable for the icon.
         */
        fun getIcon(iconId: Int): Drawable? {
            return context?.let { context ->
                val icon = ContextCompat.getDrawable(context, icons[iconId])
                icon?.setColorFilter(context.getColor(R.color.colourConfig), Mode.SRC_ATOP)
                return icon
            }
        }

        /**
         * Get the icon resource for the profile.
         * @param iconId The profile icon index.
         * @return The icon resource index.
         */
        fun getIconResource(iconId: Int): Int {
            return icons[iconId]
        }

        /**
         * Load the profiles from the saved preferences.
         */
        private fun loadProfiles() {
            try {
                if(prefs == null) {
                    prefs = context?.getSharedPreferences(TAG, Activity.MODE_PRIVATE)
                }
                profiles.clear()
                prefs?.let { prefs ->
                    noProfiles = context?.resources?.getStringArray(R.array.profile_name)?.size ?: 0
                    _currentProfile = prefs.getInt(CURRENT_PROFILE, _currentProfile)
                    _enterWifiProfile = prefs.getInt(ENTER_PROFILE, _enterWifiProfile)
                    _exitWifiProfile = prefs.getInt(EXIT_PROFILE, _exitWifiProfile)
                    _lockProfileTime = prefs.getInt(LOCK_PROFILE, _lockProfileTime)
                    _profileLockStartTime = prefs.getLong(LOCK_PROFILE_START_TIME, _profileLockStartTime)
                    for(profile in 0 until noProfiles) {
                        val audioProfile = AudioProfile(
                            prefs.getString(
                                NAME + profile,
                                context?.resources?.getStringArray(R.array.profile_name)[profile]
                            ),
                            prefs.getInt(ICON + profile, context?.resources?.getIntArray(R.array.profile_icon)[profile] ?: profile),
                            prefs.getInt(RINGTONE + profile, DEFAULT), prefs.getInt(NOTIFICATION + profile, DEFAULT),
                            prefs.getInt(MEDIA + profile, DEFAULT), prefs.getInt(SYSTEM + profile, DEFAULT),
                            prefs.getBoolean(VIBRATE + profile, true)
                        )
                        profiles.add(audioProfile)
                    }
                }
            } catch(e: Exception) {
                Log.log(e.message)
            }
        }

        /**
         * Save the profiles to the application saved preferences.
         * @param context The application context.
         */
        fun saveProfiles(context: Context) {
            if(prefs == null) {
                prefs = context.getSharedPreferences(TAG, Activity.MODE_PRIVATE)
            }
            for(profile in 0 until noProfiles) {
                val audioProfile = profiles[profile]
                prefs?.edit {
                    putString(NAME + profile, audioProfile.name)
                    putInt(ICON + profile, audioProfile.icon)
                    putInt(RINGTONE + profile, audioProfile.ringtoneVolume)
                    putInt(NOTIFICATION + profile, audioProfile.notificationVolume)
                    putInt(MEDIA + profile, audioProfile.mediaVolume)
                    putInt(SYSTEM + profile, audioProfile.systemVolume)
                    putBoolean(VIBRATE + profile, audioProfile.vibrate)
                    apply()
                }
            }
        }

        var currentProfile: Int
            /**
             * Get the current profile.
             */
            get() {
                return if(_currentProfile < noProfiles) _currentProfile else 0
            }
            /**
             * Set the current profile.
             * @param currentProfile The new profile.
             */
            set(currentProfile) {
                _currentProfile = currentProfile
                prefs?.edit {
                    putInt(CURRENT_PROFILE, _currentProfile)
                    apply()
                }
                MainActivity.updateTile()
            }

        /**
         * Previous profile when the profile was locked.
         */
        var previousProfile: Int = 0

        var enterWifiProfile: Int
            /**
             * Get the profile when entering WiFi.
             */
            get() {
                return if(_enterWifiProfile < noProfiles) _enterWifiProfile else 0
            }
            /**
             * Set the profile to switch to when entering WiFi.
             * @param enterProfile The profile to select when entering WiFi.
             */
            set(enterProfile) {
                _enterWifiProfile = enterProfile
                prefs?.edit {
                    putInt(ENTER_PROFILE, _enterWifiProfile)
                    apply()
                }
            }

        var exitWifiProfile: Int
            /**
             * Get the profile when exiting WiFi.
             */
            get() {
                return if(_exitWifiProfile < noProfiles) _exitWifiProfile else 0
            }
            /**
             * Set the profile to switch to when exiting WiFi.
             * @param exitProfile The profile to select when exiting WiFi.
             */
            set(exitProfile) {
                _exitWifiProfile = exitProfile
                prefs?.edit {
                    putInt(EXIT_PROFILE, _exitWifiProfile)
                    apply()
                }
            }

        var profileLocked: Boolean
            /**
             * Get whether the profile is currently locked.
             */
            get() {
                return _profileLocked
            }
            /**
             * Set whether the profile is locked and will not change on changing network.
             * @param profileLocked True if locked, otherwise false.
             */
            set(profileLocked) {
                _profileLocked = profileLocked
                previousProfile = if(profileLocked) currentProfile else 0
                Log.log("Profile ${getProfile(previousProfile).name} ${if(profileLocked) "locked" else "unlocked"}")
            }

        var lockProfileTime: Int
            /**
             * Get the length of time to lock the profile for.
             */
            get() {
                return _lockProfileTime
            }
            /**
             * Set the time to lock the profile for in minutes.
             * @param lockProfileTime The time to lock the profile for.
             */
            set(lockProfileTime) {
                _lockProfileTime = lockProfileTime
                prefs?.edit {
                    putInt(LOCK_PROFILE, _lockProfileTime)
                    apply()
                }
            }

        var profileLockStartTime: Long
            /**
             * Get the start time of when the profile was locked.
             */
            get() {
                return _profileLockStartTime
            }
            /**
             * Set the start time for locking the profile.
             */
            set(switchTime) {
                _profileLockStartTime = switchTime
                prefs?.edit {
                    putLong(LOCK_PROFILE_START_TIME, _profileLockStartTime)
                    apply()
                }
            }

        /**
         * Get the information for the audio profile.
         * @param profile The profile in question.
         * @return The audio profile information.
         */
        fun getProfile(profile: Int): AudioProfile {
            return profiles[profile]
        }

        /**
         * Get the list of audio profiles.
         * @return The list of profiles.
         */
        fun getProfiles(): List<AudioProfile> {
            return profiles
        }

        /**
         * Set the information for the audio profile.
         * @param profile            The profile to update.
         * @param name               The profile name.
         * @param icon               The icon to be used for the profile.
         * @param ringtoneVolume     The ringtone volume.
         * @param notificationVolume The notification volume.
         * @param mediaVolume        The media volume.
         * @param systemVolume       The system volume.
         * @param vibrate            True if the device will vibrate when in mute or false if not.
         */
        fun setProfile(
            profile: Int,
            name: String?,
            icon: Int,
            ringtoneVolume: Int,
            notificationVolume: Int,
            mediaVolume: Int,
            systemVolume: Int,
            vibrate: Boolean
        ) {
            profiles[profile] = AudioProfile(name, icon, ringtoneVolume, notificationVolume, mediaVolume, systemVolume, vibrate)
        }

        /**
         * Apply the currently selected profile and change the audio volumes.
         * @param context The application context.
         */
        fun applyProfile(context: Context) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            if(am != null) {
                val audioProfile = getProfile(currentProfile)
                if(audioProfile.ringtoneVolume != -1) {
                    if(am.getStreamVolume(AudioManager.STREAM_RING) != audioProfile.ringtoneVolume) {
                        am.setStreamVolume(AudioManager.STREAM_RING, audioProfile.ringtoneVolume, 0)
                    }
                    try {
                        val mode = am::class.java.getMethod("getRingerModeInternal").invoke(am) as Int
                        val newMode = if(audioProfile.ringtoneVolume == 0) {
                            if(audioProfile.vibrate) {
                                AudioManager.RINGER_MODE_VIBRATE
                            } else {
                                AudioManager.RINGER_MODE_SILENT
                            }
                        } else {
                            AudioManager.RINGER_MODE_NORMAL
                        }
                        if(mode != newMode) {
                            am::class.java.getMethod("setRingerModeInternal", Int::class.java).invoke(am, newMode)
                        }
                    } catch(_: Exception) {
                        try {
                            val mode = am.ringerMode
                            val newMode = if(audioProfile.ringtoneVolume == 0) {
                                if(audioProfile.vibrate) {
                                    AudioManager.RINGER_MODE_VIBRATE
                                } else {
                                    AudioManager.RINGER_MODE_SILENT
                                }
                            } else {
                                AudioManager.RINGER_MODE_NORMAL
                            }
                            if(mode != newMode) {
                                am.ringerMode = newMode
                            }
                        } catch(_: Exception) {
                            // Do nothing - this is because the app doesn't have the necessary permissions.
                        }
                    }
                }
                if(audioProfile.notificationVolume != -1) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if(am.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != audioProfile.notificationVolume) {
                            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioProfile.notificationVolume, 0)
                        }
                    }, PROFILE_DELAY)
                }
                if(audioProfile.mediaVolume != -1) {
                    if(am.getStreamVolume(AudioManager.STREAM_MUSIC) != audioProfile.mediaVolume) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, audioProfile.mediaVolume, 0)
                    }
                }
                if(audioProfile.systemVolume != -1) {
                    if(am.getStreamVolume(AudioManager.STREAM_SYSTEM) != audioProfile.systemVolume) {
                        am.setStreamVolume(AudioManager.STREAM_SYSTEM, audioProfile.systemVolume, 0)
                    }
                }
            }
        }
    }

    /**
     * Initialise the class.
     */
    init {
    }
}