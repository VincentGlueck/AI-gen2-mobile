package org.ww.ai.backup;

import android.content.Context;

import org.ww.ai.rds.entity.RenderResultLightWeight;

public abstract class AbstractBackupWriter implements BackupWriterIF<RenderResultLightWeight> {

    protected final Context mContext;
    public AbstractBackupWriter(Context context) {
        mContext = context;
    }
    public abstract int removeObsoleteBackups();

}
