package com.talsk.amadz.domain.entity

import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */
data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val image: Uri?,
) {

    fun getBackgroundColor(): Color {
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

    fun getNamePlaceHolder(): String {
        // 1. Clean the name and split by spaces
        val parts = name.trim().split("\\s+".toRegex())

        return when {
            // Case: "John Doe" -> "JD"
            parts.size >= 2 -> {
                val firstChar = parts.first().take(1)
                val lastChar = parts.last().take(1)
                (firstChar + lastChar).uppercase()
            }
            // Case: "John" -> "J"
            parts.size == 1 && parts[0].isNotEmpty() -> {
                parts[0].take(1).uppercase()
            }
            // Case: Name is empty but Company exists -> "C"

            // Fallback
            else -> ""
        }
    }

    companion object Companion {
        fun unknown(phone: String): Contact {
            return Contact(
                id = -1,
                name = "Unknown",
                phone = phone,
                image = null,
            )
        }
    }
}


fun List<Contact>.filterContacts(query: String): List<Contact> {
    return this.filter {
        it.name.lowercase().contains(query.lowercase())
            .or(it.phone.replace(" ", "").contains(query))
    }
}