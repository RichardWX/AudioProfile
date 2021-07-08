package com.rjw.audioprofile.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.Alerts
import com.rjw.audioprofile.utils.AudioProfileList
import com.rjw.audioprofile.utils.IconAdapter

class ProfileConfiguration : AudioActivity() {
    private var mProfile = 0
    private lateinit var mProfileIcon: Spinner
    private lateinit var mProfileName: EditText
    private lateinit var mUnchangedRingtone: CheckBox
    private lateinit var mUnchangedNotification: CheckBox
    private lateinit var mUnchangedMedia: CheckBox
    private lateinit var mUnchangedSystem: CheckBox
    private lateinit var mVolumeRingtone: SeekBar
    private lateinit var mVolumeNotification: SeekBar
    private lateinit var mVolumeMedia: SeekBar
    private lateinit var mVolumeSystem: SeekBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setWindowRatios(0.8f, 0.5f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        mProfileIcon = findViewById(R.id.spinnerIcon)
        mProfileName = findViewById(R.id.editProfileName)
        mUnchangedRingtone = findViewById(R.id.checkBoxUnchangedRingtone)
        mUnchangedNotification = findViewById(R.id.checkBoxUnchangedNotification)
        mUnchangedMedia = findViewById(R.id.checkBoxUnchangedMedia)
        mUnchangedSystem = findViewById(R.id.checkBoxUnchangedSystem)
        mVolumeRingtone = findViewById(R.id.seekBarRingtone)
        mVolumeNotification = findViewById(R.id.seekBarNotification)
        mVolumeMedia = findViewById(R.id.seekBarMedia)
        mVolumeSystem = findViewById(R.id.seekBarSystem)
        val am = getSystemService(AUDIO_SERVICE) as AudioManager?
        if(am == null) {
            // If the audio manager cannot be retrieved, then simply exit.
            Alerts.toast(R.string.cannot_get_system_service)
            finish()
        } else {
            mVolumeRingtone.setMax(am.getStreamMaxVolume(AudioManager.STREAM_RING))
            mVolumeNotification.setMax(am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION))
            mVolumeMedia.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
            mVolumeSystem.setMax(am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM))
            mProfile = intent.getIntExtra(AUDIO_PROFILE, -1)
            val profile = AudioProfileList.getProfile(mProfile)
            val icons = arrayOfNulls<Drawable>(AudioProfileList.length)
            for(icon in 0 until AudioProfileList.length) {
                icons[icon] = AudioProfileList.getIcon(icon)
            }
            val adapter = IconAdapter(this, icons)
            var bar: LayerDrawable
            mProfileIcon.getBackground().setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mProfileIcon.setAdapter(adapter)
            mProfileIcon.setSelection(profile.icon)
            mProfileName.setText(profile.name)
            mUnchangedRingtone.setChecked(profile.ringtoneVolume == -1)
            mUnchangedRingtone.getButtonDrawable()!!.setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeRingtone.setProgress(if(profile.ringtoneVolume == -1) am.getStreamVolume(AudioManager.STREAM_RING) else profile.ringtoneVolume)
            bar = mVolumeRingtone.getProgressDrawable().mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeRingtone.getThumb().setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mUnchangedNotification.setChecked(profile.notificationVolume == -1)
            mUnchangedNotification.getButtonDrawable()!!.setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeNotification.setProgress(if(profile.notificationVolume == -1) am.getStreamVolume(AudioManager.STREAM_NOTIFICATION) else profile.notificationVolume)
            bar = mVolumeNotification.getProgressDrawable().mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeNotification.getThumb().setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mUnchangedMedia.setChecked(profile.mediaVolume == -1)
            mUnchangedMedia.getButtonDrawable()!!.setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeMedia.setProgress(if(profile.mediaVolume == -1) am.getStreamVolume(AudioManager.STREAM_MUSIC) else profile.mediaVolume)
            bar = mVolumeMedia.getProgressDrawable().mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeMedia.getThumb().setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mUnchangedSystem.setChecked(profile.systemVolume == -1)
            mUnchangedSystem.getButtonDrawable()!!.setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeSystem.setProgress(if(profile.systemVolume == -1) am.getStreamVolume(AudioManager.STREAM_SYSTEM) else profile.systemVolume)
            bar = mVolumeSystem.getProgressDrawable().mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            mVolumeSystem.getThumb().setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
            enableControls()
            colourControls()
        }
    }

    fun onClickUnchanged(v: View?) {
        enableControls()
    }

    fun onClickClose(v: View?) {
        AudioProfileList.setProfile(
            mProfile,
            mProfileName.text.toString(),
            mProfileIcon.selectedItemPosition,
            if(mUnchangedRingtone.isChecked) -1 else mVolumeRingtone.progress,
            if(mUnchangedNotification.isChecked) -1 else mVolumeNotification.progress,
            if(mUnchangedMedia.isChecked) -1 else mVolumeMedia.progress,
            if(mUnchangedSystem.isChecked) -1 else mVolumeSystem.progress
        )
        val intent = Intent()
        intent.putExtra(AUDIO_PROFILE, mProfile)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun enableControls() {
        mVolumeRingtone.isEnabled = !mUnchangedRingtone.isChecked
        mVolumeNotification.isEnabled = !mUnchangedNotification.isChecked
        mVolumeMedia.isEnabled = !mUnchangedMedia.isChecked
        mVolumeSystem.isEnabled = !mUnchangedSystem.isChecked
    }

    companion object {
        const val AUDIO_PROFILE = "AudioProfile"
    }
}