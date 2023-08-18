package org.ww.ai.rds.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.ww.ai.rds.converter.EngineUsedNonDaoConverter;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.dao.RenderResultDao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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

    @ColumnInfo(name = "width", defaultValue = "0")
    public int width;

    @ColumnInfo(name = "height", defaultValue = "0")
    public int height;

    @ColumnInfo(name = "deleted", typeAffinity = ColumnInfo.INTEGER, defaultValue = "false")
    public boolean deleted;

    @TypeConverters(EngineUsedNonDaoConverter.class)
    @ColumnInfo(name = "engines_used")
    public List<EngineUsedNonDao> enginesUsed;

    @Ignore
    @JsonIgnore
    public RenderResult (RenderResultLightWeight lightWeight) {
        uid = lightWeight.uid;
        createdTime = lightWeight.createdTime;
        thumbNail = lightWeight.thumbNail ;
        queryString = lightWeight.queryString;
        queryUsed = lightWeight.queryUsed;
        width = lightWeight.width;
        height = lightWeight.height;
        deleted = lightWeight.deleted;
    }

    @Ignore
    @JsonIgnore
    public static RenderResult fromRenderResultLightWeight(RenderResultLightWeight lightWeight) {
        return new RenderResult(lightWeight);
    }

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
                ", deleted=" + deleted +
                '}';
    }
}
