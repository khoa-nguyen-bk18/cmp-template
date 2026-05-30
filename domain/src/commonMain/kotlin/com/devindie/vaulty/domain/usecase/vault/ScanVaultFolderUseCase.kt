package com.devindie.vaulty.domain.usecase.vault

import com.devindie.vaulty.domain.model.VaultFolderSummary
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import com.devindie.vaulty.domain.usecase.UseCaseNoParams

/**
 * Re-scans the selected folder for file count and total size (no Room index).
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultFolderRepository.scanSelectedFolder].
 *
 * @see VaultFolderRepository
 */
class ScanVaultFolderUseCase(private val repository: VaultFolderRepository) :
    UseCaseNoParams<Result<VaultFolderSummary>> {
    override suspend fun invoke(): Result<VaultFolderSummary> = repository.scanSelectedFolder()
}
