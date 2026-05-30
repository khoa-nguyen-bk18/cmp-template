package com.devindie.vaulty.domain.usecase.vault

import com.devindie.vaulty.domain.repository.VaultFolderRepository
import com.devindie.vaulty.domain.usecase.UseCaseNoParams

/**
 * Clears the persisted vault folder selection.
 *
 * **Flow:** Settings or dashboard actions → this → [VaultFolderRepository.clearSelection].
 *
 * @see VaultFolderRepository
 */
class ClearVaultFolderUseCase(private val repository: VaultFolderRepository) : UseCaseNoParams<Result<Unit>> {
    override suspend fun invoke(): Result<Unit> = repository.clearSelection()
}
