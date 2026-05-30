package com.devindie.vaulty.domain.usecase.vault

import com.devindie.vaulty.domain.gateway.VaultFolderPickerGateway
import com.devindie.vaulty.domain.model.VaultFolderSummary
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import com.devindie.vaulty.domain.usecase.UseCaseNoParams

/**
 * Opens the folder picker, persists the choice, and runs an initial folder scan.
 *
 * **Flow:** [com.devindie.vaulty.screens.list.DashboardViewModel] → this →
 * [VaultFolderPickerGateway] + [VaultFolderRepository].
 * **Failure:** [FolderPickCancelledException] when the user dismisses the picker.
 *
 * @see VaultFolderPickerGateway
 * @see VaultFolderRepository
 */
class SelectVaultFolderUseCase(
    private val picker: VaultFolderPickerGateway,
    private val repository: VaultFolderRepository,
) : UseCaseNoParams<Result<VaultFolderSummary>> {
    override suspend fun invoke(): Result<VaultFolderSummary> {
        val selection = picker.pickFolder() ?: return Result.failure(FolderPickCancelledException())
        return repository.saveSelection(selection).fold(
            onSuccess = { repository.scanSelectedFolder() },
            onFailure = { Result.failure(it) },
        )
    }
}

/** Thrown when the platform folder picker is cancelled. */
class FolderPickCancelledException : Exception("Folder pick cancelled")
