package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Query
import com.devindie.vaulty.data.vault.index.entity.VaultFileEntity

/** Read-only Room queries for indexed vault files. */
@Dao
interface VaultFileQueryDao {
    @Query(
        """
        SELECT id, relativePath, sizeBytes, modifiedAtEpochMs
        FROM vault_file
        WHERE vaultStorageKey = :vaultStorageKey
        """,
    )
    suspend fun getFileSnapshots(vaultStorageKey: String): List<VaultFileSnapshotRow>

    @Query(
        """
        SELECT id, relativePath, isMarkdown, contentBody
        FROM vault_file
        WHERE vaultStorageKey = :vaultStorageKey
        """,
    )
    suspend fun getLinkSourceRows(vaultStorageKey: String): List<VaultFileLinkSourceRow>

    @Query("SELECT * FROM vault_file WHERE vaultStorageKey = :vaultStorageKey")
    suspend fun getAllFiles(vaultStorageKey: String): List<VaultFileEntity>

    @Query(
        "SELECT * FROM vault_file WHERE vaultStorageKey = :vaultStorageKey AND relativePath = :relativePath LIMIT 1",
    )
    suspend fun getFileByPath(vaultStorageKey: String, relativePath: String): VaultFileEntity?

    @Query("SELECT * FROM vault_file WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: Long): VaultFileEntity?
}
