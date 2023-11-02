package org.ww.ai.rds.ifenum;

import org.ww.ai.rds.PagingCache;

import java.util.List;

public interface GalleryAdapterCallbackIF {

    void onCachingDone(List<PagingCache.PagingEntry> pagingEntries);

}
