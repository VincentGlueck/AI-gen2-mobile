package org.ww.ai.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import org.ww.ai.R;

public class RenderResultViewHolder extends AbstractRenderResultViewHolder {

    public RenderResultViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected void init() {
        thumbNail = itemView.findViewById(R.id.single_gallery_image_view);
        checkBox = itemView.findViewById(R.id.check_single_entry);
        checked = false;
    }
}
