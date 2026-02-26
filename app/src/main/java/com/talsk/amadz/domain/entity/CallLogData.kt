package com.talsk.amadz.domain.entity

import android.net.Uri
import android.provider.CallLog
import java.util.Date


data class CallLogData(
    val id: Long,
    val contactId: Long?,
    val name: String,
    val phone: String,
    val image: Uri?,
    val callLogType: CallLogType,
    val time: Date,
    val callDuration: Long,
    val simSlot: Int?
) {
    fun toContactData(): Contact {
        return Contact(
            id = contactId ?: -1L,
            name = name,
            phone = phone,
            image = image,
        )
    }
}

enum class CallLogType {
    INCOMING,
    OUTGOING,
    MISSED, ;

    companion object {

        fun fromInt(callType: Int): CallLogType {
            return when (callType) {
                CallLog.Calls.INCOMING_TYPE -> INCOMING
                CallLog.Calls.OUTGOING_TYPE -> OUTGOING
                CallLog.Calls.MISSED_TYPE -> MISSED
                else -> INCOMING
            }

        }
    }
}
