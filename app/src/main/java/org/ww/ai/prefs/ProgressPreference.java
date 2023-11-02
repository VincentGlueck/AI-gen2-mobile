package org.ww.ai.prefs;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import org.ww.ai.R;

public class ProgressPreference extends Preference {

    private ProgressBar mProgressBar;

    public ProgressPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.pref_progress_bar);
    }

    public ProgressPreference(@NonNull Context context) {
        super(context);
        setLayoutResource(R.layout.pref_progress_bar);
    }

    public void updateValue(int current, int total) {
        if(mProgressBar != null) {
            mProgressBar.setMax(total);
            mProgressBar.setProgress(current);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mProgressBar = (ProgressBar) holder.findViewById(R.id.pref_progressBar);
        mProgressBar.setMin(0);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        Log.w("DEBUG", "onSetInitialValue: " + defaultValue);
    }
}
