package com.rjw.audioprofile.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import com.rjw.audioprofile.R
import com.rjw.audioprofile.databinding.ContentTitleBinding
import com.rjw.audioprofile.utils.DisplayUtils

open class AudioActivity : Activity() {
    private lateinit var bindingTitle: ContentTitleBinding
    protected var view: View? = null
    protected var mWindowRatio = floatArrayOf(0.8f, 0.8f)

    /**
     * Create the activity.
     * @param savedInstanceState The state information of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(R.drawable.rounded_background)
        setFinishOnTouchOutside(false)
        setTheme(R.style.AppTheme_Dialog)
    }

    /**
     * Set the content of the activity and resize the window if necessary.
     * @param layout The id of the layout to use in the activity.
     */
    override fun setContentView(layout: Int) {
        view = layoutInflater.inflate(layout, null, false)
        setContentView(view)
        bindingTitle = ContentTitleBinding.bind(view!!)

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

    /**
     * Set the window title.
     * @param title The new window title.
     */
    override fun setTitle(title: CharSequence) {
        bindingTitle.title.text = title
    }

    /**
     * Set the window title.
     * @param title The new window title.
     */
    override fun setTitle(titleId: Int) {
        bindingTitle.title.setText(titleId)
    }

    /**
     * Set the window ratios to the screen.
     * @param xRatio The x ratio of the window compared to the screen.
     * @param yRatio The y ratio of the window compared to the screen.
     */
    protected fun setWindowRatios(xRatio: Float, yRatio: Float) {
        mWindowRatio[0] = xRatio
        mWindowRatio[1] = yRatio
    }

    /**
     * Colour the window controls.
     * @param colour The colour to use for the controls.
     */
    protected fun colourControls(colour: Int = getColor(R.color.colourConfig)) {
        DisplayUtils.colourControls(window.decorView, colour)
    }
}