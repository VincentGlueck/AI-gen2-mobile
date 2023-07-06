package org.ww.ai.rds.entity;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.ww.ai.rds.ifenum.RenderModel;

public class RenderResultLightWeight {

    public RenderResultLightWeight() {
    }

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "createdTime")
    public Long createdTime;

    @ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB)
    public byte[] thumbNail;

    @ColumnInfo(name = "render_engine")
    public RenderModel renderEngine;

    @ColumnInfo(name = "query_string")
    public String queryString;

    @Ignore
    public RenderResultLightWeight (RenderResult renderResult) {
        uid = renderResult.uid;
        createdTime = renderResult.createdTime;
        thumbNail = renderResult.thumbNail ;
        renderEngine = renderResult.renderEngine;
        queryString = renderResult.queryString;
    }

    @Ignore
    public static RenderResultLightWeight fromRenderResult(RenderResult renderResult) {
        return new RenderResultLightWeight(renderResult);
    }

}