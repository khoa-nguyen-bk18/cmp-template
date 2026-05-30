package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devindie.vaulty.data.vault.index.entity.VaultFileEntity
import com.devindie.vaulty.data.vault.index.entity.VaultLinkEntity

/** Room access for wiki-link graph edges and backlink queries. */
@Dao
interface VaultFileGraphDao {
    @Query(
        "DELETE FROM vault_link WHERE sourceFileId IN " +
            "(SELECT id FROM vault_file WHERE vaultStorageKey = :vaultStorageKey)",
    )
    suspend fun deleteAllLinksForVault(vaultStorageKey: String)

    @Query("DELETE FROM vault_link WHERE sourceFileId = :sourceFileId")
    suspend fun deleteLinksForSourceFile(sourceFileId: Long)

    @Query(
        """
        SELECT f.* FROM vault_file f
        INNER JOIN vault_link l ON l.sourceFileId = f.id
        WHERE l.resolvedTargetFileId = :fileId
        """,
    )
    suspend fun getBacklinkSources(fileId: Long): List<VaultFileEntity>

    @Query("SELECT * FROM vault_link WHERE sourceFileId = :fileId")
    suspend fun getOutgoingLinks(fileId: Long): List<VaultLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<VaultLinkEntity>)
}
