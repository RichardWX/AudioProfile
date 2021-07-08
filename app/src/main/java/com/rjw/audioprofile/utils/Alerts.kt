package com.rjw.audioprofile.utils

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity

object Alerts {
    const val TAG = "AUDIOPROFILE"
    fun toast(message: Int) {
        toast(MainActivity.instance!!.getString(message))
    }

    fun toast(message: StringBuilder) {
        toast(message.toString())
    }

    fun toast(message: String?) {
        try {
            val view = LayoutInflater.from(MainActivity.instance!!).inflate(R.layout.toast, null)
            (view.findViewById<View>(R.id.text) as TextView).text = message
            DisplayUtils.colourControls(view)
            val toast = Toast.makeText(MainActivity.instance, "", Toast.LENGTH_SHORT)
            toast.view = view
            toast.show()
        } catch(e: Exception) {
            // Do nothing.
        }
    }

    fun alert(title: StringBuilder, message: StringBuilder) {
        alert(title.toString(), message.toString())
    }

    fun alert(title: String?, message: String?) {
        try {
            val builder = AlertDialog.Builder(MainActivity.instance)
            val view = LayoutInflater.from(MainActivity.instance).inflate(R.layout.alert, null)
            (view.findViewById<View>(R.id.title) as TextView).text = title
            (view.findViewById<View>(R.id.text) as TextView).text = message
            builder.setView(view)
            val dialog = builder.create()
            view.findViewById<View>(R.id.buttonOK).setOnClickListener { dialog.dismiss() }
            DisplayUtils.colourControls(view)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch(e: Exception) {
            // Do nothing.
        }
    }
}