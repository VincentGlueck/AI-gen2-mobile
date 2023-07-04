package org.ww.ai.rds.dao;

import androidx.lifecycle.LiveData;
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

    public static final String TABLE = "renderresult";

    @Query("SELECT * from " + TABLE)
    ListenableFuture<List<RenderResult>> getAll();

    //@Query("SELECT uid, createdTime from " + TABLE + " ORDER BY createdTime DESC")
    //List<Integer> getAllUids();

    @Query("SELECT uid, createdTime, thumbnail, render_engine, query_string from " + TABLE + " ORDER BY createdTime DESC")
    ListenableFuture<List<RenderResultLightWeight>> getAllLightWeights();

    @Query("SELECT * from " + TABLE + " WHERE uid = :id")
    ListenableFuture<RenderResult> getById(int id);

    //@Query("SELECT * from " + TABLE + " WHERE uid = :uid")
    //Flow<RenderResult> findById(String uid);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Integer> updateRenderResults(List<RenderResult> renderResults);

    @Insert
    ListenableFuture<Void> insertRenderResult(RenderResult... renderResults);

    @Delete
    ListenableFuture<Integer> deleteRenderResults(List<RenderResult> renderResults);

}
