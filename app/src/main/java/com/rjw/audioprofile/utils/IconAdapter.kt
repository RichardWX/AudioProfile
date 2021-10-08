package com.rjw.audioprofile.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.RowIconBinding

class IconAdapter constructor(private val mContext: Context, objects: Array<Drawable?>?) : ArrayAdapter<Drawable?>(
    mContext, R.layout.row_icon, objects!!
) {
    private lateinit var bindingRow: RowIconBinding
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_icon, parent, false)
        }
        bindingRow = RowIconBinding.bind(row!!)
        try {
            bindingRow.icon.foreground = getItem(position)
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        return row
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_icon, parent, false)
        }
        bindingRow = RowIconBinding.bind(row!!)
        try {
            bindingRow.icon.foreground = getItem(position)
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        return row
    }
}