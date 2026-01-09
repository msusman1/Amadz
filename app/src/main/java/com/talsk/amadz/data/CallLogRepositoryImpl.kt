package com.talsk.amadz.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.talsk.amadz.core.ContactPhotoProvider
import com.talsk.amadz.core.SimInfoProvider
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repos.CallLogRepository
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

        val simInfoMap = simInfoProvider.getSimInfo()

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
                val phone = row.getString(numberColumnIndex)
                CallLogData(
                    id = row.getLong(idColumnIndex),
                    name = row.getStringOrEmpty(CallLog.Calls.CACHED_NAME),
                    phone = phone,
                    time = Date(row.getLong(dateColumnIndex)),
                    callDuration = row.getLong(durationColumnIndex),
                    callLogType = CallLogType.fromInt(row.getInt(phoneTypeColumnIndex)),
                    simSlot = simInfoMap[row.getStringOrNull(accountIdColumnIndex)]?.first ?: -1,
                    image = Uri.EMPTY
//                    image = contactPhotoProvider.getCachedPhoto(phone)
                )
            }
        } ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    override suspend fun getFrequentCalledContacts(): List<ContactData> =
        withContext(ioDispatcher) {
            val contactCounts = mutableMapOf<String, Pair<String?, Int>>() // phone -> (name, count)

            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE
            )

            val selection = "${CallLog.Calls.TYPE} != ${CallLog.Calls.MISSED_TYPE}"

            contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                null,
                null // sort order not needed, we will sort later
            )?.use { cursor ->
                val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

                while (cursor.moveToNext()) {
                    val number = cursor.getStringOrNull(numberIndex) ?: continue
                    val name = cursor.getStringOrNull(nameIndex)

                    val currentCount = contactCounts[number]?.second ?: 0
                    contactCounts[number] = name to (currentCount + 1)
                }
            }

            // Sort by count descending, take top 10
            contactCounts.entries
                .sortedByDescending { it.value.second }
                .take(10)
                .map { (phone, pair) ->
                    ContactData(
                        id = phone.hashCode().toLong(),
                        name = pair.first ?: phone,
                        phone = phone,
                        image = null, // optionally load via contact photo provider
                        isFavourite = false,
                        imageBitmap = null,
                        companyName = ""
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
            CallLog.Calls.PHONE_ACCOUNT_ID
        )
    }

}