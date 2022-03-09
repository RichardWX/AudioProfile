package com.rjw.audioprofile.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class HSVValueSlider
/**
 * Class constructor.
 * @param context The application context.
 * @param attrs   The control attributes.
 */
@JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {
    private var mListener: OnColourSelectedListener? = null
    private val mColourHsv = floatArrayOf(0f, 0f, 1f)
    private var mSrcRect: Rect? = null
    private var mDstRect: Rect? = null
    private var mBitmap: Bitmap? = null
    private var mPixels: IntArray? = null
    private var mRadius = 0f
    private var mHorizontal = true
    private val mClipPath = Path()
    private val mClipRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

    /**
     * Set the listener for a new colour being selected.
     * @param listener The new listener.
     */
    fun setListener(listener: OnColourSelectedListener?) {
        mListener = listener
    }

    /**
     * Set the alignment of the control.
     * @param horizontal True if the control is aligned horizontally, false if vertically.
     */
    fun setHorizontal(horizontal: Boolean) {
        mHorizontal = horizontal
        invalidate()
    }

    /**
     * Set the corner radius of the control.
     * @param radius The radius size (0 for none).
     */
    fun setRadius(radius: Float) {
        mRadius = radius
        invalidate()
    }

    /**
     * Set the basic colour for the control.
     * @param colour    The new colour to be controlled.
     * @param keepValue True if the value should be stored, false if not.
     */
    fun setColour(colour: Int, keepValue: Boolean) {
        val oldValue = mColourHsv[2]
        Color.colorToHSV(colour, mColourHsv)
        if(keepValue) {
            mColourHsv[2] = oldValue
        }
        mListener?.colourSelected(Color.HSVToColor(mColourHsv))
        createBitmap()
    }

    /**
     * Draw the control.
     * @param canvas The canvas to be drawn onto.
     */
    override fun onDraw(canvas: Canvas?) {
        if(canvas != null && mBitmap != null) {
            mClipPath.reset()
            mClipRect.set(0f, 0f, width.toFloat(), height.toFloat())
            mClipPath.addRoundRect(mClipRect, mRadius, mRadius, Path.Direction.CW)
            canvas.clipPath(mClipPath)
            canvas.drawBitmap(mBitmap!!, mSrcRect, mDstRect!!, null)
        }
    }

    /**
     * Update the control after resizing.
     * @param width      The new width.
     * @param height     The new height.
     * @param prevWidth  The previous width.
     * @param prevHeight The previous height.
     */
    override fun onSizeChanged(width: Int, height: Int, prevWidth: Int, prevHeight: Int) {
        super.onSizeChanged(width, height, prevWidth, prevHeight)
        mDstRect = Rect(0, 0, width, height)
        if(mHorizontal) {
            mSrcRect = Rect(0, 0, width, 1)
            mBitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888)
            mPixels = IntArray(width)
        } else {
            mSrcRect = Rect(0, 0, 1, height)
            mBitmap = Bitmap.createBitmap(1, height, Bitmap.Config.ARGB_8888)
            mPixels = IntArray(height)
        }
        createBitmap()
    }

    /**
     * Create a colour bitmap for the control.
     */
    private fun createBitmap() {
        if(mBitmap != null && mPixels != null) {
            val size: Int = if(mHorizontal) {
                width
            } else {
                height
            }
            val selectedPos = (mColourHsv[2] * size).toInt()
            val hsv = floatArrayOf(mColourHsv[0], mColourHsv[1], 1f)
            var value = 0f
            val valueStep = 1f / size
            for(pos in 0 until size) {
                value += valueStep
                if(pos >= selectedPos - 1 && pos <= selectedPos + 1) {
                    val intVal = 0xFF - (value * 0xFF).toInt()
                    val colour = intVal * 0x010101 + -0x1000000
                    mPixels!![pos] = colour
                } else {
                    hsv[2] = value
                    mPixels!![pos] = Color.HSVToColor(hsv)
                }
            }
            if(mHorizontal) {
                mBitmap!!.setPixels(mPixels, 0, size, 0, 0, size, 1)
            } else {
                mBitmap!!.setPixels(mPixels, 0, 1, 0, 0, 1, size)
            }
            invalidate()
        }
    }

    /**
     * Deal with touch events and invalidate the control when necessary.
     * @param event The event to be handled.
     * @return      True if the event was handled, false if not.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event != null) {
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val value: Float = if(mHorizontal) {
                        val x = max(0, min(mBitmap!!.width - 1, event.x.toInt()))
                        x / mBitmap!!.width.toFloat()
                    } else {
                        val y = max(0, min(mBitmap!!.height - 1, event.y.toInt()))
                        y / mBitmap!!.height.toFloat()
                    }
                    if(mColourHsv[2] != value) {
                        mColourHsv[2] = value
                        if(mListener != null) {
                            mListener!!.colourSelected(Color.HSVToColor(mColourHsv))
                        }
                        createBitmap()
                        invalidate()
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    /**
     * Class constructor.
     * @param context  The application context.
     * @param attrs    The control attributes.
     * @param defStyle The default style.
     */
    /**
     * Class constructor.
     * @param context The application context.
     */
}