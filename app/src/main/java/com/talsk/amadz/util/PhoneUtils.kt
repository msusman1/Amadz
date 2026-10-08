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

    fun formatForDisplay(phone: String): String {
        return formatForDisplay(phone, defaultRegion())
    }

    fun normalizeNumber(phone: String): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()

        // Get the user's default country code
        return try {
            // Parse the phone number
            val numberProto = phoneUtil.parse(phone, defaultRegion())

            // Format it into E.164 format (+<country_code><number>)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            null // Return null if the phone number is invalid
        }
    }

    fun getNSN(phone: String): String? {
        return getNSN(phone, defaultRegion())
    }

    private fun defaultRegion(): String =
        telephonyManager?.simCountryIso?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.getDefault())
            ?: telephonyManager?.networkCountryIso?.takeIf { it.isNotBlank() }
                ?.uppercase(Locale.getDefault())
            ?: "US"

    companion object {
        internal fun formatForDisplay(phone: String, defaultRegion: String): String {
            val phoneUtil = PhoneNumberUtil.getInstance()
            return try {
                val number = phoneUtil.parse(phone, defaultRegion)
                val format = if (phone.trimStart().startsWith("+")) {
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                } else {
                    PhoneNumberUtil.PhoneNumberFormat.NATIONAL
                }
                phoneUtil.format(number, format)
            } catch (_: NumberParseException) {
                phone
            }
        }
    }

    internal fun getNSN(phone: String, defaultRegion: String): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()

        return try {
            val numberProto = phoneUtil.parse(phone, defaultRegion)

            PhoneNumberUtil.getInstance().getNationalSignificantNumber(numberProto)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            null
        }
    }
}