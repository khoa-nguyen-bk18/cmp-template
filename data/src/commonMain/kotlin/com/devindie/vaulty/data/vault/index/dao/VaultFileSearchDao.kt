package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Query

/** Room FTS and structured filter search over indexed vault files. */
@Dao
interface VaultFileSearchDao {
    @Query(
        """
        SELECT f.id, f.relativePath, f.name, f.extension,
               f.modifiedAtEpochMs, f.mimeCategory, f.sizeBytes,
               snippet(vault_file_fts, -1, '', '', '…', 24) AS snippet,
               substr(f.contentBody, 1, 8192) AS contentBodyExcerpt,
               0.0 AS rankScore
        FROM vault_file_fts
        INNER JOIN vault_file f ON f.rowid = vault_file_fts.rowid
        WHERE vault_file_fts MATCH :matchQuery
        AND f.vaultStorageKey = :vaultStorageKey
        AND (:onlyMarkdown = 0 OR f.isMarkdown = 1)
        ORDER BY f.modifiedAtEpochMs DESC
        LIMIT :limit
        """,
    )
    suspend fun searchFts(
        vaultStorageKey: String,
        matchQuery: String,
        onlyMarkdown: Boolean,
        limit: Int,
    ): List<SearchResultRow>

    @Query(
        """
        SELECT f.id, f.relativePath, f.name, f.extension,
               f.modifiedAtEpochMs, f.mimeCategory, f.sizeBytes,
               '' AS snippet,
               substr(f.contentBody, 1, 8192) AS contentBodyExcerpt,
               0.0 AS rankScore
        FROM vault_file f
        WHERE f.vaultStorageKey = :vaultStorageKey
        AND (:onlyMarkdown = 0 OR f.isMarkdown = 1)
        AND (:modifiedFromMs < 0 OR f.modifiedAtEpochMs >= :modifiedFromMs)
        AND (:modifiedToMs < 0 OR f.modifiedAtEpochMs < :modifiedToMs)
        AND (:indexedFromMs < 0 OR f.indexedAtEpochMs >= :indexedFromMs)
        AND (:indexedToMs < 0 OR f.indexedAtEpochMs < :indexedToMs)
        AND (:orphansOnly = 0 OR (
            f.isMarkdown = 1 AND NOT EXISTS (
                SELECT 1 FROM vault_link l WHERE l.resolvedTargetFileId = f.id
            )
        ))
        AND (:minBacklinks < 0 OR (
            (SELECT COUNT(*) FROM vault_link l WHERE l.resolvedTargetFileId = f.id) >= :minBacklinks
        ))
        AND (:maxBacklinks < 0 OR (
            (SELECT COUNT(*) FROM vault_link l WHERE l.resolvedTargetFileId = f.id) <= :maxBacklinks
        ))
        AND (:requireAttachment = 0 OR f.mimeCategory IN ('image', 'audio', 'video') OR f.extension IN ('pdf', 'png', 'jpg', 'jpeg', 'gif', 'webp', 'mp3', 'wav', 'm4a'))
        AND (
            :tagCount = 0 OR (
                SELECT COUNT(DISTINCT p.value) FROM vault_file_property p
                WHERE p.fileId = f.id AND p.namespace = 'tag' AND p.value IN (:tagNames)
            ) = :tagCount
        )
        ORDER BY f.modifiedAtEpochMs DESC
        LIMIT :limit
        """,
    )
    @Suppress("kotlin:S107") // Room requires one @Query bind parameter per SQL placeholder
    suspend fun searchFiltered(
        vaultStorageKey: String,
        onlyMarkdown: Boolean,
        modifiedFromMs: Long,
        modifiedToMs: Long,
        indexedFromMs: Long,
        indexedToMs: Long,
        orphansOnly: Boolean,
        minBacklinks: Int,
        maxBacklinks: Int,
        requireAttachment: Boolean,
        tagNames: List<String>,
        tagCount: Int,
        limit: Int,
    ): List<SearchResultRow>

    @Query(
        """
        SELECT f.id FROM vault_file f
        WHERE f.vaultStorageKey = :vaultStorageKey
        AND (
            SELECT COUNT(DISTINCT p.value) FROM vault_file_property p
            WHERE p.fileId = f.id AND p.namespace = 'tag' AND p.value IN (:tagNames)
        ) = :requiredTagCount
        """,
    )
    suspend fun getFileIdsMatchingAllTags(
        vaultStorageKey: String,
        tagNames: List<String>,
        requiredTagCount: Int,
    ): List<Long>
}
