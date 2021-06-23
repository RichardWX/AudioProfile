package com.rjw.audioprofile.service;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.rjw.audioprofile.utils.AudioProfileList;

public class QuickPanel extends TileService {
    @Override
    public void onCreate() {
        super.onCreate();
        setIcon();
    }

    @Override
    public void onClick() {
        super.onClick();
        int nextProfile = AudioProfileList.getCurrentProfile() + 1;
        if(nextProfile >= AudioProfileList.NO_PROFILES) {
            nextProfile = 0;
        }
        AudioProfileList.setCurrentProfile(nextProfile);
        setIcon();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        AudioProfileList.initialise(getBaseContext());
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        AudioProfileList.initialise(getBaseContext());
        setIcon();
    }

    public void setIcon() {
        final Tile tile = getQsTile();
        if(tile != null) {
            try {
                final int profile = AudioProfileList.getCurrentProfile();
                final AudioProfileList.AudioProfile audioProfile = AudioProfileList.getProfile(profile);
                tile.setState(Tile.STATE_ACTIVE);
                tile.setIcon(Icon.createWithResource(getApplicationContext(), AudioProfileList.getIconResource(audioProfile.icon)));
                tile.setLabel(audioProfile.name);
                tile.updateTile();
                AudioProfileList.applyProfile(getApplicationContext());
            } catch (Exception e) {
                // Do nothing - no profiles found.
            }
        }
    }
}
