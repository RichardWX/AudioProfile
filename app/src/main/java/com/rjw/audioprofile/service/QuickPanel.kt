package com.rjw.audioprofile.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.rjw.audioprofile.utils.AudioProfileList
import java.lang.Exception

class QuickPanel : TileService() {
    override fun onCreate() {
        super.onCreate()
        setIcon()
    }

    override fun onClick() {
        super.onClick()
        var nextProfile = AudioProfileList.currentProfile + 1
        if(nextProfile >= AudioProfileList.NO_PROFILES) {
            nextProfile = 0
        }
        AudioProfileList.currentProfile = nextProfile
        setIcon()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        AudioProfileList.initialise(baseContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        AudioProfileList.initialise(baseContext)
        setIcon()
    }

    private fun setIcon() {
        val tile = qsTile
        if(tile != null) {
            try {
                val profile = AudioProfileList.currentProfile
                val audioProfile = AudioProfileList.getProfile(profile)
                tile.state = Tile.STATE_ACTIVE
                tile.icon = Icon.createWithResource(applicationContext, AudioProfileList.getIconResource(audioProfile.icon))
                tile.label = audioProfile.name
                tile.updateTile()
                AudioProfileList.applyProfile(applicationContext)
            } catch(e: Exception) {
                // Do nothing - no profiles found.
            }
        }
    }
}