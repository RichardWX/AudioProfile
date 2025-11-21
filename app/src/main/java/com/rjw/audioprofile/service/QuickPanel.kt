package com.rjw.audioprofile.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.AudioProfileList
import java.lang.Exception

class QuickPanel : TileService() {
    /**
     * Create the quick panel item.
     */
    override fun onCreate() {
        super.onCreate()
        setIcon()
    }

    /**
     * Handle the user clicking on the quick panel icon.
     */
    override fun onClick() {
        super.onClick()
        var nextProfile = AudioProfileList.currentProfile + 1
        if(nextProfile >= AudioProfileList.noProfiles) {
            nextProfile = 0
        }
        AudioProfileList.currentProfile = nextProfile
        AudioProfileList.profileLocked = false
        setIcon()
    }

    /**
     * Initialise the newly added tile.
     */
    override fun onTileAdded() {
        super.onTileAdded()
        AudioProfileList.initialise(baseContext)
    }

    /**
     * Start listening for user events.
     */
    override fun onStartListening() {
        super.onStartListening()
        AudioProfileList.initialise(baseContext)
        setIcon()
    }

    /**
     * Set the icon for the quick panel tile.
     */
    private fun setIcon() {
        val tile = qsTile
        if(tile != null) {
            try {
                val profile = AudioProfileList.currentProfile
                val audioProfile = AudioProfileList.getProfile(profile)
                tile.state = Tile.STATE_ACTIVE
                tile.icon = Icon.createWithResource(applicationContext, AudioProfileList.getIconResource(audioProfile.icon))
                tile.label = "${audioProfile.name} ${if(AudioProfileList.profileLocked) "\r${getString(R.string.locked)}" else ""}"
                tile.updateTile()
                AudioProfileList.applyProfile(applicationContext)
            } catch(_: Exception) {
                // Do nothing - no profiles found.
            }
        }
    }
}