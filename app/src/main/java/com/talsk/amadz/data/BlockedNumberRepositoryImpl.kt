package com.talsk.amadz.data

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedNumberRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : BlockedNumberRepository {

    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun isBlocked(phone: String): Boolean {
        val canonical = phone.toCanonicalNumber() ?: return false
        return blockedNumbers().contains(canonical)
    }

    override fun block(phone: String) {
        val canonical = phone.toCanonicalNumber() ?: return
        val updated = blockedNumbers().toMutableSet().apply { add(canonical) }
        preferences.edit().putStringSet(KEY_BLOCKED_NUMBERS, updated).apply()
    }

    override fun unblock(phone: String) {
        val canonical = phone.toCanonicalNumber() ?: return
        val updated = blockedNumbers().toMutableSet().apply { remove(canonical) }
        preferences.edit().putStringSet(KEY_BLOCKED_NUMBERS, updated).apply()
    }

    private fun blockedNumbers(): Set<String> {
        return preferences.getStringSet(KEY_BLOCKED_NUMBERS, emptySet()).orEmpty()
    }

    private fun String.toCanonicalNumber(): String? {
        val normalized = PhoneNumberUtils.normalizeNumber(this).orEmpty()
        val digits = normalized.filter { it.isDigit() }
        if (digits.isBlank()) return null
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    companion object {
        private const val PREF_NAME = "blocked_numbers_preferences"
        private const val KEY_BLOCKED_NUMBERS = "blocked_numbers"
    }
}
