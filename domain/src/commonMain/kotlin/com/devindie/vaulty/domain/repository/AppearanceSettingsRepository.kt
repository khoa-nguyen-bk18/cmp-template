package com.devindie.vaulty.domain.repository

import com.devindie.vaulty.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Persists appearance preferences (theme mode).
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.appearance.ObserveThemeModeUseCase],
 * [com.devindie.vaulty.domain.usecase.appearance.SetThemeModeUseCase].
 * **Downstream:** [com.devindie.vaulty.data.appearance.AppearanceSettingsRepositoryImpl].
 */
interface AppearanceSettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode): Result<Unit>
}
