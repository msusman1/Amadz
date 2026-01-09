package com.talsk.amadz.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.talsk.amadz.domain.repos.CallLogRepository
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
        // Anchor position = most recently accessed index
        val anchorPosition = state.anchorPosition ?: return null

        val closestPage = state.closestPageToPosition(anchorPosition)

        return closestPage?.prevKey?.plus(1)
            ?: closestPage?.nextKey?.minus(1)
    }
}
