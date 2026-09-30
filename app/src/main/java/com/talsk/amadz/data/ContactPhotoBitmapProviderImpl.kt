package com.talsk.amadz.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import coil3.decode.DecodeUtils
import coil3.size.Scale
import com.talsk.amadz.di.IODispatcher
import com.talsk.amadz.domain.repo.ContactPhotoBitmapProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactPhotoBitmapProviderImpl @Inject constructor(
    @ApplicationContext context: Context,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : ContactPhotoBitmapProvider {
    private val contentResolver = context.contentResolver

    override suspend fun getContactPhotoBitmap(photoUri: Uri?) = withContext(ioDispatcher) {
        if (photoUri == null) return@withContext null
        try {
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
                CONTACT_PHOTO_SIZE_PX,
                CONTACT_PHOTO_SIZE_PX,
                Scale.FILL
            )

            // 3. Decode scaled bitmap
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            contentResolver.openInputStream(photoUri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while loading contact image", e)
            null
        } catch (e: IOException) {
            Log.e(TAG, "Error loading contact image", e)
            null
        }
    }

    companion object Companion {

        const val TAG = "ContactPhotoProvider"
        const val CONTACT_PHOTO_SIZE_PX = 200
    }
}