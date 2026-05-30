package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.index.dao.VaultFileGraphDao
import com.devindie.vaulty.data.vault.index.dao.VaultFileQueryDao
import com.devindie.vaulty.data.vault.index.entity.VaultLinkEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Resolves wiki-link edges after all vault files are indexed. */
internal class VaultLinkIndexPass(
    private val fileQueryDao: VaultFileQueryDao,
    private val fileGraphDao: VaultFileGraphDao,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun resolve(vaultStorageKey: String, pendingLinks: List<PendingVaultLink>) {
        fileGraphDao.deleteAllLinksForVault(vaultStorageKey)

        val pathToId =
            fileQueryDao.getFileSnapshots(vaultStorageKey).associate { row ->
                row.relativePath to row.id
            }

        val linkEntities =
            if (pendingLinks.isNotEmpty()) {
                linkEntitiesFromPending(pendingLinks, pathToId)
            } else {
                resolveLinksFromDatabaseRows(vaultStorageKey, pathToId)
            }

        if (linkEntities.isNotEmpty()) {
            fileGraphDao.insertLinks(linkEntities)
        }
    }

    private suspend fun resolveLinksFromDatabaseRows(
        vaultStorageKey: String,
        pathToId: Map<String, Long>,
    ): List<VaultLinkEntity> {
        val rows = fileQueryDao.getLinkSourceRows(vaultStorageKey)
        return coroutineScope {
            rows
                .map { row ->
                    async {
                        withContext(dispatchers.default) {
                            ensureActive()
                            linkEntitiesForSourceRow(row, pathToId)
                        }
                    }
                }.awaitAll()
                .flatten()
        }
    }
}
