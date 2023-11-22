package com.talsk.amadz.data

import android.net.Uri
import android.provider.CallLog
import androidx.compose.ui.graphics.Color
import com.talsk.amadz.util.getRandomColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */
data class ContactData(
    val id: Long,
    val name: String,
    val phone: String,
    val image: Uri?,
    val bgColor: Color = getRandomColor(),
    val isFavourite: Boolean,
)

data class CallLogData(
    val id: Long,
    val name: String,
    val phone: String,
    val image: Uri?,
    val bgColor: Color = getRandomColor(),
    val callLogType: CallLogType,
    val time: Date,
    val callDuration: Long,
) {
    fun toContactData(): ContactData {
        return ContactData(
            id = id,
            name = name,
            phone = phone,
            image = image,
            bgColor = bgColor,
            isFavourite = false
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

fun List<ContactData>.filterContacts(query: String): List<ContactData> {
    return this.filter {
        it.name.lowercase().contains(query.lowercase()).or(it.phone.contains(query))
    }
}


fun Date.toReadableFormat(): String {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm aaa", Locale.getDefault())
    return dateFormat.format(this)
}