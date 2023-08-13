package org.ww.ai.backup;

import android.content.Context;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.List;

public abstract class AbstractBackupWriter implements BackupWriter<RenderResultLightWeight> {

    protected ListenableFuture<List<RenderResultLightWeight>> getRenderResultsFuture(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        return appDatabase.renderResultDao().getAllLightWeights(false);
    }

}
