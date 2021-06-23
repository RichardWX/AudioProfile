package com.rjw.audioprofile.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.rjw.audioprofile.R;
import com.rjw.audioprofile.utils.Alerts;
import com.rjw.audioprofile.utils.AudioProfileList;
import com.rjw.audioprofile.utils.IconAdapter;

public class ProfileConfiguration extends AudioActivity {
    public final static String AUDIO_PROFILE = "AudioProfile";

    private int mProfile;
    private Spinner mProfileIcon;
    private EditText mProfileName;
    private CheckBox mUnchangedRingtone;
    private CheckBox mUnchangedNotification;
    private CheckBox mUnchangedMedia;
    private CheckBox mUnchangedSystem;
    private SeekBar mVolumeRingtone;
    private SeekBar mVolumeNotification;
    private SeekBar mVolumeMedia;
    private SeekBar mVolumeSystem;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.setWindowRatios(0.8f, 0.5f);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mProfileIcon = findViewById(R.id.spinnerIcon);
        mProfileName = findViewById(R.id.editProfileName);
        mUnchangedRingtone = findViewById(R.id.checkBoxUnchangedRingtone);
        mUnchangedNotification = findViewById(R.id.checkBoxUnchangedNotification);
        mUnchangedMedia = findViewById(R.id.checkBoxUnchangedMedia);
        mUnchangedSystem = findViewById(R.id.checkBoxUnchangedSystem);
        mVolumeRingtone = findViewById(R.id.seekBarRingtone);
        mVolumeNotification = findViewById(R.id.seekBarNotification);
        mVolumeMedia = findViewById(R.id.seekBarMedia);
        mVolumeSystem = findViewById(R.id.seekBarSystem);

        final AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(am == null) {
            // If the audio manager cannot be retrieved, then simply exit.
            Alerts.toast(R.string.cannot_get_system_service);
            finish();
        }
        mVolumeRingtone.setMax(am.getStreamMaxVolume(AudioManager.STREAM_RING));
        mVolumeNotification.setMax(am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        mVolumeMedia.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mVolumeSystem.setMax(am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));

        mProfile = getIntent().getIntExtra(ProfileConfiguration.AUDIO_PROFILE, -1);
        final AudioProfileList.AudioProfile profile = AudioProfileList.getProfile(mProfile);
        final Drawable[] icons = new Drawable[AudioProfileList.getLength()];
        for(int icon = 0; icon < AudioProfileList.getLength(); icon++) {
            icons[icon] = AudioProfileList.getIcon(icon);
        }
        final IconAdapter adapter = new IconAdapter(this, icons);
        LayerDrawable bar;
        mProfileIcon.getBackground().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mProfileIcon.setAdapter(adapter);
        mProfileIcon.setSelection(profile.icon);
        mProfileName.setText(profile.name);
        mUnchangedRingtone.setChecked(profile.ringtoneVolume == -1);
        mUnchangedRingtone.getButtonDrawable().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeRingtone.setProgress(profile.ringtoneVolume == -1 ? am.getStreamVolume(AudioManager.STREAM_RING) : profile.ringtoneVolume);
        bar = (LayerDrawable)mVolumeRingtone.getProgressDrawable().mutate();
        bar.getDrawable(0).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        bar.getDrawable(2).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeRingtone.getThumb().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mUnchangedNotification.setChecked(profile.notificationVolume == -1);
        mUnchangedNotification.getButtonDrawable().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeNotification.setProgress(profile.notificationVolume == -1 ? am.getStreamVolume(AudioManager.STREAM_NOTIFICATION) : profile.notificationVolume);
        bar = (LayerDrawable)mVolumeNotification.getProgressDrawable().mutate();
        bar.getDrawable(0).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        bar.getDrawable(2).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeNotification.getThumb().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mUnchangedMedia.setChecked(profile.mediaVolume == -1);
        mUnchangedMedia.getButtonDrawable().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeMedia.setProgress(profile.mediaVolume == -1 ? am.getStreamVolume(AudioManager.STREAM_MUSIC) : profile.mediaVolume);
        bar = (LayerDrawable)mVolumeMedia.getProgressDrawable().mutate();
        bar.getDrawable(0).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        bar.getDrawable(2).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeMedia.getThumb().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mUnchangedSystem.setChecked(profile.systemVolume == -1);
        mUnchangedSystem.getButtonDrawable().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeSystem.setProgress(profile.systemVolume == -1 ? am.getStreamVolume(AudioManager.STREAM_SYSTEM) : profile.systemVolume);
        bar = (LayerDrawable)mVolumeSystem.getProgressDrawable().mutate();
        bar.getDrawable(0).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        bar.getDrawable(2).setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        mVolumeSystem.getThumb().setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        enableControls();
        colourControls();
    }

    public void onClickUnchanged(final View v) {
        enableControls();
    }

    public void onClickClose(final View v) {
        MainActivity.getProfiles().setProfile(mProfile,
                mProfileName.getText().toString(),
                mProfileIcon.getSelectedItemPosition(),
                mUnchangedRingtone.isChecked() ? -1 : mVolumeRingtone.getProgress(),
                mUnchangedNotification.isChecked() ? -1 : mVolumeNotification.getProgress(),
                mUnchangedMedia.isChecked() ? -1 : mVolumeMedia.getProgress(),
                mUnchangedSystem.isChecked() ? -1 : mVolumeSystem.getProgress());
        final Intent intent = new Intent();
        intent.putExtra(AUDIO_PROFILE, mProfile);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void enableControls() {
        mVolumeRingtone.setEnabled(!mUnchangedRingtone.isChecked());
        mVolumeNotification.setEnabled(!mUnchangedNotification.isChecked());
        mVolumeMedia.setEnabled(!mUnchangedMedia.isChecked());
        mVolumeSystem.setEnabled(!mUnchangedSystem.isChecked());
    }
}
