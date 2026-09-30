package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.CallLogType
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.ui.extensions.getStringOrEmpty
import com.talsk.amadz.util.PhoneUtils
import com.talsk.amadz.util.toT9GlobPattern
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
    private val phoneUtils: PhoneUtils,
    private val contactRepository: ContactRepository
) : CallLogRepository {
    val contentResolver: ContentResolver = context.contentResolver

    @SuppressLint("MissingPermission")
    override suspend fun getCallLogsPaged(
        limit: Int,
        offset: Int
    ) = withContext(ioDispatcher) {
        queryCallLogs(
            selection = null,
            selectionArgs = null,
            sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCallLogsByPhone(phone: String) = withContext(ioDispatcher) {
        queryCallLogs(
            selection = "${CallLog.Calls.NUMBER} = ?",
            selectionArgs = arrayOf(phone),
            sortOrder = "${CallLog.Calls.DATE} DESC"
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun deleteCallLogsByPhone(phone: String) = withContext(ioDispatcher) {
        contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            "${CallLog.Calls.NUMBER} = ?",
            arrayOf(phone)
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun deleteAllCallLogs() = withContext(ioDispatcher) {
        contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            null,
            null
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun getFrequentCalledContacts(): List<Contact> = withContext(ioDispatcher) {
        val callCounts = mutableMapOf<String, Int>()

        val projection = arrayOf(CallLog.Calls.NUMBER)
        val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        // Filter non-missed calls in the last 30 days
        val selection = """
        ${CallLog.Calls.TYPE} != ?
        AND ${CallLog.Calls.DATE} >= ?
        """.trimIndent()

        val selectionArgs = arrayOf(
            CallLog.Calls.MISSED_TYPE.toString(),
            oneMonthAgo.toString()
        )

        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val numberIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)

            while (cursor.moveToNext()) {
                val rawNumber = cursor.getString(numberIdx) ?: continue
                // Normalize phone number to group variations like +1 (234) vs 1234
                val normalizedNumber = phoneUtils.normalizeNumber(rawNumber)
                    ?.takeIf { it.isNotEmpty() }
                    ?: continue

                callCounts.merge(normalizedNumber, 1, Int::plus)
            }
        }

        // Pick top 10 unique normalized numbers
        return@withContext callCounts.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(10)
            .mapNotNull { contactRepository.getContactByPhone(it) }
            .distinctBy { it.id }
            .toList()
    }

    @SuppressLint("MissingPermission")
    override suspend fun searchCallLogContacts(
        query: String,
        limit: Int,
        offset: Int
    ): List<Contact> = withContext(ioDispatcher) {
        if (query.isBlank()) return@withContext emptyList()

        val normalizedQuery = query.trim()
        val t9Pattern = normalizedQuery.toT9GlobPattern()
        val nameColumn = CallLog.Calls.CACHED_NAME

        val selection = buildString {
            append("(${CallLog.Calls.NUMBER} LIKE ? OR $nameColumn LIKE ?")
            if (t9Pattern != null) append(" OR $nameColumn GLOB ?")
            append(")")
        }
        val selectionArgs = buildList {
            add("%$normalizedQuery%")
            add("%$normalizedQuery%")
            if (t9Pattern != null) add(t9Pattern)
        }.toTypedArray()

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_PHOTO_URI
        )

        data class RawCallRecord(
            val rawPhone: String,
            val normalizedPhone: String,
            val fallbackName: String,
            val fallbackImage: Uri?
        )

        val records = mutableListOf<RawCallRecord>()
        val seenNormalizedNumbers = HashSet<String>()

        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val photoIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI)

            while (cursor.moveToNext()) {
                val rawPhone = cursor.getStringOrNull(numberIndex).orEmpty()
                if (rawPhone.isBlank()) continue
                val normalizedPhone = phoneUtils.normalizeNumber(rawPhone) ?: continue
                // Deduplicate at cursor iteration step using normalized number
                if (seenNormalizedNumbers.add(normalizedPhone)) {
                    val fallbackName =
                        cursor.getStringOrNull(nameIndex).orEmpty().ifBlank { "Unknown" }
                    val fallbackImage = cursor.getStringOrNull(photoIndex)?.toUri()

                    records.add(
                        RawCallRecord(
                            rawPhone = rawPhone,
                            normalizedPhone = normalizedPhone,
                            fallbackName = fallbackName,
                            fallbackImage = fallbackImage
                        )
                    )
                }
            }
        }

        if (records.isEmpty()) return@withContext emptyList()

        // 2. Resolve contacts (Leverages cache; performs repository query outside cursor scope)
        records.map { record ->
            val savedContact = contactRepository.getContactByPhone(record.rawPhone)
            savedContact ?: Contact(
                id = -record.normalizedPhone.hashCode().toLong(),
                name = record.fallbackName,
                phone = record.rawPhone,
                image = record.fallbackImage
            )
        }

    }


    private suspend fun queryCallLogs(
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String
    ): List<CallLogData> {
        val simsInfo = simInfoProvider.getSimsInfo()
        val simsByAccountId = simsInfo.associateBy { it.accountId }
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
                    val callSim = if (simsInfo.size > 1)
                        simsByAccountId[cursor.getStringOrNull(accountIdColumnIndex)] else null

                    val simSlot = if (simsInfo.size > 1) callSim?.simSlotIndex ?: -1 else null
                    val contactIdFromLog = cursor.getStringOrNull(cachedLookupUriIndex)
                        ?.toUri()
                        ?.let { uri -> runCatching { ContentUris.parseId(uri) }.getOrNull() }
                    val cachedPhoto = cursor.getStringOrNull(cachedPhotoUriIndex)
                        ?.takeIf { it.isNotBlank() }
                        ?.toUri()
                        ?.takeIf { it != Uri.EMPTY }
                    val shouldLookupContact = contactIdFromLog == null || cachedPhoto == null
                    val contact = if (shouldLookupContact) {
                        contactRepository.getContactByPhone(phone)
                    } else {
                        null
                    }

                    val resolvedContactId = contactIdFromLog ?: contact?.id
                    val photo = cachedPhoto ?: contact?.image

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
                            simDisplayName = callSim?.displayName,
                            image = photo
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
