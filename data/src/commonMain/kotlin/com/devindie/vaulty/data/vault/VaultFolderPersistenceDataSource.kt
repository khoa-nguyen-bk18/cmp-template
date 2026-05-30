package com.devindie.vaulty.data.vault

import com.devindie.vaulty.domain.model.VaultFolderSelection
import kotlinx.coroutines.flow.Flow

/**
 * Persists and observes the user's vault folder [com.devindie.vaulty.domain.model.VaultFolderSelection].
 *
 * **Implemented by:** [VaultFolderPersistenceDataSourceImpl] (Preferences DataStore).
 */
interface VaultFolderPersistenceDataSource {
    fun observeSelection(): Flow<VaultFolderSelection?>

    suspend fun save(selection: VaultFolderSelection)

    suspend fun clear()

    fun currentSelection(): VaultFolderSelection?
}
