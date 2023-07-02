package org.ww.ai.rds.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.ww.ai.rds.entity.RenderResult;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import kotlinx.coroutines.flow.Flow;


@Dao
public interface RenderResultDao {

    public static final String TABLE = "renderresult";

    @Query("SELECT * from " + TABLE)
    LiveData<List<RenderResult>> getAll();

    //@Query("SELECT uid, createdTime from " + TABLE + " ORDER BY createdTime DESC")
    //List<Integer> getAllUids();

    // @Query("SELECT uid, createdTime, thumbnail from " + TABLE + " ORDER BY createdTime DESC")
    // List<RenderResultLightWeight> getAllLightWeights();

    //@Query("SELECT * from " + TABLE + " WHERE uid = :uid")
    //Flow<RenderResult> findById(String uid);

    @Insert
    void insertAll(RenderResult... renderResults);

    @Delete
    void delete(RenderResult renderResult);

}
