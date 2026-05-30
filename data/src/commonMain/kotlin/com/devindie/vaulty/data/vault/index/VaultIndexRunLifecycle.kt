package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.vault.index.dao.VaultFileQueryDao
import com.devindie.vaulty.data.vault.index.dao.VaultFileWriteDao
import com.devindie.vaulty.data.vault.index.dao.VaultIndexDao
import com.devindie.vaulty.data.vault.index.dao.toIndexSnapshot
import com.devindie.vaulty.data.vault.index.entity.VaultIndexEntity
import com.devindie.vaulty.data.vault.index.entity.VaultIndexRunEntity
import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.index.VaultFileIndexSnapshot
import com.devindie.vaulty.domain.model.index.VaultIndexPhase
import com.devindie.vaulty.domain.model.index.VaultIndexProgress
import kotlinx.coroutines.flow.MutableStateFlow

/** Index metadata, run rows, and snapshot loading for [VaultFolderIndexer]. */
internal class VaultIndexRunLifecycle(
    private val indexDao: VaultIndexDao,
    private val fileQueryDao: VaultFileQueryDao,
    private val fileWriteDao: VaultFileWriteDao,
    private val progressState: MutableStateFlow<VaultIndexProgress?>,
) {
    suspend fun markIndexStarted(selection: VaultFolderSelection, startedAt: Long) {
        progressState.value =
            VaultIndexProgress(
                phase = VaultIndexPhase.Collecting,
                processed = 0,
                total = 0,
                currentPath = "",
                startedAtEpochMs = startedAt,
            )
        indexDao.upsertIndex(
            VaultIndexEntity(
                storageKey = selection.storageKey,
                displayName = selection.displayName,
                indexedAtEpochMs = startedAt,
                schemaVersion = VAULT_DATABASE_SCHEMA_VERSION,
                status = "indexing",
            ),
        )
    }

    suspend fun beginIndexRun(selection: VaultFolderSelection, startedAt: Long, fullRebuild: Boolean): Long =
        indexDao.insertRun(
            VaultIndexRunEntity(
                vaultStorageKey = selection.storageKey,
                startedAtEpochMs = startedAt,
                finishedAtEpochMs = null,
                filesProcessed = 0,
                errorsCount = 0,
                trigger = if (fullRebuild) "full" else "incremental",
            ),
        )

    suspend fun prepareVaultForIndexing(storageKey: String, fullRebuild: Boolean) {
        if (fullRebuild) {
            fileWriteDao.clearVault(storageKey)
        }
    }

    suspend fun loadExistingSnapshots(storageKey: String, fullRebuild: Boolean): Map<String, VaultFileIndexSnapshot> =
        if (fullRebuild) {
            emptyMap()
        } else {
            fileQueryDao.getFileSnapshots(storageKey).associate { row ->
                row.relativePath to row.toIndexSnapshot()
            }
        }

    suspend fun finalizeIndexRun(selection: VaultFolderSelection, runId: Long, fileStats: IndexFileStats) {
        val finishedAt = currentEpochMillis()
        indexDao.finishRun(
            runId = runId,
            finishedAt = finishedAt,
            filesProcessed = fileStats.processed - fileStats.skipped,
            errorsCount = fileStats.errors,
        )
        indexDao.upsertIndex(
            VaultIndexEntity(
                storageKey = selection.storageKey,
                displayName = selection.displayName,
                indexedAtEpochMs = finishedAt,
                schemaVersion = VAULT_DATABASE_SCHEMA_VERSION,
                status = "ready",
            ),
        )
        progressState.value = null
    }
}

internal data class IndexFileStats(val scannedPaths: Set<String>, val processed: Int, val skipped: Int, val errors: Int)
