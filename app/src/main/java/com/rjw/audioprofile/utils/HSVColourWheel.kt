package com.rjw.audioprofile.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/**
 * Class representing a colour wheel.
 */
class HSVColourWheel : View {
    private val mContext: Context
    private var mListener: OnColourSelectedListener? = null
    private var mScale = 0
    private var mPointerLength = 0
    private var mInnerPadding = 0
    private val mPointerPaint = Paint()
    private var mRect: Rect? = null
    private var mBitmap: Bitmap? = null
    private var mPixels: IntArray? = null
    private var mInnerCircleRadius = 0f
    private var mFullCircleRadius = 0f
    private var mScaledWidth = 0
    private var mScaledHeight = 0
    private var mScaledPixels: IntArray? = null
    private var mScaledInnerCircleRadius = 0f
    private var mScaledFullCircleRadius = 0f
    private var mScaledFadeOutSize = 0f
    private val mColourHsv = floatArrayOf(0f, 0f, 1f)
    private val mSelectedPoint = Point()
    private var mBlackCursor = false

    /**
     * Class constructor.
     * @param context The application context.
     * @param attrs   The control attributes.
     */
    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        mContext = context
        init()
    }

    /**
     * Class constructor.
     * @param context  The application context.
     * @param attrs    The control attributes.
     * @param defStyle The default style.
     */
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        mContext = context
        init()
    }

    /**
     * Initialise the controls.
     */
    private fun init() {
        val density = mContext.resources.displayMetrics.density
        mScale = (density * SCALE).toInt()
        mPointerLength = (density * POINTER_LENGTH_DP).toInt()
        mPointerPaint.strokeWidth = density * POINTER_LINE_WIDTH_DP
        mPointerPaint.color = if(mBlackCursor) Color.BLACK else Color.WHITE
        mInnerPadding = mPointerLength / 2
        mBlackCursor = true
    }

    /**
     * Set the listener for the updates.
     * @param listener The new listener.
     */
    fun setListener(listener: OnColourSelectedListener?) {
        mListener = listener
    }

    /**
     * Set if the cursor should be black.
     * @param blackCursor True if the cursor should be black, false if it should be white.
     */
    fun setBlackCursor(blackCursor: Boolean) {
        mBlackCursor = blackCursor
        invalidate()
    }

    /**
     * Set the selected colour - move the cursor.
     * @param colour The selected colour.
     */
    fun setColour(colour: Int) {
        Color.colorToHSV(colour, mColourHsv)
        mColourHsv[2] = 1.0f
        invalidate()
    }

    /**
     * Draw the control.
     * @param canvas The canvas to draw the control on.
     */
    override fun onDraw(canvas: Canvas?) {
        if(canvas != null) {
            if(mBitmap != null) {
                canvas.drawBitmap(mBitmap!!, null, mRect!!, null)
                val hueInPiInterval = mColourHsv[0] / 180f * Math.PI.toFloat()
                mSelectedPoint.x = mRect!!.left + (-cos(hueInPiInterval.toDouble()) * mColourHsv[1] * mInnerCircleRadius + mFullCircleRadius).toInt()
                mSelectedPoint.y = mRect!!.top + (-sin(hueInPiInterval.toDouble()) * mColourHsv[1] * mInnerCircleRadius + mFullCircleRadius).toInt()
                canvas.drawLine(
                    (mSelectedPoint.x - mPointerLength).toFloat(),
                    mSelectedPoint.y.toFloat(),
                    (mSelectedPoint.x + mPointerLength).toFloat(),
                    mSelectedPoint.y.toFloat(),
                    mPointerPaint
                )
                canvas.drawLine(
                    mSelectedPoint.x.toFloat(),
                    (mSelectedPoint.y - mPointerLength).toFloat(),
                    mSelectedPoint.x.toFloat(),
                    (mSelectedPoint.y + mPointerLength).toFloat(),
                    mPointerPaint
                )
            }
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
        mRect = Rect(mInnerPadding, mInnerPadding, width - mInnerPadding, height - mInnerPadding)
        mBitmap = Bitmap.createBitmap(width - 2 * mInnerPadding, height - 2 * mInnerPadding, Bitmap.Config.ARGB_8888)
        mFullCircleRadius = min(mRect!!.width(), mRect!!.height()) / 2f
        mInnerCircleRadius = mFullCircleRadius * (1 - FADE_OUT_FRACTION)
        mScaledWidth = mRect!!.width() / mScale
        mScaledHeight = mRect!!.height() / mScale
        mScaledFullCircleRadius = min(mScaledWidth, mScaledHeight) / 2f
        mScaledInnerCircleRadius = mScaledFullCircleRadius * (1 - FADE_OUT_FRACTION)
        mScaledFadeOutSize = mScaledFullCircleRadius - mScaledInnerCircleRadius
        mScaledPixels = IntArray(mScaledWidth * mScaledHeight)
        mPixels = IntArray(mRect!!.width() * mRect!!.height())
        createBitmap()
    }

    /**
     * Create a colour bitmap for the control.
     */
    private fun createBitmap() {
        if(mScaledPixels != null && mPixels != null) {
            val w = mRect!!.width()
            val h = mRect!!.height()
            val hsv = floatArrayOf(0f, 0f, 1f)
            var x = (-mScaledFullCircleRadius).toInt()
            var y = (-mScaledFullCircleRadius).toInt()
            for(i in mScaledPixels!!.indices) {
                if(i % mScaledWidth == 0) {
                    x = (-mScaledFullCircleRadius).toInt()
                    y++
                } else {
                    x++
                }
                val centerDist = sqrt((x * x + y * y).toDouble())
                if(centerDist <= mScaledFullCircleRadius) {
                    hsv[0] = (atan2(y.toDouble(), x.toDouble()) / Math.PI * 180f).toFloat() + 180
                    hsv[1] = (centerDist / mScaledInnerCircleRadius).toFloat()
                    val alpha: Int = if(centerDist <= mScaledInnerCircleRadius) {
                        255
                    } else {
                        255 - ((centerDist - mScaledInnerCircleRadius) / mScaledFadeOutSize * 255).toInt()
                    }
                    mScaledPixels!![i] = Color.HSVToColor(alpha, hsv)
                } else {
                    mScaledPixels!![i] = 0x00000000
                }
            }
            var scaledX: Int
            var scaledY: Int
            x = 0
            while(x < w) {
                scaledX = x / mScale
                if(scaledX >= mScaledWidth) {
                    scaledX = mScaledWidth - 1
                }
                y = 0
                while(y < h) {
                    scaledY = y / mScale
                    if(scaledY >= mScaledHeight) {
                        scaledY = mScaledHeight - 1
                    }
                    mPixels!![x * h + y] = mScaledPixels!![scaledX * mScaledHeight + scaledY]
                    y++
                }
                x++
            }
            mBitmap!!.setPixels(mPixels, 0, w, 0, 0, w, h)
            invalidate()
        }
    }

    /**
     * Get the new measurements of the control.
     * @param widthMeasureSpec  The measurespec to extract the width from.
     * @param heightMeasureSpec The measurespec to extract the height from.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        /*
         * Make the view quadratic, with height and width equal and as large as possible
         */
        val height: Int = min(maxWidth, maxHeight)
        width = height
        setMeasuredDimension(width, height)
    }

    /**
     * Get the colour corresponding to the cursor position.
     * @param x   The x position to be used.
     * @param y   The y position to be used.
     * @param hsv The HSV triple to be updated with the new colour.
     * @return    The colour associated with the point.
     */
    private fun getColourForPoint(x: Int, y: Int, hsv: FloatArray): Int {
        val pointX = x - mFullCircleRadius
        val pointY = y - mFullCircleRadius
        val centerDist = sqrt((pointX * pointX + pointY * pointY).toDouble())
        hsv[0] = (atan2(pointY.toDouble(), pointX.toDouble()) / Math.PI * 180f).toFloat() + 180
        hsv[1] = max(0f, min(1f, (centerDist / mInnerCircleRadius).toFloat()))
        return Color.HSVToColor(hsv)
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
                    if(mListener != null) {
                        mListener!!.colourSelected(getColourForPoint(event.x.toInt(), event.y.toInt(), mColourHsv))
                    }
                    invalidate()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val SCALE = 2f
        private const val FADE_OUT_FRACTION = 0.03f
        private const val POINTER_LINE_WIDTH_DP = 2
        private const val POINTER_LENGTH_DP = 10
    }
}