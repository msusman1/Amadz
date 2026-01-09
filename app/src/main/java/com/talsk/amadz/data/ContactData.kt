package com.talsk.amadz.data

import android.graphics.Bitmap
import android.net.Uri

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/18/2023.
 */
data class ContactData(
    val id: Long,
    val name: String,
    val companyName: String,
    val phone: String,
    val image: Uri?,
    val imageBitmap: Bitmap?,
    val isFavourite: Boolean,
) {

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
            companyName.isNotBlank() -> {
                companyName.trim().take(1).uppercase()
            }
            // Fallback
            else -> ""
        }
    }

    companion object {
        fun unknown(phone: String): ContactData {
            return ContactData(
                id = -1,
                name = "Unknown",
                companyName = "",
                phone = phone,
                image = null,
                imageBitmap = null,
                isFavourite = false
            )

        }
    }
}


fun List<ContactData>.filterContacts(query: String): List<ContactData> {
    return this.filter {
        it.name.lowercase().contains(query.lowercase())
            .or(it.phone.replace(" ", "").contains(query))
    }
}