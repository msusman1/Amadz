package com.talsk.amadz.util

import android.media.ToneGenerator
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */


fun secondsToReadableTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

fun getContactColor(phone: String): Color {
    val AvatarColors = listOf(
        Color(0xFFEF5350), // Red
        Color(0xFFEC407A), // Pink
        Color(0xFFAB47BC), // Purple
        Color(0xFF7E57C2), // Deep Purple
        Color(0xFF5C6BC0), // Indigo
        Color(0xFF42A5F5), // Blue
        Color(0xFF26A69A), // Teal
        Color(0xFF66BB6A), // Green
        Color(0xFFFFA726), // Orange
        Color(0xFF8D6E63), // Brown
        Color(0xFF78909C)  // Blue Grey
    )
    if (phone.isEmpty()) return AvatarColors[0]

    // 2. Use hashCode to get a consistent number for the same string
    // Math.abs handles negative hashes to keep the index positive
    val index = abs(phone.hashCode()) % AvatarColors.size

    return AvatarColors[index]
}

fun getStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.time
}


fun Date.toReadableFormat(): String {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm aaa", Locale.getDefault())
    return dateFormat.format(this)
}

fun Date.toDayCategory(): String {
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply { time = this@toDayCategory }

    return when {
        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) ->
            "Today"

        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) == 1 ->
            "Yesterday"

        else ->
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this)
    }
}
