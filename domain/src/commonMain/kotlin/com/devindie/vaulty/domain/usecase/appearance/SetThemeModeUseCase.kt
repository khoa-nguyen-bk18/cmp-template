package com.devindie.vaulty.domain.usecase.appearance

import com.devindie.vaulty.domain.model.ThemeMode
import com.devindie.vaulty.domain.repository.AppearanceSettingsRepository

/**
 * Persists the user's theme mode preference.
 *
 * **Flow:** [com.devindie.vaulty.screens.settings.SettingsViewModel] → this → [AppearanceSettingsRepository].
 */
class SetThemeModeUseCase(private val repository: AppearanceSettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode): Result<Unit> = repository.setThemeMode(mode)
}
