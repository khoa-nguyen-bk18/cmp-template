package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.devindie.vaulty.data.vault.index.entity.VaultFileContentEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFileEntity
import com.devindie.vaulty.data.vault.index.entity.VaultFilePropertyEntity

/** Multi-table writes for a single indexed file row and stale-file cleanup. */
@Dao
interface VaultIndexedFileTransactionDao {
    @Query("DELETE FROM vault_file WHERE id = :fileId")
    suspend fun deleteFileById(fileId: Long)

    @Query("DELETE FROM vault_file_property WHERE fileId = :fileId")
    suspend fun deletePropertiesForFile(fileId: Long)

    @Query("DELETE FROM vault_link WHERE sourceFileId = :sourceFileId")
    suspend fun deleteLinksForSourceFile(sourceFileId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: VaultFileContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<VaultFilePropertyEntity>)

    @Transaction
    suspend fun upsertIndexedFile(
        existingFileId: Long?,
        file: VaultFileEntity,
        content: VaultFileContentEntity,
        properties: List<VaultFilePropertyEntity>,
    ): Long {
        existingFileId?.let { fileId ->
            deleteLinksForSourceFile(fileId)
            deletePropertiesForFile(fileId)
        }
        val fileId = insertFile(file)
        insertContent(content.copy(fileId = fileId))
        if (properties.isNotEmpty()) {
            insertProperties(properties.map { it.copy(fileId = fileId) })
        }
        return fileId
    }

    @Transaction
    suspend fun deleteFilesByIds(fileIds: List<Long>) {
        for (fileId in fileIds) {
            deleteFileById(fileId)
        }
    }
}
