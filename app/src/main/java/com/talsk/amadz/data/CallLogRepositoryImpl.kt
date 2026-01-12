package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.CallLog
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repo.SimInfoProvider
import com.talsk.amadz.domain.entity.CallLogData
import com.talsk.amadz.domain.entity.CallLogType
import com.talsk.amadz.domain.entity.Contact
import com.talsk.amadz.domain.repo.CallLogRepository
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import com.talsk.amadz.ui.extensions.getStringOrEmpty
import com.talsk.amadz.ui.extensions.map
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
    private val contactPhotoProvider: ContactPhotoProvider
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
            val contactCounts = mutableMapOf<String, Pair<String?, Int>>() // phone -> (name, count)

            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE
            )

            val selection = "${CallLog.Calls.TYPE} != ${CallLog.Calls.MISSED_TYPE}"

            // Only fetch recent logs to reduce amount of data
            val now = System.currentTimeMillis()
            val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000 // last 30 days
            val selectionWithDate = "$selection AND ${CallLog.Calls.DATE} >= $oneMonthAgo"

            contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selectionWithDate,
                null,
                null // sort order not needed
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)

                while (cursor.moveToNext()) {
                    val number = cursor.getStringOrNull(numberIndex) ?: continue
                    val name =
                        cursor.getStringOrNull(nameIndex)?.takeIf { it.isNotEmpty() } ?: "Unknown"
                    val count = contactCounts[number]?.second ?: 0
                    contactCounts[number] = name to (count + 1)
                }
            }

            // Return top 10
            contactCounts.entries
                .sortedByDescending { it.value.second }
                .take(10)
                .map { (phone, pair) ->
                    Contact(
                        id = phone.hashCode().toLong(),
                        name = pair.first ?: phone,
                        phone = phone,
                        image = contactPhotoProvider.getContactPhotoUri(phone)
                    )
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