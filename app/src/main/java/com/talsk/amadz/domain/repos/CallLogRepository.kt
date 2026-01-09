package com.talsk.amadz.domain.repos

import com.talsk.amadz.data.CallLogData
import com.talsk.amadz.data.ContactData

interface CallLogRepository {
    suspend fun getCallLogsPaged(limit: Int, offset: Int): List<CallLogData>
    suspend fun getFrequentCalledContacts(): List<ContactData>
}