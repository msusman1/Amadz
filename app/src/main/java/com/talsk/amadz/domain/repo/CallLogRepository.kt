package com.talsk.amadz.domain.repo

import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.Contact

interface CallLogRepository {
    suspend fun getCallLogsPaged(limit: Int, offset: Int): List<CallLogData>
    suspend fun getCallLogsByPhone(phone: String): List<CallLogData>
    suspend fun deleteCallLogsByPhone(phone: String): Int
    suspend fun getFrequentCalledContacts(): List<Contact>
    suspend fun searchCallLogContacts(query: String, limit: Int, offset: Int): List<Contact>
}