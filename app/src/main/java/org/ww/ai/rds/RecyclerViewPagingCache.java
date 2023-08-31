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

    private static final int CAPACITY = 100;
    private static RecyclerViewPagingCache mInstance;
    private final AppDatabase mAppDatabase;
    private final List<PagingEntry> entries;

    private RecyclerViewPagingCache(Context context) {
        mAppDatabase = AppDatabase.getInstance(context);
        entries = new ArrayList<>(CAPACITY);
    }

    public static RecyclerViewPagingCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RecyclerViewPagingCache(context);
        }
        return mInstance;
    }

    public synchronized void addAll(int startIdx, List<RenderResultLightWeight> lightWeights) {
        if (lightWeights == null || lightWeights.isEmpty()) {
            throw new IllegalArgumentException("Invalid use of add(lightWeights): lightweights: " + lightWeights);
        }
        int available = getAvailableCapacity();
        if ((available - lightWeights.size()) < 0) {
            removeOldEntries(lightWeights.size() - available);
        }
        AtomicInteger idx = new AtomicInteger(startIdx);
        lightWeights.forEach(l -> entries.add(new PagingEntry(idx.getAndIncrement(), l)));
    }

    private void removeOldEntries(int count) {
        entries.sort(Collections.reverseOrder());
        if (count > 0) {
            entries.subList(0, count).clear();
        }
    }

    int getAvailableCapacity() {
        if (entries.size() < CAPACITY) {
            return CAPACITY - entries.size();
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        entries.parallelStream().filter(Objects::isNull).forEach(
                entry -> atomicInteger.set(atomicInteger.incrementAndGet()));
        return atomicInteger.get();
    }

    public void displayThumbnail(final Context context,
                                 final int idx,
                                 final GalleryAdapterCallbackIF pagingCacheCallback,
                                 final boolean showTrash) {
        RenderResultLightWeight lightWeight = entries.stream().parallel()
                .filter(f -> f.idx == idx).map(
                        r -> r.renderResultLightWeight).findFirst().orElse(null);
        if (lightWeight == null) {
            ListenableFuture<List<RenderResultLightWeight>> future = mAppDatabase.renderResultDao()
                    .getPagedRenderResultsLw(idx, showTrash);
            AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
            asyncDbFuture.processFuture(future, result -> {
                addAll(idx, result);
                RenderResultLightWeight lw = entries.stream().parallel()
                        .filter(f -> f.idx == idx).map(
                                r -> r.renderResultLightWeight).findFirst().orElse(null);
                pagingCacheCallback.cachingDone(idx, lw);
            }, context);
        } else {
            pagingCacheCallback.cachingDone(idx, lightWeight);
        }
    }

    public void remove(int idx) {
        int size = entries.size();
        Optional<PagingEntry> optional = entries.stream()
                .filter(e -> e.idx == idx).findFirst();
        optional.ifPresent(entries::remove);
    }

    public static class PagingEntry implements Comparable<PagingEntry> {
        public RenderResultLightWeight renderResultLightWeight;
        public long timeAdded;
        public int idx;

        public PagingEntry(int idx, RenderResultLightWeight renderResultLightWeight) {
            this.idx = idx;
            this.renderResultLightWeight = renderResultLightWeight;
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
