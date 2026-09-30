package com.talsk.amadz.util

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat.getSystemService
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class PhoneUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val telephonyManager = getSystemService(context, TelephonyManager::class.java)

    fun normalizeNumber(phone: String): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()

        // Get the user's default country code
        val defaultRegion = telephonyManager?.simCountryIso?.uppercase(Locale.getDefault())
            ?: telephonyManager?.networkCountryIso?.uppercase(Locale.getDefault())
            ?: "US" // Default to "US" if unknown

        return try {
            // Parse the phone number
            val numberProto = phoneUtil.parse(phone, defaultRegion)

            // Format it into E.164 format (+<country_code><number>)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            null // Return null if the phone number is invalid
        }
    }
}