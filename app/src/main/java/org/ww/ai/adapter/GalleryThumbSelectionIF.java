package org.ww.ai.adapter;

import java.util.List;

public interface GalleryThumbSelectionIF {

    void thumbSelected(int idx, RenderResultViewHolder holder, boolean selected);

    boolean isSelectionMode();

    boolean isAnySelected();

    List<Integer> getSelectedThumbs();

    void deleteSelected(boolean useTrash, boolean flagUndeleted);

}
