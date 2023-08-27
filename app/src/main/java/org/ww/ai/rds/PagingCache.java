package org.ww.ai.rds;

import android.content.Context;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import org.ww.ai.rds.entity.RenderResultLightWeight;
import org.ww.ai.rds.ifenum.PagingCacheCallbackIF;
import org.ww.ai.rds.ifenum.ThumbnailCallbackIF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PagingCache {

    private static PagingCache mInstance;
    private static final int CAPACITY = 100;
    private static final int MAX_CAPACITY = 1000;
    private static final int MIN_CAPACITY = 1;
    private static final int PAGE_SIZE = 20;
    private final AtomicBoolean mUseDummies = new AtomicBoolean(false);
    private AppDatabase mAppDatabase;
    private List<PagingEntry> entries;
    private int mCapacity = CAPACITY;

    private PagingCache(Context context, int... capacity) {
        mAppDatabase = AppDatabase.getInstance(context);
        if (capacity.length > 0) {
            if (capacity[0] > MAX_CAPACITY) {
                mCapacity = MAX_CAPACITY;
            } else if (capacity[0] < MIN_CAPACITY) {
                mCapacity = MIN_CAPACITY;
            }
        }
        entries = new ArrayList<>(mCapacity);
    }

    protected PagingCache(int capacity) {
        // for tests only!
        mCapacity = capacity;
    }

    public static PagingCache getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new PagingCache(context);
        }
        return mInstance;
    }


    public synchronized void addAll(List<RenderResultLightWeight> lightWeights, long... fakeTime) {
        if (lightWeights == null || lightWeights.isEmpty()) {
            throw new IllegalArgumentException("Invalid use of add(lightWeights): lightweights: " + lightWeights);
        }
        if (lightWeights.size() > mCapacity) {
            throw new IllegalArgumentException("too many lightWeights for cache size ("
                    + lightWeights.size() + ", capacity " + mCapacity + ")");
        }
        int available = getAvailableCapacity();
        if ((available - lightWeights.size()) < 0) {
            removeOldEntries(lightWeights.size() - available);
        }
        lightWeights.forEach(l -> addEntry(l, fakeTime));
    }

    private void addEntry(RenderResultLightWeight lightWeight, long... fakeTime) {
        PagingEntry entry = new PagingEntry();
        entry.timeAdded = fakeTime.length > 0 ? fakeTime[0] : System.currentTimeMillis();
        entry.renderResultLightWeight = lightWeight;
        entries.add(entry);
    }

    private void removeOldEntries(int count) {
        entries.sort(Collections.reverseOrder());
        if (count > 0) {
            entries.subList(0, count).clear();
        }
    }

    int getAvailableCapacity() {
        if (entries.size() < mCapacity) {
            return mCapacity - entries.size();
        }
        AtomicInteger atomicInteger = new AtomicInteger(0);
        entries.parallelStream().filter(Objects::isNull).forEach(
                entry -> atomicInteger.set(atomicInteger.incrementAndGet()));
        return atomicInteger.get();
    }

    public boolean hasId(int uid) {
        return entries.stream().parallel().anyMatch(f -> f.renderResultLightWeight.uid == uid);
    }

    public void displayThumbnail(Context context, int uid, PagingCacheCallbackIF pagingCacheCallback,
                                 ThumbnailCallbackIF callback, boolean showTrash) {
        if(mUseDummies.get()) {
            RenderResultLightWeight lightWeight = new RenderResultLightWeight();
            lightWeight.uid = uid;
            callback.setThumbnail(lightWeight);
            pagingCacheCallback.cachingDone();
            return;
        }
        RenderResultLightWeight lightWeight = entries.stream().parallel()
                .filter(f -> f.renderResultLightWeight.uid == uid).map(
                        r -> r.renderResultLightWeight).findFirst().orElse(null);
        if (lightWeight == null) {
            ListenableFuture<List<RenderResultLightWeight>> future = mAppDatabase.renderResultDao()
                    .getPagedRenderResultsLw(uid, PAGE_SIZE, 0, showTrash);
            AsyncDbFuture<List<RenderResultLightWeight>> asyncDbFuture = new AsyncDbFuture<>();
            asyncDbFuture.processFuture(future, result -> {
                addAll(result);
                RenderResultLightWeight lw = entries.stream().parallel()
                        .filter(f -> f.renderResultLightWeight.uid == uid).map(
                                r -> r.renderResultLightWeight).findFirst().orElse(null);
                if(lw != null) {
                    callback.setThumbnail(lw);
                } else {
                    Log.e("DISPLAY", "Got a null lightweight after db query for uid " + uid);
                }
                pagingCacheCallback.cachingDone();
            }, context);
        } else {
            callback.setThumbnail(lightWeight);
            pagingCacheCallback.cachingDone();
        }
    }

    public List<RenderResultLightWeight> getAllEntries() {
        return entries.stream().map(e -> e.renderResultLightWeight).collect(Collectors.toList());
    }

    public void setUseDummies(boolean useDummies) {
        mUseDummies.set(useDummies);
    }

    public void remove(int uid) {
        int size = entries.size();
        Optional<PagingEntry> optional = entries.stream()
                .filter(e -> e.renderResultLightWeight.uid == uid).findFirst();
        optional.ifPresent(pagingEntry -> entries.remove(pagingEntry));
    }

    public AppDatabase getAppDatabase() {
        return mAppDatabase;
    }

    public static class PagingEntry implements Comparable<PagingEntry> {
        public RenderResultLightWeight renderResultLightWeight;
        public long timeAdded;

        @Override
        public int compareTo(PagingEntry o) {
            if (o == null) {
                return -1;
            }
            return Long.compare(timeAdded, o.timeAdded);
        }
    }

}
