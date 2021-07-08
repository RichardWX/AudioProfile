package com.rjw.audioprofile.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.DisplayUtils
import com.rjw.audioprofile.utils.HSVColourWheel
import com.rjw.audioprofile.utils.HSVValueSlider
import com.rjw.audioprofile.utils.OnColourSelectedListener

/**
 * Class representing the Colour picker dialog box.
 */
class ColourPicker : AudioActivity() {
    private lateinit var mValueSlider: HSVValueSlider
    private lateinit var mSelectedColourView: ImageView

    /**
     * Get the selected colour.
     * @return The selected colour.
     */
    var colour = 0
        private set

    /**
     * Create the view to display the entry.
     * @param savedInstanceState Previous information state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        setWindowRatios(0.8f, 0.6f)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colour_picker)
        setTitle(R.string.settings_app_colour)

        // Set up the colour wheel.
        colour = intent.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, 0)
        val colourWheel: HSVColourWheel = findViewById(R.id.colourWheel)
        colourWheel.setColour(colour)
        colourWheel.setBlackCursor(false)
        colourWheel.setListener(object : OnColourSelectedListener {
            override fun colourSelected(colour: Int) {
                mValueSlider.setColour(colour, true)
            }
        })

        // Set up the saturation slider.
        mValueSlider = findViewById(R.id.colourValue)
        mValueSlider.setColour(colour, false)
        mValueSlider.setHorizontal(false)
        mValueSlider.setRadius(resources.getDimension(R.dimen.border_radius))
        mValueSlider.setListener(object : OnColourSelectedListener {
            override fun colourSelected(colour: Int) {
                this@ColourPicker.colour = colour
                setColourView()
            }
        })
        mSelectedColourView = findViewById(R.id.colourView)
        setColourView()
        colourControls()
    }

    /**
     * Close the dialog box with a positive result.
     * @param v The view being clicked.
     */
    fun onClickOK(v: View?) {
        val intent = Intent()
        intent.putExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, colour)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Close the dialog box with a negative result.
     * @param v The view being clicked.
     */
    fun onClickCancel(v: View?) {
        setResult(RESULT_CANCELED)
        finish()
    }

    /**
     * Update the colour of the image to reflect the user's choice.
     */
    private fun setColourView() {
        // Update the colour control with the selected colour.
        mSelectedColourView.setBackgroundColor(DisplayUtils.lighten(colour, DisplayUtils.COLOUR_LEVELS))
    }
}