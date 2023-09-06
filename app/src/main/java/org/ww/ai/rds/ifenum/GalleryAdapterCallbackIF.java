package org.ww.ai.rds.ifenum;

import org.ww.ai.rds.RecyclerViewPagingCache;

import java.util.List;

public interface GalleryAdapterCallbackIF {

    void onCachingDone(List<RecyclerViewPagingCache.PagingEntry> pagingEntries);

}
