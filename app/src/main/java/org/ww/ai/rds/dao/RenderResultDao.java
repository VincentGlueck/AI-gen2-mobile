package org.ww.ai.rds.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.List;

@Dao
public interface RenderResultDao {

    String TABLE = "renderresult";

    @Query("SELECT uid, createdTime, thumbnail, render_engine, query_string, query_used, width," +
            " height, engines_used, deleted from " + TABLE + " ORDER BY createdTime DESC")
    ListenableFuture<List<RenderResultLightWeight>> getAllLightWeights();

    @Query("SELECT * from " + TABLE + " WHERE uid = :id")
    ListenableFuture<RenderResult> getById(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Integer> updateRenderResults(List<RenderResult> renderResults);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insertRenderResult(RenderResult renderResults);

    @Delete
    ListenableFuture<Integer> deleteRenderResults(List<RenderResult> renderResults);

}
