package org.ww.ai.backup;

import java.io.File;
import java.util.List;

public interface BackupCallbackIF {

    BackupHolder onBackupCreated(File file, int count);

    void onGotAvailableBackups(List<BackupHolder> backupHolderList);

}
