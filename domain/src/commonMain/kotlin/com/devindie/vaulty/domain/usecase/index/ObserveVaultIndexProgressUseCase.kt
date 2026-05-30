package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultIndexProgress
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes in-flight indexing progress (`null` when idle).
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultIndexRepository.observeIndexProgress].
 *
 * @see VaultIndexRepository
 */
class ObserveVaultIndexProgressUseCase(private val repository: VaultIndexRepository) {
    operator fun invoke(): Flow<VaultIndexProgress?> = repository.observeIndexProgress()
}
