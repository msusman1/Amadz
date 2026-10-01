package com.talsk.amadz.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test


class T9SearchTest {
    @Test
    fun createsGlobPatternForDialpadDigits() {
        assertEquals("*[abcABC]*[defDEF]*[ghiGHI]*[defDEF]*", "2343".toT9GlobPattern())
    }

    @Test
    fun returnsNullWhenQueryContainsNonT9Digits() {
        assertNull("201".toT9GlobPattern())
        assertNull("".toT9GlobPattern())
    }
}
