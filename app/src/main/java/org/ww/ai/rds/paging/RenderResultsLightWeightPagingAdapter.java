package org.ww.ai.rds.paging;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

import org.ww.ai.rds.entity.RenderResultLightWeight;

import kotlin.coroutines.CoroutineContext;

public class RenderResultsLightWeightPagingAdapter extends PagingDataAdapter<RenderResultLightWeight, RenderResultLightWeightViewHolder> {

    public RenderResultsLightWeightPagingAdapter(@NonNull DiffUtil.ItemCallback<RenderResultLightWeight> diffCallback, @NonNull CoroutineContext mainDispatcher) {
        super(diffCallback, mainDispatcher);
    }

    public RenderResultsLightWeightPagingAdapter(@NonNull DiffUtil.ItemCallback<RenderResultLightWeight> diffCallback) {
        super(diffCallback);
    }

    public RenderResultsLightWeightPagingAdapter(@NonNull DiffUtil.ItemCallback<RenderResultLightWeight> diffCallback, @NonNull CoroutineContext mainDispatcher, @NonNull CoroutineContext workerDispatcher) {
        super(diffCallback, mainDispatcher, workerDispatcher);
    }

    @NonNull
    @Override
    public RenderResultLightWeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RenderResultLightWeightViewHolder holder, int position) {

    }
}
