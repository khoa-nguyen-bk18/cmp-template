package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Query

/** Room aggregates for dashboard and vault health stats. */
@Dao
interface VaultFileStatsDao {
    @Query(
        """
        SELECT COUNT(*) FROM vault_link
        WHERE sourceFileId IN (SELECT id FROM vault_file WHERE vaultStorageKey = :vaultStorageKey)
        AND resolvedTargetFileId IS NULL
        """,
    )
    suspend fun countBrokenLinks(vaultStorageKey: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM vault_file f
        WHERE f.vaultStorageKey = :vaultStorageKey
        AND f.isMarkdown = 1
        AND NOT EXISTS (
            SELECT 1 FROM vault_link l WHERE l.resolvedTargetFileId = f.id
        )
        """,
    )
    suspend fun countOrphanNotes(vaultStorageKey: String): Int

    @Query(
        """
        SELECT extension AS extension, COUNT(*) AS count
        FROM vault_file
        WHERE vaultStorageKey = :vaultStorageKey
        GROUP BY extension
        ORDER BY count DESC
        """,
    )
    suspend fun extensionBreakdown(vaultStorageKey: String): List<ExtensionCountRow>

    @Query(
        """
        SELECT value AS tag, COUNT(*) AS count
        FROM vault_file_property
        WHERE fileId IN (SELECT id FROM vault_file WHERE vaultStorageKey = :vaultStorageKey)
        AND namespace = 'tag'
        GROUP BY value
        ORDER BY count DESC
        LIMIT 10
        """,
    )
    suspend fun topTags(vaultStorageKey: String): List<TagCountRow>

    @Query("SELECT COUNT(*) FROM vault_file WHERE vaultStorageKey = :vaultStorageKey")
    suspend fun countFiles(vaultStorageKey: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM vault_file
        WHERE vaultStorageKey = :vaultStorageKey AND isMarkdown = 1
        """,
    )
    suspend fun countMarkdownFiles(vaultStorageKey: String): Int

    @Query(
        """
        SELECT COALESCE(SUM(sizeBytes), 0) FROM vault_file
        WHERE vaultStorageKey = :vaultStorageKey
        """,
    )
    suspend fun totalSize(vaultStorageKey: String): Long
}
