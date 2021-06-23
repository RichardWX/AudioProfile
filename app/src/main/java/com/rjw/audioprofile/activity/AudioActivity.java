package com.rjw.audioprofile.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.rjw.audioprofile.R;
import com.rjw.audioprofile.utils.DisplayUtils;

public class AudioActivity extends Activity {
    protected float[] mWindowRatio = new float[] {0.8f, 0.7f};

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
        setFinishOnTouchOutside(false);
        setTheme(R.style.AppTheme_Dialog);
    }

    @Override
    public void setContentView(final int layout) {
        super.setContentView(layout);

        // Set the window to be the right size.
        final ViewTreeObserver vto = getWindow().getDecorView().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Set the window size so it fits the screen whatever device it is running on.
                boolean changed = false;
                final DisplayMetrics dm = getResources().getDisplayMetrics();
                final WindowManager.LayoutParams lp = getWindow().getAttributes();
                if(getWindow().getDecorView().getWidth() < (int)(dm.widthPixels * mWindowRatio[0])) {
                    lp.width = (int)(dm.widthPixels * mWindowRatio[0]);
                    changed = true;
                }
                if(getWindow().getDecorView().getHeight() < (int)(dm.heightPixels * mWindowRatio[1])) {
                    lp.height = (int)(dm.heightPixels * mWindowRatio[1]);
                    changed = true;
                }
                if(changed) {
                    getWindow().setAttributes(lp);
                }
                final ViewTreeObserver vto = getWindow().getDecorView().getViewTreeObserver();
                vto.removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        ((TextView)findViewById(R.id.title)).setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        ((TextView)findViewById(R.id.title)).setText(titleId);
    }

    protected void setWindowRatios(final float xRatio, final float yRatio) {
        mWindowRatio[0] = xRatio;
        mWindowRatio[1] = yRatio;
    }

    protected void colourControls() {
        colourControls(MainActivity.getConfigColour());
    }
    protected void colourControls(final int colour) {
        DisplayUtils.colourControls(getWindow().getDecorView(), colour);
    }
}
