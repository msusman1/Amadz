package com.talsk.amadz.domain.repo

import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.Contact

interface CallLogRepository {
    suspend fun getCallLogsPaged(limit: Int, offset: Int): List<CallLogData>
    suspend fun getFrequentCalledContacts(): List<Contact>
}