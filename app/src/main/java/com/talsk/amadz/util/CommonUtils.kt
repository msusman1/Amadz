package com.talsk.amadz.util

import androidx.compose.ui.graphics.Color
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */


fun secondsToReadableTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun getRandomColor(): Color {
    // Generate a random color using RGB values
    val random = Random.Default
    return Color(
        red = random.nextFloat(),
        green = random.nextFloat(),
        blue = random.nextFloat(),
        alpha = 1f
    )
}

fun getStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.time
}