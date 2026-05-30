package com.devindie.vaulty.data.vault

import com.devindie.vaulty.domain.model.VaultFolderSummary

/**
 * Platform port to walk the selected vault folder and compute file count and total size.
 *
 * **Implemented by:** [com.devindie.vaulty.data.vault.AndroidVaultFolderScannerDataSource],
 * [com.devindie.vaulty.data.vault.IosVaultFolderScannerDataSource].
 */
fun interface VaultFolderScannerDataSource {
    suspend fun scan(storageKey: String, folderDisplayName: String): Result<VaultFolderSummary>
}
