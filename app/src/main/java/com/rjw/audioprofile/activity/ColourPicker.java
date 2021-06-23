package com.rjw.audioprofile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.rjw.audioprofile.R;
import com.rjw.audioprofile.utils.DisplayUtils;
import com.rjw.audioprofile.utils.HSVColourWheel;
import com.rjw.audioprofile.utils.HSVValueSlider;
import com.rjw.audioprofile.utils.OnColourSelectedListener;

/**
 * Class representing the Colour picker dialog box.
 */
public class ColourPicker extends AudioActivity {
    private HSVColourWheel mColourWheel;
    private HSVValueSlider mValueSlider;
    private ImageView mSelectedColourView;
    private int mSelectedColour;

    /**
     * Create the view to display the entry.
     * @param savedInstanceState Previous information state.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setWindowRatios(0.8f, 0.6f);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colour_picker);
        setTitle(R.string.settings_app_colour);

        // Set up the colour wheel.
        mSelectedColour = getIntent().getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, 0);
        mColourWheel = findViewById(R.id.colourWheel);
        mColourWheel.setColour(mSelectedColour);
        mColourWheel.setBlackCursor(false);
        mColourWheel.setListener(new OnColourSelectedListener() {
            @Override
            public void colourSelected(final int colour) {
                if(mValueSlider != null) {
                    mValueSlider.setColour(colour, true);
                }
            }
        });

        // Set up the saturation slider.
        mValueSlider = findViewById(R.id.colourValue);
        mValueSlider.setColour(mSelectedColour, false);
        mValueSlider.setHorizontal(false);
        mValueSlider.setRadius(getResources().getDimension(R.dimen.border_radius));
        mValueSlider.setListener(new OnColourSelectedListener() {
            @Override
            public void colourSelected(final int colour) {
                mSelectedColour = colour;
                setColourView();
            }
        });
        mSelectedColourView = findViewById(R.id.colourView);
        setColourView();
        colourControls();
    }

    /**
     * Close the dialog box with a positive result.
     * @param v The view being clicked.
     */
    public void onClickOK(final View v) {
        final Intent intent = new Intent();
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, mSelectedColour);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Close the dialog box with a negative result.
     * @param v The view being clicked.
     */
    public void onClickCancel(final View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Get the selected colour.
     * @return The selected colour.
     */
    public int getColour() {
        return mSelectedColour;
    }

    /**
     * Update the colour of the image to reflect the user's choice.
     */
    private void setColourView() {
        if(mSelectedColourView != null) {
            // Update the colour control with the selected colour.
            mSelectedColourView.setBackgroundColor(DisplayUtils.lighten(mSelectedColour, DisplayUtils.COLOUR_LEVELS));
        }
    }
}
