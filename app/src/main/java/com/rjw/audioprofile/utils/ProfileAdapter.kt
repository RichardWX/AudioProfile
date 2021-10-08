package com.rjw.audioprofile.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.RowProfileBinding
import com.rjw.audioprofile.utils.AudioProfileList.AudioProfile

class ProfileAdapter constructor(private val mContext: Context, objects: Array<AudioProfile?>?) : ArrayAdapter<AudioProfile?>(
    mContext, R.layout.row_profile, objects!!
) {
    private lateinit var bindingRow: RowProfileBinding
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.row_profile, parent, false)
        bindingRow = RowProfileBinding.bind(row)
        try {
            val profile = getItem(position)
            bindingRow.icon.foreground = AudioProfileList.getIcon(profile!!.icon)
            bindingRow.profile.text = profile.name
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        DisplayUtils.colourControls(row)
        return row
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView ?: LayoutInflater.from(mContext).inflate(R.layout.row_profile, parent, false)
        bindingRow = RowProfileBinding.bind(row)
        try {
            val profile = getItem(position)
            bindingRow.icon.foreground = AudioProfileList.getIcon(profile!!.icon)
            bindingRow.profile.text = profile.name
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        DisplayUtils.colourControls(row)
        return row
    }
}