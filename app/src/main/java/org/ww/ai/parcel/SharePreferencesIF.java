package org.ww.ai.parcel;

import android.content.SharedPreferences;

public interface SharePreferencesIF {
    void writeToSharedPreferences(SharedPreferences preferences);

    void getFromPreferences(SharedPreferences preferences);


}
