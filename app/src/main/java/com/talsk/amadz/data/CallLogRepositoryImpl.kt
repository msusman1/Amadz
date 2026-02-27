package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.CallLog
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.CallLogType
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactDetailProvider
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.ui.extensions.getStringOrEmpty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */


class CallLogRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val simInfoProvider: SimInfoProvider,
    private val contactPhotoProvider: ContactPhotoProvider,
    private val contactDetailProvider: ContactDetailProvider
) : CallLogRepository {
    val contentResolver: ContentResolver = context.contentResolver

    @SuppressLint("MissingPermission")
    override suspend fun getCallLogsPaged(
        limit: Int,
        offset: Int
    ): List<CallLogData> = withContext(ioDispatcher) {
        queryCallLogs(
            selection = null,
            selectionArgs = null,
            sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCallLogsByPhone(phone: String): List<CallLogData> = withContext(ioDispatcher) {
        queryCallLogs(
            selection = "${CallLog.Calls.NUMBER} = ?",
            selectionArgs = arrayOf(phone),
            sortOrder = "${CallLog.Calls.DATE} DESC"
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun deleteCallLogsByPhone(phone: String): Int = withContext(ioDispatcher) {
        contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            "${CallLog.Calls.NUMBER} = ?",
            arrayOf(phone)
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun getFrequentCalledContacts(): List<Contact> =
        withContext(ioDispatcher) {

            val callCounts = mutableMapOf<String, Int>() // phone -> count

            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME
            )

            val now = System.currentTimeMillis()
            val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000 // last 30 days

            val selection = """
            ${CallLog.Calls.TYPE} != ${CallLog.Calls.MISSED_TYPE}
            AND ${CallLog.Calls.DATE} >= ?
            AND ${CallLog.Calls.CACHED_NAME} IS NOT NULL
        """.trimIndent()

            val selectionArgs = arrayOf(oneMonthAgo.toString())

            contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->

                val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)

                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIdx) ?: continue
                    callCounts[number] = (callCounts[number] ?: 0) + 1
                }
            }

            // Top 10 most frequently called saved numbers
            val topNumbers = callCounts.entries
                .sortedByDescending { it.value }
                .distinct()
                .take(10)
                .map { it.key }

            // Safe: all numbers are guaranteed to be saved contacts
            return@withContext topNumbers.mapNotNull {
                contactDetailProvider.getContactByPhone(it)
            }.distinctBy { it.id }
        }

    @SuppressLint("MissingPermission")
    override suspend fun searchCallLogContacts(
        query: String,
        limit: Int,
        offset: Int
    ): List<Contact> = withContext(ioDispatcher) {
        if (query.isBlank()) return@withContext emptyList()

        val normalizedQuery = query.trim()
        val selection = """
            ${CallLog.Calls.NUMBER} LIKE ?
            OR ${CallLog.Calls.CACHED_NAME} LIKE ?
        """.trimIndent()
        val selectionArgs = arrayOf("%$normalizedQuery%", "%$normalizedQuery%")

        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_PHOTO_URI
            ),
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val photoIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI)
            val byNumber = LinkedHashMap<String, Contact>()

            while (cursor.moveToNext()) {
                val phone = cursor.getString(numberIndex).orEmpty()
                if (phone.isBlank() || byNumber.containsKey(phone)) continue

                val savedContact = contactDetailProvider.getContactByPhone(phone)
                val fallbackName = cursor.getStringOrNull(nameIndex).orEmpty().ifBlank { "Unknown" }
                val fallbackImage = cursor.getStringOrNull(photoIndex)?.toUri()

                byNumber[phone] = savedContact ?: Contact(
                    id = -1,
                    name = fallbackName,
                    phone = phone,
                    image = fallbackImage
                )
            }

            byNumber.values.toList()
        } ?: emptyList()
    }


    private suspend fun queryCallLogs(
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String
    ): List<CallLogData> {
        val simsInfo = simInfoProvider.getSimsInfo()
        val contactIdsByPhone = mutableMapOf<String, Long?>()

        return contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            PROJECTION,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val idColumnIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val phoneTypeColumnIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val accountIdColumnIndex = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
            val cachedPhotoUriIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
            val cachedLookupUriIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_LOOKUP_URI)

            buildList {
                while (cursor.moveToNext()) {
                    val phone = cursor.getString(numberColumnIndex)
                    var simSlot: Int? = null
                    if (simsInfo.size > 1) {
                        simSlot = simsInfo
                            .find { it.accountId == cursor.getStringOrNull(accountIdColumnIndex) }
                            ?.simSlotIndex ?: -1
                    }
                    val contactIdFromLog = cursor.getStringOrNull(cachedLookupUriIndex)
                        ?.toUri()
                        ?.let { uri -> runCatching { ContentUris.parseId(uri) }.getOrNull() }
                    val resolvedContactId = contactIdFromLog
                        ?: contactIdsByPhone.getOrPut(phone) {
                            contactDetailProvider.getContactByPhone(phone)?.id
                        }

                    add(
                        CallLogData(
                            id = cursor.getLong(idColumnIndex),
                            contactId = resolvedContactId,
                            name = cursor.getStringOrEmpty(CallLog.Calls.CACHED_NAME),
                            phone = phone,
                            time = Date(cursor.getLong(dateColumnIndex)),
                            callDuration = cursor.getLong(durationColumnIndex),
                            callLogType = CallLogType.fromInt(cursor.getInt(phoneTypeColumnIndex)),
                            simSlot = simSlot,
                            image = cursor.getStringOrNull(cachedPhotoUriIndex)?.toUri()
                                ?: contactPhotoProvider.getContactPhotoUri(phone)
                        )
                    )
                }
            }
        } ?: emptyList()
    }

    companion object {
        private val PROJECTION = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.CACHED_PHOTO_URI,
            CallLog.Calls.CACHED_LOOKUP_URI,
        )
    }

}
