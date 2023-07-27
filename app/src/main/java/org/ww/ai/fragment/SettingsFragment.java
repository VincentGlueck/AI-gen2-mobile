package org.ww.ai.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.ww.ai.R;

import java.util.concurrent.atomic.AtomicReference;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String PREF_AI_RENDER_URL = "pref_ai_site_url";
    private static final String PREF_AI_TEST_URL = "pref_ai_test_url";

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        initPreferences();
    }

    private void initPreferences() {
        AtomicReference<String> aiRenderUrl = new AtomicReference<>();
        assert getPreferenceManager().getSharedPreferences() != null;
        String strRenderUrl = getPreferenceManager().getSharedPreferences().getString(PREF_AI_RENDER_URL, null);
        aiRenderUrl.set(strRenderUrl);
        PreferenceScreen preferenceScreen = getPreferenceManager().getPreferenceScreen();
        EditTextPreference editRenderUrl = getPreferenceManager().findPreference(PREF_AI_RENDER_URL);
        Preference preferenceSiteUrl = preferenceScreen.findPreference(PREF_AI_TEST_URL);
        assert editRenderUrl != null;
        assert preferenceSiteUrl != null;
        if (aiRenderUrl.get() != null) {
            editRenderUrl.setSummary(aiRenderUrl.get());
            preferenceSiteUrl.setSummary(aiRenderUrl.get());
        }
        editRenderUrl.setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_TEXT_VARIATION_URI));
        editRenderUrl.setOnPreferenceChangeListener((p, newVal) -> {
            aiRenderUrl.set((String) newVal);
            editRenderUrl.setSummary(aiRenderUrl.get());
            preferenceSiteUrl.setSummary(aiRenderUrl.get());
            return false;
        });
        preferenceSiteUrl.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setData(Uri.parse(aiRenderUrl.get()));
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
            return false;
        });
    }

}

