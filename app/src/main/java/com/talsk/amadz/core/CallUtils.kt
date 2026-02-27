package com.talsk.amadz.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import java.net.URLDecoder

/**
 * Created by Muhammad Usman : msusman97@gmail.com on 11/17/2023.
 */


fun Call.callerName(): String {
    return this.details.callerDisplayName?.takeIf { it.isNotEmpty() } ?: "Unknown"

}

fun Call.callerPhone(): String {
    val encodedString = this.details.handle.toString().removePrefix("tel:")
    return URLDecoder.decode(encodedString, "UTF-8")

}

@SuppressLint("MissingPermission")
fun Context.hasDefaultCallingSimConfigured(): Boolean {
    val telecomManager = this.getSystemService<TelecomManager>() ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        runCatching {
            telecomManager.userSelectedOutgoingPhoneAccount != null
        }.getOrDefault(false)
    } else {
        runCatching {
            SubscriptionManager.getDefaultVoiceSubscriptionId() !=
                    SubscriptionManager.INVALID_SUBSCRIPTION_ID
        }.getOrDefault(false)
    }
}

fun Context.dial(phone: String, accountId: String? = null) {
    val telecomManager = this.getSystemService<TelecomManager>()
    val uri = Uri.fromParts("tel", phone, null)
    val extras = Bundle()
    extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false)
    if (accountId != null && telecomManager != null) {
        val matchingHandle: PhoneAccountHandle? = runCatching {
            telecomManager.callCapablePhoneAccounts.firstOrNull { it.id == accountId }
        }.getOrNull()
        if (matchingHandle != null) {
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, matchingHandle)
        }
    }
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    telecomManager?.placeCall(uri, extras)

}
