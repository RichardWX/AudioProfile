package com.rjw.audioprofile.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.service.Notifications
import java.util.*

class AudioProfileList(context: Context?) {
    /**
     * Class constructor.
     * @param name               The profile name.
     * @param icon               The icon to be used for the profile.
     * @param ringtoneVolume     The ringtone volume.
     * @param notificationVolume The notification volume.
     * @param mediaVolume        The media volume.
     * @param systemVolume       The system volume.
     */
    class AudioProfile(name: String?, icon: Int, ringtoneVolume: Int, notificationVolume: Int, mediaVolume: Int, systemVolume: Int) {
        var name: String? = null
        var icon = 0
        var ringtoneVolume = 0
        var notificationVolume = 0
        var mediaVolume = 0
        var systemVolume = 0

        /**
         * Set the audio profile information.
         * @param name               The profile name.
         * @param icon               The icon to be used for the profile.
         * @param ringtoneVolume     The ringtone volume.
         * @param notificationVolume The notification volume.
         * @param mediaVolume        The media volume.
         * @param systemVolume       The system volume.
         */
        operator fun set(name: String?, icon: Int, ringtoneVolume: Int, notificationVolume: Int, mediaVolume: Int, systemVolume: Int) {
            this.name = name
            this.icon = icon
            this.ringtoneVolume = ringtoneVolume
            this.notificationVolume = notificationVolume
            this.mediaVolume = mediaVolume
            this.systemVolume = systemVolume
        }

        /**
         * Class initialisation.
         */
        init {
            set(name, icon, ringtoneVolume, notificationVolume, mediaVolume, systemVolume)
        }
    }

    companion object {
        const val NO_PROFILES = 4
        private const val DEFAULT = -1
        private var mCurrentProfile = 0
        private var mEnterWifiProfile = -1
        private var mExitWifiProfile = -1
        private var mProfileLocked = false
        private var mLockProfileTime = -1
        private var mContext: Context? = null
        private var mPrefs: SharedPreferences? = null
        private val mProfiles = ArrayList<AudioProfile>()
        private var mIcons: IntArray? = null
        private var mProfileLockStartTime = -1L

        /**
         * Initialise the class.
         * @param context The application context.
         */
        fun initialise(context: Context?) {
            mContext = context?.applicationContext
            Log.d("AudioProfile", "Initialising audio profiles")
            if(mIcons == null || mIcons!!.isEmpty()) {
                mIcons = intArrayOf(
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
                    R.drawable.icon13
                )
            }
            if(mProfiles.isEmpty()) {
                loadProfiles()
            }
        }

        val length: Int
            /**
             * Get the number of icons.
             */
            get() {
                return mIcons!!.size
            }

        /**
         * Get the icon for the profile.
         * @param iconId The profile icon index.
         * @return The drawable for the icon.
         */
        fun getIcon(iconId: Int): Drawable {
            val icon = ContextCompat.getDrawable(mContext!!, mIcons!![iconId])
            icon!!.setColorFilter(mContext!!.getColor(R.color.colourConfig), Mode.SRC_ATOP)
            return icon
        }

        /**
         * Get the icon resource for the profile.
         * @param iconId The profile icon index.
         * @return The icon resource index.
         */
        fun getIconResource(iconId: Int): Int {
            return mIcons!![iconId]
        }

        /**
         * Load the profiles from the saved preferences.
         */
        private fun loadProfiles() {
            if(mPrefs == null) {
                mPrefs = mContext!!.getSharedPreferences(MainActivity.TAG, Activity.MODE_PRIVATE)
            }
            mProfiles.clear()
            mCurrentProfile = mPrefs!!.getInt(CURRENT_PROFILE, mCurrentProfile)
            mEnterWifiProfile = mPrefs!!.getInt(ENTER_PROFILE, mEnterWifiProfile)
            mExitWifiProfile = mPrefs!!.getInt(EXIT_PROFILE, mExitWifiProfile)
            mLockProfileTime = mPrefs!!.getInt(LOCK_PROFILE, mLockProfileTime)
            mProfileLockStartTime = mPrefs!!.getLong(LOCK_PROFILE_START_TIME, mProfileLockStartTime)
            for(profile in 0 until NO_PROFILES) {
                val audioProfile = AudioProfile(
                    mPrefs!!.getString(
                        NAME + profile,
                        mContext!!.resources.getStringArray(R.array.profile)[profile]
                    ),
                    mPrefs!!.getInt(ICON + profile, profile),
                    mPrefs!!.getInt(RINGTONE + profile, DEFAULT), mPrefs!!.getInt(NOTIFICATION + profile, DEFAULT),
                    mPrefs!!.getInt(MEDIA + profile, DEFAULT), mPrefs!!.getInt(SYSTEM + profile, DEFAULT)
                )
                mProfiles.add(audioProfile)
            }
        }

        /**
         * Save the profiles to the application saved preferences.
         * @param context The application context.
         */
        fun saveProfiles(context: Context) {
            if(mPrefs == null) {
                mPrefs = context.getSharedPreferences(MainActivity.TAG, Activity.MODE_PRIVATE)
            }
            for(profile in 0 until NO_PROFILES) {
                val audioProfile = mProfiles[profile]
                mPrefs!!.edit().putString(NAME + profile, audioProfile.name).apply()
                mPrefs!!.edit().putInt(ICON + profile, audioProfile.icon).apply()
                mPrefs!!.edit().putInt(RINGTONE + profile, audioProfile.ringtoneVolume).apply()
                mPrefs!!.edit().putInt(NOTIFICATION + profile, audioProfile.notificationVolume).apply()
                mPrefs!!.edit().putInt(MEDIA + profile, audioProfile.mediaVolume).apply()
                mPrefs!!.edit().putInt(SYSTEM + profile, audioProfile.systemVolume).apply()
            }
        }

        var currentProfile: Int
            /**
             * Get the current profile.
             */
            get() {
                return if(mCurrentProfile < NO_PROFILES) mCurrentProfile else 0
            }
            /**
             * Set the current profile.
             * @param currentProfile The new profile.
             */
            set(currentProfile) {
                mCurrentProfile = currentProfile
                mPrefs!!.edit().putInt(CURRENT_PROFILE, mCurrentProfile).apply()
                Notifications.updateNotification(mContext!!)
            }

        var enterWifiProfile: Int
            /**
             * Get the profile when entering WiFi.
             */
            get() {
                return if(mEnterWifiProfile < NO_PROFILES) mEnterWifiProfile else 0
            }
            /**
             * Set the profile to switch to when entering WiFi.
             * @param enterProfile The profile to select when entering WiFi.
             */
            set(enterProfile) {
                mEnterWifiProfile = enterProfile
                mPrefs!!.edit().putInt(ENTER_PROFILE, mEnterWifiProfile).apply()
            }

        var exitWifiProfile: Int
            /**
             * Get the profile when exiting WiFi.
             */
            get() {
                return if(mExitWifiProfile < NO_PROFILES) mExitWifiProfile else 0
            }
            /**
             * Set the profile to switch to when exiting WiFi.
             * @param exitProfile The profile to select when exiting WiFi.
             */
            set(exitProfile) {
                mExitWifiProfile = exitProfile
                mPrefs!!.edit().putInt(EXIT_PROFILE, mExitWifiProfile).apply()
            }

        var profileLocked: Boolean
            /**
             * Get whether the profile is currently locked.
             */
            get() {
                return mProfileLocked
            }
            /**
             * Set whether the profile is locked and will not change on changing network.
             * @param profileLocked True if locked, otherwise false.
             */
            set(profileLocked) {
                mProfileLocked = profileLocked
            }

        var lockProfileTime: Int
            /**
             * Get the length of time to lock the profile for.
             */
            get() {
                return mLockProfileTime
            }
            /**
             * Set the time to lock the profile for in minutes.
             * @param lockProfileTime The time to lock the profile for.
             */
            set(lockProfileTime) {
                mLockProfileTime = lockProfileTime
                mPrefs!!.edit().putInt(LOCK_PROFILE, mLockProfileTime).apply()
            }

        var profileLockStartTime: Long
            /**
             * Get the start time of when the profile was locked.
             */
            get() {
                return mProfileLockStartTime
            }
            /**
             * Set the start time for locking the profile.
             */
            set(switchTime) {
                mProfileLockStartTime = switchTime
                mPrefs!!.edit().putLong(LOCK_PROFILE_START_TIME, mProfileLockStartTime).apply()
            }

        /**
         * Get the information for the audio profile.
         * @param profile The profile in question.
         * @return The audio profile information.
         */
        fun getProfile(profile: Int): AudioProfile {
            return mProfiles[profile]
        }

        /**
         * Get the list of audio profiles.
         * @return The list of profiles.
         */
        fun getProfiles(): List<AudioProfile> {
            return mProfiles
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
         */
        fun setProfile(profile: Int, name: String?, icon: Int, ringtoneVolume: Int, notificationVolume: Int, mediaVolume: Int, systemVolume: Int) {
            mProfiles[profile][name, icon, ringtoneVolume, notificationVolume, mediaVolume] = systemVolume
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
                    am.setStreamVolume(AudioManager.STREAM_RING, audioProfile.ringtoneVolume, 0)
                }
                if(audioProfile.notificationVolume != -1) {
                    am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioProfile.notificationVolume, 0)
                }
                if(audioProfile.mediaVolume != -1) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, audioProfile.mediaVolume, 0)
                }
                if(audioProfile.systemVolume != -1) {
                    am.setStreamVolume(AudioManager.STREAM_SYSTEM, audioProfile.systemVolume, 0)
                }
            }
        }
    }

    /**
     * Initialise the class.
     */
    init {
        Log.d("AudioProfile", "Initialising audio profile list completely")
        initialise(context)
    }
}