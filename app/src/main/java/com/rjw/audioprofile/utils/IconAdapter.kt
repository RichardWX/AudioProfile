package com.rjw.audioprofile.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.databinding.RowIconBinding

class IconAdapter(private val mContext: Context, objects: Array<Drawable?>) : ArrayAdapter<Drawable?>(
    mContext, R.layout.row_icon, objects
) {
    private lateinit var bindingRow: RowIconBinding

    /**
     * Get the view for the specified position.
     * @param position    The position of the item in the list.
     * @param convertView The row view to reuse if possible.
     * @param parent      The parent of the row.
     * @return            The formatted and populated view.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.row_icon, parent, false)
        bindingRow = RowIconBinding.bind(row)
        try {
            bindingRow.icon.foreground = getItem(position)
            bindingRow.icon.foreground.setColorFilter(MainActivity.configColour)
        } catch(e: Exception) {
            // Do nothing.
        }
        DisplayUtils.colourControls(row)
        return row
    }

    /**
     * Get the dropdown view for the specified position.
     * @param position    The position of the item in the list.
     * @param convertView The row view to reuse if possible.
     * @param parent      The parent of the row.
     * @return            The formatted and populated view.
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.row_icon, parent, false)
        bindingRow = RowIconBinding.bind(row)
        try {
            bindingRow.icon.foreground = getItem(position)
            bindingRow.icon.foreground.setColorFilter(MainActivity.configColour)
        } catch(e: Exception) {
            // Do nothing.
        }
        DisplayUtils.colourControls(row)
        return row
    }
}