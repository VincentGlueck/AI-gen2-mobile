package org.ww.ai.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract  class AbstractRenderResultViewHolder extends RecyclerView.ViewHolder {

    public ImageView thumbNail;
    public CheckBox checkBox;
    public boolean checked;
    public int position = -1;
    public int requestedPosition;

    public View rootView;

    public AbstractRenderResultViewHolder(@NonNull View itemView) {
        super(itemView);
        rootView = itemView;
        init();
    }

    protected abstract void init();

    @NonNull
    @Override
    public String toString() {
        return "RenderResultViewHolder{" +
                ", position: " + position +
                ", thumbNail.getWidth(): " + thumbNail.getWidth() +
                '}';
    }
}
