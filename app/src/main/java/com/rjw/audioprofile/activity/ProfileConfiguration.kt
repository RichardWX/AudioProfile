package com.rjw.audioprofile.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivityConfigBinding
import com.rjw.audioprofile.utils.Alerts
import com.rjw.audioprofile.utils.AudioProfileList
import com.rjw.audioprofile.utils.IconAdapter

class ProfileConfiguration : AudioActivity() {
    private lateinit var binding: ActivityConfigBinding
    private var _profile = 0

    /**
     * Create the configuration screen.
     * @param savedInstanceState State information from last time the activity was run.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.setWindowRatios(0.8f, 0.5f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        binding = ActivityConfigBinding.bind(view)
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
            _profile = intent.getIntExtra(AUDIO_PROFILE, -1)
            val profile = AudioProfileList.getProfile(_profile)
            val icons = arrayOfNulls<Drawable>(AudioProfileList.length)
            for(icon in 0 until AudioProfileList.length) {
                icons[icon] = AudioProfileList.getIcon(icon)
            }
            val adapter = IconAdapter(this, icons)
            binding.spinnerIcon.adapter = adapter
            binding.spinnerIcon.setSelection(profile.icon)
            binding.editProfileName.setText(profile.name)
            binding.checkBoxUnchangedRingtone.isChecked = profile.ringtoneVolume == -1
            binding.seekBarRingtone.progress = if(profile.ringtoneVolume == -1) am.getStreamVolume(AudioManager.STREAM_RING) else profile.ringtoneVolume
            binding.checkBoxVibrateRingtone.isChecked = profile.vibrate
            binding.checkBoxUnchangedNotification.isChecked = profile.notificationVolume == -1
            binding.seekBarNotification.progress = if(profile.notificationVolume == -1) am.getStreamVolume(AudioManager.STREAM_NOTIFICATION) else profile.notificationVolume
            binding.checkBoxUnchangedMedia.isChecked = profile.mediaVolume == -1
            binding.seekBarMedia.progress = if(profile.mediaVolume == -1) am.getStreamVolume(AudioManager.STREAM_MUSIC) else profile.mediaVolume
            binding.checkBoxUnchangedSystem.isChecked = profile.systemVolume == -1
            binding.seekBarSystem.progress = if(profile.systemVolume == -1) am.getStreamVolume(AudioManager.STREAM_SYSTEM) else profile.systemVolume

            // Set up the control operations.
            binding.checkBoxUnchangedRingtone.setOnClickListener { updateControls() }
            binding.seekBarRingtone.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, startPos: Int, endPos: Boolean) {
                    updateControls()
                }

                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) {}
            })
            binding.checkBoxUnchangedNotification.setOnClickListener { updateControls() }
            binding.checkBoxUnchangedMedia.setOnClickListener { updateControls() }
            binding.checkBoxUnchangedSystem.setOnClickListener { updateControls() }
            binding.buttonClose.setOnClickListener {
                AudioProfileList.setProfile(
                    _profile,
                    binding.editProfileName.text.toString(),
                    binding.spinnerIcon.selectedItemPosition,
                    if(binding.checkBoxUnchangedRingtone.isChecked) -1 else binding.seekBarRingtone.progress,
                    if(binding.checkBoxUnchangedNotification.isChecked) -1 else binding.seekBarNotification.progress,
                    if(binding.checkBoxUnchangedMedia.isChecked) -1 else binding.seekBarMedia.progress,
                    if(binding.checkBoxUnchangedSystem.isChecked) -1 else binding.seekBarSystem.progress,
                    if(binding.seekBarRingtone.progress == 0) binding.checkBoxVibrateRingtone.isChecked else true
                )
                val intent = Intent()
                intent.putExtra(AUDIO_PROFILE, _profile)
                setResult(RESULT_OK, intent)
                finish()
            }

            updateControls()
            colourControls()
        }
    }

    /**
     * Enable or disable the controls depending on the state.
     */
    private fun updateControls() {
        binding.seekBarRingtone.isEnabled = !binding.checkBoxUnchangedRingtone.isChecked
        binding.checkBoxVibrateRingtone.visibility = if(!binding.checkBoxUnchangedRingtone.isChecked && binding.seekBarRingtone.progress == 0) View.VISIBLE else View.GONE
        binding.seekBarNotification.isEnabled = !binding.checkBoxUnchangedNotification.isChecked
        binding.seekBarMedia.isEnabled = !binding.checkBoxUnchangedMedia.isChecked
        binding.seekBarSystem.isEnabled = !binding.checkBoxUnchangedSystem.isChecked
    }

    companion object {
        const val AUDIO_PROFILE = "AudioProfile"
    }
}