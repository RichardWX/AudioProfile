package com.rjw.audioprofile.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.databinding.RowProfileBinding
import com.rjw.audioprofile.utils.AudioProfileList.AudioProfile

class ProfileAdapter(context: Context, objects: Array<AudioProfile?>) : ArrayAdapter<AudioProfile?>(
    context, R.layout.row_profile, objects
) {
    private lateinit var bindingRow: RowProfileBinding

    /**
     * Get the view for the specified position.
     * @param position    The position of the item in the list.
     * @param convertView The row view to reuse if possible.
     * @param parent      The parent of the row.
     * @return            The formatted and populated view.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(context).inflate(R.layout.row_profile, parent, false)
        bindingRow = RowProfileBinding.bind(row)
        try {
            getItem(position)?.let { profile ->
                bindingRow.icon.foreground = AudioProfileList.getIcon(profile.icon)
                bindingRow.icon.foreground.setColorFilter(MainActivity.configColour)
                bindingRow.profile.text = profile.name
            }
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
        val row = convertView ?: LayoutInflater.from(context).inflate(R.layout.row_profile, parent, false)
        bindingRow = RowProfileBinding.bind(row)
        try {
            getItem(position)?.let { profile ->
                bindingRow.icon.foreground = AudioProfileList.getIcon(profile.icon)
                bindingRow.icon.foreground.setColorFilter(MainActivity.configColour)
                bindingRow.profile.text = profile.name
            }
        } catch(e: Exception) {
            // Do nothing.
        }
        DisplayUtils.colourControls(row)
        return row
    }
}