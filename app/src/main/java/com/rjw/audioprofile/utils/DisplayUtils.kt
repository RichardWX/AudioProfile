package com.rjw.audioprofile.utils

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity.Companion.configColour
import com.rjw.audioprofile.activity.MainActivity.Companion.instance
import com.rjw.audioprofile.activity.MainActivity.Companion.whiteColour

object DisplayUtils {
    const val EXTRA_CUSTOM_COLOUR = "CustomColour"
    private const val COLOUR_LEVELS = 6

    /**
     * Colour the view controls.
     * @param v      The view to be coloured.
     * @param colour The base colour for the view.
     */
    fun colourControls(v: View?, colour: Int = configColour) {
        if(v == null) {
            return
        }
        if(v is ViewGroup) {
            for(child in 0 until v.childCount) {
                colourControls(v.getChildAt(child), colour)
            }
        }
        if(v.id == R.id.layoutTitle) {
            val background = v.background
            if(background != null) {
                background.setColorFilter(colour, Mode.SRC_ATOP)
                v.background = background
            }
        } else if(v is ImageView && v.getId() != R.id.icon) {
            v.setColorFilter(colour, PorterDuff.Mode.SRC_ATOP)
        } else if(v is TextView) {
            if(v is Button && v !is CompoundButton) {
                val background = v.background
                background?.setColorFilter(colour, Mode.SRC_ATOP)
                if(isDark(colour)) {
                    v.setTextColor(whiteColour)
                } else {
                    v.setTextColor(darken(colour, COLOUR_LEVELS))
                }
            } else if(v.id == R.id.title) {
                val resources = instance!!.resources
                v.setShadowLayer(
                    resources.getDimension(R.dimen.shadow_radius), resources.getDimension(R.dimen.shadow_offset),
                    resources.getDimension(R.dimen.shadow_offset), darken(colour, COLOUR_LEVELS)
                )
            } else {
                v.setTextColor(darken(colour, COLOUR_LEVELS))
            }
        }
    }

    /**
     * Darken the specified colour.
     * @param colour The colour to be darkened.
     * @return       The new darkened colour.
     */
    private fun darken(colour: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(colour, hsv)
        hsv[2] *= 0.9f
        return Color.HSVToColor(hsv)
    }

    /**
     * Darken the specified colour.
     * @param colour The colour to be darken.
     * @param levels The number of iterations to darkened by.
     * @return       The new darkened colour.
     */
    private fun darken(colour: Int, levels: Int): Int {
        var newColour = colour
        for(level in 0 until levels) {
            newColour = darken(newColour)
        }
        return newColour
    }

    /**
     * Lighten the specified colour.
     * @param colour The colour to be lightened.
     * @return       The new lightened colour.
     */
    private fun lighten(colour: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(colour, hsv)
        hsv[2] = 0.1f + 0.9f * hsv[2]
        return Color.HSVToColor(hsv)
    }

    /**
     * Lighten the specified colour.
     * @param colour The colour to be lightened.
     * @param levels The number of iterations to lighten by.
     * @return       The new lightened colour.
     */
    fun lighten(colour: Int, levels: Int): Int {
        var newColour = colour
        for(level in 0 until levels) {
            newColour = lighten(newColour)
        }
        return newColour
    }

    /**
     * Return whether the colour is dark or not.
     * @return True if the colour is determined as dark, otherwise false.
     */
    private fun isDark(colour: Int): Boolean {
        return ColorUtils.calculateLuminance(colour) < 0.2
    }
}

/**
 * Set the drawable colour filter (actual function is deprecated).
 * @param colour The colour to be applied.
 * @param mode   The mode for the filter.
 */
fun Drawable.setColorFilter(colour: Int, mode: Mode = Mode.SRC_ATOP) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = BlendModeColorFilter(colour, mode.getBlendMode())
    } else {
        @Suppress("DEPRECATION")
        setColorFilter(colour, mode.getPorterDuffMode())
    }
}

// This class is needed to call the setColorFilter with different BlendMode on older API (before 29).
enum class Mode {
    CLEAR,
    SRC,
    DST,
    SRC_OVER,
    DST_OVER,
    SRC_IN,
    DST_IN,
    SRC_OUT,
    DST_OUT,
    SRC_ATOP,
    DST_ATOP,
    XOR,
    DARKEN,
    LIGHTEN,
    MULTIPLY,
    SCREEN,
    ADD,
    OVERLAY;

    /**
     * Return the porter duff mode when using Android Q or above.
     * @return The corresponding porter duff mode.
     */
    fun getPorterDuffMode(): PorterDuff.Mode {
        return when(this) {
            CLEAR -> PorterDuff.Mode.CLEAR
            SRC -> PorterDuff.Mode.SRC
            DST -> PorterDuff.Mode.DST
            SRC_OVER -> PorterDuff.Mode.SRC_OVER
            DST_OVER -> PorterDuff.Mode.DST_OVER
            SRC_IN -> PorterDuff.Mode.SRC_IN
            DST_IN -> PorterDuff.Mode.DST_IN
            SRC_OUT -> PorterDuff.Mode.SRC_OUT
            DST_OUT -> PorterDuff.Mode.DST_OUT
            SRC_ATOP -> PorterDuff.Mode.SRC_ATOP
            DST_ATOP -> PorterDuff.Mode.DST_ATOP
            XOR -> PorterDuff.Mode.XOR
            DARKEN -> PorterDuff.Mode.DARKEN
            LIGHTEN -> PorterDuff.Mode.LIGHTEN
            MULTIPLY -> PorterDuff.Mode.MULTIPLY
            SCREEN -> PorterDuff.Mode.SCREEN
            ADD -> PorterDuff.Mode.ADD
            OVERLAY -> PorterDuff.Mode.OVERLAY
        }
    }

    /**
     * Return the blend mode when using Android Q or above.
     * @return The corresponding blend mode.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getBlendMode(): BlendMode {
        return when(this) {
            CLEAR -> BlendMode.CLEAR
            SRC -> BlendMode.SRC
            DST -> BlendMode.DST
            SRC_OVER -> BlendMode.SRC_OVER
            DST_OVER -> BlendMode.DST_OVER
            SRC_IN -> BlendMode.SRC_IN
            DST_IN -> BlendMode.DST_IN
            SRC_OUT -> BlendMode.SRC_OUT
            DST_OUT -> BlendMode.DST_OUT
            SRC_ATOP -> BlendMode.SRC_ATOP
            DST_ATOP -> BlendMode.DST_ATOP
            XOR -> BlendMode.XOR
            DARKEN -> BlendMode.DARKEN
            LIGHTEN -> BlendMode.LIGHTEN
            MULTIPLY -> BlendMode.MULTIPLY
            SCREEN -> BlendMode.SCREEN
            ADD -> BlendMode.PLUS
            OVERLAY -> BlendMode.OVERLAY
        }
    }
}