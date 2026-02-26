package com.talsk.amadz.ui.home.calllogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.talsk.amadz.data.CallLogsPagingSource
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.util.isSameDay
import com.talsk.amadz.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date


@HiltViewModel
class CallLogsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
) : ViewModel() {
    val callLogs: Flow<PagingData<CallLogUiModel>> = Pager(
        config = PagingConfig(pageSize = CallLogsPagingSource.PAGE_SIZE),
        initialKey = CallLogsPagingSource.FIRST_PAGE
    ) { CallLogsPagingSource(callLogRepository) }.flow
        .map { pagingData ->
            pagingData.map {
                CallLogUiModel.Item(it)
            }.insertSeparators { before, after ->
                when {
                    after == null -> null
                    before == null -> {
                        CallLogUiModel.Header(after.log.time.startOfDay())
                    }

                    !before.log.time.isSameDay(after.log.time) -> {
                        CallLogUiModel.Header(after.log.time.startOfDay())
                    }

                    else -> null
                }
            }
        }.cachedIn(viewModelScope)
}

sealed interface CallLogUiModel {
    data class Header(val date: Date) : CallLogUiModel
    data class Item(val log: CallLogData) : CallLogUiModel
}