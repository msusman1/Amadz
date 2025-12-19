package com.talsk.amadz.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract
import com.talsk.amadz.domain.repos.ContactRepository

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/22/2023.
 */
class FavouriteRepositoryImpl(private val context: Context): ContactRepository {

    private val contentResolver = context.contentResolver

    /**
     * Mark contact as favourite (STARRED = 1)
     */
    fun addToFav(contactId: Long) {
        updateStarred(contactId, true)
    }

    /**
     * Remove contact from favourites (STARRED = 0)
     */
    fun removeFromFav(contactId: Long) {
        updateStarred(contactId, false)
    }

    /**
     * Toggle favourite state
     */
    fun toggleFavourite(contactId: Long, makeFavourite: Boolean) {
        updateStarred(contactId, makeFavourite)
    }

    /**
     * Get all favourite contacts as ContactData list
     */
    fun getAllFavourites(): List<ContactData> {
        val favourites = mutableListOf<ContactData>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.Contacts.STARRED
        )

        val selection = "${ContactsContract.Contacts.STARRED} = 1"

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                favourites.add(it.toContactData())
            }
        }

        // One contact may have multiple numbers → dedupe
        return favourites.distinctBy { it.id }
    }

    /**
     * Internal helper to update STARRED flag
     */
    private fun updateStarred(contactId: Long, starred: Boolean) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, if (starred) 1 else 0)
        }

        val uri = ContentUris.withAppendedId(
            ContactsContract.Contacts.CONTENT_URI,
            contactId
        )

        contentResolver.update(uri, values, null, null)
    }

}