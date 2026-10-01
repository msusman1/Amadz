package com.talsk.amadz.util

import android.content.Context
import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PhoneUtilsTest {

    // =========================================================================
    // 1. Display Formatting Tests (National vs International)
    // =========================================================================

    @Test
    fun `formatForDisplay formats national numbers using region rules`() {
        assertEquals("(051) 2521005", PhoneUtils.formatForDisplay("0512521005", "PK"))
        assertEquals("(415) 555-2671", PhoneUtils.formatForDisplay("4155552671", "US"))
        assertEquals("020 7946 0912", PhoneUtils.formatForDisplay("02079460912", "GB"))
    }

    @Test
    fun `formatForDisplay formats international numbers with leading plus sign`() {
        assertEquals("+1 346-828-0786", PhoneUtils.formatForDisplay("+13468280786", "US"))
        assertEquals("+92 51 2521005", PhoneUtils.formatForDisplay("+92512521005", "US"))
        assertEquals("+44 20 7946 0912", PhoneUtils.formatForDisplay("+442079460912", "PK"))
    }

    @Test
    fun `formatForDisplay handles leading whitespace before international prefix`() {
        assertEquals("+1 346-828-0786", PhoneUtils.formatForDisplay("   +13468280786", "US"))
    }

    @Test
    fun `formatForDisplay returns original input on unparseable string`() {
        assertEquals("not a number", PhoneUtils.formatForDisplay("not a number", "US"))
        assertEquals("123", PhoneUtils.formatForDisplay("123", "US")) // Short code / invalid length
        assertEquals("", PhoneUtils.formatForDisplay("", "US"))
    }

    @Test
    fun `formatForDisplay falls back gracefully on unknown region code`() {
        // Unknown region code falls back to parsing as invalid national number
        assertEquals("0512521005", PhoneUtils.formatForDisplay("0512521005", "ZZ"))
        // Valid international numbers still format correctly even with unknown region
        assertEquals("+1 346-828-0786", PhoneUtils.formatForDisplay("+13468280786", "ZZ"))
    }

    // =========================================================================
    // 2. E.164 Normalization Tests
    // =========================================================================

    @Test
    fun `normalizeNumber converts valid national numbers to E164 format`() {
        val phoneUtils = createPhoneUtilsWithRegion("US")
        assertEquals("+14155552671", phoneUtils.normalizeNumber("4155552671"))
        assertEquals("+14155552671", phoneUtils.normalizeNumber("(415) 555-2671"))
    }

    @Test
    fun `normalizeNumber converts international numbers regardless of default region`() {
        val phoneUtils = createPhoneUtilsWithRegion("US")
        assertEquals("+92512521005", phoneUtils.normalizeNumber("+92 51 2521005"))
        assertEquals("+442079460912", phoneUtils.normalizeNumber("+44 20 7946 0912"))
    }

    @Test
    fun `normalizeNumber strips visual formatting, spaces, and hyphens`() {
        val phoneUtils = createPhoneUtilsWithRegion("PK")
        assertEquals("+92512521005", phoneUtils.normalizeNumber("  051-2521005  "))
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun createPhoneUtilsWithRegion(region: String): PhoneUtils {
        val telephonyManager = mock(TelephonyManager::class.java).apply {
            `when`(simCountryIso).thenReturn(region)
            `when`(networkCountryIso).thenReturn(region)
        }

        val context = mock(Context::class.java).apply {
            `when`(getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
            `when`(getSystemService(TelephonyManager::class.java)).thenReturn(telephonyManager)
        }

        return PhoneUtils(context)
    }
}