package com.rjw.audioprofile.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Spinner
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.AudioProfileList
import com.rjw.audioprofile.utils.DisplayUtils
import com.rjw.audioprofile.utils.ProfileAdapter

class Settings : AudioActivity() {
    private lateinit var mEnterProfile: Spinner
    private lateinit var mExitProfile: Spinner
    private lateinit var mEnterProfileDefault: CheckBox
    private lateinit var mExitProfileDefault: CheckBox
    private var mAppColour: Int = MainActivity.configColour
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowRatios(0.8f, 0.3f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setTitle(R.string.settings_title)
        val adapter = ProfileAdapter(this, AudioProfileList.getProfiles().toTypedArray())
        mEnterProfile = findViewById(R.id.spinnerEnterWifi)
        mEnterProfile.setAdapter(adapter)
        mEnterProfileDefault = findViewById(R.id.checkboxEnterWifiDefault)
        mEnterProfileDefault.getButtonDrawable()!!.setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
        var profile = AudioProfileList.enterWifiProfile
        if(profile == -1) {
            mEnterProfileDefault.setChecked(true)
        } else {
            mEnterProfile.setSelection(profile)
        }
        mExitProfile = findViewById(R.id.spinnerExitWifi)
        mExitProfile.setAdapter(adapter)
        mExitProfileDefault = findViewById(R.id.checkboxExitWifiDefault)
        mExitProfileDefault.getButtonDrawable()!!.setColorFilter(MainActivity.configColour, PorterDuff.Mode.SRC_ATOP)
        profile = AudioProfileList.exitWifiProfile
        if(profile == -1) {
            mExitProfileDefault.setChecked(true)
        } else {
            mExitProfile.setSelection(profile)
        }
        enableControls()
        colourControls()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if(requestCode == MainActivity.ACTIVITY_SELECT_THEME_COLOUR) {
            if(resultCode == RESULT_OK) {
                mAppColour = data.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour)
                MainActivity.setAppColour(mAppColour)
                val intent = Intent(this, javaClass)
                startActivity(intent)
                finish()
            }
        }
    }

    fun onClickUnchanged(v: View?) {
        enableControls()
    }

    fun onClickAppColour(v: View?) {
        val intent = Intent(this, ColourPicker::class.java)
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour)
        startActivityForResult(intent, MainActivity.ACTIVITY_SELECT_THEME_COLOUR)
    }

    fun onClickClose(v: View?) {
        if(mEnterProfileDefault.isChecked) {
            AudioProfileList.enterWifiProfile = -1
        } else {
            AudioProfileList.enterWifiProfile = mEnterProfile.selectedItemPosition
        }
        if(mExitProfileDefault.isChecked) {
            AudioProfileList.exitWifiProfile = -1
        } else {
            AudioProfileList.exitWifiProfile = mExitProfile.selectedItemPosition
        }
        val intent = Intent()
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun enableControls() {
        mEnterProfile.visibility = if(mEnterProfileDefault.isChecked) View.INVISIBLE else View.VISIBLE
        mExitProfile.visibility = if(mExitProfileDefault.isChecked) View.INVISIBLE else View.VISIBLE
    }
}