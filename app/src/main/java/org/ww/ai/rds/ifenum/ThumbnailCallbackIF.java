package org.ww.ai.rds.ifenum;

import android.widget.LinearLayout;

import org.ww.ai.rds.entity.RenderResultLightWeight;

public interface ThumbnailCallbackIF {

    int THUMBS_PER_ROW = 3;
    void setThumbnail(RenderResultLightWeight lightWeight);

    void processCleanup();

    boolean isUseDummies();

    void setUseDummies(boolean flagUseDummies);

    void onScrollPositionChanged(int scrollY, int oldScrollY);

    void notifyRowHeight(int height);

    LinearLayout getLinearLayoutByUid(int uid);

    void setCheckBoxesVisibilty(boolean visible);

    boolean isAnyCheckBoxChecked();

}
