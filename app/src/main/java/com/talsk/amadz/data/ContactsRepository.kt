package com.talsk.amadz.data

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.App

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */
class ContactsRepository(val context: Context) {
    val TAG = "ContactsRepository"

    fun getAllContacts(): List<ContactData> {
        Log.d(TAG, "getAllContacts() called")
        val contactsList = mutableListOf<ContactData>()

        val contentResolver: ContentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        if (cursor != null) {

            val idColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (cursor.moveToNext()) {
                val contactId = cursor.getLong(idColumnIndex)
                val contactName = cursor.getString(nameColumnIndex)
                val contactNumber = cursor.getString(numberColumnIndex)
                val photoUri = cursor.getStringOrNull(photoUriColumnIndex)?.toUri()
//                val contactImage = contactImageRepository.loadContactImage(photoUri)
                val contact = ContactData(contactId, contactName, contactNumber, photoUri, isFavourite = false)
                contactsList.add(contact)
            }
            cursor.close()
        }

        return contactsList.distinctBy { it.id }

    }

    fun getContactData(phoneNumber: String): ContactData? {

        val contentResolver: ContentResolver = context.contentResolver
        // Define the columns you want to retrieve
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        // Define the selection criteria
        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)

        // Query the contacts
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        var contactData: ContactData? = null
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Get the column indices
                val idColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoUriColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                // Retrieve the contact details
                val contactId = cursor.getLong(idColumnIndex)
                val contactName = cursor.getString(nameColumnIndex)
                val contactNumber = cursor.getString(numberColumnIndex)
                val photoUri = cursor.getStringOrNull(photoUriColumnIndex)?.toUri()
//                val contactImage = contactImageRepository.loadContactImage(photoUri)
                contactData = ContactData(contactId, contactName, contactNumber, photoUri,isFavourite = false)
            }
            cursor.close()
        }
        return contactData

    }


}

fun Context.openContactDetailScreen(contactId: Long) {
    App.needDataReload = true
    val intent = Intent(Intent.ACTION_VIEW)
    val contactUri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
        .appendPath(contactId.toString())
        .build()
    intent.data = contactUri
    startActivity(intent)
}
fun Context.openContactAddScreen(phone: String) {
    App.needDataReload = true
    val intent = Intent(Intent.ACTION_INSERT)
    intent.type = ContactsContract.Contacts.CONTENT_TYPE

    intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
    startActivity(intent)
}