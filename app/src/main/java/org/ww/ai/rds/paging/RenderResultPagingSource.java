package org.ww.ai.rds.paging;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;

import org.ww.ai.rds.AppDatabase;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.List;

import kotlin.coroutines.Continuation;

public class RenderResultPagingSource extends PagingSource<Integer, RenderResultLightWeight> {

    private final AppDatabase mDatabase;

    private final int mPageSize;

    public RenderResultPagingSource(Context context, int pageSize) {
        this.mDatabase = AppDatabase.getInstance(context);
        this.mPageSize = pageSize;
    }

    @Nullable
    @Override
    public Object load(@NonNull LoadParams<Integer> params,
                       @NonNull Continuation<? super LoadResult<Integer, RenderResultLightWeight>> continuation) {
        int currentPage = params.getKey() != null ? params.getKey() : 1;
        int offset = currentPage * mPageSize;
        List<RenderResultLightWeight> lightWeights = mDatabase.renderResultDao().getPagedRenderResultsLw(mPageSize, offset, false);
        boolean hasNextPage = lightWeights.size() == params.getLoadSize();
        return new LoadResult.Page<>(
                lightWeights,
                currentPage == 1 ? null : currentPage - 1,
                hasNextPage ? currentPage + 1 : null
        );
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, RenderResultLightWeight> pagingState) {
        return null;
    }
}
