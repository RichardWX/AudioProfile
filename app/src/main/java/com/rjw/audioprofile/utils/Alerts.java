package com.rjw.audioprofile.utils;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rjw.audioprofile.activity.MainActivity;
import com.rjw.audioprofile.R;

public class Alerts {
    public static final String TAG = "AUDIOPROFILE";

    public static void toast(final int message) {
        toast(MainActivity.getInstance().getString(message));
    }
    public static void toast(final StringBuilder message) {
        toast(message.toString());
    }

    public static void toast(final String message) {
        try {
            final LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
            final View view = inflater.inflate(R.layout.toast, null);
            ((TextView)view.findViewById(R.id.text)).setText(message);
            DisplayUtils.colourControls(view);
            final Toast toast = Toast.makeText(MainActivity.getInstance(), "", Toast.LENGTH_SHORT);
            toast.setView(view);
            toast.show();
        } catch(Exception e) {
            // Do nothing.
        }
    }

    public static void alert(final StringBuilder title, final StringBuilder message) {
        alert(title.toString(), message.toString());
    }

    public static void alert(final String title, final String message) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
            final View view = LayoutInflater.from(MainActivity.getInstance()).inflate(R.layout.alert, null);
            ((TextView)view.findViewById(R.id.title)).setText(title);
            ((TextView)view.findViewById(R.id.text)).setText(message);
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            view.findViewById(R.id.buttonOK).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    dialog.dismiss();
                }
            });
            DisplayUtils.colourControls(view);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } catch(Exception e) {
            // Do nothing.
        }
    }
}
