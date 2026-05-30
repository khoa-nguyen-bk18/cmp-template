package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.index.VaultFileContentDataSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/** Polls security-scoped vault metadata and emits when the snapshot changes. */
class IosVaultFolderWatcherDataSource(
    private val fileContent: VaultFileContentDataSource,
    private val dispatchers: DispatcherProvider,
) : VaultFolderWatcherDataSource {
    override fun observeChanges(storageKey: String): Flow<Unit> = flow {
        var previous: VaultFolderMetadataSnapshot? = null
        while (currentCoroutineContext().isActive) {
            val snapshot =
                withContext(dispatchers.io) {
                    val entries =
                        fileContent.collectFiles(storageKey).getOrElse { emptyList() }
                    VaultFolderMetadataSnapshot.fromEntries(entries)
                }
            if (snapshot.hasChangesFrom(previous)) {
                emit(Unit)
            }
            previous = snapshot
            delay(POLL_INTERVAL_MS)
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
    }
}
