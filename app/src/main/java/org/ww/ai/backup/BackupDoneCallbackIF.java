package org.ww.ai.backup;

import java.io.File;

public interface BackupDoneCallbackIF {

    void backupDone(File zipFile);

    void notifyProgress(int done, int total);

}
