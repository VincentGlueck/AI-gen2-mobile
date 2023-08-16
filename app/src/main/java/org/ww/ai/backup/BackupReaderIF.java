package org.ww.ai.backup;

import androidx.annotation.NonNull;

public interface BackupReaderIF {

    void restoreBackup(@NonNull BackupHolder backupHolder);

}
