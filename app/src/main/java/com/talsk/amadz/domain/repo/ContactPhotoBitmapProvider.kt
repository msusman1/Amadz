package com.talsk.amadz.domain.repo

import android.graphics.Bitmap
import android.net.Uri

interface ContactPhotoBitmapProvider {
    suspend fun getContactPhotoBitmap(photoUri: Uri?): Bitmap?
}