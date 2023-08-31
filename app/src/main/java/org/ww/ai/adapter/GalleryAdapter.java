package org.ww.ai.adapter;

import static org.ww.ai.ui.Animations.ANIMATIONS;
import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.ww.ai.R;
import org.ww.ai.rds.RecyclerViewPagingCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GalleryAdapter extends RecyclerView.Adapter<RenderResultViewHolder>
        implements GalleryThumbSelectionIF {

    private static final long FADE_TIME = 250L;
    private final RecyclerViewPagingCache mPagingCache;
    private final Context mContext;
    private final boolean mUseTrash;
    private final OnGalleryThumbSelectionIF mOnGalleryThumbSelection;
    private final List<Integer> mSelectedThumbs = new ArrayList<>();
    private int mCount;
    private boolean mSelectionMode;
    private Boolean mLastSelectionMode = null;
    private int mSelectionSize = 0;

    public GalleryAdapter(Context context, OnGalleryThumbSelectionIF onGalleryThumbSelection,
                          int count, boolean useTrash) {
        mContext = context;
        mPagingCache = RecyclerViewPagingCache.getInstance(context);
        mOnGalleryThumbSelection = onGalleryThumbSelection;
        mCount = count;
        mUseTrash = useTrash;
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
        holder.thumbNail.startAnimation(
                ANIMATIONS.getAlphaAnimation(1.0f, 0.0f, FADE_TIME, true)
        );
        holder.checkBox.setVisibility(mSelectionMode ? View.VISIBLE : View.GONE);
        int position = holder.getBindingAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            holder.position = position;
        }
        holder.checkBox.setChecked(mSelectedThumbs.contains(holder.position));
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultViewHolder holder, int position) {
        holder.checkBox.setVisibility(mSelectionMode ? View.VISIBLE : View.GONE);
        holder.position = holder.getAbsoluteAdapterPosition();
        holder.checkBox.setChecked(mSelectedThumbs.contains(holder.getAbsoluteAdapterPosition()));
        holder.checkBox.setOnClickListener(v -> {
            CheckBox checkBox = (CheckBox) v;
            holder.checked = checkBox.isChecked();
            thumbSelected(position, holder, holder.checked);
            updateVisibles(position, holder);
        });
        holder.thumbNail.setOnLongClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            holder.checked = holder.checkBox.isChecked();
            thumbSelected(position, holder, holder.checked);
            updateVisibles(position, holder);
            return false;
        });
        holder.requestedPosition = holder.getAbsoluteAdapterPosition();
        displayThumbnail(holder, position);
    }

    private void displayThumbnail(@NonNull RenderResultViewHolder holder, int position) {
        mPagingCache.displayThumbnail(mContext, position, (idx, lightWeight) -> {
            if(holder.requestedPosition != position) {
                return;
            }
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transform(new CenterCrop(),
                    new RoundedCorners(mSelectionMode ? 8 : 1));
            if(mSelectionMode) {
                requestOptions = requestOptions.override(140);
            }
            holder.thumbNail.startAnimation(
                    ANIMATIONS.getAlphaAnimation(0.1f, 1.0f, FADE_TIME, true)
            );
            Glide.with(mContext)
                    .asBitmap()
                    .load(IMAGE_UTIL.convertBlobToImage(lightWeight.thumbNail))
                    .apply(requestOptions)
                    .into(holder.thumbNail);
        }, mUseTrash);
    }


    @Override
    public int getItemCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    private void updateVisibles(int position, RenderResultViewHolder holder) {
        if(Boolean.valueOf(mSelectionMode).equals(mLastSelectionMode)) {
            return;
        }
        mSelectionMode = !mSelectedThumbs.isEmpty();
        mLastSelectionMode = null;
        Log.d("SELMODE", "mSelectionMode is " + mSelectionMode);
        if(mSelectedThumbs.size() == 0 || (mSelectedThumbs.size() == 1 && mSelectionSize == 0)) {
            mOnGalleryThumbSelection.thumbSelected(mSelectionMode, holder, position);
        }
    }

    @Override
    public void thumbSelected(int idx, RenderResultViewHolder holder, boolean selected) {
        mSelectionSize = mSelectedThumbs.size();
        if(selected && !mSelectedThumbs.contains(idx)) {
            mSelectedThumbs.add(idx);
        } else if (!selected) {
            Optional<Integer> optional = mSelectedThumbs.stream().filter(f -> f == idx).findFirst();
            optional.ifPresent(mSelectedThumbs::remove);
            mSelectionMode = !mSelectedThumbs.isEmpty();
            mLastSelectionMode = null;
            Log.d("REMOVE", "no longer selected: " + idx + ", selMode: " + mSelectionMode);
        }
        updateVisibles(idx, holder);
    }


    public boolean isSelectionMode() {
        return mSelectionMode;
    }

}
