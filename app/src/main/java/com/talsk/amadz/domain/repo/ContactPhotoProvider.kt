package com.talsk.amadz.domain.repo

import android.graphics.Bitmap
import android.net.Uri

interface ContactPhotoProvider {
    suspend fun getContactPhotoUri(phone: String): Uri?
    suspend fun getContactPhotoBitmap(photoUri: Uri?): Bitmap?
}