package com.talsk.amadz.domain.repos

import android.graphics.Bitmap
import android.net.Uri
import com.talsk.amadz.data.ContactData

interface ContactRepository {
    suspend fun getAllContacts(): List<ContactData>
    suspend fun searchContacts(query: String): List<ContactData>
    suspend fun getContactByPhone(phoneNumber: String): ContactData?
    suspend fun loadContactImage(photoUri: Uri?): Bitmap?
    suspend fun getAllFavourites(): List<ContactData>
    suspend fun updateStarred(contactId: Long, starred: Boolean)
}