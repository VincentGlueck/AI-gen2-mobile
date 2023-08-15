package org.ww.ai.fragment;

import static org.ww.ai.prefs.Preferences.PREF_RENDER_ENGINE_URL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.backup.AbstractBackupWriter;
import org.ww.ai.backup.BackupCallbackIF;
import org.ww.ai.backup.BackupHolder;
import org.ww.ai.backup.LocalStorageBackupWriter;
import org.ww.ai.prefs.Preferences;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsFragment extends PreferenceFragmentCompat implements BackupCallbackIF {

    private static final String PREF_AI_RENDER_URL = "pref_ai_site_url";
    private static final String PREF_AI_TEST_URL = "pref_ai_test_url";
    private static final String PREF_USE_TRANSLATION = "pref_translate";
    private static final String PREF_USE_TRASH = "pref_use_trash";
    private static final String PREF_CREATE_BACKUP = "pref_create_backup";
    private static final String PREF_RESTORE_BACKUP = "pref_restore_backup";
    private static final String PREF_REMOVE_OBSOLETE_BACKUPS = "pref_remove_obsolete_backups";
    private final AtomicReference<String> mAiRenderUrl = new AtomicReference<>();
    private final AtomicBoolean mUseTranslation = new AtomicBoolean();
    private final AtomicBoolean mUseTrash = new AtomicBoolean();
    private PreferenceScreen mPreferenceScreen;
    private AbstractBackupWriter mBackupWriter;


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        mBackupWriter = new LocalStorageBackupWriter(requireContext(), this);
        mBackupWriter.setBackupCallback(this);
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
        initBackupSection();
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

    private void initBackupSection() {
        Preference preferenceCreateBackup = mPreferenceScreen.findPreference(PREF_CREATE_BACKUP);
        assert preferenceCreateBackup != null;
        preferenceCreateBackup.setOnPreferenceClickListener(preference -> {
            writeBackup();
            return false;
        });
        mBackupWriter.getBackupFiles();
    }

    private void writeBackup() {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        ListenableFuture<List<RenderResultLightWeight>> listenableFuture =
                appDatabase.renderResultDao().getAllLightWeights(false);
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(listenableFuture,
                r -> mBackupWriter.writeBackup(r), requireContext());
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
        } finally {
            editor.apply();
        }
    }

    @Override
    public BackupHolder onBackupCreated(File file, int count) {
        if(file == null || count == 0) {
            Log.e("BACKUP", "Failed to create backup (file == null or count == 0)");
            return null;
        }
        BackupHolder holder = BackupHolder.create(file, count);
        CheckBoxPreference checkBoxPreference = mPreferenceScreen.findPreference(PREF_REMOVE_OBSOLETE_BACKUPS);
        assert checkBoxPreference != null;
        if(checkBoxPreference.isChecked()) {
            removeObsoleteBackups();
        }
        initRestoreBackupPreference(List.of(holder), new AtomicReference<>(file.getName()));
        return holder;
    }

    @Override
    public void onGotAvailableBackups(List<BackupHolder> backupHolderList) {
        AtomicReference<String> fullName = new AtomicReference<>("");
        if(backupHolderList != null && !backupHolderList.isEmpty()) {
            fullName.set(backupHolderList.get(0).file.getAbsolutePath());
        }
        assert backupHolderList != null;
        initRestoreBackupPreference(backupHolderList, fullName);
    }

    private void initRestoreBackupPreference(List<BackupHolder> backupHolderList, AtomicReference<String> fullName) {
        Preference preferenceRestoreBackup = mPreferenceScreen.findPreference(PREF_RESTORE_BACKUP);
        assert preferenceRestoreBackup != null;
        preferenceRestoreBackup.setOnPreferenceClickListener(preference -> {
            Toast.makeText(requireContext(), "Not implemented yet, but would read " +
                    fullName.get(), Toast.LENGTH_LONG).show();
            return false;
        });
        assert backupHolderList != null;
        preferenceRestoreBackup.setSummary(backupHolderList.get(0).toReadableForm(requireContext()));
    }

    @Override
    public void removeObsoleteBackups() {
        mBackupWriter.removeObsoleteBackups();
    }

    @Override
    public void onRemoveBackupsDone(int count) {
        String str = getString(R.string.pref_remove_obsolete_result, count);
        Toast.makeText(requireContext(), str, Toast.LENGTH_LONG).show();
    }
}

