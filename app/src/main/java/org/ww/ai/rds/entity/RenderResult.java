package org.ww.ai.rds.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.ww.ai.rds.dao.RenderResultDao;
import org.ww.ai.rds.ifenum.RenderModel;

import java.io.Serializable;
import java.util.Arrays;

@Entity(tableName = RenderResultDao.TABLE)
public class RenderResult implements Serializable {

    public RenderResult() {
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "createdTime")
    public Long createdTime;

    @ColumnInfo(name = "query_string")
    public String queryString;

    @ColumnInfo(name = "query_used")
    public String queryUsed;

    @ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB)
    public byte[] thumbNail;

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    @ColumnInfo(name = "render_engine")
    public RenderModel renderEngine;

    @ColumnInfo(name = "credits", typeAffinity = ColumnInfo.INTEGER)
    public int credits;

    @ColumnInfo(name = "width", defaultValue = "0")
    public int width;

    @ColumnInfo(name = "height", defaultValue = "0")
    public int height;

    @NonNull
    @Override
    public String toString() {
        return "RenderResult{" +
                "uid=" + uid +
                ", createdTime=" + createdTime +
                ", queryString='" + queryString + '\'' +
                ", queryUsed='" + queryUsed + '\'' +
                ", thumbNail=" + Arrays.toString(thumbNail) +
                ", image=" + Arrays.toString(image) +
                ", renderEngine=" + renderEngine +
                ", credits=" + credits +
                '}';
    }
}
