package com.rjw.audioprofile.utils

/**
 * Interface to handle selecting a colour from the colour wheel.
 */
interface OnColourSelectedListener {
    /**
     * Select a new colour.
     * @param colour The selected colour.
     */
    fun colourSelected(colour: Int)
}