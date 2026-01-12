package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.CallLog
import android.util.Log
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.CallLogType
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactDetailProvider
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import com.talsk.amadz.domain.repo.ContactRepository
import com.talsk.amadz.ui.extensions.getStringOrEmpty
import com.talsk.amadz.ui.extensions.map
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
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

        val simsInfo = simInfoProvider.getSimsInfo()
        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            PROJECTION,
            null,
            null,
            "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
        )?.use { cursor ->
            cursor.map { row ->
                val numberColumnIndex = row.getColumnIndex(CallLog.Calls.NUMBER)
                val idColumnIndex = row.getColumnIndex(CallLog.Calls._ID)
                val dateColumnIndex = row.getColumnIndex(CallLog.Calls.DATE)
                val durationColumnIndex = row.getColumnIndex(CallLog.Calls.DURATION)
                val phoneTypeColumnIndex = row.getColumnIndex(CallLog.Calls.TYPE)
                val accountIdColumnIndex = row.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
                val cachedPhotoUriIndex = row.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
                val phone = row.getString(numberColumnIndex)
                val simSlot = simsInfo
                    .find { it.accountId == row.getStringOrNull(accountIdColumnIndex) }
                    ?.simSlotIndex ?: -1
                CallLogData(
                    id = row.getLong(idColumnIndex),
                    name = row.getStringOrEmpty(CallLog.Calls.CACHED_NAME),
                    phone = phone,
                    time = Date(row.getLong(dateColumnIndex)),
                    callDuration = row.getLong(durationColumnIndex),
                    callLogType = CallLogType.fromInt(row.getInt(phoneTypeColumnIndex)),
                    simSlot = simSlot,
                    image = row.getStringOrNull(cachedPhotoUriIndex)?.toUri()
                        ?: contactPhotoProvider.getContactPhotoUri(phone)
                )
            }
        } ?: emptyList()
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
                .take(10)
                .map { it.key }

            // Safe: all numbers are guaranteed to be saved contacts
            return@withContext topNumbers.mapNotNull {
                contactDetailProvider.getContactByPhone(it)
            }
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
        )
    }

}