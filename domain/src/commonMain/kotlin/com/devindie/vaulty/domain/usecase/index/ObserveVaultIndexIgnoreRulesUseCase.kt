package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.repository.VaultIndexIgnoreRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes exclude rules text for a vault [storageKey].
 *
 * **Flow:** [com.devindie.vaulty.screens.settings.SettingsViewModel] → this →
 * [VaultIndexIgnoreRepository.observeRules].
 */
class ObserveVaultIndexIgnoreRulesUseCase(private val repository: VaultIndexIgnoreRepository) {
    operator fun invoke(storageKey: String): Flow<String> = repository.observeRules(storageKey)
}
