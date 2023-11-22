package com.talsk.amadz.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import android.telecom.TelecomManager
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.talsk.amadz.App
import java.net.URLDecoder

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */


fun Call.callerName(): String {
    return this.details.callerDisplayName?.takeIf { it.isNotEmpty() } ?: "Unknown"

}

fun Call.callPhone(): String {
    val encodedString = this.details.handle.toString().removePrefix("tel:")
    return URLDecoder.decode(encodedString, "UTF-8")

}

fun Context.dial(phone: String) {
    App.needDataReload = true
    val telecomManager = this.getSystemService<TelecomManager>()
    val uri = Uri.fromParts("tel", phone, null)
    val extras = Bundle()
    extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    telecomManager?.placeCall(uri, extras)

}
