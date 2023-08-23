package org.ww.ai.rds.paging;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.adapter.RenderResultAdapter;
import org.ww.ai.rds.entity.RenderResultLightWeight;

public class RenderResultLightWeightViewHolder extends RecyclerView.ViewHolder {

    protected final TextView queryUsedTextView;
    private final TextView renderTitleTextView;
    private final ImageView thumb;
    private final TextView textViewDate;
    private final TextView textViewSizeLabel;
    private final LinearLayout linearLayout;
    private final View rootView;

    public RenderResultLightWeightViewHolder(@NonNull View view) {
        super(view);
        rootView = view;
        renderTitleTextView = view.findViewById(R.id.render_result_title);
        thumb = view.findViewById(R.id.history_render_result_thumb);
        textViewDate = view.findViewById(R.id.render_result_date);
        queryUsedTextView = view.findViewById(R.id.render_result_query_used);
        textViewSizeLabel = view.findViewById(R.id.image_view_size_lbl);
        linearLayout = view.findViewById(R.id.render_result_linear_layout);
    }

    public void bind(final RenderResultLightWeight item, final RenderResultAdapter.OnItemClickListener listener) {
        itemView.setOnClickListener(l -> listener.onItemClick(item));
    }

    public TextView getQueryStringTextView() {
        return renderTitleTextView;
    }

    public TextView getQueryUsedTextView() {
        return queryUsedTextView;
    }

    public ImageView getThumb() {
        return thumb;
    }

    public TextView getTextViewDate() {
        return textViewDate;
    }

    public TextView getTextViewSizeLabel() {
        return textViewSizeLabel;
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public View getRootView() {
        return rootView;
    }


}
