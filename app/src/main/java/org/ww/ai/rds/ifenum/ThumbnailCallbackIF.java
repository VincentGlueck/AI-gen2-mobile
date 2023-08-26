package org.ww.ai.rds.ifenum;

import org.ww.ai.rds.entity.RenderResultLightWeight;

public interface ThumbnailCallbackIF {

    int THUMBS_PER_ROW = 3;
    void setThumbnail(RenderResultLightWeight lightWeight);

    void processCleanup();

    boolean isUseDummyImages();

    void setUseDummyImages(boolean flagUseDummies);

    void onScrollPositionChanged(int scrollY, int oldScrollY);

    void notifyRowHeight(int height);
}
