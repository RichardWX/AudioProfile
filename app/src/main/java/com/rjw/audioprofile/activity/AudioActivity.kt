package com.rjw.audioprofile.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.widget.TextView
import com.rjw.audioprofile.R
import com.rjw.audioprofile.utils.DisplayUtils

open class AudioActivity : Activity() {
    protected val mWindowRatio = floatArrayOf(0.8f, 0.7f)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(R.drawable.rounded_background)
        setFinishOnTouchOutside(false)
        setTheme(R.style.AppTheme_Dialog)
    }

    override fun setContentView(layout: Int) {
        super.setContentView(layout)

        // Set the window to be the right size.
        val vto = window.decorView.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Set the window size so it fits the screen whatever device it is running on.
                var changed = false
                val dm = resources.displayMetrics
                val lp = window.attributes
                if(window.decorView.width < (dm.widthPixels * mWindowRatio[0]).toInt()) {
                    lp.width = (dm.widthPixels * mWindowRatio[0]).toInt()
                    changed = true
                }
                if(window.decorView.height < (dm.heightPixels * mWindowRatio[1]).toInt()) {
                    lp.height = (dm.heightPixels * mWindowRatio[1]).toInt()
                    changed = true
                }
                if(changed) {
                    window.attributes = lp
                }
                val vto = window.decorView.viewTreeObserver
                vto.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun setTitle(title: CharSequence) {
        (findViewById<View>(R.id.title) as TextView).text = title
    }

    override fun setTitle(titleId: Int) {
        (findViewById<View>(R.id.title) as TextView).setText(titleId)
    }

    protected fun setWindowRatios(xRatio: Float, yRatio: Float) {
        mWindowRatio[0] = xRatio
        mWindowRatio[1] = yRatio
    }

    protected fun colourControls(colour: Int = MainActivity.configColour) {
        DisplayUtils.colourControls(window.decorView, colour)
    }
}