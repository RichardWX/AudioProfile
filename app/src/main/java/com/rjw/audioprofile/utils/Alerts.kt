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
        MainActivity.instance.let { activity ->
            toast(activity.getString(message))
        }
    }

    /**
     * Display a customised toast message.
     * @param message The message to be displayed.
     */
    fun toast(message: String) {
        try {
            bindingToast = ToastBinding.inflate(LayoutInflater.from(MainActivity.instance))
            bindingToast.text.text = message
            DisplayUtils.colourControls(bindingToast.root)
            val toast = Toast.makeText(MainActivity.instance, "", Toast.LENGTH_SHORT)
            toast.view = bindingToast.root
            toast.show()
        } catch(_: Exception) {
            // Do nothing.
        }
        log(message)
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
            bindingAlert = AlertBinding.inflate(LayoutInflater.from(MainActivity.instance))
            val bindingTitle = ContentTitleBinding.bind(bindingAlert.layoutTitle.root)
            val builder = AlertDialog.Builder(MainActivity.instance)
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
        } catch(_: Exception) {
            // Do nothing.
        }
        log("$title\n$message")
    }

    /**
     * Write an entry to the application log.
     * @param message   The text to be written to the log file.
     */
    fun log(message: StringBuilder) {
        log(message.toString())
    }

    private const val logFilename = "entryLog"
    /**
     * Write an entry to the application log.
     * @param message   The text to be written to the log file.
     */
    fun log(message: String?) {
        try {
            if(message != null) {
                MainActivity.instance.let { instance ->
                    instance.openFileOutput(logFilename, Context.MODE_APPEND).apply {
                        val now = Calendar.getInstance()
                        val outputMessage = "${DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now.timeInMillis)} - $message\n"
                        write(outputMessage.toByteArray())
                        flush()
                        close()
                    }
                }
            }
        } catch(_: Exception) {
            // We can't write to the file - carry on regardless.
        }
    }

    /**
     * Read the log file.
     * @return The contents of the log file.
     */
    fun readLog(): String {
        return try {
            MainActivity.instance.openFileInput(logFilename).bufferedReader().useLines { lines ->
                lines.joinToString("\n")
            }
        } catch(_: Exception) {
            ""
        }
    }

    /**
     * Clear the log file.
     */
    fun clearLog() {
        try {
            MainActivity.instance.openFileOutput(logFilename, Context.MODE_PRIVATE).apply {
                flush()
                close()
            }
        } catch(_: Exception) {
            // Do nothing...
        }
    }
}