package com.devindie.vaulty.domain.usecase.vault

import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the persisted vault folder selection.
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultFolderRepository.observeSelection].
 *
 * @see VaultFolderRepository
 */
class ObserveVaultFolderUseCase(private val repository: VaultFolderRepository) {
    operator fun invoke(): Flow<VaultFolderSelection?> = repository.observeSelection()
}
