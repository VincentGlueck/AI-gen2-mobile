package org.ww.ai.adapter;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.ww.ai.R;
import org.ww.ai.rds.PagingCache;
import org.ww.ai.rds.dao.EngineUsedNonDao;
import org.ww.ai.ui.inclues.ShowRenderModelsLinearLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RenderHistoryAdapter extends GenericThumbnailAdapter<RenderHistoryViewHolder> {

    private final SimpleDateFormat simpleDateFormat =
            (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());

    public RenderHistoryAdapter(Context context,
                                DisplayMetrics displayMetrics,
                                OnGallerySelectionIF onGalleryThumbSelection,
                                int count,
                                boolean useTrash) {
        super(context, displayMetrics, onGalleryThumbSelection, count, useTrash);
    }

    @NonNull
    @Override
    public RenderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View singleRenderResultView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_render_result_row, parent, false);
        return new RenderHistoryViewHolder(singleRenderResultView);
    }

    @Override
    public void onBindViewHolder(@NonNull RenderHistoryViewHolder holder, int position) {
        holder.position = holder.getAbsoluteAdapterPosition();
        holder.thumbNail.setOnClickListener(l -> {
            int uid = mPosToUidMapping
                    .keySet().stream().filter(f -> f == holder.position)
                    .findFirst().map(mPosToUidMapping::get).orElse(-1);
            Log.w("UID", "sel on " + holder.position + " > uid = " + uid);
            mOnGalleryThumbSelection.onImageClickListener(uid);
        });
        holder.requestedPosition = holder.getAbsoluteAdapterPosition();

        doCacheManagement(holder, position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void displayThumbnail(@NonNull PagingCache.PagingEntry pagingEntry) {
        // TODO similar to GalleryAdapter's implementation
        RenderHistoryViewHolder holder = getHolder(pagingEntry.requestPosition);
        if (holder == null) {
            return;
        }
        if (holder.requestedPosition != pagingEntry.idx) {
            Log.w("SKIP", "it's " + pagingEntry.idx + ", but I need " + holder.requestedPosition);
            return;
        }

        holder.titleTextView.setText(pagingEntry.renderResultLightWeight.queryString);
        holder.dateTextView.setText(simpleDateFormat.format(
                new Date(pagingEntry.renderResultLightWeight.createdTime)));
        holder.dimensionTextView.setText(
                pagingEntry.renderResultLightWeight.width + " x " +
                        pagingEntry.renderResultLightWeight.height
        );
        holder.queryUsedTextView.setText(pagingEntry.renderResultLightWeight.queryString);

        List<EngineUsedNonDao> enginesUsed = pagingEntry.renderResultLightWeight.enginesUsed;
        if (enginesUsed != null && !enginesUsed.isEmpty()) {
            ShowRenderModelsLinearLayout enginesUsedView = new ShowRenderModelsLinearLayout(mContext);
            holder.flexboxLayout.removeAllViews();
            holder.flexboxLayout.invalidate();
            enginesUsedView.init(mContext, holder.rootView, enginesUsed);
            holder.flexboxLayout.addView(enginesUsedView);
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(),
                new RoundedCorners(mSelectionMode ? 24 : 1));

        Glide.with(mContext)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(pagingEntry.renderResultLightWeight.thumbNail))
                .apply(requestOptions)
                .override(mDisplayMetrics.widthPixels / 3)
                .into(holder.thumbNail);

        mPosToUidMapping.put(holder.getAbsoluteAdapterPosition(), pagingEntry.renderResultLightWeight.uid);
    }

    @Override
    public int getPerRow() {
        return 1;
    }

}
