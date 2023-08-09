package org.ww.ai.ui.inclues;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import org.ww.ai.rds.dao.EngineUsedNonDao;

import java.util.List;

public interface RenderModelsUI {

    void init(Context context, View view);

    void init(Context context, @NonNull View view, @NonNull List<EngineUsedNonDao> enginesUsed);

    void setEngineList(List<EngineUsedNonDao> list);

    List<EngineUsedNonDao> getmEngineList();

}
