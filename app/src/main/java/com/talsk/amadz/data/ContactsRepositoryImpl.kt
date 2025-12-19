package com.talsk.amadz.data

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repos.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */

const val TAG = "ContactsRepository"

class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactRepository {
    val contentResolver: ContentResolver = context.contentResolver
    val telephonyManager = getSystemService(context, TelephonyManager::class.java)
    private val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Organization.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
        ContactsContract.Contacts.STARRED
    )


    override suspend fun getAllContacts(): List<ContactData> = withContext(ioDispatcher) {
        val contacts = mutableMapOf<Long, ContactData>()
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val contact = cursor.toContactData()
                contacts.putIfAbsent(contact.id, contact)
            }
        }
        contacts.values.toList()
    }


    override suspend fun getContactByPhone(phoneNumber: String): ContactData? =
        withContext(ioDispatcher) {
            val normalizedPhone = normalizePhoneNumber(phoneNumber) ?: return@withContext null

            val selection = """
                REPLACE(REPLACE(REPLACE(${ContactsContract.CommonDataKinds.Phone.NUMBER},
                ' ', ''), '-', ''), '(', '') LIKE ?
            """.trimIndent()
            val selectionArgs = arrayOf("%$normalizedPhone%")

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use {
                if (it.moveToFirst()) {
                    val oldContactData = it.toContactData()
                    oldContactData.copy(companyName = getCompanyName(oldContactData.id) ?: "")
                } else {
                    null
                }
            }
        }

    private suspend fun getCompanyName(contactId: Long): String? = withContext(ioDispatcher) {
        val orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?"
        val orgWhereParams = arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        )

        contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
            orgWhere,
            orgWhereParams,
            null
        )?.use {
            if (it.moveToFirst()) {
                val companyNameColumnIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)
                it.getStringOrNull(companyNameColumnIndex)
            } else {
                null
            }
        }
    }


    fun normalizePhoneNumber(phone: String): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()

        // Get the user's default country code
        val defaultRegion = telephonyManager?.simCountryIso?.uppercase(Locale.getDefault())
            ?: telephonyManager?.networkCountryIso?.uppercase(Locale.getDefault())
            ?: "US" // Default to "US" if unknown

        return try {
            // Parse the phone number
            val numberProto = phoneUtil.parse(phone, defaultRegion)

            // Format it into E.164 format (+<country_code><number>)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            null // Return null if the phone number is invalid
        }
    }
}


fun Cursor.toContactData(): ContactData {
    val idColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
    val nameColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

    val numberColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val photoUriColumnIndex =
        this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

    val starredColumnIndex =
        getColumnIndexOrThrow(ContactsContract.Contacts.STARRED) // ✅ NEW

    // Retrieve the contact details
    val contactId = this.getLong(idColumnIndex)
    val contactName = this.getString(nameColumnIndex)
    val contactNumber = this.getString(numberColumnIndex)
    val photoUri = this.getStringOrNull(photoUriColumnIndex)?.toUri()
//                val contactImage = contactImageRepository.loadContactImage(photoUri)
    val isFavourite = getInt(starredColumnIndex) == 1
    return ContactData(
        id = contactId,
        name = contactName,
        companyName = "",
        phone = contactNumber,
        image = photoUri,
        isFavourite = isFavourite
    )
}

