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
import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.R;
import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.RecyclerViewPagingCache;
import org.ww.ai.rds.entity.RenderResult;
import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.rds.ifenum.GalleryAdapterCallbackIF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class GalleryAdapter extends RecyclerView.Adapter<RenderResultViewHolder>
        implements GalleryThumbSelectionIF, GalleryAdapterCallbackIF {

    private static final long FADE_TIME = 250L;
    private final RecyclerViewPagingCache mPagingCache;
    private final Context mContext;
    private final boolean mUseTrash;
    private final OnGalleryThumbSelectionIF mOnGalleryThumbSelection;
    private final List<SelectionHolder> mSelectedThumbs = new ArrayList<>();
    private final int mCount;
    private boolean mSelectionMode;
    private Boolean mLastSelectionMode = null;
    private int mSelectionSize = 0;
    private final List<ThumbLoadRequest> mThumbRequests = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, RenderResultViewHolder> mHolderMap = new HashMap<>();
    private final Map<Integer, Integer> mPosToUidMapping = new HashMap<>();

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
        int position = holder.getAbsoluteAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            holder.position = position;
        }
        holder.checkBox.setChecked(mSelectedThumbs.stream().map(s -> s.position)
                .collect(Collectors.toList()).contains(holder.position));
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultViewHolder holder, int position) {
        holder.checkBox.setVisibility(mSelectionMode ? View.VISIBLE : View.GONE);
        holder.position = holder.getAbsoluteAdapterPosition();
        holder.checkBox.setChecked(mSelectedThumbs.stream().map(s -> s.position)
                .collect(Collectors.toList()).contains(holder.getAbsoluteAdapterPosition()));
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
        Optional<RecyclerViewPagingCache.PagingEntry> optional = mPagingCache.getPagingEntries()
                .stream().parallel().filter(p -> p.idx == position).findAny();
        if (optional.isPresent()) {
            mHolderMap.put(holder.requestedPosition, holder);
            displayThumbnail(optional.get());
        } else {
            boolean needsInc = needsIncrementCacheReload(position);
            boolean needsDec = needsDecrementCacheReload(position);
            if(needsInc || needsDec) {
                mThumbRequests.add(new ThumbLoadRequest(new RecyclerViewPagingCache.PagingEntry(), holder));
                mPagingCache.fillCache(mContext, holder.requestedPosition, needsDec && !needsInc
                        ? position-RecyclerViewPagingCache.PAGE_SIZE
                        : position, this, mUseTrash, needsDec && !needsInc);
            }
            mHolderMap.put(holder.requestedPosition, holder);
        }
    }


    private void displayThumbnail(@NonNull RecyclerViewPagingCache.PagingEntry pagingEntry) {
        RenderResultViewHolder holder = getHolder(pagingEntry.requestPosition);
        if(holder == null) {
            Log.e("SORRY", "but no holder for " + pagingEntry);
            return;
        }
        if (holder.requestedPosition != pagingEntry.idx) {
            Log.w("SKIP", "it's " + pagingEntry.idx + ", but I need " + holder.requestedPosition);
            return;
        }
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(),
                new RoundedCorners(mSelectionMode ? 8 : 1));
        if (mSelectionMode) {
            requestOptions = requestOptions.override(140);
        }
        holder.thumbNail.startAnimation(
                ANIMATIONS.getAlphaAnimation(0.1f, 1.0f, FADE_TIME, true)
        );
        Glide.with(mContext)
                .asBitmap()
                .load(IMAGE_UTIL.convertBlobToImage(pagingEntry.renderResultLightWeight.thumbNail))
                .apply(requestOptions)
                .into(holder.thumbNail);
        mPosToUidMapping.put(holder.getAbsoluteAdapterPosition(), pagingEntry.renderResultLightWeight.uid);
    }

    private RenderResultViewHolder getHolder(int requestedPosition) {
        RenderResultViewHolder holder = mHolderMap.get(requestedPosition);
        mHolderMap.remove(requestedPosition);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    private void updateVisibles(int position, RenderResultViewHolder holder) {
        if (Boolean.valueOf(mSelectionMode).equals(mLastSelectionMode)) {
            return;
        }
        mSelectionMode = !mSelectedThumbs.isEmpty();
        mLastSelectionMode = null;
        if (mSelectedThumbs.size() == 0 || (mSelectedThumbs.size() == 1 && mSelectionSize == 0)) {
            mOnGalleryThumbSelection.thumbSelected(mSelectionMode, holder, position);
        }
    }

    @Override
    public void thumbSelected(int idx, RenderResultViewHolder holder, boolean selected) {
        mSelectionSize = mSelectedThumbs.size();
        if (selected && !mSelectedThumbs.stream().map(s -> s.position)
                .collect(Collectors.toList()).contains(idx)) {
            Optional<Integer> optional = mPosToUidMapping.keySet().stream()
                    .filter(m -> m == idx).findFirst();
            if (optional.isPresent()) {
                Integer uid = mPosToUidMapping.getOrDefault(optional.get(), null);
                if (uid != null) {
                    mSelectedThumbs.add(new SelectionHolder(idx, uid));
                } else {
                    Log.w("WARN_IDX", "No UID found for position " + idx);
                }
            }
        } else if (!selected) {
            mSelectedThumbs.stream().filter(f -> f.position == idx)
                    .findFirst().ifPresent(mSelectedThumbs::remove);
            mSelectionMode = !mSelectedThumbs.isEmpty();
            mLastSelectionMode = null;
        }
        updateVisibles(holder.getAbsoluteAdapterPosition(), holder);
    }

    @Override
    public boolean isAnySelected() {
        return !mSelectedThumbs.isEmpty();
    }

    @Override
    public boolean isSelectionMode() {
        return mSelectionMode;
    }

    @Override
    public List<Integer> getSelectedThumbs() {
        return mSelectedThumbs.stream().map(s -> s.position).collect(Collectors.toList());
    }

    @Override
    public void deleteSelected(boolean useTrash, boolean flagUndeleted) {
        if (mSelectedThumbs.isEmpty()) {
            return;
        }
        if (useTrash) {
            softDeletedSelected(flagUndeleted);
        }
        hardDeleteSelected();
    }

    private void hardDeleteSelected() {
        //return 0;
    }

    private void softDeletedSelected(boolean flagUndelete) {
        List<String> ids = mSelectedThumbs.stream().map(i -> mPosToUidMapping.get(i.position))
                .map(String::valueOf).collect(Collectors.toList());
        mSelectedThumbs.clear();
        ListenableFuture<List<RenderResultLightWeight>> future =
                getAppDatabase().renderResultDao().getLightWeightByIds(ids);
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            List<RenderResult> renderResults = new ArrayList<>();
            result.forEach(lw -> {
                RenderResult renderResult = RenderResult.fromRenderResultLightWeight(lw);
                renderResult.deleted = !flagUndelete;
                renderResults.add(renderResult);
            });
            ListenableFuture<Integer> listenableFuture = getAppDatabase()
                    .renderResultDao().updateRenderResults(renderResults);
            AsyncDbFuture<Integer> updateFuture = new AsyncDbFuture<>();
            updateFuture.processFuture(listenableFuture, i -> refreshAdapter(), mContext);
        }, mContext);
    }

    private void refreshAdapter() {
        Log.w("REFRESH", "Should now refresh the adapter view");
    }


    private AppDatabase getAppDatabase() {
        return AppDatabase.getInstance(mContext);
    }

    @Override
    public void onCachingDone(List<RecyclerViewPagingCache.PagingEntry> pagingEntries) {
        pagingEntries.forEach(this::displayThumbnail);
        mThumbRequests.clear();
    }

    private boolean needsIncrementCacheReload(int idx) {
        if(mThumbRequests.isEmpty()) {
            return true;
        }
        OptionalInt max = mThumbRequests.stream().mapToInt(t -> t.startIdx).max();
        int maxIdxAvail = max.getAsInt() + RecyclerViewPagingCache.PAGE_SIZE - 1;
        return idx > maxIdxAvail;
    }

    private boolean needsDecrementCacheReload(int idx) {
        if(mThumbRequests.isEmpty()) {
            return true;
        }
        OptionalInt min = mThumbRequests.stream().mapToInt(t -> t.startIdx).min();
        int minIdxAvail = min.getAsInt();
        return idx < minIdxAvail;
    }

private static class ThumbLoadRequest {
    public RecyclerViewPagingCache.PagingEntry pagingEntry;
    public RenderResultViewHolder holder;
    public int startIdx;

    public ThumbLoadRequest(RecyclerViewPagingCache.PagingEntry pagingEntry,
                            RenderResultViewHolder holder) {
        this.pagingEntry = pagingEntry;
        this.holder = holder;
        this.startIdx = holder.getAbsoluteAdapterPosition();
    }

    @NonNull
    @Override
    public String toString() {
        return "ThumbLoadRequest{" +
                "pagingEntry=" + pagingEntry +
                ", holder=" + holder +
                '}';
    }
}

private static class SelectionHolder {
    public int position;
    public int uid;

    public SelectionHolder(int position, int uid) {
        this.position = position;
        this.uid = uid;
    }

    @NonNull
    @Override
    public String toString() {
        return "SelectionHolder{" +
                "position=" + position +
                ", uid=" + uid +
                '}';
    }
}
}
