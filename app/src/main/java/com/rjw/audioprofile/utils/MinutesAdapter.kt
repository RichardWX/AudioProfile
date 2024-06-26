package com.rjw.audioprofile.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.RowSimpleBinding
import java.lang.Exception

class MinutesAdapter(private val mContext: Context, objects: Array<Int>) : ArrayAdapter<Int>(
    mContext, R.layout.row_simple, objects
) {
    private lateinit var bindingRow: RowSimpleBinding

    /**
     * Get the view for the specified position.
     * @param position    The position of the item in the list.
     * @param convertView The row view to reuse if possible.
     * @param parent      The parent of the row.
     * @return            The formatted and populated view.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.row_simple, parent, false)
        bindingRow = RowSimpleBinding.bind(row)
        try {
            val value = getItem(position) as Int
            bindingRow.value.text = value.toString()
            bindingRow.minutes.text = mContext.resources.getQuantityText(R.plurals.minutes, value)
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
        val row = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.row_simple, parent, false)
        bindingRow = RowSimpleBinding.bind(row)
        try {
            val value = getItem(position) as Int
            bindingRow.value.text = value.toString()
            bindingRow.minutes.text = mContext.resources.getQuantityText(R.plurals.minutes, value)
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        DisplayUtils.colourControls(row)
        return row
    }
}