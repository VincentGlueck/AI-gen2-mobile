package org.ww.ai.rds;

import android.content.Context;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.rds.ifenum.GalleryAdapterCallbackIF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class RecyclerViewPagingCache {

    public static int PAGE_SIZE = 16;
    private static final int CAPACITY = 16 * PAGE_SIZE;

    private static RecyclerViewPagingCache mInstance;
    private final AppDatabase mAppDatabase;
    private final List<PagingEntry> mPagingEntries;

    private RecyclerViewPagingCache(Context context) {
        mAppDatabase = AppDatabase.getInstance(context);
        mPagingEntries = new ArrayList<>(CAPACITY);
    }

    public static RecyclerViewPagingCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RecyclerViewPagingCache(context);
        }
        return mInstance;
    }

    public synchronized void addAll(int startIdx, List<PagingEntry> pagingEntries) {
        if (pagingEntries == null || pagingEntries.isEmpty()) {
            throw new IllegalArgumentException("Invalid use of add(lightWeights): lightweights: " + pagingEntries);
        }
        int available = getAvailableCapacity();
        if ((available - pagingEntries.size()) < 0) {
            removeOldEntries(pagingEntries.size() - available);
        }
        AtomicInteger idx = new AtomicInteger(startIdx);
        pagingEntries.forEach(p -> mPagingEntries
                .add(new PagingEntry(idx.getAndIncrement(),
                        p.renderResultLightWeight, p.requestPosition)));
    }

    private void removeOldEntries(int count) {
        mPagingEntries.sort(Collections.reverseOrder());
        if (count > 0) {
            mPagingEntries.subList(0, count).clear();
        }
    }

    int getAvailableCapacity() {
        if (mPagingEntries.size() < CAPACITY) {
            return CAPACITY - mPagingEntries.size();
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        mPagingEntries.parallelStream().filter(Objects::isNull).forEach(
                entry -> atomicInteger.set(atomicInteger.incrementAndGet()));
        return atomicInteger.get();
    }

    public void fillCache(final Context context,
                          final int idx,
                          final int requestedPosition,
                          final GalleryAdapterCallbackIF pagingCacheCallback,
                          final boolean showTrash,
                          final boolean backwards) {
        ListenableFuture<List<RenderResultLightWeight>> future = mAppDatabase.renderResultDao()
                .getPagedRenderResultsLw(idx, RecyclerViewPagingCache.PAGE_SIZE, showTrash);
        AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
        asyncDbFuture.processFuture(future, result -> {
            AtomicInteger counter = new AtomicInteger(idx);
            AtomicInteger realRequestPosition = new AtomicInteger(requestedPosition);
            List<PagingEntry> pagingEntries = new ArrayList<>();
            result.forEach(lightweight -> pagingEntries.add(new PagingEntry(backwards ?
                    counter.getAndDecrement() :
                    counter.getAndIncrement(),
                    lightweight, backwards ?
                    realRequestPosition.getAndDecrement() :
                    realRequestPosition.getAndIncrement())));
            addAll(idx, pagingEntries);
            pagingCacheCallback.onCachingDone(pagingEntries);
        }, context);
    }

    public void remove(int idx) {
        int size = mPagingEntries.size();
        Optional<PagingEntry> optional = mPagingEntries.stream()
                .filter(e -> e.idx == idx).findFirst();
        optional.ifPresent(mPagingEntries::remove);
    }

    public List<PagingEntry> getPagingEntries() {
        return mPagingEntries;
    }

    public static class PagingEntry implements Comparable<PagingEntry> {
        public RenderResultLightWeight renderResultLightWeight;
        public long timeAdded;
        public int idx;

        public int requestPosition;

        public PagingEntry(int idx, RenderResultLightWeight renderResultLightWeight,
                           int requestPosition) {
            this();
            this.idx = idx;
            this.renderResultLightWeight = renderResultLightWeight;
            this.requestPosition = requestPosition;
        }

        public PagingEntry() {
            this.timeAdded = System.currentTimeMillis();
        }

        @Override
        public int compareTo(PagingEntry o) {
            if (o == null) {
                return -1;
            }
            return Long.compare(timeAdded, o.timeAdded);
        }
    }

}
