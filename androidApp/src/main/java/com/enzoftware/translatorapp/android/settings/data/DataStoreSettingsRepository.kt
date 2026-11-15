package com.enzoftware.translatorapp.android.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.enzoftware.translatorapp.android.settings.domain.AppTheme
import com.enzoftware.translatorapp.android.settings.domain.SettingsRepository
import com.enzoftware.translatorapp.android.settings.domain.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            UserSettings(
                userName = prefs[Keys.userName].orEmpty(),
                theme = AppTheme.fromStorage(prefs[Keys.theme]),
            )
        }
        .distinctUntilChanged()

    override suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.userName] = name }
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[Keys.theme] = theme.name.lowercase() }
    }

    private object Keys {
        // Same key names as the iOS SettingsStore.
        val userName = stringPreferencesKey("cozy.settings.userName")
        val theme = stringPreferencesKey("cozy.settings.theme")
    }
}
