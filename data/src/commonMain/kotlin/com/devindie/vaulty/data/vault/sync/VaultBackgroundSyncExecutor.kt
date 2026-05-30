package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSource
import com.devindie.vaulty.data.vault.index.VaultFileContentDataSource
import com.devindie.vaulty.data.vault.index.VaultFileEntry
import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.index.VaultIndexStatus
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import kotlinx.coroutines.flow.first

/** Outcome of one background sync attempt (WorkManager / BG refresh). */
sealed interface VaultBackgroundSyncResult {
    data object Indexed : VaultBackgroundSyncResult

    data class Skipped(val reason: Reason) : VaultBackgroundSyncResult {
        enum class Reason {
            SyncDisabled,
            NoVaultSelected,
            IndexNotReady,
            IndexInProgress,
            NoMetadataChanges,
        }
    }

    data class Failed(val error: Throwable) : VaultBackgroundSyncResult
}

/**
 * Shared background sync pipeline: gates, metadata diff, incremental index.
 *
 * Used by [VaultSyncWorker] and iOS BG refresh handler.
 */
class VaultBackgroundSyncExecutor(
    private val syncSettings: VaultSyncSettingsRepository,
    private val folderPersistence: VaultFolderPersistenceDataSource,
    private val indexRepository: VaultIndexRepository,
    private val fileContent: VaultFileContentDataSource,
    private val snapshotStore: VaultFolderSnapshotStore,
) {
    suspend fun run(): VaultBackgroundSyncResult {
        val skipReason = evaluateSkipReason()
        return if (skipReason != null) {
            VaultBackgroundSyncResult.Skipped(skipReason)
        } else {
            executeBackgroundSync()
        }
    }

    private suspend fun executeBackgroundSync(): VaultBackgroundSyncResult {
        val selection = folderPersistence.currentSelection()!!
        return fileContent.collectFiles(selection.storageKey).fold(
            onFailure = { VaultBackgroundSyncResult.Failed(it) },
            onSuccess = { entries -> syncWithMetadata(selection, entries) },
        )
    }

    private suspend fun syncWithMetadata(
        selection: VaultFolderSelection,
        entries: List<VaultFileEntry>,
    ): VaultBackgroundSyncResult {
        val current = VaultFolderMetadataSnapshot.fromEntries(entries)
        val previous = snapshotStore.load(selection.storageKey)

        if (!current.hasChangesFrom(previous)) {
            if (previous == null) {
                snapshotStore.save(selection.storageKey, current)
            }
            return VaultBackgroundSyncResult.Skipped(
                VaultBackgroundSyncResult.Skipped.Reason.NoMetadataChanges,
            )
        }

        snapshotStore.save(selection.storageKey, current)
        return indexRepository.indexSelectedVault(fullRebuild = false).fold(
            onSuccess = { VaultBackgroundSyncResult.Indexed },
            onFailure = { VaultBackgroundSyncResult.Failed(it) },
        )
    }

    private suspend fun evaluateSkipReason(): VaultBackgroundSyncResult.Skipped.Reason? {
        if (!syncSettings.observeEnabled().first()) {
            return VaultBackgroundSyncResult.Skipped.Reason.SyncDisabled
        }
        val selection = folderPersistence.currentSelection()
        return when (selection) {
            null -> VaultBackgroundSyncResult.Skipped.Reason.NoVaultSelected
            else -> evaluateIndexSkipReason(selection)
        }
    }

    private suspend fun evaluateIndexSkipReason(
        selection: VaultFolderSelection,
    ): VaultBackgroundSyncResult.Skipped.Reason? = when {
        indexRepository.observeIndexStatus(selection.storageKey).first() != VaultIndexStatus.Ready ->
            VaultBackgroundSyncResult.Skipped.Reason.IndexNotReady
        indexRepository.observeIndexProgress().first() != null ->
            VaultBackgroundSyncResult.Skipped.Reason.IndexInProgress
        else -> null
    }
}
