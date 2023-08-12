package org.ww.ai.rds.entity;

import android.util.Pair;
import android.widget.CheckBox;

import androidx.annotation.Dimension;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.checkerframework.checker.units.qual.C;
import org.ww.ai.rds.converter.EngineUsedNonDaoConverter;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.ifenum.RenderModel;

import java.io.Serializable;
import java.util.List;

public class RenderResultLightWeight implements Serializable {

    public RenderResultLightWeight() {
    }

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "createdTime")
    public Long createdTime;

    @ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB)
    public byte[] thumbNail;

    @ColumnInfo(name = "query_string")
    public String queryString;

    @ColumnInfo(name = "query_used")
    public String queryUsed;

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
    public boolean flagHighLight;

    @Ignore
    public CheckBox checkBox;

    @Ignore
    public RenderResultLightWeight (RenderResult renderResult) {
        uid = renderResult.uid;
        createdTime = renderResult.createdTime;
        thumbNail = renderResult.thumbNail ;
        queryString = renderResult.queryString;
        queryUsed = renderResult.queryUsed;
        width = renderResult.width;
        height = renderResult.height;
        deleted = renderResult.deleted;
    }

    @Ignore
    public static RenderResultLightWeight fromRenderResult(RenderResult renderResult) {
        return new RenderResultLightWeight(renderResult);
    }

}
