package com.devindie.vaulty.domain.usecase.appearance

import com.devindie.vaulty.domain.model.ThemeMode
import com.devindie.vaulty.domain.repository.AppearanceSettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the user's theme mode preference (default [ThemeMode.System]).
 *
 * **Flow:** [com.devindie.vaulty.App] → this → [AppearanceSettingsRepository].
 */
class ObserveThemeModeUseCase(private val repository: AppearanceSettingsRepository) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
