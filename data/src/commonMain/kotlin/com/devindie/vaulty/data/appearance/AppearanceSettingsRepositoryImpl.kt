package com.devindie.vaulty.data.appearance

import com.devindie.vaulty.domain.model.ThemeMode
import com.devindie.vaulty.domain.repository.AppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow

/** Implements [AppearanceSettingsRepository] via [AppearanceSettingsDataSource]. */
class AppearanceSettingsRepositoryImpl(private val dataSource: AppearanceSettingsDataSource) :
    AppearanceSettingsRepository {
    override fun observeThemeMode(): Flow<ThemeMode> = dataSource.observeThemeMode()

    override suspend fun setThemeMode(mode: ThemeMode): Result<Unit> = runCatching {
        dataSource.setThemeMode(mode)
    }
}
