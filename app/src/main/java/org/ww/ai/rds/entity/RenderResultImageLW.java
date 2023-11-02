package org.ww.ai.rds.entity;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public class RenderResultImageLW {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

}
