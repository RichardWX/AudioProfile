package com.rjw.audioprofile.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rjw.audioprofile.R;

public class ProfileAdapter extends ArrayAdapter<AudioProfileList.AudioProfile> {
    private Context mContext;

    public ProfileAdapter(final Context context, final AudioProfileList.AudioProfile[] objects) {
        this(context, objects, null);
    }

    public ProfileAdapter(final Context context, final AudioProfileList.AudioProfile[] objects, final View associatedView) {
        super(context, R.layout.row_profile, objects);
        mContext = context;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_profile, parent, false);
        }
        try {
            final AudioProfileList.AudioProfile profile = getItem(position);
            row.findViewById(R.id.icon).setForeground(AudioProfileList.getIcon(profile.icon));
            ((TextView)row.findViewById(R.id.profile)).setText(profile.name);
        } catch(Exception e) {
            // Do nothing.
            e.printStackTrace();
        }
        DisplayUtils.colourControls(row);
        return row;
    }

    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        View row = convertView;
        if(row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_profile, parent, false);
        }
        try {
            final AudioProfileList.AudioProfile profile = getItem(position);
            row.findViewById(R.id.icon).setForeground(AudioProfileList.getIcon(profile.icon));
            ((TextView)row.findViewById(R.id.profile)).setText(profile.name);
        } catch(Exception e) {
            // Do nothing.
            e.printStackTrace();
        }
        DisplayUtils.colourControls(row);
        return row;
    }
}
