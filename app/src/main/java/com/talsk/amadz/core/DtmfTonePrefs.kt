package com.talsk.amadz.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import com.talsk.amadz.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.dtmfToneDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DtmfTonePrefs.PREFS_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, DtmfTonePrefs.PREFS_NAME))
    }
)

@Singleton
class DtmfTonePrefs @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @ApplicationScope applicationScope: CoroutineScope
) {
    private val enabledKey = booleanPreferencesKey(KEY_DTMF_TONE_ENABLED)

    val isEnabled: StateFlow<Boolean> = appContext.dtmfToneDataStore.data
        .map { preferences -> preferences[enabledKey] ?: true }
        .catch { exception ->
            if (exception is IOException) {
                emit(true)
            } else {
                throw exception
            }
        }
        .stateIn(applicationScope, SharingStarted.Eagerly, true)

    suspend fun setEnabled(enabled: Boolean) {
        appContext.dtmfToneDataStore.edit { preferences ->
            preferences[enabledKey] = enabled
        }
    }

    companion object {
        const val PREFS_NAME = "amadz_prefs"
        const val KEY_DTMF_TONE_ENABLED = "dtmf_tone_enabled"
    }
}
