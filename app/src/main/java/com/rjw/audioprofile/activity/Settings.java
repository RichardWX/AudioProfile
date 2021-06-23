package com.rjw.audioprofile.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.rjw.audioprofile.R;
import com.rjw.audioprofile.utils.AudioProfileList;
import com.rjw.audioprofile.utils.DisplayUtils;
import com.rjw.audioprofile.utils.ProfileAdapter;

import java.util.ArrayList;

public class Settings extends AudioActivity {
    private Spinner mEnterProfile;
    private Spinner mExitProfile;
    private CheckBox mEnterProfileDefault;
    private CheckBox mExitProfileDefault;

    private int mAppColour = MainActivity.getConfigColour();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setWindowRatios(0.8f, 0.3f);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings_title);

        final ArrayList<String> profileNames = new ArrayList<>();
        for(int profileId = 0; profileId < AudioProfileList.NO_PROFILES; profileId++) {
            final AudioProfileList.AudioProfile profile = AudioProfileList.getProfile(profileId);
            profileNames.add(profile.name);
        }
        final ProfileAdapter adapter = new ProfileAdapter(this, AudioProfileList.getProfiles().toArray(new AudioProfileList.AudioProfile[0]));
        mEnterProfile = findViewById(R.id.spinnerEnterWifi);
        mEnterProfile.setAdapter(adapter);
        mEnterProfileDefault = findViewById(R.id.checkboxEnterWifiDefault);
        mEnterProfileDefault.getButtonDrawable().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        int profile = MainActivity.getProfiles().getEnterWifiProfile();
        if(profile == -1) {
            mEnterProfileDefault.setChecked(true);
        } else {
            mEnterProfile.setSelection(profile);
        }
        mExitProfile = findViewById(R.id.spinnerExitWifi);
        mExitProfile.setAdapter(adapter);
        mExitProfileDefault = findViewById(R.id.checkboxExitWifiDefault);
        mExitProfileDefault.getButtonDrawable().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        profile = MainActivity.getProfiles().getExitWifiProfile();
        if(profile == -1) {
            mExitProfileDefault.setChecked(true);
        } else {
            mExitProfile.setSelection(profile);
        }
        enableControls();
        colourControls();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch(requestCode) {
            case MainActivity.ACTIVITY_SELECT_THEME_COLOUR:
                if(resultCode == RESULT_OK) {
                    mAppColour = data.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour);
                    MainActivity.setAppColour(mAppColour);
                    final Intent intent = new Intent(this, getClass());
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    public void onClickUnchanged(final View view) {
        enableControls();
    }

    public void onClickAppColour(final View view) {
        final Intent intent = new Intent(this, ColourPicker.class);
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour);
        startActivityForResult(intent, MainActivity.ACTIVITY_SELECT_THEME_COLOUR);
    }

    public void onClickClose(final View view) {
        if(mEnterProfileDefault.isChecked()) {
            AudioProfileList.setEnterWifiProfile(-1);
        } else {
            AudioProfileList.setEnterWifiProfile(mEnterProfile.getSelectedItemPosition());
        }
        if(mExitProfileDefault.isChecked()) {
            AudioProfileList.setExitWifiProfile(-1);
        } else {
            AudioProfileList.setExitWifiProfile(mExitProfile.getSelectedItemPosition());
        }
        final Intent intent = new Intent();
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void enableControls() {
        mEnterProfile.setVisibility(mEnterProfileDefault.isChecked() ? View.INVISIBLE : View.VISIBLE);
        mExitProfile.setVisibility(mExitProfileDefault.isChecked() ? View.INVISIBLE : View.VISIBLE);
    }
}
