package com.rjw.audioprofile.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ActivityColourPickerBinding
import com.rjw.audioprofile.utils.DisplayUtils
import com.rjw.audioprofile.utils.OnColourSelectedListener

/**
 * Class representing the Colour picker dialog box.
 */
class ColourPicker : AudioActivity() {
    private lateinit var binding: ActivityColourPickerBinding

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
        binding = ActivityColourPickerBinding.bind(view!!)
        setTitle(R.string.settings_app_colour)

        // Set up the colour wheel.
        colour = intent.getIntExtra(DisplayUtils.EXTRA_CUSTOM_COLOUR, 0)
        binding.colourWheel.setColour(colour)
        binding.colourWheel.setBlackCursor(false)
        binding.colourWheel.setListener(object : OnColourSelectedListener {
            override fun colourSelected(colour: Int) {
                binding.colourValue.setColour(colour, true)
            }
        })

        // Set up the saturation slider.
        binding.colourValue.setColour(colour, false)
        binding.colourValue.setHorizontal(false)
        binding.colourValue.setRadius(resources.getDimension(R.dimen.border_radius))
        binding.colourValue.setListener(object : OnColourSelectedListener {
            override fun colourSelected(colour: Int) {
                this@ColourPicker.colour = colour
                setColourView()
            }
        })
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
        binding.colourView.setBackgroundColor(DisplayUtils.lighten(colour, DisplayUtils.COLOUR_LEVELS))
    }
}