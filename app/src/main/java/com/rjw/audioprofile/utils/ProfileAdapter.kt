package com.rjw.audioprofile.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.AudioProfileList.AudioProfile

class ProfileAdapter @JvmOverloads constructor(private val mContext: Context, objects: Array<AudioProfile?>?, associatedView: View? = null) : ArrayAdapter<AudioProfile?>(
    mContext, R.layout.row_profile, objects!!
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_profile, parent, false)
        }
        try {
            val profile = getItem(position)
            row!!.findViewById<View>(R.id.icon).foreground = AudioProfileList.getIcon(profile!!.icon)
            (row.findViewById<View>(R.id.profile) as TextView).text = profile.name
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        DisplayUtils.colourControls(row)
        return row!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_profile, parent, false)
        }
        try {
            val profile = getItem(position)
            row!!.findViewById<View>(R.id.icon).foreground = AudioProfileList.Companion.getIcon(profile!!.icon)
            (row!!.findViewById<View>(R.id.profile) as TextView).text = profile.name
        } catch(e: Exception) {
            // Do nothing.
            e.printStackTrace()
        }
        DisplayUtils.colourControls(row)
        return row!!
    }
}