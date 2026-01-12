package com.talsk.amadz.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.ContactRepository
import javax.inject.Inject

class ContactsPagingSource @Inject constructor(
    private val contactRepository: ContactRepository
) : PagingSource<Int, Contact>() {

    companion object {
        const val PAGE_SIZE = 50
        const val FIRST_PAGE = 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Contact> {
        return try {
            val pageIndex = params.key ?: FIRST_PAGE
            val offset = pageIndex * PAGE_SIZE

            val data = contactRepository.getContactsPaged(
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

    override fun getRefreshKey(state: PagingState<Int, Contact>): Int? {
        // Anchor position = most recently accessed index
        val anchorPosition = state.anchorPosition ?: return null

        val closestPage = state.closestPageToPosition(anchorPosition)

        return closestPage?.prevKey?.plus(1)
            ?: closestPage?.nextKey?.minus(1)
    }
}
