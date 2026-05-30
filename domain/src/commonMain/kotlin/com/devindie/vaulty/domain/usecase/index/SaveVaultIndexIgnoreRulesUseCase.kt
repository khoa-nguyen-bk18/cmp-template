package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.SaveVaultIndexIgnoreRulesParams
import com.devindie.vaulty.domain.repository.VaultIndexIgnoreRepository
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Persists exclude rules for a vault.
 *
 * **Flow:** [com.devindie.vaulty.screens.settings.SettingsViewModel] → this →
 * [VaultIndexIgnoreRepository.saveRules].
 */
class SaveVaultIndexIgnoreRulesUseCase(private val repository: VaultIndexIgnoreRepository) :
    UseCase<SaveVaultIndexIgnoreRulesParams, Result<Unit>> {
    override suspend fun invoke(parameters: SaveVaultIndexIgnoreRulesParams): Result<Unit> = repository.saveRules(
        storageKey = parameters.storageKey,
        rulesText = parameters.rulesText,
    )
}
