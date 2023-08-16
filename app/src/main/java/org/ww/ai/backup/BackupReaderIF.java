package org.ww.ai.backup;

import androidx.annotation.NonNull;

public interface BackupReaderIF {

    int restoreBackup(@NonNull BackupHolder backupHolder);

}
