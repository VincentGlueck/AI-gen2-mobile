package org.ww.ai.adapter;

import static org.ww.ai.ui.Animations.ANIMATIONS;
import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.ww.ai.R;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.ui.inclues.ShowRenderModelsLinearLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RenderResultAdapter extends RecyclerView.Adapter<RenderResultAdapter.ViewHolder> {

    private static final int PREVIEW_SIZE = 272;
    private final List<RenderResultLightWeight> localDataSet;
    private final OnItemClickListener listener;

    private final DateFormat dateFormat;

    private final Context context;


    public RenderResultAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    public void addRenderResults(List<RenderResultLightWeight> renderResults) {
        if (renderResults == null || renderResults.isEmpty()) {
            Log.d("ADD_RENDER_RESULTS", "Attempt to add null or empty list of RenderResults");
            return;
        }
        int oldLength = localDataSet.size();
        localDataSet.addAll(renderResults);
        notifyItemRangeInserted(oldLength, localDataSet.size());
    }

    public void removeResult(int position) {
        if (position >= 0 && position < localDataSet.size()) {
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
        viewHolder.getQueryStringTextView().setText(item.queryString);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(16));
        Glide.with(context)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(item.thumbNail))
                .override(PREVIEW_SIZE)
                .apply(requestOptions)
                .into(viewHolder.getThumb());
        viewHolder.getTextViewSizeLabel().setText(item.width + "x" + item.height);
        List<EngineUsedNonDao> enginesUsed = item.enginesUsed;
        if(enginesUsed != null && !enginesUsed.isEmpty()) {
            ShowRenderModelsLinearLayout enginesUsedView = new ShowRenderModelsLinearLayout(context);
            enginesUsedView.init(context, viewHolder.getRootView());
            viewHolder.getLinearLayout().addView(enginesUsedView);
            enginesUsedView.setEngineList(enginesUsed);
        }
        SpannableString spanString = new SpannableString(dateFormat.format(
                new Date(item.createdTime)));
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        viewHolder.getTextViewDate().setText(spanString);
        viewHolder.getQueryUsedTextView().setText(item.queryUsed);
        if (item.flagHighLight) {
            final AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(ANIMATIONS.getAlphaAnimation(0.4F, 1.0F, 1000L, true));
            animationSet.addAnimation(ANIMATIONS.getScaleAnimation(0.3F, 1.0F, 700L, true));
            viewHolder.getRootView().setAnimation(animationSet);
            animationSet.start();
            item.flagHighLight = false;
        }
        viewHolder.bind(item, listener);
    }

    public int getPositionOfUid(int uid) {
        for (int n = 0; n < localDataSet.size(); n++) {
            if (localDataSet.get(n).uid == uid) {
                return n;
            }
        }
        return -1;
    }


    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void highLightNewRow(int position) {
        localDataSet.get(position).flagHighLight = true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView renderTitleTextView;
        private final ImageView thumb;
        protected final TextView queryUsedTextView;
        private final TextView textViewDate;
        private final TextView textViewSizeLabel;
        private final LinearLayout linearLayout;
        private final View rootView;


        public ViewHolder(View view) {
            super(view);
            rootView = view;
            renderTitleTextView = view.findViewById(R.id.render_result_title);
            thumb = view.findViewById(R.id.history_render_result_thumb);
            textViewDate = view.findViewById(R.id.render_result_date);
            queryUsedTextView = view.findViewById(R.id.render_result_query_used);
            textViewSizeLabel = view.findViewById(R.id.image_view_size_lbl);
            linearLayout = view.findViewById(R.id.render_result_linear_layout);
        }

        public void bind(final RenderResultLightWeight item, final OnItemClickListener listener) {
            itemView.setOnClickListener(l -> {
                listener.onItemClick(item);
            });
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

    public interface OnItemClickListener {
        void onItemClick(RenderResultLightWeight item);
    }
}
