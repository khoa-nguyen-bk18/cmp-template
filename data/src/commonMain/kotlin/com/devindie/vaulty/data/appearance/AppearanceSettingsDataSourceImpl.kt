package com.devindie.vaulty.data.appearance

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.devindie.vaulty.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/** Persists theme mode in the shared Preferences [DataStore]. */
class AppearanceSettingsDataSourceImpl(private val dataStore: DataStore<Preferences>) : AppearanceSettingsDataSource {
    private val themeModeState = MutableStateFlow(runBlocking { loadThemeMode() })

    override fun observeThemeMode(): Flow<ThemeMode> = themeModeState.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
        themeModeState.value = mode
    }

    private suspend fun loadThemeMode(): ThemeMode {
        val stored = dataStore.data.first()[KEY_THEME_MODE] ?: return ThemeMode.System
        return ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.System
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
