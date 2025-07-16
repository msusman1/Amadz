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
import java.util.Date

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */
class CallLogsRepository(val context: Context) {
    val TAG = "CallLogsRepository"
    private val contactImageRepository = ContactImageRepository(context)

    @SuppressLint("MissingPermission")
    fun getCallLogsPaged(limit: Int, offset: Int): List<CallLogData> {
        Log.d(TAG, "getCallLogsPaged() called with limit=$limit offset=$offset")


        val simInfoMap: Map<String, Pair<Int, String?>> = run {
            val map = mutableMapOf<String, Pair<Int, String?>>()
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val phoneAccounts = telecomManager.callCapablePhoneAccounts

            for (accountHandle in phoneAccounts) {
                val id = accountHandle.id // PHONE_ACCOUNT_ID
                val subscriptionId = accountHandle.id.toIntOrNull() ?: continue
                val info = subscriptionManager.getActiveSubscriptionInfo(subscriptionId) ?: continue
                val simSlot = info.simSlotIndex
                val simName = info.displayName?.toString()
                map[id] = simSlot to simName
            }
            map
        }
        Log.d(TAG, "getAllCallLogs() called")
        val callLogsList = mutableListOf<CallLogData>()
        // Set up the ContentResolver
        val contentResolver: ContentResolver = context.contentResolver
        // Define the columns you want to retrieve
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE,
            CallLog.Calls.PHONE_ACCOUNT_ID
        )


        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
        val cursor: Cursor? = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )
        if (cursor != null) {
            val idColumnIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameColumnIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val dateColumnIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationColumnIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val typeColumnIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val simIndex = cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)

            while (cursor.moveToNext()) {
                val callLogId = cursor.getLong(idColumnIndex)
                val phoneNumber = cursor.getString(numberColumnIndex)
                val contactName = cursor.getStringOrNull(nameColumnIndex) ?: ""
                val callDate = cursor.getLong(dateColumnIndex)
                val callDuration = cursor.getLong(durationColumnIndex)
                val callType = cursor.getInt(typeColumnIndex)
                val phoneAccountId = cursor.getStringOrNull(simIndex)
                val (simSlot, simName) = simInfoMap[phoneAccountId] ?: (-1 to null)

                val image = getContactImage(phoneNumber)
                val callLogItem = CallLogData(
                    id = callLogId,
                    name = contactName,
                    phone = phoneNumber,
                    time = Date(callDate),
                    image = image,
                    callLogType = CallLogType.fromInt(callType),
                    callDuration = callDuration,
                    simSlot = simSlot
                )
                callLogsList.add(callLogItem)
            }
            cursor.close()
        }

        return callLogsList
    }

    private fun getContactImage(phoneNumber: String): Uri? {

        val contentResolver: ContentResolver = context.contentResolver
        val contactUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)
        )
        val contactProjection = arrayOf(ContactsContract.PhoneLookup.PHOTO_URI)
        val contactCursor = contentResolver.query(
            contactUri, contactProjection, null, null, null
        )
        var photoUri: Uri? = null
        if (contactCursor != null) {
            if (contactCursor.moveToFirst()) {
                photoUri =
                    contactCursor.getStringOrNull(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI))
                        ?.toUri()
            }
            contactCursor.close()
        }

        return photoUri
    }
}