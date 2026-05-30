package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultDashboardStats
import com.devindie.vaulty.domain.repository.VaultIndexRepository

/**
 * Loads aggregated dashboard statistics from the index.
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultIndexRepository.getDashboardStats].
 *
 * @see VaultIndexRepository
 */
class ObserveVaultDashboardStatsUseCase(private val repository: VaultIndexRepository) {
    suspend operator fun invoke(): Result<VaultDashboardStats> = repository.getDashboardStats()
}
