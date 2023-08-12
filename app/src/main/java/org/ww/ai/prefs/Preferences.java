package org.ww.ai.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences {
    public static final String PREF_USE_TRANSLATION = "pref_translate";
    public static final String PREF_USE_TRASH = "pref_use_trash";

    public static final String PREF_RENDER_ENGINE_URL = "pref_render_engine_url";

    private static Preferences instance;
    private static final String TAG = Preferences.class.getSimpleName();

    private static SharedPreferences mPreferences;

    public static synchronized Preferences getInstance(Context context) {
        if(mPreferences == null) {
            mPreferences = context.getSharedPreferences(Preferences.class.getCanonicalName(), Context.MODE_PRIVATE);
        }
        if(instance == null) {
            instance = new Preferences();
        }
        return instance;
    }


    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public boolean getBoolean(final String name) {
        return mPreferences.getBoolean(name, true);
    }

    public String getString(final String name) {
        return mPreferences.getString(name, "");
    }

    public double getDouble(final String name, double... minMax) {
        String value = mPreferences.getString(name, "0");
        try {
            double d = Double.parseDouble(value.replace(",", "."));
            if(minMax.length > 0) {
                d = Math.max(d, minMax[0]);
                if(minMax.length > 1) {
                    d = Math.min(d, minMax[1]);
                }
            }
            return d;
        } catch (NumberFormatException e) {
            Log.e(TAG, "value: " + value + ": " + e.getMessage());
        }
        return 0d;
    }

    public int getInt(final String name, int... minMax) {
        int value = 0;
        try {
            value = Integer.parseInt(mPreferences.getString(name, "0"));
        } catch (NumberFormatException e) {
            Log.e(TAG, name + " is not a Integer: " + mPreferences.getString(name, ""));
        } catch (ClassCastException e) {
            value = mPreferences.getInt(name, 0);
        }
        if(minMax.length > 0) {
            value = Math.max(value, minMax[0]);
            if(minMax.length > 1) {
                value = Math.min(value, minMax[1]);
            }
        }
        return value;
    }

}
