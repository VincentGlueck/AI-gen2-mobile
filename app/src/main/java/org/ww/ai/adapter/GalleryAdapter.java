package org.ww.ai.adapter;

import static org.ww.ai.ui.Animations.ANIMATIONS;
import static org.ww.ai.ui.ImageUtil.IMAGE_UTIL;

import android.content.Context;
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
import org.ww.ai.rds.RecyclerViewPagingCache;
import org.ww.ai.rds.ifenum.GalleryAdapterCallbackIF;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class GalleryAdapter extends GenericThumbnailAdapter<RenderResultViewHolder>
        implements GalleryThumbSelectionIF<RenderResultViewHolder>, GalleryAdapterCallbackIF {

    private static final long FADE_TIME = 200L;

    public GalleryAdapter(Context context,
                          OnGalleryThumbSelectionIF onGalleryThumbSelection,
                          int count,
                          boolean useTrash) {
        super(context, onGalleryThumbSelection, count, useTrash);
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
        int position = holder.getAbsoluteAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            holder.position = position;
        }
        holder.checkBox.setChecked(mSelectedThumbs.stream().map(s -> s.position)
                .collect(Collectors.toList()).contains(holder.position));
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
        holder.requestedPosition = holder.getAbsoluteAdapterPosition();
        Optional<RecyclerViewPagingCache.PagingEntry> optional = mPagingCache.getPagingEntries()
                .stream().parallel().filter(p -> p.idx == position).findAny();
        if (optional.isPresent()) {
            mHolderMap.put(holder.requestedPosition, holder);
            displayThumbnail(optional.get());
            Log.d("SUCCESS", ">>> got " + position + ", uid=" + optional.get().renderResultLightWeight.uid);
        } else {
            boolean needsInc = needsIncrementCacheReload(position);
            boolean needsDec = needsDecrementCacheReload(position);
            Log.w("FAILURE", "<<< currently no thumb for " + position + ", forward:" + needsInc + ", backwards:" + needsDec);
            if (needsInc || needsDec) {
                mThumbRequests.add(new ThumbLoadRequest<RenderResultViewHolder>(new RecyclerViewPagingCache.PagingEntry(), holder));
                mPagingCache.fillCache(mContext, holder.requestedPosition, needsDec && !needsInc
                        ? position - RecyclerViewPagingCache.PAGE_SIZE
                        : position, this, mUseTrash, needsDec && !needsInc);
            }
            mHolderMap.put(holder.requestedPosition, holder);
        }
    }

    @Override
    protected void displayThumbnail(@NonNull RecyclerViewPagingCache.PagingEntry pagingEntry) {
        RenderResultViewHolder holder = getHolder(pagingEntry.requestPosition);
        if (holder == null) {
            Log.e("SORRY", "but no holder for " + pagingEntry);
            return;
        }
        if (holder.requestedPosition != pagingEntry.idx) {
            Log.w("SKIP", "it's " + pagingEntry.idx + ", but I need " + holder.requestedPosition);
            return;
        }
        if (mDisplayWidth == -1 && holder.thumbNail != null) {
            mDisplayWidth = ((View) holder.thumbNail.getParent().getParent()).getWidth();
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(),
                new RoundedCorners(mSelectionMode ? 8 : 1));
        if (mSelectionMode) {
            requestOptions = requestOptions.override(140);
        }

        if(mDisplayWidth > 0) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout
                    .LayoutParams(mDisplayWidth / 3, mDisplayWidth / 3);
            holder.thumbNail.setLayoutParams(layoutParams);
        }

        holder.thumbNail.startAnimation(
                ANIMATIONS.getAlphaAnimation(0.4f, 1.0f, FADE_TIME, true)
        );
        Glide.with(mContext)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(pagingEntry.renderResultLightWeight.thumbNail))
                .apply(requestOptions)
                .into(holder.thumbNail);
        mPosToUidMapping.put(holder.getAbsoluteAdapterPosition(), pagingEntry.renderResultLightWeight.uid);
    }

    private boolean needsIncrementCacheReload(int idx) {
        if (mThumbRequests.isEmpty()) {
            return true;
        }
        OptionalInt max = mThumbRequests.stream().mapToInt(t -> t.startIdx).max();
        int maxIdxAvail = max.getAsInt() + RecyclerViewPagingCache.PAGE_SIZE - 1;
        return idx > maxIdxAvail;
    }

    private boolean needsDecrementCacheReload(int idx) {
        if (mThumbRequests.isEmpty()) {
            return false;
        }
        OptionalInt min = mThumbRequests.stream().mapToInt(t -> t.startIdx).min();
        int minIdxAvail = min.getAsInt();
        return idx < minIdxAvail;
    }

}
