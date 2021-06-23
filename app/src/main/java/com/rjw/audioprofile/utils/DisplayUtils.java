package com.rjw.audioprofile.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.rjw.audioprofile.activity.MainActivity;
import com.rjw.audioprofile.R;

public class DisplayUtils {
    public static final String EXTRA_CUSTOM_COLOUR = "CustomColour";
    public static int COLOUR_LEVELS = 6;

    public static void colourControls(final View v) {
        colourControls(v, MainActivity.getConfigColour());
    }

    public static void colourControls(final View v, final int colour) {
        if(v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup)v;
            for(int child = 0; child < vg.getChildCount(); child++) {
                colourControls(vg.getChildAt(child), colour);
            }
        }
        if(v.getId() == R.id.layoutTitle) {
            final Drawable background = v.getBackground();
            if(background != null) {
                background.setColorFilter(colour, PorterDuff.Mode.SRC_ATOP);
                v.setBackground(background);
            }
        } else if(v instanceof ImageView && v.getId() != R.id.icon) {
            final ImageView iv = (ImageView)v;
            iv.setColorFilter(colour, PorterDuff.Mode.SRC_ATOP);
        } else if(v instanceof TextView) {
            final TextView tv = (TextView)v;
            if(v instanceof Button && !(v instanceof CompoundButton)) {
                final Button b = (Button)v;
                final Drawable background = b.getBackground();
                if(background != null) {
                    background.setColorFilter(colour, PorterDuff.Mode.SRC_ATOP);
                }
                if(isDark(colour)) {
                    b.setTextColor(MainActivity.getWhiteColour());
                } else {
                    tv.setTextColor(darken(colour, COLOUR_LEVELS));
                }
            } else if(tv.getId() == R.id.title) {
                final Resources resources = MainActivity.getInstance().getResources();
                tv.setShadowLayer(resources.getDimension(R.dimen.shadow_radius), resources.getDimension(R.dimen.shadow_offset),
                        resources.getDimension(R.dimen.shadow_offset), darken(colour, COLOUR_LEVELS));
            } else {
                tv.setTextColor(darken(colour, COLOUR_LEVELS));
            }
        }
    }

    public static int darken(final int colour) {
        final float[] hsv = new float[3];
        Color.colorToHSV(colour, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }

    public static int darken(final int colour, final int levels) {
        int newColour = colour;
        for(int level = 0; level < levels; level++) {
            newColour = darken(newColour);
        }
        return newColour;
    }

    public static int lighten(final int colour) {
        final float[] hsv = new float[3];
        Color.colorToHSV(colour, hsv);
        hsv[2] = 0.1f + 0.9f * hsv[2];
        return Color.HSVToColor(hsv);
    }

    public static int lighten(final int colour, final int levels) {
        int newColour = colour;
        for(int level = 0; level < levels; level++) {
            newColour = lighten(newColour);
        }
        return newColour;
    }

    public static boolean isDark(final int colour) {
        return ColorUtils.calculateLuminance(colour) < 0.2;
    }
}
