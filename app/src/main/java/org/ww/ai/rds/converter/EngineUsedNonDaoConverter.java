package org.ww.ai.rds.converter;

import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.ww.ai.rds.dao.EngineUsedNonDao;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

public class EngineUsedNonDaoConverter implements Serializable {

    @TypeConverter
    public String fromEngineUsedNonDaoList(List<EngineUsedNonDao> list) {
        if (list == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<EngineUsedNonDao>>() {
        }.getType();
        return gson.toJson(list, type);
    }

    @TypeConverter
    public List<EngineUsedNonDao> toEngineUsedNonDaoList(String str) {
        if (str == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<EngineUsedNonDao>>() {
        }.getType();
        return gson.fromJson(str, type);
    }
}
