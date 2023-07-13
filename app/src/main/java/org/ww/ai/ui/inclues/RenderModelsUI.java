package org.ww.ai.ui.inclues;

import android.content.Context;
import android.view.View;

import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.ifenum.RenderModel;

import java.util.List;

public interface RenderModelsUI {

    void init(Context context, View view);

    void setEngineList(List<EngineUsedNonDao> list);

    List<EngineUsedNonDao> getEngineList();

}
