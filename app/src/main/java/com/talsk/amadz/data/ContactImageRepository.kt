package com.talsk.amadz.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.IOException

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/21/2023.
 */
class ContactImageRepository(val context: Context) {
    fun loadContactImage(photoUri: Uri?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            if (photoUri != null) {
                val inputStream = context.contentResolver.openInputStream(photoUri)
                bitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeStream(inputStream),
                    200,
                    200,
                    true
                )
                inputStream?.close()
            }
        } catch (e: IOException) {
            Log.e("ContactFetcher", "Error loading contact image", e)

        }
        return bitmap
    }
}