package org.ww.ai.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.AsyncDbFuture;
import org.ww.ai.rds.PagingCache;
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

public abstract class GenericThumbnailAdapter<T extends AbstractRenderResultViewHolder>
        extends RecyclerView.Adapter<T>
        implements GalleryThumbSelectionIF<T>,
        GalleryAdapterCallbackIF {

    public static final float SCALE_SELECTED = 0.94f;

    final PagingCache mPagingCache;
    final Context mContext;
    final boolean mUseTrash;
    final OnGallerySelectionIF mOnGalleryThumbSelection;
    final List<SelectionHolder> mSelectedThumbs = new ArrayList<>();
    final List<ThumbLoadRequest<?>> mThumbRequests
            = Collections.synchronizedList(new ArrayList<>());
    final Map<Integer, AbstractRenderResultViewHolder> mHolderMap = new HashMap<>();
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
                                   OnGallerySelectionIF onGalleryThumbSelection,
                                   int count,
                                   boolean useTrash) {
        mContext = context;
        mDisplayMetrics = displayMetrics;
        mPagingCache = PagingCache.getInstance(context);
        mOnGalleryThumbSelection = onGalleryThumbSelection;
        mCount = count;
        mUseTrash = useTrash;
    }

    protected abstract void displayThumbnail(@NonNull PagingCache.PagingEntry pagingEntry);

    public abstract int getPerRow();

    public OnGallerySelectionIF getOnGalleryThumbSelection() {
        return mOnGalleryThumbSelection;
    }

    /** @noinspection unchecked*/
    protected T getHolder(int requestedPosition) {
        T holder = (T) mHolderMap.get(requestedPosition);
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

    public List<String> getSelectedUids() {
        return mSelectedThumbs.stream().map(s -> String.valueOf(s.uid)).collect(Collectors.toList());
    }

    @Override
    public void deleteSelected(boolean useTrash, boolean flagUndelete) {
        if (mSelectedThumbs.isEmpty()) {
            return;
        }
        if(!useTrash && flagUndelete) {
            throw new IllegalArgumentException("Can't undelete if hard delete is active!");
        }
        ListenableFuture<List<RenderResultLightWeight>> future =
                getAppDatabase().renderResultDao().getLightWeightByIds(getSelectedUids());
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            List<RenderResult> renderResults = new ArrayList<>();
            result.forEach(lw -> {
                RenderResult renderResult = RenderResult.fromRenderResultLightWeight(lw);
                renderResult.deleted = !flagUndelete;
                renderResults.add(renderResult);
            });
            ListenableFuture<Integer> listenableFuture = useTrash ? getSoftDeleteFuture(renderResults)
                    : getHardDeleteFuture(renderResults);
            AsyncDbFuture<Integer> updateFuture = new AsyncDbFuture<>();
            updateFuture.processFuture(listenableFuture, this::afterAsyncDelete, mContext);
        }, mContext);
    }

    private ListenableFuture<Integer> getSoftDeleteFuture(List<RenderResult> renderResults) {
        return getAppDatabase()
                .renderResultDao().updateRenderResults(renderResults);
    }

    private ListenableFuture<Integer> getHardDeleteFuture(List<RenderResult> renderResults) {
        return getAppDatabase()
                .renderResultDao().deleteRenderResults(renderResults);
    }

    public void afterAsyncDelete(Integer deleteCount) {
        notifyItemRangeRemoved(0, mCount);
        mSelectedThumbs.clear();
        mSelectionMode = false;
        mLastSelectionMode = null;
        mCount = mCount - deleteCount;
        mPagingCache.getPagingEntries().clear();
        mOnGalleryThumbSelection.onDeleteDone();
    }

    public void refresh() {
        mPagingCache.getPagingEntries().clear();
        notifyItemRangeChanged(0, mCount);
    }

    private AppDatabase getAppDatabase() {
        return AppDatabase.getInstance(mContext);
    }

    @Override
    public void onCachingDone(List<PagingCache.PagingEntry> pagingEntries) {
        pagingEntries.forEach(this::displayThumbnail);
        mThumbRequests.clear();
    }

    protected void doCacheManagement(AbstractRenderResultViewHolder holder, int position) {
        Optional<PagingCache.PagingEntry> optional = mPagingCache.getPagingEntries()
                .stream().parallel().filter(p -> p.idx == position).findAny();
        if (optional.isPresent()) {
            mHolderMap.put(holder.requestedPosition, holder);
            displayThumbnail(optional.get());
        } else {
            boolean needsInc = needsIncrementCacheReload(position);
            boolean needsDec = needsDecrementCacheReload(position);
            if (needsInc || needsDec) {
                mThumbRequests.add(new ThumbLoadRequest<>(new PagingCache.PagingEntry(), holder));
                mPagingCache.fillCache(mContext, holder.requestedPosition, needsDec && !needsInc
                        ? position - PagingCache.PAGE_SIZE
                        : position, this, mUseTrash, needsDec && !needsInc);
            }
            mHolderMap.put(holder.requestedPosition, holder);
        }
    }

    protected boolean needsIncrementCacheReload(int idx) {
        if (mThumbRequests.isEmpty()) {
            return true;
        }
        OptionalInt max = mThumbRequests.stream().mapToInt(t -> t.startIdx).max();
        int maxIdxAvail = max.getAsInt() + PagingCache.PAGE_SIZE - 1;
        return idx > maxIdxAvail;
    }

    protected boolean needsDecrementCacheReload(int idx) {
        if (mThumbRequests.isEmpty()) {
            return false;
        }
        OptionalInt min = mThumbRequests.stream().mapToInt(t -> t.startIdx).min();
        int minIdxAvail = min.getAsInt();
        return idx < minIdxAvail;
    }

    public int getThumbWidth() {
        return mDisplayMetrics.widthPixels / getPerRow();
    }

    public int getThumbHeight() {
        return mDisplayMetrics.heightPixels / (getPerRow() + 2);
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
        public PagingCache.PagingEntry pagingEntry;
        public AbstractRenderResultViewHolder holder;
        public int startIdx;

        public ThumbLoadRequest(PagingCache.PagingEntry pagingEntry,
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
