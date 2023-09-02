package org.ww.ai.adapter;

import java.util.List;

public interface GalleryThumbSelectionIF<T extends AbstractRenderResultViewHolder> {

    void thumbSelected(int idx, T holder, boolean selected);

    boolean isSelectionMode();

    boolean isAnySelected();

    List<Integer> getSelectedThumbs();

    void deleteSelected(boolean useTrash, boolean flagUndeleted);

}
