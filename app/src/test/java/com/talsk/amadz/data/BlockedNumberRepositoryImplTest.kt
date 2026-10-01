package com.talsk.amadz.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.talsk.amadz.domain.entity.BlockedNumber
import com.talsk.amadz.util.PhoneUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BlockedNumberRepositoryImplTest {

    @Test
    fun migratesLegacyExactNumbersAndSupportsRegexRules() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            context.getSharedPreferences(
                BlockedNumberRepositoryImpl.LEGACY_PREF_NAME,
                Context.MODE_PRIVATE
            ).edit()
                .putStringSet("blocked_numbers", setOf("2025550123"))
                .commit()

            val repository = BlockedNumberRepositoryImpl(context, PhoneUtils(context))

            assertTrue(repository.isBlocked("+1 (202) 555-0123"))
            assertFalse(repository.isBlocked("2025550199"))

            repository.blockPattern("^202\\d{7}$")
            assertTrue(repository.isBlocked("2025550199"))
            assertEquals(
                listOf(
                    BlockedNumber("2025550123", BlockedNumber.Type.EXACT),
                    BlockedNumber("^202\\d{7}$", BlockedNumber.Type.REGEX)
                ),
                repository.getBlockedNumbers().first()
            )

            repository.unblock(BlockedNumber("^202\\d{7}$", BlockedNumber.Type.REGEX))
            assertFalse(repository.isBlocked("2025550199"))
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { repository.blockPattern("[") }
            }
        }
    }
}
