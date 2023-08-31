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

    @Query("SELECT uid, createdTime, thumbnail, query_string, query_used, width," +
            " height, engines_used, deleted FROM " + TABLE +
            " WHERE deleted = :flagDeleted ORDER BY uid")
    ListenableFuture<List<RenderResultLightWeight>> getAllLightWeights(boolean flagDeleted);

    @Query("SELECT uid, createdTime, thumbnail, query_string, query_used, width," +
            " height, engines_used, deleted FROM " + TABLE +
            " WHERE uid IN (:ids)")
    ListenableFuture<List<RenderResultLightWeight>> getLightWeightByIds(List<String> ids);

    @Query("SELECT * FROM " + TABLE + " WHERE uid = :id")
    ListenableFuture<RenderResult> getById(int id);

    @Query("SELECT * FROM " + TABLE + " WHERE deleted = 0")
    ListenableFuture<List<RenderResult>> getAll();

    @Query("SELECT * FROM " + TABLE + " WHERE deleted = 0")
    List<RenderResult> getAllOnThread();

    @Query("SELECT * FROM " + TABLE + " WHERE uid = :id")
    RenderResult getByIdOnThread(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRenderResultOnThread(RenderResult renderResults);

    @Query("SELECT uid, createdTime, thumbnail, query_string, query_used, width," +
            " height, engines_used, deleted FROM " + TABLE +
            " WHERE deleted = :flagDeleted ORDER BY createdTime LIMIT 1 OFFSET :offset")
    ListenableFuture<List<RenderResultLightWeight>> getPagedRenderResultsLw(int offset, boolean flagDeleted);

    @Query("SELECT COUNT(1) FROM " + TABLE)
    int getCount();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Integer> updateRenderResults(List<RenderResult> renderResults);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insertRenderResult(RenderResult renderResult);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    ListenableFuture<List<Long>> insertRenderResults(List<RenderResult> renderResults);

    @Delete
    ListenableFuture<Integer> deleteRenderResults(List<RenderResult> renderResults);

    // **** USE WITH CARE! ****
    @Query("DELETE FROM " + TABLE + " WHERE deleted = 1")
    ListenableFuture<Integer> emptyTrash();

}
