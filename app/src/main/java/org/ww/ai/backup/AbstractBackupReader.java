package org.ww.ai.backup;

import android.content.Context;

public abstract class AbstractBackupReader implements BackupReaderIF {

    protected final Context mContext;
    public AbstractBackupReader(Context context) {
        this.mContext = context;
    }

}
