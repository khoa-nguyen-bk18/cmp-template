package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultIndexStatus
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes index lifecycle state for a vault [storageKey].
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultIndexRepository.observeIndexStatus].
 *
 * @see VaultIndexRepository
 */
class ObserveVaultIndexStatusUseCase(private val repository: VaultIndexRepository) {
    operator fun invoke(storageKey: String): Flow<VaultIndexStatus> = repository.observeIndexStatus(storageKey)
}
