package com.talsk.amadz.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.talsk.amadz.domain.entity.BlockedNumber
import com.talsk.amadz.domain.repo.BlockedNumberRepository
import com.talsk.amadz.util.PhoneUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.blockedNumbersDataStore: DataStore<Preferences> by preferencesDataStore(
    name = BlockedNumberRepositoryImpl.PREF_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, BlockedNumberRepositoryImpl.LEGACY_PREF_NAME))
    }
)

@Singleton
class BlockedNumberRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val phoneUtils: PhoneUtils
) : BlockedNumberRepository {

    private val exactNumbersKey = stringSetPreferencesKey(KEY_BLOCKED_NUMBERS)
    private val blockedPatternsKey = stringSetPreferencesKey(KEY_BLOCKED_PATTERNS)

    override suspend fun isBlocked(phone: String): Boolean {
        val canonical = phone.toCanonicalNumber() ?: return false
        val preferences = context.blockedNumbersDataStore.data.first()
        if (canonical in preferences[exactNumbersKey].orEmpty()) return true
        return preferences[blockedPatternsKey].orEmpty().any { pattern ->
            Regex(pattern).containsMatchIn(canonical)
        }
    }

    override fun getBlockedNumbers(): Flow<List<BlockedNumber>> =
        context.blockedNumbersDataStore.data.map { preferences ->
            buildList {
                preferences[exactNumbersKey].orEmpty().forEach { value ->
                    add(BlockedNumber(value, BlockedNumber.Type.EXACT))
                }
                preferences[blockedPatternsKey].orEmpty().forEach { value ->
                    add(BlockedNumber(value, BlockedNumber.Type.REGEX))
                }
            }.sortedWith(compareBy({ it.value }, { it.type }))
        }

    override suspend fun block(phone: String) {
        val canonical = phone.toCanonicalNumber() ?: return
        context.blockedNumbersDataStore.edit { preferences ->
            preferences[exactNumbersKey] = preferences[exactNumbersKey].orEmpty() + canonical
        }
    }

    override suspend fun blockPattern(pattern: String) {
        require(pattern.isNotBlank()) { "A regex pattern cannot be blank." }
        Regex(pattern)
        context.blockedNumbersDataStore.edit { preferences ->
            preferences[blockedPatternsKey] = preferences[blockedPatternsKey].orEmpty() + pattern
        }
    }

    override suspend fun unblock(blockedNumber: BlockedNumber) {
        val key = when (blockedNumber.type) {
            BlockedNumber.Type.EXACT -> exactNumbersKey
            BlockedNumber.Type.REGEX -> blockedPatternsKey
        }
        context.blockedNumbersDataStore.edit { preferences ->
            preferences[key] = preferences[key].orEmpty() - blockedNumber.value
        }
    }

    override suspend fun unblock(phone: String) {
        val canonical = phone.toCanonicalNumber() ?: return
        context.blockedNumbersDataStore.edit { preferences ->
            preferences[exactNumbersKey] = preferences[exactNumbersKey].orEmpty() - canonical
        }
    }

    private fun String.toCanonicalNumber(): String? {
        val normalized = phoneUtils.normalizeNumber(this).orEmpty()
        val digits = normalized.filter { it.isDigit() }
        if (digits.isBlank()) return null
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    companion object {
        const val PREF_NAME = "blocked_numbers_preferences"
        const val LEGACY_PREF_NAME = PREF_NAME
        private const val KEY_BLOCKED_NUMBERS = "blocked_numbers"
        private const val KEY_BLOCKED_PATTERNS = "blocked_number_patterns"
    }
}
