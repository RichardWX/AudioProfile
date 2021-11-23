package com.rjw.audioprofile.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.service.Notifications
import java.util.*

class AudioProfileList(context: Context?) {
    class AudioProfile(name: String?, icon: Int, ringtoneVolume: Int, notificationVolume: Int, mediaVolume: Int, systemVolume: Int) {
        var name: String? = null
        var icon = 0
        var ringtoneVolume = 0
        var notificationVolume = 0
        var mediaVolume = 0
        var systemVolume = 0

        operator fun set(name: String?, icon: Int, ringtoneVolume: Int, notificationVolume: Int, mediaVolume: Int, systemVolume: Int) {
            this.name = name
            this.icon = icon
            this.ringtoneVolume = ringtoneVolume
            this.notificationVolume = notificationVolume
            this.mediaVolume = mediaVolume
            this.systemVolume = systemVolume
        }

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
        private var mLockProfileTime = -1
        private var mContext: Context? = null
        private var mPrefs: SharedPreferences? = null
        private val mProfiles = ArrayList<AudioProfile>()
        private var mIcons: IntArray? = null
        private var mLastProfileSwitchTime = -1L
        fun initialise(context: Context?) {
            mContext = context?.applicationContext
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
            get() {
                return mIcons!!.size
            }

        fun getIcon(iconId: Int): Drawable {
            val icon = ContextCompat.getDrawable(mContext!!, mIcons!![iconId])
            icon!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            return icon
        }

        fun getIconResource(iconId: Int): Int {
            return mIcons!![iconId]
        }

        private fun loadProfiles() {
            if(mPrefs == null) {
                mPrefs = mContext!!.getSharedPreferences(MainActivity.TAG, Activity.MODE_PRIVATE)
            }
            mProfiles.clear()
            mCurrentProfile = mPrefs!!.getInt(CURRENT_PROFILE, mCurrentProfile)
            mEnterWifiProfile = mPrefs!!.getInt(ENTER_PROFILE, mEnterWifiProfile)
            mExitWifiProfile = mPrefs!!.getInt(EXIT_PROFILE, mExitWifiProfile)
            mLockProfileTime = mPrefs!!.getInt(LOCK_PROFILE, mLockProfileTime)
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
            get() {
                return if(mCurrentProfile < NO_PROFILES) mCurrentProfile else 0
            }
            set(currentProfile) {
                mCurrentProfile = currentProfile
                mPrefs!!.edit().putInt(CURRENT_PROFILE, mCurrentProfile).apply()
                mLastProfileSwitchTime = Calendar.getInstance().timeInMillis
                Notifications.updateNotification(mContext!!)
            }

        var enterWifiProfile: Int
            get() {
                return if(mEnterWifiProfile < NO_PROFILES) mEnterWifiProfile else 0
            }
            set(enterProfile) {
                mEnterWifiProfile = enterProfile
                mPrefs!!.edit().putInt(ENTER_PROFILE, mEnterWifiProfile).apply()
            }

        var exitWifiProfile: Int
            get() {
                return if(mExitWifiProfile < NO_PROFILES) mExitWifiProfile else 0
            }
            set(exitProfile) {
                mExitWifiProfile = exitProfile
                mPrefs!!.edit().putInt(EXIT_PROFILE, mExitWifiProfile).apply()
            }

        var lockProfileTime: Int
            get() {
                return mLockProfileTime
            }
            set(lockProfileTime) {
                mLockProfileTime = lockProfileTime
                mPrefs!!.edit().putInt(LOCK_PROFILE, mLockProfileTime).apply()
            }

        var lastProfileSwitchTime: Long
            get() {
                return mLastProfileSwitchTime
            }
            set(switchTime) {
                mLastProfileSwitchTime = switchTime
            }

        fun getProfile(profile: Int): AudioProfile {
            return mProfiles[profile]
        }

        fun getProfiles(): List<AudioProfile> {
            return mProfiles
        }

        fun setProfile(profile: Int, name: String?, icon: Int, ringtoneVolume: Int, notificationVolume: Int, mediaVolume: Int, systemVolume: Int) {
            mProfiles[profile][name, icon, ringtoneVolume, notificationVolume, mediaVolume] = systemVolume
        }

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

    init {
        initialise(context)
    }
}