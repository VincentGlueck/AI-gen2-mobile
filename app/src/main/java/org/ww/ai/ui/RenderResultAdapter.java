package org.ww.ai.ui;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.activity.RenderResultDetailsActivity;
import org.ww.ai.activity.RenderResultsActivity;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.ArrayList;
import java.util.List;

public class RenderResultAdapter extends RecyclerView.Adapter<RenderResultAdapter.ViewHolder> {

    private final List<RenderResultLightWeight> localDataSet;
    private final OnItemClickListener listener;

    public RenderResultAdapter(RenderResultAdapter.OnItemClickListener listener) {
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
    }

    public void addRenderResults(List<RenderResult> renderResults) {

        if(renderResults == null || renderResults.isEmpty()) {
            Log.d("ADD_RENDER_RESULTS", "Attempt to add null or empty list of RenderResults");
            return;
        }
        renderResults.forEach(r -> {
            localDataSet.add(new RenderResultLightWeight(r));
        });
    }

    @NonNull
    @Override
    public RenderResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.render_result_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultAdapter.ViewHolder viewHolder, int position) {
        viewHolder.getTextView().setText(localDataSet.get(position).queryString);
        viewHolder.getThumb().setImageBitmap(IMAGE_UTIL.convertBlobToImage(localDataSet.get(position).thumbNail));
        RenderResultLightWeight item = localDataSet.get(position);
        viewHolder.bind(item, listener);
    }



    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView thumb;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.render_result_title);
            thumb = view.findViewById(R.id.history_render_result_thumb);
            View row = view.findViewById(R.id.render_result_row);

        }

        public void bind(final RenderResultLightWeight item, final OnItemClickListener listener) {
            itemView.setOnClickListener(l -> {
                listener.onItemClick(item);
            });
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getThumb() {
            return thumb;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RenderResultLightWeight item);
    }
}
