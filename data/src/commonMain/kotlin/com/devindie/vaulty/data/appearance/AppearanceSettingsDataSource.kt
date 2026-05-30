package com.devindie.vaulty.data.appearance

import com.devindie.vaulty.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic persistence for appearance settings.
 *
 * **Implemented by:** [AppearanceSettingsDataSourceImpl].
 */
interface AppearanceSettingsDataSource {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
