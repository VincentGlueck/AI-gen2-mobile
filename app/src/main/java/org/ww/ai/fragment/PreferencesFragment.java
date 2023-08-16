package org.ww.ai.fragment;

import static org.ww.ai.prefs.Preferences.PREF_RENDER_ENGINE_URL;
import static org.ww.ai.tools.ExecutorUtil.EXECUTOR_UTIL;

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

import com.fasterxml.jackson.core.JsonProcessingException;

import org.ww.ai.R;
import org.ww.ai.backup.AbstractBackupWriter;
import org.ww.ai.backup.BackupHolder;
import org.ww.ai.backup.LocalStorageBackupReader;
import org.ww.ai.backup.LocalStorageBackupWriter;
import org.ww.ai.prefs.Preferences;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.tools.ExecutorUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PreferencesFragment extends PreferenceFragmentCompat {

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

    private BackupHolder mLatestBackupHolder;


    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        mBackupWriter = new LocalStorageBackupWriter(requireContext());
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
            EXECUTOR_UTIL.execute(new ExecutorUtil.ExecutionIF() {
                BackupHolder backupHolder = null;
                Exception exception = null;
                @Override
                public void runInBackground() {
                    try {
                        backupHolder = writeBackup();
                    } catch (JsonProcessingException e) {
                        exception = e;
                    }
                }

                @Override
                public void onExecutionFinished() {
                    if(backupHolder != null) {
                        getBackupFilesAsync();
                        Toast.makeText(getContext(), R.string.pref_backup_created_toast,
                                Toast.LENGTH_LONG).show();
                        removeObsoleteBackups();
                    } else if (exception != null) {
                        Toast.makeText(getContext(), "Error: "
                                + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            return false;
        });
        getBackupFilesAsync();
    }

    private void getBackupFilesAsync() {
        EXECUTOR_UTIL.execute(new ExecutorUtil.ExecutionIF() {

            List<BackupHolder> backupFiles = null;

            @Override
            public void runInBackground() {
                backupFiles = mBackupWriter.getBackupFiles();
            }

            @Override
            public void onExecutionFinished() {
                if(backupFiles != null) {
                    setPreferenceSummary(backupFiles);
                }
            }

        });
    }

    private void setPreferenceSummary(List<BackupHolder> backupFiles) {
        AtomicReference<String> fullName = new AtomicReference<>("");
        if(backupFiles != null && !backupFiles.isEmpty()) {
            fullName.set(backupFiles.get(0).file.getAbsolutePath());
        } else {
            mLatestBackupHolder = null;
            return;
        }
        String str = initRestoreBackupPreference(backupFiles, fullName);
        Preference preference = mPreferenceScreen.findPreference(PREF_RESTORE_BACKUP);
        assert preference != null;
        preference.setSummary(str);
        mLatestBackupHolder = backupFiles.get(0);
    }

    private BackupHolder writeBackup() throws JsonProcessingException {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        List<RenderResult> renderResultList = appDatabase.renderResultDao().getAllOnThread();
        return mBackupWriter.writeBackup(renderResultList);
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
    }

    private String initRestoreBackupPreference(List<BackupHolder> backupHolderList, AtomicReference<String> fullName) {
        Preference preferenceRestoreBackup = mPreferenceScreen.findPreference(PREF_RESTORE_BACKUP);
        assert preferenceRestoreBackup != null;
        preferenceRestoreBackup.setOnPreferenceClickListener(preference -> {
            if(mLatestBackupHolder == null) {
                Log.e("LOCAL", "mLatestBackupHolder is null");
            } else {
                LocalStorageBackupReader localStorageBackupReader = new LocalStorageBackupReader(getContext());
                localStorageBackupReader.restoreBackup(mLatestBackupHolder);
            }
            return false;
        });
        assert backupHolderList != null;
        return backupHolderList.get(0).toReadableForm(requireContext());
    }

    public void removeObsoleteBackups() {
        CheckBoxPreference checkBoxPreference = mPreferenceScreen.findPreference(PREF_REMOVE_OBSOLETE_BACKUPS);
        assert checkBoxPreference != null;
        if(checkBoxPreference.isChecked()) {
            EXECUTOR_UTIL.execute(() -> mBackupWriter.removeObsoleteBackups());
        }
    }

}

