package com.devindie.vaulty.data.vault.index.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.devindie.vaulty.data.vault.index.entity.VaultIndexEntity
import com.devindie.vaulty.data.vault.index.entity.VaultIndexRunEntity
import kotlinx.coroutines.flow.Flow

/** Room access for per-vault index status and indexing run history. */
@Dao
interface VaultIndexDao {
    /** Uses SQLite UPSERT (not REPLACE) so updating index metadata does not CASCADE-delete vault files. */
    @Upsert
    suspend fun upsertIndex(index: VaultIndexEntity)

    @Update
    suspend fun updateIndex(index: VaultIndexEntity)

    @Query("SELECT * FROM vault_index WHERE storageKey = :storageKey LIMIT 1")
    suspend fun getIndex(storageKey: String): VaultIndexEntity?

    @Query("SELECT * FROM vault_index WHERE storageKey = :storageKey LIMIT 1")
    fun observeIndex(storageKey: String): Flow<VaultIndexEntity?>

    @Insert
    suspend fun insertRun(run: VaultIndexRunEntity): Long

    @Query(
        """
        UPDATE vault_index_run
        SET finishedAtEpochMs = :finishedAt, filesProcessed = :filesProcessed, errorsCount = :errorsCount
        WHERE id = :runId
        """,
    )
    suspend fun finishRun(runId: Long, finishedAt: Long, filesProcessed: Int, errorsCount: Int)
}
