package org.ww.ai.backup;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LocalStorageBackupReader extends AbstractBackupReader {

    public LocalStorageBackupReader(Context context) {
        super(context);
    }

    @Override
    public int restoreBackup(@NonNull BackupHolder backupHolder) {
        if(!backupHolder.file.exists()) {
            Log.e("BACKUPHOLDER", "file does not exist: " + backupHolder.file.getAbsolutePath());
            return -1;
        }
        try {
            processBackupFile(backupHolder.file);
        } catch (IOException e) {
            Log.e("RESTORE", "restoreBackup failed with exception: " + e.getMessage());
            return -1;
        }
        return 0;
    }

    private void processBackupFile(File file) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file.toPath()));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            Log.d("ZIPENTRY", zipEntry.getName());
        }
    }
}
