package com.rjw.audioprofile.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.rjw.audioprofile.R;

public class IconAdapter extends ArrayAdapter<Drawable> {
    private Context mContext;

    public IconAdapter(final Context context, final Drawable[] objects) {
        this(context, objects, null);
    }

    public IconAdapter(final Context context, final Drawable[] objects, final View associatedView) {
        super(context, R.layout.row_icon, objects);
        mContext = context;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_icon, parent, false);
        }
        try {
            final ImageView icon = row.findViewById(R.id.icon);
            icon.setForeground(getItem(position));
        } catch(Exception e) {
            // Do nothing.
            e.printStackTrace();
        }
        return row;
    }

    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_icon, parent, false);
        }
        try {
            final ImageView icon = row.findViewById(R.id.icon);
            icon.setForeground(getItem(position));
        } catch(Exception e) {
            // Do nothing.
            e.printStackTrace();
        }
        return row;
    }
}
