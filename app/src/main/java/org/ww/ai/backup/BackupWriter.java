package org.ww.ai.backup;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface BackupWriter<T> {

    void writeBackup(List<T> renderResults) throws  JsonProcessingException;

    void getBackupFiles();

}
