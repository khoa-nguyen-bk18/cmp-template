package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSource
import com.devindie.vaulty.domain.model.index.VaultDashboardStats
import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher
import com.devindie.vaulty.domain.model.index.VaultIndexProgress
import com.devindie.vaulty.domain.model.index.VaultIndexStatus
import com.devindie.vaulty.domain.model.index.VaultLink
import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import com.devindie.vaulty.domain.repository.VaultIndexIgnoreRepository
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implements [com.devindie.vaulty.domain.repository.VaultIndexRepository] with Room and [VaultFolderIndexer].
 *
 * **Upstream:** Index/search use cases.
 * **Downstream:** [VaultDatabase], [VaultFolderPersistenceDataSource], [VaultFileContentDataSource].
 * Index runs are serialized with [indexMutex].
 *
 * @see com.devindie.vaulty.domain.repository.VaultIndexRepository
 * @see VaultFolderIndexer
 */
class VaultIndexRepositoryImpl(
    private val database: VaultDatabase,
    private val persistence: VaultFolderPersistenceDataSource,
    private val fileContent: VaultFileContentDataSource,
    private val ignoreRules: VaultIndexIgnoreRepository,
    private val dispatchers: DispatcherProvider,
) : VaultIndexRepository {
    private companion object {
        const val NO_FOLDER_SELECTED = "No folder selected"
    }

    private val progressState = MutableStateFlow<VaultIndexProgress?>(null)
    private val indexMutex = Mutex()
    private val searchCoordinator = VaultIndexSearchCoordinator()
    private val indexer by lazy {
        VaultFolderIndexer(
            database = database,
            fileContent = fileContent,
            dispatchers = dispatchers,
            progressState = progressState,
        )
    }

    override fun observeIndexStatus(storageKey: String): Flow<VaultIndexStatus> =
        database.vaultIndexDao().observeIndex(storageKey).map { entity ->
            entity?.status?.toIndexStatus() ?: VaultIndexStatus.NotIndexed
        }

    override fun observeIndexProgress(): Flow<VaultIndexProgress?> = progressState

    override suspend fun indexSelectedVault(fullRebuild: Boolean): Result<Unit> = indexMutex.withLock {
        val selection =
            persistence.currentSelection()
                ?: return Result.failure(IllegalStateException(NO_FOLDER_SELECTED))
        val rulesText = ignoreRules.getRules(selection.storageKey)
        val ignoreMatcher = VaultIndexIgnoreMatcher.parse(rulesText)
        indexer.index(
            selection = selection,
            fullRebuild = fullRebuild,
            ignoreMatcher = ignoreMatcher,
        )
    }

    override suspend fun search(query: VaultSearchQuery): Result<List<VaultSearchResult>> = runCatching {
        val selection =
            persistence.currentSelection()
                ?: error(NO_FOLDER_SELECTED)
        searchCoordinator.searchVault(
            storageKey = selection.storageKey,
            query = query,
            searchDao = database.vaultFileSearchDao(),
        )
    }

    override suspend fun getDashboardStats(): Result<VaultDashboardStats> = runCatching {
        val selection =
            persistence.currentSelection()
                ?: error(NO_FOLDER_SELECTED)
        val key = selection.storageKey
        val statsDao = database.vaultFileStatsDao()
        val index = database.vaultIndexDao().getIndex(key)
        buildDashboardStats(
            DashboardStatsSource(
                lastIndexedAt = index?.indexedAtEpochMs,
                extensionRows = statsDao.extensionBreakdown(key),
                tagRows = statsDao.topTags(key),
                totalFiles = statsDao.countFiles(key),
                markdownFiles = statsDao.countMarkdownFiles(key),
                totalSize = statsDao.totalSize(key),
                brokenLinks = statsDao.countBrokenLinks(key),
                orphanNotes = statsDao.countOrphanNotes(key),
            ),
        )
    }

    override suspend fun getBacklinks(fileId: Long): Result<List<VaultFileRef>> = runCatching {
        database.vaultFileGraphDao().getBacklinkSources(fileId).map { it.toRef() }
    }

    override suspend fun getOutgoingLinks(fileId: Long): Result<List<VaultLink>> = runCatching {
        database.vaultFileGraphDao().getOutgoingLinks(fileId).map { it.toDomain() }
    }
}
