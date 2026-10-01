package com.talsk.amadz.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.talsk.amadz.domain.entity.CallLogGroup
import com.talsk.amadz.domain.repo.CallLogRepository
import javax.inject.Inject

class CallLogsPagingSource @Inject constructor(
    private val callLogRepository: CallLogRepository
) : PagingSource<Int, CallLogGroup>() {

    companion object {
        const val PAGE_SIZE = 50
        const val FIRST_PAGE = 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CallLogGroup> {
        return try {
            val startOffset = params.key ?: FIRST_PAGE
            val groups = mutableListOf<CallLogGroup>()
            var currentGroup: CallLogGroup? = null
            var offset = startOffset
            var nextKey: Int? = null
            var reachedEnd = false

            while (groups.size < params.loadSize && !reachedEnd) {
                val logs = callLogRepository.getCallLogsPaged(
                    limit = PAGE_SIZE,
                    offset = offset
                )

                for ((index, log) in logs.withIndex()) {
                    val activeGroup = currentGroup
                    if (activeGroup == null) {
                        currentGroup = CallLogGroup(log, 1)
                    } else if (
                        activeGroup.log.phone == log.phone &&
                        activeGroup.log.callLogType == log.callLogType
                    ) {
                        currentGroup = activeGroup.copy(count = activeGroup.count + 1)
                    } else {
                        groups += activeGroup
                        if (groups.size == params.loadSize) {
                            nextKey = offset + index
                            reachedEnd = true
                            break
                        }
                        currentGroup = CallLogGroup(log, 1)
                    }
                }

                if (!reachedEnd) {
                    offset += logs.size
                    if (logs.size < PAGE_SIZE) {
                        currentGroup?.let(groups::add)
                        currentGroup = null
                        reachedEnd = true
                    }
                }
            }

            LoadResult.Page(
                data = groups,
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CallLogGroup>): Int? {
        // For call logs we always want newest entries first after refresh.
        // Returning FIRST_PAGE prevents refresh from reloading around a mid-list anchor.
        return FIRST_PAGE
    }
}
