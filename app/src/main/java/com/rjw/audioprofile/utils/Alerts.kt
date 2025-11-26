@file:Suppress("DEPRECATION")

package com.rjw.audioprofile.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.rjw.audioprofile.activity.MainActivity
import com.rjw.audioprofile.databinding.AlertBinding
import com.rjw.audioprofile.databinding.ContentTitleBinding
import com.rjw.audioprofile.databinding.ToastBinding
import java.text.DateFormat
import java.util.Calendar

object Alerts {
    private lateinit var bindingToast: ToastBinding
    private lateinit var bindingAlert: AlertBinding

    /**
     * Display a customised toast message.
     * @param message The message id to be displayed.
     */
    fun toast(message: Int) {
        MainActivity.instance?.let { activity ->
            toast(activity.getString(message))
        }
    }

    /**
     * Display a customised toast message.
     * @param message The message to be displayed.
     */
    fun toast(message: String) {
        try {
            MainActivity.instance?.let { activity ->
                bindingToast = ToastBinding.inflate(LayoutInflater.from(activity))
                bindingToast.text.text = message
                DisplayUtils.colourControls(bindingToast.root)
                val toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT)
                toast.view = bindingToast.root
                toast.show()
            }
        } catch(_: Exception) {
            // Do nothing.
        }
        Log.log(message)
    }

    /**
     * Display a customised alert message.
     * @param title           The alert title.
     * @param message         The message to be displayed.
     * @param onClickHandler  The handler for the button.
     */
    fun alert(title: StringBuilder, message: StringBuilder, onClickHandler: (() -> Unit)? = null) {
        alert(title.toString(), message.toString(), onClickHandler)
    }

    /**
     * Display a customised alert message.
     * @param title           The alert title.
     * @param message         The message to be displayed.
     * @param onClickHandler  The handler for the button.
     */
    fun alert(title: String, message: String, onClickHandler: (() -> Unit)? = null) {
        try {
            MainActivity.instance?.let { activity ->
                bindingAlert = AlertBinding.inflate(LayoutInflater.from(activity))
                val bindingTitle = ContentTitleBinding.bind(bindingAlert.layoutTitle.root)
                val builder = AlertDialog.Builder(activity)
                bindingTitle.title.text = title
                bindingAlert.text.text = message
                builder.setView(bindingAlert.root)
                val dialog = builder.create()
                bindingAlert.buttonOK.setOnClickListener {
                    onClickHandler?.invoke()
                    dialog.dismiss()
                }
                DisplayUtils.colourControls(bindingAlert.root)
                dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                dialog.setCanceledOnTouchOutside(false)
                dialog.show()
            }
        } catch(_: Exception) {
            // Do nothing.
        }
        Log.log("$title\n$message")
    }
}