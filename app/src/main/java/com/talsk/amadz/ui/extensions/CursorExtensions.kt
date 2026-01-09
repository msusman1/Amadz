package com.talsk.amadz.ui.extensions

import android.database.Cursor
import androidx.core.database.getStringOrNull

inline fun <T> Cursor.map(transform: (Cursor) -> T): List<T> {
    val list = mutableListOf<T>()
    while (moveToNext()) {
        list += transform(this)
    }
    return list
}

fun Cursor.getStringOrEmpty(column: String): String =
    getStringOrNull(getColumnIndex(column)) ?: ""