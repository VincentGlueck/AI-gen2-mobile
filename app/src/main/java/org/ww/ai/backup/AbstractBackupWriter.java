package org.ww.ai.backup;

import android.content.Context;

import org.ww.ai.rds.entity.RenderResultLightWeight;

public abstract class AbstractBackupWriter implements BackupWriter<RenderResultLightWeight> {

    protected final Context mContext;
    protected BackupCallbackIF mBackupCallback;

    public AbstractBackupWriter(Context context) {
        mContext = context;
    }

    public void setBackupCallback(BackupCallbackIF backupCallback) {
        mBackupCallback = backupCallback;
    }

    public abstract void removeObsoleteBackups();

}
