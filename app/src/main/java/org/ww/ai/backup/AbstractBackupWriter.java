package org.ww.ai.backup;

import android.content.Context;

public abstract class AbstractBackupWriter implements BackupWriterIF {

    protected final Context mContext;
    public AbstractBackupWriter(Context context) {
        mContext = context;
    }
    public abstract void removeObsoleteBackups();

}
