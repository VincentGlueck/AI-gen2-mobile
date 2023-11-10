package org.ww.ai.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.flexbox.FlexboxLayout;

import org.ww.ai.R;

public class RenderHistoryViewHolder extends AbstractRenderResultViewHolder {

    public TextView titleTextView;
    public TextView dateTextView;
    public TextView dimensionTextView;
    public TextView queryUsedTextView;
    public FlexboxLayout flexboxLayout;


    public RenderHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected void init() {
        thumbNail = itemView.findViewById(R.id.history_render_result_thumb);
        titleTextView = itemView.findViewById(R.id.render_result_title);
        dateTextView = itemView.findViewById(R.id.render_result_date);
        dimensionTextView = itemView.findViewById(R.id.image_view_size_lbl);
        queryUsedTextView = itemView.findViewById(R.id.render_result_query_used);
        flexboxLayout = itemView.findViewById(R.id.render_details_include);
    }

    @Override
    public String toString() {
        return "RenderHistoryViewHolder{" +
                "titleTextView=" + titleTextView.getText() +
                ", dateTextView=" + dateTextView.getText() +
                ", dimensionTextView=" + dimensionTextView.getText() +
                ", queryUsedTextView=" + queryUsedTextView.getText() +
                ", position=" + position +
                ", requestedPosition=" + requestedPosition +
                '}';
    }
}
