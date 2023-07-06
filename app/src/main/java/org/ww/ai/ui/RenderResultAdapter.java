package org.ww.ai.ui;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ww.ai.R;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RenderResultAdapter extends RecyclerView.Adapter<RenderResultAdapter.ViewHolder> {

    private final List<RenderResultLightWeight> localDataSet;
    private final OnItemClickListener listener;

    private final DateFormat dateFormat;

    public RenderResultAdapter(Context context, RenderResultAdapter.OnItemClickListener listener) {
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    public void addRenderResults(List<RenderResultLightWeight> renderResults) {
        if(renderResults == null || renderResults.isEmpty()) {
            Log.d("ADD_RENDER_RESULTS", "Attempt to add null or empty list of RenderResults");
            return;
        }
        int oldLength = localDataSet.size();
        localDataSet.addAll(renderResults);
        notifyItemRangeInserted(oldLength, localDataSet.size());
    }

    public void removeResult(int position) {
        if(position >= 0 && position < localDataSet.size()) {
            localDataSet.remove(position);
            notifyItemRemoved(position);
        } else {
            Log.e("REMOVE_RESULT", "with position " + position + ", but localDataSet.size() is " + localDataSet.size());
        }
    }

    public void restoreResult(RenderResultLightWeight renderResult, int position) {
        localDataSet.add(position, renderResult);
        notifyItemInserted(position);
    }

    public RenderResultLightWeight itemAt(int position) {
        return localDataSet.get(position);
    }

    @NonNull
    @Override
    public RenderResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_render_result_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RenderResultAdapter.ViewHolder viewHolder, int position) {
        RenderResultLightWeight item = localDataSet.get(position);
        viewHolder.getTextView().setText(item.queryString);
        viewHolder.getThumb().setImageBitmap(IMAGE_UTIL.convertBlobToImage(item.thumbNail));
        viewHolder.getTextViewDate().setText(dateFormat.format(new Date(item.createdTime)));
        viewHolder.uidTextView.setText("id: " + item.uid);
        viewHolder.bind(item, listener);
    }



    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView thumb;

        protected final TextView uidTextView;

        private final TextView textViewDate;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.render_result_title);
            thumb = view.findViewById(R.id.history_render_result_thumb);
            textViewDate = view.findViewById(R.id.render_result_date);
            uidTextView = view.findViewById(R.id.render_result_uid);
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

        public TextView getTextViewDate() {
            return textViewDate;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RenderResultLightWeight item);
    }
}
