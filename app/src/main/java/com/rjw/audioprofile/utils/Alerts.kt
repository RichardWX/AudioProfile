package com.rjw.audioprofile.utils

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.databinding.AlertBinding
import com.rjw.audioprofile.databinding.ContentTitleBinding
import com.rjw.audioprofile.databinding.ToastBinding

object Alerts {
    private lateinit var bindingToast: ToastBinding
    private lateinit var bindingAlert: AlertBinding


    /**
     * Display a customised toast message.
     * @param message The message id to be displayed.
     */
    fun toast(message: Int) {
        toast(MainActivity.instance!!.getString(message))
    }

    /**
     * Display a customised toast message.
     * @param message The message to be displayed.
     */
    fun toast(message: String) {
        try {
            bindingToast = ToastBinding.inflate(LayoutInflater.from(MainActivity.instance!!))
            bindingToast.text.text = message
            DisplayUtils.colourControls(bindingToast.root)
            val toast = Toast.makeText(MainActivity.instance, "", Toast.LENGTH_SHORT)
            toast.view = bindingToast.root
            toast.show()
        } catch(e: Exception) {
            // Do nothing.
        }
        Log.d(MainActivity.TAG, message)
    }

    /**
     * Display a customised alert message.
     * @param title   The alert title.
     * @param message The message to be displayed.
     */
    fun alert(title: StringBuilder, message: StringBuilder) {
        alert(title.toString(), message.toString())
    }

    /**
     * Display a customised alert message.
     * @param title   The alert title.
     * @param message The message to be displayed.
     */
    private fun alert(title: String, message: String) {
        try {
            bindingAlert = AlertBinding.inflate(LayoutInflater.from(MainActivity.instance!!))
            val bindingTitle = ContentTitleBinding.bind(bindingAlert.layoutTitle.root)
            val builder = AlertDialog.Builder(MainActivity.instance)
            bindingTitle.title.text = title
            bindingAlert.text.text = message
            builder.setView(bindingAlert.root)
            val dialog = builder.create()
            bindingAlert.buttonOK.setOnClickListener { dialog.dismiss() }
            DisplayUtils.colourControls(bindingAlert.root)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        } catch(e: Exception) {
            // Do nothing.
        }
        Log.d(MainActivity.TAG, "$title\n$message")
    }
}