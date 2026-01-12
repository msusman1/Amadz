package com.talsk.amadz.domain.repo

import com.talsk.amadz.domain.entity.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    suspend fun getContactsPaged(limit: Int, offset: Int): List<Contact>
    suspend fun searchContacts(query: String, limit: Int, offset: Int): List<Contact>
    suspend fun getContactByPhone(phoneNumber: String): Contact?
    suspend fun getCompanyName(contactId: Long): String?
    fun observeFavourites(): Flow<List<Contact>>
}