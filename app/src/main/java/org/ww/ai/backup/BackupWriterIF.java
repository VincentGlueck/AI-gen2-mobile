package org.ww.ai.backup;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.ww.ai.rds.entity.RenderResult;

import java.util.List;

public interface BackupWriterIF<T> {

    BackupHolder writeBackup(List<RenderResult> renderResults) throws  JsonProcessingException;

    List<BackupHolder> getBackupFiles();

    int removeObsoleteBackups();

}
