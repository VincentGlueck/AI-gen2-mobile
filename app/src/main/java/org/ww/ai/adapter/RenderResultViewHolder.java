package org.ww.ai.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.ww.ai.R;

public class RenderResultViewHolder extends AbstractRenderResultViewHolder {

    public ImageView thumbNail;
    public CheckBox checkBox;
    public boolean checked;
    public int position = -1;
    public int requestedPosition;

    public RenderResultViewHolder(@NonNull View itemView) {
        super(itemView);
        thumbNail = itemView.findViewById(R.id.single_gallery_image_view);
        checkBox = itemView.findViewById(R.id.check_single_entry);
        checked = false;
    }

    @NonNull
    @Override
    public String toString() {
        return "RenderResultViewHolder{" +
                "checked: " + checked +
                ", thumbNail=" + thumbNail +
                ", checkBox=" + checkBox +
                '}';
    }
}