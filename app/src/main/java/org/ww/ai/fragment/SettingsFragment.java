package org.ww.ai.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.ww.ai.R;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private String aiRenderUrl;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        aiRenderUrl = Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).getString("pref_ai_site_url", "");
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        Preference preferenceSiteUrl = preferenceScreen.findPreference("pref_ai_site_url");
        assert preferenceSiteUrl != null;
        Preference preferenceBtnTestUrl = preferenceScreen.findPreference("pref_ai_test_url");
        preferenceSiteUrl.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                aiRenderUrl = newValue.toString();
                return true;
            }
        });
        assert preferenceBtnTestUrl != null;
        preferenceBtnTestUrl.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(aiRenderUrl));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                return false;
            }
        });
    }
}
