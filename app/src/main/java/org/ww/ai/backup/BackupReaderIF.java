package org.ww.ai.backup;

import androidx.annotation.NonNull;

public interface BackupReaderIF {

    BackupReaderResultHolder restoreBackup(@NonNull BackupHolder backupHolder);


}
