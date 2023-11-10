package org.ww.ai.adapter;

import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.ww.ai.R;
import org.ww.ai.rds.PagingCache;
import org.ww.ai.rds.ifenum.GalleryAdapterCallbackIF;

import java.util.stream.Collectors;

public class GalleryAdapter extends GenericThumbnailAdapter<RenderResultViewHolder>
        implements GalleryThumbSelectionIF<RenderResultViewHolder>, GalleryAdapterCallbackIF {

    public GalleryAdapter(Context context,
                          DisplayMetrics displayMetrics,
                          OnGallerySelectionIF onGalleryThumbSelection,
                          int count,
                          boolean useTrash) {
        super(context, displayMetrics, onGalleryThumbSelection, count, useTrash);
    }

    @NonNull
    @Override
    public RenderResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View singleGalleryView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_gallery_image, parent, false);
        return new RenderResultViewHolder(singleGalleryView);
    }

    @Override
    public void onViewRecycled(@NonNull RenderResultViewHolder holder) {
        super.onViewRecycled(holder);
        holder.checkBox.setVisibility(mSelectionMode ? View.VISIBLE : View.GONE);
        holder.thumbNail.setScaleX(1.0f);
        holder.thumbNail.setScaleY(1.0f);
        int position = holder.getAbsoluteAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            holder.position = position;
        }
        if (mSelectionMode) {
            holder.checkBox.setChecked(mSelectedThumbs.stream().map(s -> s.position)
                    .collect(Collectors.toList()).contains(holder.position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultViewHolder holder,
                                 int position) {
        holder.checkBox.setVisibility(mSelectionMode ? View.VISIBLE : View.GONE);
        holder.position = holder.getAbsoluteAdapterPosition();
        holder.checkBox.setChecked(mSelectedThumbs.stream().map(s -> s.position)
                .collect(Collectors.toList()).contains(holder.getAbsoluteAdapterPosition()));
        holder.checkBox.setOnClickListener(v -> {
            CheckBox checkBox = (CheckBox) v;
            holder.checked = checkBox.isChecked();
            thumbSelected(position, holder, holder.checked);
        });
        holder.thumbNail.setOnLongClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            holder.checked = holder.checkBox.isChecked();
            thumbSelected(position, holder, holder.checked);
            return false;
        });
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

    @Override
    protected void displayThumbnail(@NonNull PagingCache.PagingEntry pagingEntry) {
        RenderResultViewHolder holder = getHolder(pagingEntry.requestPosition);
        if (holder == null) {
            return;
        }
        if (holder.requestedPosition != pagingEntry.idx) {
            Log.w("SKIP", "it's " + pagingEntry.idx + ", but I need " + holder.requestedPosition);
            return;
        }

        if(mSelectionMode) {
            holder.thumbNail.setScaleX(SCALE_SELECTED);
            holder.thumbNail.setScaleY(SCALE_SELECTED);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout
                .LayoutParams(mDisplayMetrics.widthPixels / 3, mDisplayMetrics.heightPixels / 5);
        holder.thumbNail.setLayoutParams(layoutParams);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(),
                new RoundedCorners(mSelectionMode ? 24 : 1));

        Glide.with(mContext)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(pagingEntry.renderResultLightWeight.thumbNail))
                .apply(requestOptions)
                .into(holder.thumbNail);
        mPosToUidMapping.put(holder.getAbsoluteAdapterPosition(), pagingEntry.renderResultLightWeight.uid);
    }

    @Override
    public int getPerRow() {
        return 3;
    }

}
