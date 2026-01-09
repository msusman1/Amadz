package com.talsk.amadz.ui.home.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.CallLogsPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow


@HiltViewModel
class RecentViewModel @Inject constructor(
    private val callLogsPagingSource: CallLogsPagingSource,
) : ViewModel() {
    val callLogs: Flow<PagingData<CallLogData>> = Pager(
        config = PagingConfig(pageSize = CallLogsPagingSource.PAGE_SIZE),
        initialKey = CallLogsPagingSource.FIRST_PAGE
    ) { callLogsPagingSource }.flow
        .cachedIn(viewModelScope)
}