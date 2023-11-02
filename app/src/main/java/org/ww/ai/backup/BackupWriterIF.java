package org.ww.ai.backup;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface BackupWriterIF {

    void writeBackup(BackupDoneCallbackIF backupDoneCallback) throws JsonProcessingException;

    List<BackupHolder> getBackupFiles();

    void removeObsoleteBackups();


}
