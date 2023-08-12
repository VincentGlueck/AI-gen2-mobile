package org.ww.ai.fragment;

import static org.ww.ai.prefs.Preferences.PREF_RENDER_ENGINE_URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.ww.ai.R;
import org.ww.ai.prefs.Preferences;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String PREF_AI_RENDER_URL = "pref_ai_site_url";
    private static final String PREF_AI_TEST_URL = "pref_ai_test_url";
    private static final String PREF_USE_TRANSLATION = "pref_translate";
    private static final String PREF_USE_TRASH = "pref_use_trash";
    private final AtomicReference<String> mAiRenderUrl = new AtomicReference<>();
    private final AtomicBoolean mUseTranslation = new AtomicBoolean();
    private final AtomicBoolean mUseTrash = new AtomicBoolean();

    private PreferenceScreen mPreferenceScreen;


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        initPreferences();
    }

    private void initPreferences() {
        assert getPreferenceManager().getSharedPreferences() != null;
        mPreferenceScreen = getPreferenceManager().getPreferenceScreen();
        SharedPreferences sharedPreferences = requireContext()
                .getSharedPreferences(Preferences.class.getCanonicalName(), Context.MODE_PRIVATE);
        mAiRenderUrl.set(sharedPreferences.getString(PREF_RENDER_ENGINE_URL, null));
        mUseTranslation.set(sharedPreferences.getBoolean(Preferences.PREF_USE_TRANSLATION, true));
        mUseTrash.set(sharedPreferences.getBoolean(Preferences.PREF_USE_TRASH, true));
        addBooleanListener(mUseTranslation, PREF_USE_TRANSLATION);
        addBooleanListener(mUseTrash, PREF_USE_TRASH);
        initRenderingUrlSection();
    }

    private void addBooleanListener(final AtomicBoolean atomicBoolean, final String prefKey) {
        Preference preference = mPreferenceScreen.findPreference(prefKey);
        if(preference != null) {
            preference.setOnPreferenceChangeListener((p, v) -> {
                if(!Boolean.class.isAssignableFrom(v.getClass())) {
                    Log.e("PREF", "Attempt to use " + v.getClass() + " as " + Boolean.class.getCanonicalName());
                } else {
                    atomicBoolean.set((Boolean) v);
                }
                return true;
            });
        } else {
            Log.e("PREF", "Unknown preference: " + prefKey);
        }
    }

    private void initRenderingUrlSection() {
        EditTextPreference editRenderUrl = getPreferenceManager().findPreference(PREF_AI_RENDER_URL);
        Preference preferenceSiteUrl = mPreferenceScreen.findPreference(PREF_AI_TEST_URL);
        assert editRenderUrl != null;
        assert preferenceSiteUrl != null;
        if (mAiRenderUrl.get() != null) {
            editRenderUrl.setSummary(mAiRenderUrl.get());
            preferenceSiteUrl.setSummary(mAiRenderUrl.get());
        }
        editRenderUrl.setOnBindEditTextListener(t -> t.setInputType(InputType.TYPE_TEXT_VARIATION_URI));
        editRenderUrl.setOnPreferenceChangeListener((p, newVal) -> {
            mAiRenderUrl.set((String) newVal);
            editRenderUrl.setSummary(mAiRenderUrl.get());
            preferenceSiteUrl.setSummary(mAiRenderUrl.get());
            return false;
        });
        preferenceSiteUrl.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setData(Uri.parse(mAiRenderUrl.get()));
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = Preferences.getInstance(requireContext()).getEditor();
        try {
            editor.putString(PREF_RENDER_ENGINE_URL, mAiRenderUrl.get());
            editor.putBoolean(PREF_USE_TRANSLATION, mUseTranslation.get());
            editor.putBoolean(Preferences.PREF_USE_TRASH, mUseTrash.get());
            Log.w("PREFS", "********* wrote a lot *******************");
        } finally {
            editor.apply();
        }
    }
}

