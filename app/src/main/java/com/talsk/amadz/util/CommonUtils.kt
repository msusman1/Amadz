package com.talsk.amadz.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */


fun secondsToReadableTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}



fun Date.toReadableFormat(): String {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm aaa", Locale.getDefault())
    return dateFormat.format(this)
}

fun Date.toDayCategory(): String {
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { time = this@toDayCategory }
    return DateUtils.getRelativeTimeSpanString(
        cal.timeInMillis,
        now.timeInMillis,
        DateUtils.DAY_IN_MILLIS
    ).toString()

}

fun Date.startOfDay(): Date {
    val cal = Calendar.getInstance().apply {
        time = this@startOfDay
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.time
}

fun Date.isSameDay(other: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = this@isSameDay }
    val c2 = Calendar.getInstance().apply { time = other }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}