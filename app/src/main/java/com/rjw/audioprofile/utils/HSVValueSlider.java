package com.rjw.audioprofile.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Class to represent a slider to select a colour.
 */
public class HSVValueSlider extends View {
    private OnColourSelectedListener mListener;
    float[] mColourHsv = { 0f, 0f, 1f };
    private Rect mSrcRect;
    private Rect mDstRect;
    private Bitmap mBitmap;
    private int[] mPixels;
    private float mRadius;
    private boolean mHorizontal;

    /**
     * Class constructor.
     * @param context The application context.
     */
    public HSVValueSlider(final Context context) {
        this(context, null, 0);
    }

    /**
     * Class constructor.
     * @param context The application context.
     * @param attrs   The control attributes.
     */
    public HSVValueSlider(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Class constructor.
     * @param context  The application context.
     * @param attrs    The control attributes.
     * @param defStyle The default style.
     */
    public HSVValueSlider(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mHorizontal = true;
    }

    /**
     * Set the listener for a new colour being selected.
     * @param listener The new listener.
     */
    public void setListener(final OnColourSelectedListener listener) {
        this.mListener = listener;
    }

    /**
     * Set the alignment of the control.
     * @param horizontal True if the control is aligned horizontally, false if vertically.
     */
    public void setHorizontal(final boolean horizontal) {
        mHorizontal = horizontal;
        invalidate();
    }

    /**
     * Set the corner radius of the control.
     * @param radius The radius size (0 for none).
     */
    public void setRadius(final float radius) {
        mRadius = radius;
        invalidate();
    }

    /**
     * Set the basic colour for the control.
     * @param colour    The new colour to be controlled.
     * @param keepValue True if the value should be stored, false if not.
     */
    public void setColour(final int colour, final boolean keepValue) {
        final float oldValue = mColourHsv[2];
        Color.colorToHSV(colour, mColourHsv);
        if(keepValue) {
            mColourHsv[2] = oldValue;
        }
        if(mListener != null) {
            mListener.colourSelected(Color.HSVToColor(mColourHsv));
        }

        createBitmap();
    }

    /**
     * Draw the control.
     * @param canvas The canvas to be drawn onto.
     */
    @Override
    protected void onDraw(final Canvas canvas) {
        if(mBitmap != null) {
            final Path clipPath = new Path();
            final RectF rect = new RectF(0, 0, getWidth(), getHeight());
            clipPath.addRoundRect(rect, mRadius, mRadius, Path.Direction.CW);
            canvas.clipPath(clipPath);
            canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
        }
    }

    /**
     * Update the control after resizing.
     * @param width      The new width.
     * @param height     The new height.
     * @param prevWidth  The previous width.
     * @param prevHeight The previous height.
     */
    @Override
    protected void onSizeChanged(final int width, final int height, final int prevWidth, final int prevHeight) {
        super.onSizeChanged(width, height, prevWidth, prevHeight);
        mDstRect = new Rect(0, 0, width, height);
        if(mHorizontal) {
            mSrcRect = new Rect(0, 0, width, 1);
            mBitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
            mPixels = new int[width];
        } else {
            mSrcRect = new Rect(0, 0, 1, height);
            mBitmap = Bitmap.createBitmap(1, height, Bitmap.Config.ARGB_8888);
            mPixels = new int[height];
        }
        createBitmap();
    }

    /**
     * Create a colour bitmap for the control.
     */
    private void createBitmap() {
        if(mBitmap == null) {
            return;
        }
        final int size;
        if(mHorizontal) {
            size = getWidth();
        } else {
            size = getHeight();
        }
        final int selectedPos = (int)(mColourHsv[2] * size);
        final float[] hsv = new float[] { mColourHsv[0], mColourHsv[1], 1f };
        float value = 0;
        final float valueStep = 1f / size;
        for(int pos = 0; pos < size; pos++) {
            value += valueStep;
            if(pos >= selectedPos - 1 && pos <= selectedPos + 1) {
                final int intVal = 0xFF - (int)(value * 0xFF);
                final int colour = intVal * 0x010101 + 0xFF000000;
                mPixels[pos] = colour;
            } else {
                hsv[2] = value;
                mPixels[pos] = Color.HSVToColor(hsv);
            }
        }
        if(mHorizontal) {
            mBitmap.setPixels(mPixels, 0, size, 0, 0, size, 1);
        } else {
            mBitmap.setPixels(mPixels, 0, 1, 0, 0, 1, size);
        }
        invalidate();
    }

    /**
     * Deal with touch events and invalidate the control when necessary.
     * @param event The event to be handled.
     * @return      True if the event was handled, false if not.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float value;
                if(mHorizontal) {
                    final int x = Math.max(0, Math.min(mBitmap.getWidth() - 1, (int)event.getX()));
                    value = x / (float)mBitmap.getWidth();
                } else {
                    final int y = Math.max(0, Math.min(mBitmap.getHeight() - 1, (int)event.getY()));
                    value = y / (float)mBitmap.getHeight();
                }
                if(mColourHsv[2] != value) {
                    mColourHsv[2] = value;
                    if(mListener != null) {
                        mListener.colourSelected(Color.HSVToColor(mColourHsv));
                    }
                    createBitmap();
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}
