package org.ww.ai.rds;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.ww.ai.rds.dao.RenderResultDao;
import org.ww.ai.rds.entity.RenderResult;

@Database(entities = {RenderResult.class}, exportSchema = true, version = 1,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2, spec = AppDatabase.MigrateRenderResult_1_2.class)
        })
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "aigen2";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if(instance == null) {
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
            db.execSQL("ALTER TABLE renderresult "
                    + " ADD COLUMN width INTEGER");
            db.execSQL("ALTER TABLE renderresult "
                    + " ADD COLUMN height INTEGER");
        }
    }

}
