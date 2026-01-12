package com.talsk.amadz.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.util.LruCache
import androidx.core.net.toUri
import coil3.decode.DecodeUtils
import coil3.size.Scale
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repo.ContactPhotoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactPhotoProviderImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactPhotoProvider {
    private val contentResolver = context.contentResolver
    private val cache = LruCache<String, Uri?>(100)

    suspend fun loadPhoto(phoneNumber: String): Uri? =
        withContext(ioDispatcher) {
            val normalizedNumber = PhoneNumberUtils.normalizeNumber(phoneNumber)
                ?: return@withContext null

            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(normalizedNumber)
            )

            contentResolver.query(
                lookupUri,
                PROJECTION,
                null,
                null,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst()) {
                    val photoUriIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.PhoneLookup.PHOTO_URI
                    )
                    val thumbnailUriIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
                    )
                    cursor.getString(photoUriIndex)?.let { return@withContext it.toUri() }
                    cursor.getString(thumbnailUriIndex)?.let { return@withContext it.toUri() }
                }
            }

            null
        }


    override suspend fun getContactPhotoUri(phone: String): Uri? {
        return cache[phone] ?: loadPhoto(phone)?.also {
            cache.put(phone, it)
        }
    }

    override suspend fun getContactPhotoBitmap(photoUri: Uri?) = withContext(ioDispatcher) {
        if (photoUri == null) return@withContext null
        val targetSizePx = 200
        return@withContext try {
            // 1. Decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            contentResolver.openInputStream(photoUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return@withContext null
            }

            // 2. Calculate optimal inSampleSize
            options.inSampleSize = DecodeUtils.calculateInSampleSize(
                options.outWidth,
                options.outHeight,
                targetSizePx,
                targetSizePx,
                Scale.FILL
            )

            // 3. Decode scaled bitmap
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            contentResolver.openInputStream(photoUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

        } catch (e: SecurityException) {
            Log.e("ContactFetcher", "Permission denied while loading contact image", e)
            null
        } catch (e: IOException) {
            Log.e("ContactFetcher", "Error loading contact image", e)
            null
        }
    }

    companion object Companion {
        private val PROJECTION = arrayOf(
            ContactsContract.PhoneLookup.PHOTO_URI,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        )
    }
}