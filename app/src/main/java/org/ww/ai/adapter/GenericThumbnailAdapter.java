package org.ww.ai.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;

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
import java.util.stream.Collectors;

public abstract class GenericThumbnailAdapter<T extends AbstractRenderResultViewHolder>
        extends RecyclerView.Adapter<T>
        implements GalleryThumbSelectionIF<T>,
        GalleryAdapterCallbackIF {

    public static final float SCALE_SELECTED = 0.94f;

    public static final int PER_ROW = 3;

    final RecyclerViewPagingCache mPagingCache;
    final Context mContext;
    final boolean mUseTrash;
    final OnGalleryThumbSelectionIF mOnGalleryThumbSelection;
    final List<SelectionHolder> mSelectedThumbs = new ArrayList<>();
    final List<ThumbLoadRequest<T>> mThumbRequests
            = Collections.synchronizedList(new ArrayList<>());
    final Map<Integer, T> mHolderMap = new HashMap<>();
    final Map<Integer, Integer> mPosToUidMapping = new HashMap<>();
    int mCount;
    boolean mSelectionMode;
    Boolean mLastSelectionMode = null;
    int mSelectionSize = 0;
    DisplayMetrics mDisplayMetrics;

    Float mFromX = null;
    Float mFromY = null;

    public GenericThumbnailAdapter(Context context,
                                   DisplayMetrics displayMetrics,
                                   OnGalleryThumbSelectionIF onGalleryThumbSelection,
                                   int count,
                                   boolean useTrash) {
        mContext = context;
        mDisplayMetrics = displayMetrics;
        mPagingCache = RecyclerViewPagingCache.getInstance(context);
        mOnGalleryThumbSelection = onGalleryThumbSelection;
        mCount = count;
        mUseTrash = useTrash;
    }

    protected abstract void displayThumbnail(@NonNull RecyclerViewPagingCache.PagingEntry pagingEntry);


    protected T getHolder(int requestedPosition) {
        T holder = mHolderMap.get(requestedPosition);
        mHolderMap.remove(requestedPosition);
        return holder;
    }

    public int getItemCount() {
        return mCount;
    }

    protected void updateVisibles(int position, T holder) {
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
    public void thumbSelected(int idx, T holder, boolean selected) {
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

    public int getThumbWidth() {
        return mDisplayMetrics.widthPixels / GenericThumbnailAdapter.PER_ROW;
    }

    public int getThumbHeight() {
        return mDisplayMetrics.heightPixels / (GenericThumbnailAdapter.PER_ROW + 2);
    }

    public Float getFromX() {
        return mFromX;
    }

    public void setFromX(float value) {
        mFromX = value;
    }

    public Float getFromY() {
        return mFromY;
    }

    public void setFromY(float value) {
        mFromY = value;
    }


static class ThumbLoadRequest<T extends AbstractRenderResultViewHolder> {
    public RecyclerViewPagingCache.PagingEntry pagingEntry;
    public AbstractRenderResultViewHolder holder;
    public int startIdx;

    public ThumbLoadRequest(RecyclerViewPagingCache.PagingEntry pagingEntry,
                            T holder) {
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

static class SelectionHolder {
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
