package com.rjw.audioprofile.utils

import android.content.Context
import java.text.DateFormat
import java.util.Calendar

object Log {
    var instance: Context? = null
    private val logFilename = "entryLog"

    /**
     * Write an entry to the application log.
     * @param message   The text to be written to the log file.
     */
    fun log(message: StringBuilder) {
        log(message.toString())
    }

    /**
     * Write an entry to the application log.
     * @param message   The text to be written to the log file.
     * @param trace     True to include the calling function.
     */
    fun log(message: String?, trace: Boolean = false) {
        try {
            if(message != null) {
                instance?.let { instance ->
                    instance.openFileOutput(logFilename, Context.MODE_APPEND).apply {
                        val now = Calendar.getInstance()
                        val outputMessage = "${DateFormat.getDateInstance(DateFormat.MEDIUM).format(now.timeInMillis)} ${
                            DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now.timeInMillis)
                        } - $message${
                            if(trace) {
                                val stack = Throwable("").stackTrace[1]
                                " - called from ${stack.methodName} (${stack.fileName}:${stack.lineNumber})"
                            } else {
                                ""
                            }
                        }\n"
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
            instance?.openFileInput(logFilename)?.bufferedReader()?.useLines { lines ->
                lines.joinToString("\n")
            } ?: ""
        } catch(_: Exception) {
            ""
        }
    }

    /**
     * Clear the log file.
     */
    fun clearLog() {
        try {
            instance?.openFileOutput(logFilename, Context.MODE_PRIVATE)?.apply {
                flush()
                close()
            }
        } catch(_: Exception) {
            // Do nothing...
        }
    }
}