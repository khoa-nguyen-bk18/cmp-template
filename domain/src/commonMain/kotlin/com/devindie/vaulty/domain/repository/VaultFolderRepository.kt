package com.devindie.vaulty.domain.repository

import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.VaultFolderSummary
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the user's chosen vault folder: persistence, observation, and lightweight scan.
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.vault.ObserveVaultFolderUseCase],
 * [com.devindie.vaulty.domain.usecase.vault.SelectVaultFolderUseCase],
 * [com.devindie.vaulty.domain.usecase.vault.ScanVaultFolderUseCase],
 * [com.devindie.vaulty.domain.usecase.vault.ClearVaultFolderUseCase].
 * **Downstream:** [com.devindie.vaulty.data.vault.VaultFolderRepositoryImpl].
 *
 * @see com.devindie.vaulty.data.vault.VaultFolderRepositoryImpl
 */
interface VaultFolderRepository {
    /** Emits the persisted selection, or `null` when none is saved. */
    fun observeSelection(): Flow<VaultFolderSelection?>

    /** Persists [selection]; [storageKey] is platform-specific (URI string on Android, bookmark on iOS). */
    suspend fun saveSelection(selection: VaultFolderSelection): Result<Unit>

    /** Removes the saved selection. */
    suspend fun clearSelection(): Result<Unit>

    /**
     * Walks the selected folder and returns file count and total size (no full-text index).
     * Fails with [Result.failure] when access is revoked or no folder is selected.
     */
    suspend fun scanSelectedFolder(): Result<VaultFolderSummary>
}
