package com.rjw.audioprofile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;

import com.rjw.audioprofile.activity.MainActivity;
import com.rjw.audioprofile.R;

import java.util.ArrayList;
import java.util.List;

public class AudioProfileList {
    public static final int NO_PROFILES = 4;

    private static final int DEFAULT = -1;
    private static final String NAME = "Name";
    private static final String ICON = "Icon";
    private static final String RINGTONE = "Ringtone";
    private static final String NOTIFICATION = "Notification";
    private static final String MEDIA = "Media";
    private static final String SYSTEM = "System";
    private static final String CURRENT_PROFILE = "CurrentProfile";
    private static final String ENTER_PROFILE = "EnterProfile";
    private static final String EXIT_PROFILE = "ExitProfile";

    public static class AudioProfile {
        public String name;
        public int icon;
        public int ringtoneVolume;
        public int notificationVolume;
        public int mediaVolume;
        public int systemVolume;

        public AudioProfile(final String name, final int icon, final int ringtoneVolume, final int notificationVolume, final int mediaVolume, final int systemVolume) {
            set(name, icon, ringtoneVolume, notificationVolume, mediaVolume, systemVolume);
        }

        public void set(final String name, final int icon, final int ringtoneVolume, final int notificationVolume, final int mediaVolume, final int systemVolume) {
            this.name = name;
            this.icon = icon;
            this.ringtoneVolume = ringtoneVolume;
            this.notificationVolume = notificationVolume;
            this.mediaVolume = mediaVolume;
            this.systemVolume = systemVolume;
        }
    }

    private static int mCurrentProfile = 0;
    private static int mEnterWifiProfile = -1;
    private static int mExitWifiProfile = -1;

    private static Context mContext;
    private static SharedPreferences mPrefs;
    private static ArrayList<AudioProfile> mProfiles = new ArrayList<>();
    private static int[] mIcons;

    public AudioProfileList(final Context context) {
        initialise(context);
    }

    public static void initialise(final Context context) {
        mContext = context;
        if(mIcons == null || mIcons.length == 0) {
            mIcons = new int[] { R.drawable.icon00,
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
                    R.drawable.icon13};
        }
        if(mProfiles.isEmpty()) {
            loadProfiles();
        }
    }

    public static int getLength() {
        return mIcons.length;
    }

    public static Drawable getIcon(final int iconId) {
        final Drawable icon = mContext.getDrawable(mIcons[iconId]);
        icon.setColorFilter(MainActivity.getConfigColour(), PorterDuff.Mode.SRC_ATOP);
        return icon;
    }

    public static int getIconResource(final int iconId) {
        return mIcons[iconId];
    }

    public static int loadProfiles() {
        if(mPrefs == null) {
            mPrefs = mContext.getSharedPreferences(MainActivity.TAG, Activity.MODE_PRIVATE);
        }
        mProfiles.clear();
        mCurrentProfile = mPrefs.getInt(CURRENT_PROFILE, mCurrentProfile);
        mEnterWifiProfile = mPrefs.getInt(ENTER_PROFILE, mEnterWifiProfile);
        mExitWifiProfile = mPrefs.getInt(EXIT_PROFILE, mExitWifiProfile);
        for(int profile = 0; profile < NO_PROFILES; profile++) {
            final AudioProfile audioProfile = new AudioProfile(mPrefs.getString(NAME + profile,
                    mContext.getResources().getStringArray(R.array.profile)[profile]),
                    mPrefs.getInt(ICON + profile, profile),
                    mPrefs.getInt(RINGTONE + profile, DEFAULT), mPrefs.getInt(NOTIFICATION + profile, DEFAULT),
                    mPrefs.getInt(MEDIA + profile, DEFAULT), mPrefs.getInt(SYSTEM + profile, DEFAULT));
            mProfiles.add(audioProfile);
        }
        return mProfiles.size();
    }

    public static void saveProfiles(final Context context) {
        if(mPrefs == null) {
            mPrefs = context.getSharedPreferences(MainActivity.TAG, Activity.MODE_PRIVATE);
        }
        for(int profile = 0; profile < NO_PROFILES; profile++) {
            final AudioProfile audioProfile = mProfiles.get(profile);
            mPrefs.edit().putString(NAME + profile, audioProfile.name).apply();
            mPrefs.edit().putInt(ICON + profile, audioProfile.icon).apply();
            mPrefs.edit().putInt(RINGTONE + profile, audioProfile.ringtoneVolume).apply();
            mPrefs.edit().putInt(NOTIFICATION + profile, audioProfile.notificationVolume).apply();
            mPrefs.edit().putInt(MEDIA + profile, audioProfile.mediaVolume).apply();
            mPrefs.edit().putInt(SYSTEM + profile, audioProfile.systemVolume).apply();
        }
    }

    public static int getCurrentProfile() {
        return mCurrentProfile < NO_PROFILES ? mCurrentProfile : 0;
    }

    public static void setCurrentProfile(final int currentProfile) {
        mCurrentProfile = currentProfile;
        mPrefs.edit().putInt(CURRENT_PROFILE, mCurrentProfile).apply();
    }

    public static int getEnterWifiProfile() {
        return mEnterWifiProfile < NO_PROFILES ? mEnterWifiProfile : 0;
    }

    public static void setEnterWifiProfile(final int enterProfile) {
        mEnterWifiProfile = enterProfile;
        mPrefs.edit().putInt(ENTER_PROFILE, mEnterWifiProfile).apply();
    }

    public static int getExitWifiProfile() {
        return mExitWifiProfile < NO_PROFILES ? mExitWifiProfile : 0;
    }

    public static void setExitWifiProfile(final int exitProfile) {
        mExitWifiProfile = exitProfile;
        mPrefs.edit().putInt(EXIT_PROFILE, mExitWifiProfile).apply();
    }

    public static AudioProfile getProfile(final int profile) {
        return mProfiles.get(profile);
    }

    public static List<AudioProfile> getProfiles() {
        return mProfiles;
    }

    public static void setProfile(final int profile, final String name, final int icon, final int ringtoneVolume, final int notificationVolume, final int mediaVolume, final int systemVolume) {
        mProfiles.get(profile).set(name, icon, ringtoneVolume, notificationVolume, mediaVolume, systemVolume);
    }

    public static void applyProfile(final Context context) {
        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(am != null) {
            final AudioProfile audioProfile = getProfile(getCurrentProfile());
            if(audioProfile.ringtoneVolume != -1) {
                am.setStreamVolume(AudioManager.STREAM_RING, audioProfile.ringtoneVolume, 0);
            }
            if(audioProfile.notificationVolume != -1) {
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, audioProfile.notificationVolume, 0);
            }
            if(audioProfile.mediaVolume != -1) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, audioProfile.mediaVolume, 0);
            }
            if(audioProfile.systemVolume != -1) {
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, audioProfile.systemVolume, 0);
            }
        }
    }
}
