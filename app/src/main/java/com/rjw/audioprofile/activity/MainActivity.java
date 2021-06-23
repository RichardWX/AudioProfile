package com.rjw.audioprofile.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rjw.audioprofile.service.AudioProfileService;
import com.rjw.audioprofile.BuildConfig;
import com.rjw.audioprofile.service.QuickPanel;
import com.rjw.audioprofile.R;
import com.rjw.audioprofile.utils.Alerts;
import com.rjw.audioprofile.utils.AudioProfileList;
import com.rjw.audioprofile.utils.DisplayUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AudioActivity {
    public final static String TAG = "AudioProfile";
    public final static int REQUEST_PERMISSIONS = 1;
    public final static int REQUEST_AUDIO_PROFILE = 2;
    public static final int ACTIVITY_SETTINGS = 3;
    public static final int ACTIVITY_SELECT_THEME_COLOUR = 4;

    public static final String CHANNEL_ID = "AudioProfileChannelId";
    public static final CharSequence CHANNEL_NAME = "AudioProfile";
    public static final String CHANNEL_DESCRIPTION = "AudioProfile";
    public static final int SERVICE_NOTIFICATION_ID = 100;

    public static final String PREF_APPLICATION_COLOUR = "ApplicationColour";

    private static MainActivity mThis;
    private static AudioProfileList mProfiles;
    private static NotificationManager mNm;
    private static int mAppColour;

    private RadioButton[] mRadioProfile = new RadioButton[AudioProfileList.NO_PROFILES];
    private ImageView[] mImageProfile = new ImageView[AudioProfileList.NO_PROFILES];
    private TextView[] mTextProfile = new TextView[AudioProfileList.NO_PROFILES];

    public static MainActivity getInstance() {
        if(mThis == null) {
            new MainActivity();
        }
        return mThis;
    }

    protected final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
        }
    };

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mThis = this;

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable e) {
                e.printStackTrace();
            }
        });

        // Load the settings.
        mProfiles = new AudioProfileList(this);

        mRadioProfile[0] = findViewById(R.id.radioProfile0);
        mRadioProfile[1] = findViewById(R.id.radioProfile1);
        mRadioProfile[2] = findViewById(R.id.radioProfile2);
        mRadioProfile[3] = findViewById(R.id.radioProfile3);
        mImageProfile[0] = findViewById(R.id.imageProfile0);
        mImageProfile[1] = findViewById(R.id.imageProfile1);
        mImageProfile[2] = findViewById(R.id.imageProfile2);
        mImageProfile[3] = findViewById(R.id.imageProfile3);
        mTextProfile[0] = findViewById(R.id.textProfile0);
        mTextProfile[1] = findViewById(R.id.textProfile1);
        mTextProfile[2] = findViewById(R.id.textProfile2);
        mTextProfile[3] = findViewById(R.id.textProfile3);

        // Set the profile names and icons.
        for(int profile = 0; profile < AudioProfileList.NO_PROFILES; profile++) {
            final AudioProfileList.AudioProfile audioProfile = AudioProfileList.getProfile(profile);
            mRadioProfile[profile].getButtonDrawable().setColorFilter(getConfigColour(), PorterDuff.Mode.SRC_ATOP);
            mTextProfile[profile].setText(audioProfile.name);
            mImageProfile[profile].setImageDrawable(AudioProfileList.getIcon(audioProfile.icon));
        }

        // Select the current profile.
        selectRadio(AudioProfileList.getCurrentProfile());

        // Check we have the required permissions.
        final Intent intent = new Intent(this, PermissionRequest.class);
        startActivityForResult(intent, REQUEST_PERMISSIONS);
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
               checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION }, 0);
        }
        if(!isServiceRunning()) {
            startService();
        }
        final SharedPreferences prefs = getSharedPreferences(TAG, Activity.MODE_PRIVATE);
        mAppColour = prefs.getInt(PREF_APPLICATION_COLOUR, getColor(R.color.colourConfig));
        colourControls();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch(requestCode) {
            case REQUEST_PERMISSIONS:
                updateTile(this);
                break;
            case REQUEST_AUDIO_PROFILE:
                if(resultCode == RESULT_OK) {
                    AudioProfileList.saveProfiles(this);
                    final int modified = data.getIntExtra(ProfileConfiguration.AUDIO_PROFILE, 0);
                    mTextProfile[modified].setText(AudioProfileList.getProfile(modified).name);
                    mImageProfile[modified].setImageDrawable(AudioProfileList.getIcon(AudioProfileList.getProfile(modified).icon));
                    if(AudioProfileList.getCurrentProfile() == modified) {
                        selectRadio(AudioProfileList.getCurrentProfile());
                    }
                }
                break;
            case ACTIVITY_SETTINGS:
                if(data != null) {
                    final int themeColour = data.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mAppColour);
                    if(themeColour != mAppColour) {
                        mAppColour = themeColour;
                        setAppColour(mAppColour);
                    }
                }
                final Intent intent = new Intent(this, getClass());
                startActivity(intent);
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void onClickAudioProfile(final View v) {
        switch(v.getId()) {
            case R.id.radioProfile0:
            case R.id.imageProfile0:
            case R.id.textProfile0:
                selectRadio(0);
                break;
            case R.id.radioProfile1:
            case R.id.imageProfile1:
            case R.id.textProfile1:
                selectRadio(1);
                break;
            case R.id.radioProfile2:
            case R.id.imageProfile2:
            case R.id.textProfile2:
                selectRadio(2);
                break;
            case R.id.radioProfile3:
            case R.id.imageProfile3:
            case R.id.textProfile3:
                selectRadio(3);
                break;
        }
    }

    public void onClickAudioConfiguration(final View v) {
        final Intent intent = new Intent(this, ProfileConfiguration.class);
        switch(v.getId()) {
            case R.id.buttonConfigureProfile0:
                intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 0);
                break;
            case R.id.buttonConfigureProfile1:
                intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 1);
                break;
            case R.id.buttonConfigureProfile2:
                intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 2);
                break;
            case R.id.buttonConfigureProfile3:
                intent.putExtra(ProfileConfiguration.AUDIO_PROFILE, 3);
                break;
        }
        startActivityForResult(intent, REQUEST_AUDIO_PROFILE);
    }

    public void onClickAbout(final View view) {
        final StringBuilder about = new StringBuilder();
        try {
            final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            about.append(getString(R.string.about_version)).append(": ").append(pInfo.versionName);
            final Calendar buildDate = Calendar.getInstance();
            buildDate.setTimeInMillis(BuildConfig.TIMESTAMP);
            final StringBuilder builtDate = new StringBuilder(DateFormat.getDateInstance(DateFormat.SHORT).format(buildDate.getTime()));
            if(BuildConfig.DEBUG) {
                builtDate.append(" ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(buildDate.getTime()));
            }
            about.append("\n\n").append(getString(R.string.about_built)).append(" ").append(builtDate);
            about.append("\n\n").append(String.format(getString(R.string.copyright), new SimpleDateFormat("yyyy", Locale.getDefault()).format(buildDate.getTime())));
            final StringBuilder title = new StringBuilder(getString(R.string.about_title)).append(" ").append(getString(R.string.app_name));
            Alerts.alert(title, about);
        } catch(final Throwable e) {
            // Do nothing.
        }
    }

    public void onClickSettings(final View v) {
        final Intent intent = new Intent(this, Settings.class);
        startActivityForResult(intent, ACTIVITY_SETTINGS);
    }

    public void onClickClose(final View v) {
        finish();
    }

    public void selectRadio(final int profile) {
        AudioProfileList.setCurrentProfile(profile);
        switch(profile) {
            case 0:
                mRadioProfile[0].setChecked(true);
                mRadioProfile[1].setChecked(false);
                mRadioProfile[2].setChecked(false);
                mRadioProfile[3].setChecked(false);
                break;
            case 1:
                mRadioProfile[0].setChecked(false);
                mRadioProfile[1].setChecked(true);
                mRadioProfile[2].setChecked(false);
                mRadioProfile[3].setChecked(false);
                break;
            case 2:
                mRadioProfile[0].setChecked(false);
                mRadioProfile[1].setChecked(false);
                mRadioProfile[2].setChecked(true);
                mRadioProfile[3].setChecked(false);
                break;
            case 3:
                mRadioProfile[0].setChecked(false);
                mRadioProfile[1].setChecked(false);
                mRadioProfile[2].setChecked(false);
                mRadioProfile[3].setChecked(true);
                break;
        }
        AudioProfileList.applyProfile(this);
        updateTile(this);
    }

    public static void updateTile(final Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                TileService.requestListeningState(context, new ComponentName(context, QuickPanel.class));
            } catch(Exception e) {
                Log.d("AudioProfile", "Tile update failure - starting service");
            }
        }
    }

    public static AudioProfileList getProfiles() {
        return mProfiles;
    }

    public static int getConfigColour() {
        return mAppColour;
    }

    public static int getWhiteColour() {
        return mThis.getColor(R.color.colourWhiteText);
    }

    public void startService() {
        final Intent serviceIntent = new Intent(mThis, AudioProfileService.class);
        try {
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        } catch(Exception e) {
            Alerts.toast(e.toString());
        }
    }

    private boolean isServiceRunning() {
        try {
            final ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            for(final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if(AudioProfileService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch(Exception e) {
            // Do nothing.
        }
        return false;
    }

    public static void createNotificationChannel(final Context context) {
        try {
            mNm = (NotificationManager)mThis.getSystemService(Context.NOTIFICATION_SERVICE);
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
                channel.setDescription(CHANNEL_DESCRIPTION);
                channel.setShowBadge(false);
                channel.enableLights(false);
                channel.setSound(null, null);
                mNm.createNotificationChannel(channel);
            }
        } catch(Exception e) {
            Alerts.toast("Creating notification channel: " + e.getMessage());
        }
    }

    public static void showServiceNotification(final Service service, final String msg, final PendingIntent pendingIntent) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                final Notification.Builder builder;
                builder = new Notification.Builder(service)
                        .setChannelId(CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification)
                        .setContentText(msg)
                        .setContentIntent(pendingIntent);
                service.startForeground(SERVICE_NOTIFICATION_ID, builder.build());
            } catch(Exception e) {
                Alerts.toast("Creating notification: " + e.getMessage());
            }
            Log.d(TAG, msg);
        }
    }

    public static void setAppColour(final int colour) {
        mAppColour = colour;
        final SharedPreferences prefs = mThis.getSharedPreferences(TAG, Activity.MODE_PRIVATE);
        prefs.edit().putInt(PREF_APPLICATION_COLOUR, mAppColour).apply();
    }
}
