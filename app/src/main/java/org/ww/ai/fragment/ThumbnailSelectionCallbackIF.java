package org.ww.ai.fragment;

import android.widget.LinearLayout;

import org.ww.ai.rds.entity.RenderResultLightWeight;

public interface ThumbnailSelectionCallbackIF {

    void initSingleImageView(RenderResultLightWeight lightWeight, LinearLayout layoutHolder);

    void finishedRender(RenderResultLightWeight lightWeight, LinearLayout singleImageLayout);

}
