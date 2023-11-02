package org.ww.ai.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;

public class AbstractRenderResultViewHolder extends RecyclerView.ViewHolder {

    public ImageView thumbNail;
    public CheckBox checkBox;
    public boolean checked;
    public int position = -1;
    public int requestedPosition;

    public AbstractRenderResultViewHolder(@NonNull View itemView) {
        super(itemView);
        thumbNail = itemView.findViewById(R.id.single_gallery_image_view);
        checkBox = itemView.findViewById(R.id.check_single_entry);
        checked = false;
    }

    @NonNull
    @Override
    public String toString() {
        return "RenderResultViewHolder{" +
                ", position: " + position +
                ", thumbNail.getWidth(): " + thumbNail.getWidth() +
                ", checkBox.isChecked():" + checkBox.isChecked() +
                '}';
    }
}
