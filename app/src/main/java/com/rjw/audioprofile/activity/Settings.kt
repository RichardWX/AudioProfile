package com.rjw.audioprofile.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivitySettingsBinding
import com.rjw.audioprofile.utils.*

class Settings : AudioActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val lockTimings = arrayOf(1, 2, 5, 10, 20, 30)
    private var mAppColour: Int = MainActivity.configColour

    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowRatios(0.8f, 0.3f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        binding = ActivitySettingsBinding.bind(view!!)
        setTitle(R.string.settings_title)
        val adapter = ProfileAdapter(this, AudioProfileList.getProfiles().toTypedArray())
        binding.spinnerEnterWifi.adapter = adapter
        binding.checkboxEnterWifiDefault.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
        var profile = AudioProfileList.enterWifiProfile
        if(profile == -1) {
            binding.checkboxEnterWifiDefault.isChecked = true
        } else {
            binding.spinnerEnterWifi.setSelection(profile)
        }
        binding.spinnerExitWifi.adapter = adapter
        binding.checkboxExitWifiDefault.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
        profile = AudioProfileList.exitWifiProfile
        if(profile == -1) {
            binding.checkboxExitWifiDefault.isChecked = true
        } else {
            binding.spinnerExitWifi.setSelection(profile)
        }
        val profileLockTime = AudioProfileList.lockProfileTime
        val lockAdapter = MinutesAdapter(this, lockTimings)
        binding.spinnerLockProfile.adapter = lockAdapter
        binding.checkboxLockProfile.buttonDrawable!!.setColorFilter(MainActivity.configColour, Mode.SRC_ATOP)
        if(profileLockTime == -1) {
            binding.checkboxLockProfile.isChecked = false
        } else {
            binding.checkboxLockProfile.isChecked = true
            for(item in 0..lockAdapter.count - 1) {
                if(lockAdapter.getItem(item) == profileLockTime) {
                    binding.spinnerLockProfile.setSelection(item)
                }
            }
        }

        enableControls()
        colourControls()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == MainActivity.ACTIVITY_SELECT_THEME_COLOUR) {
            if(resultCode == RESULT_OK) {
                if(data != null) {
                    mAppColour = data.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour)
                    MainActivity.setAppColour(mAppColour)
                    val intent = Intent(this, javaClass)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    fun onClickUnchanged(v: View?) {
        enableControls()
    }

    fun onClickLockProfile(v: View?) {
        enableControls()
    }

    fun onClickAppColour(v: View?) {
        val intent = Intent(this, ColourPicker::class.java)
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour)
        startActivityForResult(intent, MainActivity.ACTIVITY_SELECT_THEME_COLOUR)
    }

    fun onClickClose(v: View?) {
        AudioProfileList.enterWifiProfile = if(binding.checkboxEnterWifiDefault.isChecked) -1 else binding.spinnerEnterWifi.selectedItemPosition
        AudioProfileList.exitWifiProfile = if(binding.checkboxExitWifiDefault.isChecked) -1 else binding.spinnerExitWifi.selectedItemPosition
        AudioProfileList.lockProfileTime = if(binding.checkboxLockProfile.isChecked) binding.spinnerLockProfile.getItemAtPosition(
            binding.spinnerLockProfile.selectedItemPosition
        ) as Int else -1
        val intent = Intent()
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun enableControls() {
        binding.spinnerEnterWifi.visibility = if(binding.checkboxEnterWifiDefault.isChecked) View.INVISIBLE else View.VISIBLE
        binding.spinnerExitWifi.visibility = if(binding.checkboxExitWifiDefault.isChecked) View.INVISIBLE else View.VISIBLE
        binding.spinnerLockProfile.visibility = if(binding.checkboxLockProfile.isChecked) View.VISIBLE else View.INVISIBLE
    }
}