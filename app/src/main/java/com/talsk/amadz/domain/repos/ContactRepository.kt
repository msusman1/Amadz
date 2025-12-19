package com.talsk.amadz.domain.repos

import com.talsk.amadz.data.ContactData

interface ContactRepository {
    suspend fun getAllContacts(): List<ContactData>
    suspend fun getContactByPhone(phoneNumber: String): ContactData?
}