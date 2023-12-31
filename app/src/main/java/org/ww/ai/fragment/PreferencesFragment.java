package org.ww.ai.fragment;

import static org.ww.ai.tools.ExecutorUtil.EXECUTOR_UTIL;
import static org.ww.ai.tools.FileUtil.FILE_UTIL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.ww.ai.R;
import org.ww.ai.backup.AbstractBackupWriter;
import org.ww.ai.backup.BackupDoneCallbackIF;
import org.ww.ai.backup.BackupHolder;
import org.ww.ai.backup.BackupReaderResultHolder;
import org.ww.ai.backup.LocalStorageBackupReader;
import org.ww.ai.backup.LocalStorageBackupWriter;
import org.ww.ai.prefs.Preferences;
import org.ww.ai.prefs.ProgressPreference;
import org.ww.ai.tools.ExecutorUtil;
import org.ww.ai.ui.DialogUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PreferencesFragment extends PreferenceFragmentCompat implements BackupDoneCallbackIF {

    private static final String PREF_USE_TRANSLATION = "pref_translate";
    private static final String PREF_USE_TRASH = "pref_use_trash";
    private static final String PREF_CREATE_BACKUP = "pref_create_backup";
    private static final String PREF_RESTORE_BACKUP = "pref_restore_backup";
    private static final String PREF_REMOVE_OBSOLETE_BACKUPS = "pref_remove_obsolete_backups";
    private static final String PREF_SHOW_PROGRESS = "pref_show_progress";
    private final AtomicReference<String> mAiRenderUrl = new AtomicReference<>();
    private final AtomicBoolean mUseTranslation = new AtomicBoolean();
    private final AtomicBoolean mUseTrash = new AtomicBoolean();
    private final AtomicBoolean mStartImmediately = new AtomicBoolean();
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
        mPreferenceScreen = getPreferenceManager().getPreferenceScreen();
        Preferences preferences = Preferences.getInstance(requireContext());
        mAiRenderUrl.set(preferences.getString(Preferences.PREF_RENDER_ENGINE_URL));
        mUseTranslation.set(preferences.getBoolean(Preferences.PREF_USE_TRANSLATION));
        mUseTrash.set(preferences.getBoolean(Preferences.PREF_USE_TRASH));
        mStartImmediately.set(preferences.getBoolean(Preferences.PREF_START_IMMEDIATELY));
        addBooleanListener(mUseTranslation, PREF_USE_TRANSLATION);
        addBooleanListener(mUseTrash, PREF_USE_TRASH);
        addBooleanListener(mStartImmediately, Preferences.PREF_START_IMMEDIATELY);
        initRenderingUrlSection();
        initBackupSection();
    }

    private void addBooleanListener(final AtomicBoolean atomicBoolean, final String prefKey) {
        Preference preference = mPreferenceScreen.findPreference(prefKey);
        if (preference != null) {
            preference.setOnPreferenceChangeListener((p, v) -> {
                atomicBoolean.set((Boolean) v);
                return true;
            });
        } else {
            Log.e("PREF", "Unknown preference: " + prefKey);
        }
    }

    private void initRenderingUrlSection() {
        final EditTextPreference editRenderUrl = getPreferenceManager()
                .findPreference(Preferences.PREF_RENDER_ENGINE_URL);
        assert editRenderUrl != null;
        if (mAiRenderUrl.get() != null) {
            editRenderUrl.setSummary(mAiRenderUrl.get());
        }
        editRenderUrl.setOnBindEditTextListener(t -> {
            editRenderUrl.setText(mAiRenderUrl.get());
            t.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        });
        editRenderUrl.setOnPreferenceChangeListener((p, newVal) -> {
            mAiRenderUrl.set((String) newVal);
            editRenderUrl.setSummary(mAiRenderUrl.get());
            return false;
        });
    }

    private void writeSharedPreferences() {
        Preferences preferences = Preferences.getInstance(requireContext());
        SharedPreferences.Editor editor = preferences.getPreferences().edit();
        editor.putString(Preferences.PREF_RENDER_ENGINE_URL, mAiRenderUrl.get());
        editor.putBoolean(Preferences.PREF_START_IMMEDIATELY, mStartImmediately.get());
        editor.putBoolean(Preferences.PREF_USE_TRASH, mUseTrash.get());
        editor.putBoolean(Preferences.PREF_USE_TRANSLATION, mUseTranslation.get());
        editor.apply();
    }


    private void initBackupSection() {
        Preference preferenceCreateBackup = mPreferenceScreen.findPreference(PREF_CREATE_BACKUP);
        assert preferenceCreateBackup != null;
        preferenceCreateBackup.setOnPreferenceClickListener(preference -> {
            EXECUTOR_UTIL.execute(new ExecutorUtil.ExecutionIF() {
                Exception exception = null;

                @Override
                public void runInBackground() {
                    getProgressPreference().setVisible(true);
                    try {
                        mBackupWriter.writeBackup(PreferencesFragment.this);
                    } catch (JsonProcessingException e) {
                        exception = e;
                    }
                }

                @Override
                public void onExecutionFinished() {
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
                if (backupFiles != null) {
                    setPreferenceSummary(backupFiles);
                }
            }

        });
    }

    private void setPreferenceSummary(List<BackupHolder> backupFiles) {
        Preference preference = mPreferenceScreen.findPreference(PREF_RESTORE_BACKUP);
        assert preference != null;
        AtomicReference<String> fullName = new AtomicReference<>("");
        if (backupFiles != null && !backupFiles.isEmpty()) {
            fullName.set(backupFiles.get(0).file.getAbsolutePath());
        } else {
            preference.setEnabled(false);
            mLatestBackupHolder = null;
            return;
        }
        preference.setEnabled(true);
        try {
            String str = initRestoreBackupPreference(backupFiles);
            preference.setSummary(str);
            mLatestBackupHolder = backupFiles.get(0);
        } catch (IllegalStateException e) {
            Log.e("PREFS", "Backup list done, but Fragment already closed!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        writeSharedPreferences();
    }

    private String initRestoreBackupPreference(List<BackupHolder> backupHolderList) throws IllegalStateException {
        Preference preferenceRestoreBackup = mPreferenceScreen.findPreference(PREF_RESTORE_BACKUP);
        assert preferenceRestoreBackup != null;
        preferenceRestoreBackup.setOnPreferenceClickListener(preference -> {
            if (mLatestBackupHolder == null) {
                Log.e("LOCAL", "mLatestBackupHolder is null");
            } else {
                doRestoreBackupAsync();
            }
            return false;
        });
        assert backupHolderList != null;
        return backupHolderList.get(0).toReadableForm(requireContext());
    }

    private void doRestoreBackupAsync() {
        EXECUTOR_UTIL.execute(new ExecutorUtil.ExecutionIF() {

            BackupReaderResultHolder holder;

            @Override
            public void runInBackground() {
                LocalStorageBackupReader localStorageBackupReader = new LocalStorageBackupReader(getContext());
                holder = localStorageBackupReader.restoreBackup(mLatestBackupHolder);
            }

            @Override
            public void onExecutionFinished() {
                if (holder == null) {
                    Toast.makeText(getContext(), "Fatal: no holder returned", Toast.LENGTH_LONG).show();
                    return;
                }
                if (holder.messages.isEmpty()) {
                    String str = getResources().getString(R.string.pref_backup_restored, holder.restored);
                    DialogUtil.DIALOG_UTIL.showMessage(getContext(), R.string.pref_section_backup, str, R.drawable.info);
                } else {
                    showDetailedResultMessage(holder);
                }
            }
        });
    }

    private void showDetailedResultMessage(BackupReaderResultHolder holder) {
        String title = getResources().getString(R.string.pref_section_backup);
        String str = getResources().getString(R.string.pref_backup_restored_with_failures,
                holder.restored, (holder.failures + holder.skipped));
        DialogUtil.DIALOG_UTIL.showPrompt(
                getContext(),
                title,
                str,
                R.string.btn_yes,
                (dialog, which) -> showMessageDetails(holder),
                R.string.btn_no,
                (dialog, which) -> {
                },
                R.drawable.warning
        );
    }

    private void showMessageDetails(BackupReaderResultHolder holder) {
        StringBuilder sb = new StringBuilder();
        holder.messages.forEach(m -> sb.append(sb.length() > 0 ? "\n" : "").append(m));
        DialogUtil.DIALOG_UTIL.showLargeTextDialog(
                getContext(),
                R.string.dialog_title_log,
                sb.toString()
        );
    }

    public void removeObsoleteBackups() {
        CheckBoxPreference checkBoxPreference = mPreferenceScreen.findPreference(PREF_REMOVE_OBSOLETE_BACKUPS);
        assert checkBoxPreference != null;
        if (checkBoxPreference.isChecked()) {
            EXECUTOR_UTIL.execute(() -> mBackupWriter.removeObsoleteBackups());
        }
    }

    @Override
    public void backupDone(File zipFile) {
        CharSequence charSequence = getResources().getString(R.string.pref_backup_created_toast,
                FILE_UTIL.readableFileSize(zipFile.length()));
        Toast.makeText(getContext(),
                charSequence,
                Toast.LENGTH_LONG
        ).show();
        removeObsoleteBackups();
        getBackupFilesAsync();
        getProgressPreference().setVisible(false);
    }

    @Override
    public void notifyProgress(int done, int total) {
        ProgressPreference progressPreference = getProgressPreference();
        progressPreference.updateValue(done, total);
    }

    @NonNull
    private ProgressPreference getProgressPreference() {
        Preference preference = mPreferenceScreen.findPreference(PREF_SHOW_PROGRESS);
        assert preference != null;
        return (ProgressPreference) preference;
    }
}

