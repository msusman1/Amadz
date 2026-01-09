package com.talsk.amadz.core

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.LruCache
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactPhotoProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val resolver = context.contentResolver
    private val cache = LruCache<String, Uri?>(100)

    fun getCachedPhoto(phone: String): Uri? {
        return cache[phone] ?: loadPhoto(phone).also {
            cache.put(phone, it)
        }
    }

    private fun loadPhoto(phone: String): Uri? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phone)
        )
        return resolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.PHOTO_URI),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getStringOrNull(0)?.toUri()
            } else null
        }
    }
}
