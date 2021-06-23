package com.rjw.audioprofile.utils;

/**
 * Interface to handle selecting a colour from the colour wheel.
 */
public interface OnColourSelectedListener {
    /**
     * Select a new colour.
     * @param colour The selected colour.
     */
    void colourSelected(final int colour);
}
