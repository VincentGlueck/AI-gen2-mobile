package org.ww.ai.rds.paging

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn

class RenderResultLightWeightViewModel(context: Context?, pageSize: Int) : ViewModel() {
    val renderResultLightWeight = Pager(PagingConfig(pageSize = 20)) {
        RenderResultPagingSource(context, pageSize)
    }.flow.cachedIn(viewModelScope)

}