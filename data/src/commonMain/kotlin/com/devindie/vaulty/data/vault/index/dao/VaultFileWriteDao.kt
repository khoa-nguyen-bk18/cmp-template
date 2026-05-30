package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/** Vault-scoped file row deletes used before full rebuilds. */
@Dao
interface VaultFileWriteDao {
    @Query("DELETE FROM vault_file WHERE vaultStorageKey = :vaultStorageKey")
    suspend fun deleteAllFilesForVault(vaultStorageKey: String)

    @Transaction
    suspend fun clearVault(vaultStorageKey: String) {
        deleteAllFilesForVault(vaultStorageKey)
    }
}
