package org.ww.ai.rds.entity;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class RenderResultSkeleton {

    @PrimaryKey
    public int uid;
    @ColumnInfo(name = "createdTime")
    public Long createdTime;
    @ColumnInfo(name = "deleted", typeAffinity = ColumnInfo.INTEGER, defaultValue = "false")
    public boolean deleted;

    public RenderResultSkeleton() {
    }

}
