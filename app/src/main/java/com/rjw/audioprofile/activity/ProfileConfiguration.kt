package com.rjw.audioprofile.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivityConfigBinding
import com.rjw.audioprofile.utils.*

class ProfileConfiguration : AudioActivity() {
    private lateinit var binding: ActivityConfigBinding
    private var mProfile = 0

    /**
     * Create the configuration screen.
     * @param savedInstanceState State information from last time the activity was run.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setWindowRatios(0.8f, 0.5f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        binding = ActivityConfigBinding.bind(view!!)
        val am = getSystemService(AUDIO_SERVICE) as AudioManager?
        if(am == null) {
            // If the audio manager cannot be retrieved, then simply exit.
            Alerts.toast(R.string.cannot_get_system_service)
            finish()
        } else {
            binding.seekBarRingtone.max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
            binding.seekBarNotification.max = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            binding.seekBarMedia.max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            binding.seekBarSystem.max = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
            mProfile = intent.getIntExtra(AUDIO_PROFILE, -1)
            val profile = AudioProfileList.getProfile(mProfile)
            val icons = arrayOfNulls<Drawable>(AudioProfileList.length)
            for(icon in 0 until AudioProfileList.length) {
                icons[icon] = AudioProfileList.getIcon(icon)
            }
            val adapter = IconAdapter(this, icons)
            binding.spinnerIcon.background.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.spinnerIcon.adapter = adapter
            binding.spinnerIcon.setSelection(profile.icon)
            binding.editProfileName.setText(profile.name)
            binding.checkBoxUnchangedRingtone.isChecked = profile.ringtoneVolume == -1
            binding.checkBoxUnchangedRingtone.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarRingtone.progress = if(profile.ringtoneVolume == -1) am.getStreamVolume(AudioManager.STREAM_RING) else profile.ringtoneVolume
            var bar: LayerDrawable = binding.seekBarRingtone.progressDrawable.mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarRingtone.thumb.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.checkBoxUnchangedNotification.isChecked = profile.notificationVolume == -1
            binding.checkBoxUnchangedNotification.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarNotification.progress = if(profile.notificationVolume == -1) am.getStreamVolume(AudioManager.STREAM_NOTIFICATION) else profile.notificationVolume
            bar = binding.seekBarNotification.progressDrawable.mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarNotification.thumb.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.checkBoxUnchangedMedia.isChecked = profile.mediaVolume == -1
            binding.checkBoxUnchangedMedia.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarMedia.progress = if(profile.mediaVolume == -1) am.getStreamVolume(AudioManager.STREAM_MUSIC) else profile.mediaVolume
            bar = binding.seekBarMedia.progressDrawable.mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarMedia.thumb.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.checkBoxUnchangedSystem.isChecked = profile.systemVolume == -1
            binding.checkBoxUnchangedSystem.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarSystem.progress = if(profile.systemVolume == -1) am.getStreamVolume(AudioManager.STREAM_SYSTEM) else profile.systemVolume
            bar = binding.seekBarSystem.progressDrawable.mutate() as LayerDrawable
            bar.getDrawable(0).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            bar.getDrawable(2).setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            binding.seekBarSystem.thumb.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
            enableControls()
            colourControls()
        }
    }

    /**
     * Handle the unchanged profile button.
     * @param v The view in question.
     */
    fun onClickUnchanged(v: View?) {
        enableControls()
    }

    /**
     * Close the activity.
     * @param v The view in question.
     */
    fun onClickClose(v: View?) {
        AudioProfileList.setProfile(
            mProfile,
            binding.editProfileName.text.toString(),
            binding.spinnerIcon.selectedItemPosition,
            if(binding.checkBoxUnchangedRingtone.isChecked) -1 else binding.seekBarRingtone.progress,
            if(binding.checkBoxUnchangedNotification.isChecked) -1 else binding.seekBarNotification.progress,
            if(binding.checkBoxUnchangedMedia.isChecked) -1 else binding.seekBarMedia.progress,
            if(binding.checkBoxUnchangedSystem.isChecked) -1 else binding.seekBarSystem.progress
        )
        val intent = Intent()
        intent.putExtra(AUDIO_PROFILE, mProfile)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Enable or disable the controls depending on the state.
     */
    private fun enableControls() {
        binding.seekBarRingtone.isEnabled = !binding.checkBoxUnchangedRingtone.isChecked
        binding.seekBarNotification.isEnabled = !binding.checkBoxUnchangedNotification.isChecked
        binding.seekBarMedia.isEnabled = !binding.checkBoxUnchangedMedia.isChecked
        binding.seekBarSystem.isEnabled = !binding.checkBoxUnchangedSystem.isChecked
    }

    companion object {
        const val AUDIO_PROFILE = "AudioProfile"
    }
}