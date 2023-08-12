package org.ww.ai.rds;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.ww.ai.rds.converter.EngineUsedNonDaoConverter;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.dao.RenderResultDao;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.ifenum.RenderModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Database(entities = {RenderResult.class}, version = 9,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2, spec = AppDatabase.MigrateRenderResult_1_2.class),
                @AutoMigration(from = 3, to = 4, spec = AppDatabase.MigrateRenderResult_3_4.class),
                @AutoMigration(from = 5, to = 6, spec = AppDatabase.MigrateRenderResult_5_6.class)
        })
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "ai-gen-2";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return instance;
    }

    public abstract RenderResultDao renderResultDao();


    static class MigrateRenderResult_1_2 implements AutoMigrationSpec {
        @Override
        public void onPostMigrate(@NonNull SupportSQLiteDatabase db) {
            AutoMigrationSpec.super.onPostMigrate(db);
            db.beginTransaction();
            db.execSQL("ALTER TABLE " + RenderResultDao.TABLE
                    + " ADD COLUMN width INTEGER");
            db.execSQL("ALTER TABLE " + RenderResultDao.TABLE
                    + " ADD COLUMN height INTEGER");
            db.endTransaction();
        }
    }

    static class MigrateRenderResult_3_4 implements AutoMigrationSpec {
        @Override
        public void onPostMigrate(@NonNull SupportSQLiteDatabase db) {
            AutoMigrationSpec.super.onPostMigrate(db);
            Map<Integer, String> updateMap = new HashMap<>();
            EngineUsedNonDaoConverter converter = new EngineUsedNonDaoConverter();
            db.beginTransaction();
            Cursor cursor = db.query("SELECT uid, render_engine, credits FROM "
                    + RenderResultDao.TABLE);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int uid = cursor.getInt(0);
                String renderEngine = cursor.getString(1);
                int renderCosts = cursor.getInt(2);
                EngineUsedNonDao engineUsedNonDao = new EngineUsedNonDao();
                engineUsedNonDao.renderModel = RenderModel.valueOf(renderEngine);
                engineUsedNonDao.credits = renderCosts;
                List<EngineUsedNonDao> list = List.of(engineUsedNonDao);
                String json = converter.fromEngineUsedNonDaoList(list);
                updateMap.put(uid, json);
            }
            for (Integer idx : updateMap.keySet()) {
                String sqlString = "UPDATE " + RenderResultDao.TABLE + " SET engines_used = '" +
                        updateMap.get(idx) + "' WHERE uid = " + idx;
                db.execSQL(sqlString);
            }
            ;
            db.endTransaction();
        }
    }

    static class MigrateRenderResult_5_6 implements AutoMigrationSpec {
        @Override
        public void onPostMigrate(@NonNull SupportSQLiteDatabase db) {
            AutoMigrationSpec.super.onPostMigrate(db);
            db.beginTransaction();
            db.execSQL("ALTER TABLE " + RenderResultDao.TABLE
                    + " ADD COLUMN deleted BOOLEAN");
            db.endTransaction();
        }
    }

}
