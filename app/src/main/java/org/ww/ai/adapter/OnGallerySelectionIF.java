package org.ww.ai.adapter;

public interface OnGallerySelectionIF {

    void thumbSelected(boolean selected, AbstractRenderResultViewHolder holder, int position);

    void onDeleteDone();

    void onImageClickListener(int uid);

}
