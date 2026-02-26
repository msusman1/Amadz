package com.talsk.amadz.data

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */


class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactRepository {
    val TAG = "ContactsRepositoryImpl"
    val contentResolver: ContentResolver = context.contentResolver
    val telephonyManager = getSystemService(context, TelephonyManager::class.java)
    private val phoneProjection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
    )
    private val contactProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.PHOTO_URI,
    )

    private fun Cursor.toContacts(): List<Contact> {
        val contacts = mutableMapOf<Long, Contact>()
        use { cursor ->
            while (cursor.moveToNext()) {
                val contact = cursor.toContactData()
                contacts.putIfAbsent(contact.id, contact)
            }
        }
        return contacts.values.toList()
    }

    private fun Cursor.toContactRows(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        use { cursor ->
            while (cursor.moveToNext()) {
                contacts += cursor.toContactData()
            }
        }
        return contacts
    }

    override suspend fun getContactsPaged(limit: Int, offset: Int): List<Contact> =
        withContext(ioDispatcher) {
            Log.d(TAG, "getContactsPaged: $limit, $offset")
            val contacts = mutableListOf<Contact>()
            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                contactProjection,
                "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0",
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC LIMIT $limit OFFSET $offset"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val nameIndex =
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val photoIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)
                while (cursor.moveToNext()) {
                    contacts += Contact(
                        id = cursor.getLong(idIndex),
                        name = cursor.getString(nameIndex).orEmpty(),
                        phone = "",
                        image = cursor.getStringOrNull(photoIndex)?.toUri(),
                    )
                }
            }

            if (contacts.isEmpty()) return@withContext emptyList()
            val phoneNumbers = loadPhoneNumbers(contacts.map { it.id })
            contacts.mapNotNull { contact ->
                val phone = phoneNumbers[contact.id] ?: return@mapNotNull null
                contact.copy(phone = phone)
            }
        }

    override suspend fun searchContacts(query: String, limit: Int, offset: Int): List<Contact> =
        withContext(ioDispatcher) {
            if (query.isBlank()) return@withContext emptyList()
            val selection =
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ? ".trimIndent()
            val args = arrayOf("%$query%", "%$query%")
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                selection,
                args,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT $limit OFFSET $offset"
            )?.toContactRows() ?: emptyList()
        }

    override suspend fun getContactByPhone(phoneNumber: String): Contact? =
        withContext(ioDispatcher) {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)
            )

            contentResolver.query(
                uri, arrayOf(
                    ContactsContract.PhoneLookup._ID,
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.PHOTO_URI,
                    ContactsContract.PhoneLookup.NUMBER
                ), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.toContactDataForPhoneLookup() else null
            }
        }

    suspend fun getContactByPhoneOld(phoneNumber: String): Contact? = withContext(ioDispatcher) {
        val normalizedPhone = normalizePhoneNumber(phoneNumber) ?: return@withContext null

        val selection = """
                REPLACE(REPLACE(REPLACE(${ContactsContract.CommonDataKinds.Phone.NUMBER},
                ' ', ''), '-', ''), '(', '') LIKE ?
            """.trimIndent()
        val selectionArgs = arrayOf("%$normalizedPhone%")
        ContactsContract.Contacts.CONTENT_URI
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            selection,
            selectionArgs,
            null
        )?.use {
            if (it.moveToFirst()) it.toContactData() else null
        }
    }

    override suspend fun getCompanyName(contactId: Long): String? = withContext(ioDispatcher) {
        val orgWhere =
            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"
        val orgWhereParams = arrayOf(
            contactId.toString(), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        )
        contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Organization.COMPANY),
            orgWhere,
            orgWhereParams,
            null
        )?.use {
            if (it.moveToFirst()) it.getStringOrNull(it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)) else null
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


    /**
     * Get all favourite contacts as ContactData list
     */


    override fun observeFavourites(): Flow<List<Contact>> = callbackFlow {

        fun load(): List<Contact> {
            val selection = "${ContactsContract.Contacts.STARRED} = 1"
            return contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                selection,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.toContacts() ?: emptyList()
        }

        trySend(load())

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(load())
            }
        }
        contentResolver.registerContentObserver(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, observer
        )

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }

    }.flowOn(ioDispatcher)


    private fun Cursor.toContactData(): Contact {
        val idColumnIndex = this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val nameColumnIndex =
            this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberColumnIndex = this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoUriColumnIndex =
            this.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

        // Retrieve the contact details
        val contactId = this.getLong(idColumnIndex)
        val contactName = this.getString(nameColumnIndex)
        val contactNumber = this.getString(numberColumnIndex)
        val photoUri = this.getStringOrNull(photoUriColumnIndex)?.toUri()
        return Contact(
            id = contactId,
            name = contactName,
            phone = contactNumber,
            image = photoUri,
        )
    }

    private fun Cursor.toContactDataForPhoneLookup(): Contact {

        val idColumnIndex = this.getColumnIndex(ContactsContract.PhoneLookup._ID)
        val nameColumnIndex = this.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
        val numberColumnIndex = this.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)
        val photoUriColumnIndex = this.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

        // Retrieve the contact details
        val contactId = this.getLong(idColumnIndex)
        val contactName = this.getString(nameColumnIndex)
        val contactNumber = this.getString(numberColumnIndex)
        val photoUri = this.getStringOrNull(photoUriColumnIndex)?.toUri()
        return Contact(
            id = contactId,
            name = contactName,
            phone = contactNumber,
            image = photoUri,
        )
    }

    private fun loadPhoneNumbers(contactIds: List<Long>): Map<Long, String> {
        if (contactIds.isEmpty()) return emptyMap()
        val placeholders = contactIds.joinToString(",") { "?" }
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} IN ($placeholders)"
        val args = contactIds.map { it.toString() }.toTypedArray()
        val numbers = mutableMapOf<Long, String>()
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
            ),
            selection,
            args,
            "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val number = cursor.getString(numberIndex)
                if (!numbers.containsKey(id) && !number.isNullOrBlank()) {
                    numbers[id] = number
                }
            }
        }
        return numbers
    }
}
