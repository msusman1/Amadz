package com.talsk.amadz.data

import androidx.paging.PagingSource
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.CallLogType
import com.talsk.amadz.domain.repo.CallLogRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class CallLogsPagingSourceTest {
    @Test
    fun groupsMatchingLogsAcrossRawPageBoundaries() = runBlocking {
        val logs = List(53) { callLog(it.toLong(), "123", CallLogType.OUTGOING) } +
            callLog(53, "123", CallLogType.MISSED)
        val source = CallLogsPagingSource(FakeCallLogRepository(logs))

        val firstPage = source.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 1,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        assertEquals(1, firstPage.data.size)
        assertEquals(53, firstPage.data.single().count)
        assertEquals(CallLogType.OUTGOING, firstPage.data.single().log.callLogType)
        assertEquals(53, firstPage.nextKey)

        val secondPage = source.load(
            PagingSource.LoadParams.Append(
                key = firstPage.nextKey!!,
                loadSize = 1,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        assertEquals(1, secondPage.data.single().count)
        assertEquals(CallLogType.MISSED, secondPage.data.single().log.callLogType)
        assertNull(secondPage.nextKey)
    }

    private fun callLog(id: Long, phone: String, type: CallLogType) = CallLogData(
        id = id,
        contactId = null,
        name = "",
        phone = phone,
        image = null,
        callLogType = type,
        time = Date(id),
        callDuration = 0,
        simSlot = null
    )

    private class FakeCallLogRepository(
        private val logs: List<CallLogData>
    ) : CallLogRepository {
        override suspend fun getCallLogsPaged(limit: Int, offset: Int) =
            logs.drop(offset).take(limit)

        override suspend fun getCallLogsByPhone(phone: String) = emptyList<CallLogData>()

        override suspend fun deleteCallLogsByPhone(phone: String) = 0

        override suspend fun deleteAllCallLogs() = 0

        override suspend fun getFrequentCalledContacts() =
            emptyList<com.talsk.amadz.domain.entity.Contact>()

        override suspend fun searchCallLogContacts(query: String, limit: Int, offset: Int) =
            emptyList<com.talsk.amadz.domain.entity.Contact>()
    }
}
