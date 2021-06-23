package com.rjw.audioprofile.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Class representing a colour wheel.
 */
public class HSVColourWheel extends View {
    private static final float SCALE = 2f;
    private static final float FADE_OUT_FRACTION = 0.03f;

    private static final int POINTER_LINE_WIDTH_DP = 2;
    private static final int POINTER_LENGTH_DP = 10;

    private final Context mContext;
    private OnColourSelectedListener mListener;
    private int mScale;
    private int mPointerLength;
    private int mInnerPadding;
    private final Paint mPointerPaint = new Paint();
    private Rect mRect;
    private Bitmap mBitmap;
    private int[] mPixels;
    private float mInnerCircleRadius;
    private float mFullCircleRadius;
    private int mScaledWidth;
    private int mScaledHeight;
    private int[] mScaledPixels;
    private float mScaledInnerCircleRadius;
    private float mScaledFullCircleRadius;
    private float mScaledFadeOutSize;
    private final float[] mColourHsv = { 0f, 0f, 1f };
    private final Point mSelectedPoint = new Point();
    private boolean mBlackCursor;

    /**
     * Class constuctor.
     * @param context The application context.
     */
    public HSVColourWheel(final Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    /**
     * Class constuctor.
     * @param context The application context.
     * @param attrs   The control attributes.
     */
    public HSVColourWheel(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    /**
     * Class constuctor.
     * @param context  The application context.
     * @param attrs    The control attributes.
     * @param defStyle The default style.
     */
    public HSVColourWheel(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    /**
     * Initialise the controls.
     */
    private void init() {
        final float density = mContext.getResources().getDisplayMetrics().density;
        mScale = (int)(density * SCALE);
        mPointerLength = (int)(density * POINTER_LENGTH_DP);
        mPointerPaint.setStrokeWidth((int)(density * POINTER_LINE_WIDTH_DP));
        mPointerPaint.setColor(mBlackCursor ? Color.BLACK : Color.WHITE);
        mInnerPadding = mPointerLength / 2;
        mBlackCursor = true;
    }

    /**
     * Set the listener for the updates.
     * @param listener The new listener.
     */
    public void setListener(final OnColourSelectedListener listener) {
        this.mListener = listener;
    }

    /**
     * Set if the cursor should be black.
     * @param blackCursor True if the cursor should be black, false if it should be white.
     */
    public final void setBlackCursor(final boolean blackCursor) {
        mBlackCursor = blackCursor;
        invalidate();
    }

    /**
     * Set the selected colour - move the cursor.
     * @param colour The selected colour.
     */
    public void setColour(final int colour) {
        Color.colorToHSV(colour, mColourHsv);
        mColourHsv[2] = 1.0f;
        invalidate();
    }

    /**
     * Draw the control.
     * @param canvas The canvas to draw the control on.
     */
    @Override
    protected void onDraw(final Canvas canvas) {
        if(mBitmap != null) {
            canvas.drawBitmap(mBitmap, null, mRect, null);
            final float hueInPiInterval = mColourHsv[0] / 180f * (float)Math.PI;
            mSelectedPoint.x = mRect.left + (int)(-Math.cos(hueInPiInterval) * mColourHsv[1] * mInnerCircleRadius + mFullCircleRadius);
            mSelectedPoint.y = mRect.top + (int)(-Math.sin(hueInPiInterval) * mColourHsv[1] * mInnerCircleRadius + mFullCircleRadius);
            canvas.drawLine(mSelectedPoint.x - mPointerLength, mSelectedPoint.y, mSelectedPoint.x + mPointerLength, mSelectedPoint.y, mPointerPaint);
            canvas.drawLine(mSelectedPoint.x, mSelectedPoint.y - mPointerLength, mSelectedPoint.x, mSelectedPoint.y + mPointerLength, mPointerPaint);
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

        mRect = new Rect(mInnerPadding, mInnerPadding, width - mInnerPadding, height - mInnerPadding);
        mBitmap = Bitmap.createBitmap(width - 2 * mInnerPadding, height - 2 * mInnerPadding, Bitmap.Config.ARGB_8888);
        mFullCircleRadius = Math.min(mRect.width(), mRect.height()) / 2f;
        mInnerCircleRadius = mFullCircleRadius * (1 - FADE_OUT_FRACTION);
        mScaledWidth = mRect.width() / mScale;
        mScaledHeight = mRect.height() / mScale;
        mScaledFullCircleRadius = Math.min(mScaledWidth, mScaledHeight) / 2f;
        mScaledInnerCircleRadius = mScaledFullCircleRadius * (1 - FADE_OUT_FRACTION);
        mScaledFadeOutSize = mScaledFullCircleRadius - mScaledInnerCircleRadius;
        mScaledPixels = new int[mScaledWidth * mScaledHeight];
        mPixels = new int[mRect.width() * mRect.height()];
        createBitmap();
    }

    /**
     * Create a colour bitmap for the control.
     */
    private void createBitmap() {
        final int w = mRect.width();
        final int h = mRect.height();
        final float[] hsv = new float[] { 0f, 0f, 1f };

        int x = (int)-mScaledFullCircleRadius;
        int y = (int)-mScaledFullCircleRadius;
        for(int i = 0; i < mScaledPixels.length; i++) {
            if(i % mScaledWidth == 0) {
                x = (int)-mScaledFullCircleRadius;
                y++;
            } else {
                x++;
            }

            final double centerDist = Math.sqrt(x*x + y*y);
            if(centerDist <= mScaledFullCircleRadius) {
                hsv[0] = (float)(Math.atan2(y, x) / Math.PI * 180f) + 180;
                hsv[1] = (float)(centerDist / mScaledInnerCircleRadius);
                final int alpha;
                if(centerDist <= mScaledInnerCircleRadius) {
                    alpha = 255;
                } else {
                    alpha = 255 - (int)((centerDist - mScaledInnerCircleRadius) / mScaledFadeOutSize * 255);
                }
                mScaledPixels[i] = Color.HSVToColor(alpha, hsv);
            } else {
                mScaledPixels[i] = 0x00000000;
            }
        }

        int scaledX, scaledY;
        for(x = 0; x < w; x++) {
            scaledX = x / mScale;
            if(scaledX >= mScaledWidth) {
                scaledX = mScaledWidth - 1;
            }
            for(y = 0; y < h; y++) {
                scaledY = y / mScale;
                if(scaledY >= mScaledHeight) {
                    scaledY = mScaledHeight - 1;
                }
                mPixels[x * h + y] = mScaledPixels[scaledX * mScaledHeight + scaledY];
            }
        }
        mBitmap.setPixels(mPixels, 0, w, 0, 0, w, h);
        invalidate();
    }

    /**
     * Get the new measurements of the control.
     * @param widthMeasureSpec  The measurespec to extract the width from.
     * @param heightMeasureSpec The measurespec to extract the height from.
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        /*
         * Make the view quadratic, with height and width equal and as large as possible
         */
        width = height = Math.min(maxWidth, maxHeight);
        setMeasuredDimension(width, height);
    }

    /**
     * Get the colour corresponding to the cursor position.
     * @param x   The x position to be used.
     * @param y   The y position to be used.
     * @param hsv The HSV triple to be updated with the new colour.
     * @return    The colour associated with the point.
     */
    public int getColourForPoint(final int x, final int y, final float[] hsv) {
        final float pointX = x - mFullCircleRadius;
        final float pointY = y - mFullCircleRadius;
        final double centerDist = Math.sqrt(pointX * pointX+ pointY * pointY);
        hsv[0] = (float)(Math.atan2(pointY, pointX) / Math.PI * 180f) + 180;
        hsv[1] = Math.max(0f, Math.min(1f, (float)(centerDist / mInnerCircleRadius)));
        return Color.HSVToColor(hsv);
    }

    /**
     * Deal with touch events and invalidate the control when necessary.
     * @param event The event to be handled.
     * @return      True if the event was handled, false if not.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getActionMasked();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(mListener != null) {
                    mListener.colourSelected(getColourForPoint((int)event.getX(), (int)event.getY(), mColourHsv));
                }
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }
}
