package com.talsk.amadz.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.util.LruCache
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.util.PhoneUtils
import com.talsk.amadz.util.toT9GlobPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */


class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val phoneUtils: PhoneUtils
) : ContactRepository {
    val TAG = "ContactsRepositoryImpl"
    val contentResolver: ContentResolver = context.contentResolver
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
    private val contactDetailProjection = arrayOf(
        ContactsContract.PhoneLookup._ID,
        ContactsContract.PhoneLookup.DISPLAY_NAME,
        ContactsContract.PhoneLookup.PHOTO_URI,
        ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
        ContactsContract.PhoneLookup.NUMBER
    )


    override suspend fun getContactsPaged(limit: Int, offset: Int) = withContext(ioDispatcher) {
        Log.d(TAG, "getContactsPaged: $limit, $offset")
        val contacts = contentResolver.query(
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
            val seenIds = HashSet<Long>()
            val contactList = buildList {
                while (cursor.moveToNext()) {
                    val contact = Contact(
                        id = cursor.getLong(idIndex),
                        name = cursor.getString(nameIndex).orEmpty(),
                        phone = "",
                        image = cursor.getStringOrNull(photoIndex)?.toUri(),
                    )
                    if (seenIds.add(contact.id)) add(contact)
                }
            }
            contactList
        } ?: return@withContext emptyList()

        val phoneNumbers = getPhoneNumbersForContactIds(contacts.map { it.id })
        contacts.mapNotNull { contact ->
            val phone = phoneNumbers[contact.id] ?: return@mapNotNull null
            contact.copy(phone = phone)
        }
    }

    override suspend fun searchContacts(query: String, limit: Int, offset: Int) =
        withContext(ioDispatcher) {
            if (query.isBlank()) return@withContext emptyList()
            val normalizedQuery = query.trim()
            val phoneMatches = searchContactsByNameOrPhone(
                normalizedQuery,
                limit,
                offset
            )

            if (phoneMatches.size >= limit) {
                return@withContext phoneMatches
            }

            val remaining = limit - phoneMatches.size

            val emailMatches = searchContactsByEmailOrAddress(
                query = normalizedQuery,
                limit = remaining,
                offset = 0
            )

            (phoneMatches + emailMatches)
                .distinctBy { it.id to it.phone }
                .take(limit)
        }

    private val cache = LruCache<String, Contact>(100)
    override suspend fun getContactByPhone(phoneNumber: String) = withContext(ioDispatcher) {
        val key = phoneUtils.normalizeNumber(phoneNumber)
            ?.takeIf { it.isNotEmpty() } ?: return@withContext null
        cache.get(key)?.let { return@withContext it }
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(key)
        )
        return@withContext contentResolver.query(
            uri, contactDetailProjection, null, null, null
        )?.use { cursor ->
            val idColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
            val nameColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val numberColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)
            val photoUriColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)
            val photoThumbnailUriColumnIndex =
                cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
            if (cursor.moveToFirst())
                Contact(
                    id = cursor.getLong(idColumnIndex),
                    name = cursor.getString(nameColumnIndex),
                    phone = cursor.getString(numberColumnIndex),
                    image = (
                        cursor.getStringOrNull(photoUriColumnIndex)?.takeIf { it.isNotBlank() }
                            ?: cursor.getStringOrNull(photoThumbnailUriColumnIndex)
                                ?.takeIf { it.isNotBlank() }
                        )?.toUri()
                ) else null
        }?.also {
            cache.put(key, it)
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

    override suspend fun removeFromFavourites(contactId: Long): Unit = withContext(ioDispatcher) {
        val values = ContentValues().apply {
            put(ContactsContract.Contacts.STARRED, 0)
        }
        contentResolver.update(
            ContactsContract.Contacts.CONTENT_URI,
            values,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(contactId.toString())
        )
    }

    override fun observeFavourites(): Flow<List<Contact>> = callbackFlow {
        fun load(): List<Contact> {
            val selection = "${ContactsContract.Contacts.STARRED} = 1"
            return contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                selection,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val idColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoUriColumnIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val seenIds = HashSet<Long>()
                val contactList = buildList {
                    while (cursor.moveToNext()) {
                        val contact = Contact(
                            id = cursor.getLong(idColumnIndex),
                            name = cursor.getString(nameColumnIndex),
                            phone = cursor.getString(numberColumnIndex),
                            image = cursor.getStringOrNull(photoUriColumnIndex)?.toUri()
                        )
                        if (seenIds.add(contact.id)) add(contact)
                    }
                }
                contactList
            } ?: emptyList()
        }

        trySend(load())

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                launch(ioDispatcher) {
                    trySend(load())
                }
            }
        }
        contentResolver.registerContentObserver(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, observer
        )

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }

    }.flowOn(ioDispatcher)

    private fun searchContactsByNameOrPhone(
        normalizedQuery: String,
        limit: Int,
        offset: Int
    ): List<Contact> {
        val t9Pattern = normalizedQuery.toT9GlobPattern()
        val nameColumn = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        val numberColumn = ContactsContract.CommonDataKinds.Phone.NUMBER
        val selection = buildString {
            append("($nameColumn LIKE ? OR $numberColumn LIKE ?")
            if (t9Pattern != null) append(" OR $nameColumn GLOB ?")
            append(")")
        }
        val args = buildList {
            add("%$normalizedQuery%")
            add("%$normalizedQuery%")
            if (t9Pattern != null) add(t9Pattern)
        }.toTypedArray()
        val phoneAndNameMatches = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            selection,
            args,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT $limit OFFSET $offset"
        )?.use { cursor ->
            val idColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriColumnIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            val contactList = buildList {
                while (cursor.moveToNext()) {
                    val contact = Contact(
                        id = cursor.getLong(idColumnIndex),
                        name = cursor.getString(nameColumnIndex),
                        phone = cursor.getString(numberColumnIndex),
                        image = cursor.getStringOrNull(photoUriColumnIndex)?.toUri()
                    )
                    add(contact)
                }
            }
            contactList

        } ?: emptyList()
        return phoneAndNameMatches
    }

    private fun searchContactsByEmailOrAddress(
        query: String,
        limit: Int,
        offset: Int
    ): List<Contact> {
        val mimetypeColumn = ContactsContract.Data.MIMETYPE
        val selection = """
            ($mimetypeColumn = ? AND ${ContactsContract.CommonDataKinds.Email.ADDRESS} LIKE ?)
            OR
            ($mimetypeColumn = ? AND ${ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS} LIKE ?)
        """.trimIndent()
        val args = arrayOf(
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            "%$query%",
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
            "%$query%"
        )

        val emailAndAddressMatchedIds = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data.CONTACT_ID),
            selection,
            args,
            "${ContactsContract.Data.DISPLAY_NAME} COLLATE NOCASE ASC LIMIT $limit OFFSET $offset"
        )?.use { cursor ->
            val contactIdIndex = cursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val ids = LinkedHashSet<Long>()
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(contactIdIndex))
            }
            ids.toList()
        } ?: emptyList()

        return loadContactsByIds(emailAndAddressMatchedIds)
    }

    private fun loadContactsByIds(contactIds: List<Long>): List<Contact> {
        if (contactIds.isEmpty()) return emptyList()

        val placeholders = contactIds.joinToString(",") { "?" }
        val selection =
            "${ContactsContract.Contacts._ID} IN ($placeholders) AND ${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0"
        val args = contactIds.map { it.toString() }.toTypedArray()
        val contacts = mutableListOf<Contact>()

        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            contactProjection,
            selection,
            args,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"
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
                    image = cursor.getStringOrNull(photoIndex)?.toUri()
                )
            }
        }

        if (contacts.isEmpty()) return emptyList()
        val phoneNumbers = getPhoneNumbersForContactIds(contacts.map { it.id })
        return contacts.mapNotNull { contact ->
            val phone = phoneNumbers[contact.id] ?: return@mapNotNull null
            contact.copy(phone = phone)
        }
    }

    private fun getPhoneNumbersForContactIds(contactIds: List<Long>): Map<Long, String> {
        if (contactIds.isEmpty()) return emptyMap()
        val placeholders = contactIds.joinToString(",") { "?" }
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} IN ($placeholders)"
        val args = contactIds.map { it.toString() }.toTypedArray()

        return contentResolver.query(
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
            val idIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex =
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            buildMap {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val number = cursor.getString(numberIndex)
                    if (!number.isNullOrBlank()) {
                        putIfAbsent(id, number)
                    }
                }
            }
        } ?: emptyMap()

    }
}
