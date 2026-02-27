package com.talsk.amadz.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.repo.CallLogRepository
import javax.inject.Inject

class CallLogsPagingSource @Inject constructor(
    private val callLogRepository: CallLogRepository
) : PagingSource<Int, CallLogData>() {

    companion object {
        const val PAGE_SIZE = 50
        const val FIRST_PAGE = 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CallLogData> {
        return try {
            val pageIndex = params.key ?: FIRST_PAGE
            val offset = pageIndex * PAGE_SIZE

            val data = callLogRepository.getCallLogsPaged(
                limit = PAGE_SIZE,
                offset = offset
            )

            LoadResult.Page(
                data = data,
                prevKey = if (pageIndex == FIRST_PAGE) null else pageIndex - 1,
                nextKey = if (data.size < PAGE_SIZE) null else pageIndex + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CallLogData>): Int? {
        // For call logs we always want newest entries first after refresh.
        // Returning FIRST_PAGE prevents refresh from reloading around a mid-list anchor.
        return FIRST_PAGE
    }
}
