package com.devindie.vaulty.data.vault

import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.VaultFolderSummary
import com.devindie.vaulty.domain.repository.VaultFolderRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implements [com.devindie.vaulty.domain.repository.VaultFolderRepository] using platform persistence and scan.
 *
 * **Upstream:** Vault folder use cases.
 * **Downstream:** [VaultFolderPersistenceDataSource], [VaultFolderScannerDataSource].
 *
 * @see com.devindie.vaulty.domain.repository.VaultFolderRepository
 */
class VaultFolderRepositoryImpl(
    private val persistence: VaultFolderPersistenceDataSource,
    private val scanner: VaultFolderScannerDataSource,
) : VaultFolderRepository {
    override fun observeSelection(): Flow<VaultFolderSelection?> = persistence.observeSelection()

    override suspend fun saveSelection(selection: VaultFolderSelection): Result<Unit> = runCatching {
        persistence.save(selection)
    }

    override suspend fun clearSelection(): Result<Unit> = runCatching {
        persistence.clear()
    }

    override suspend fun scanSelectedFolder(): Result<VaultFolderSummary> {
        val selection =
            persistence.currentSelection()
                ?: return Result.failure(IllegalStateException("No folder selected"))
        return scanner.scan(selection.storageKey, selection.displayName)
    }
}
