package com.devindie.vaulty.data.vault.sync

import com.devindie.vaulty.data.vault.VaultFolderPersistenceDataSource
import com.devindie.vaulty.data.vault.index.VaultFileContentDataSource
import com.devindie.vaulty.data.vault.index.VaultFileEntry
import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.index.VaultFileRef
import com.devindie.vaulty.domain.model.index.VaultIndexStatus
import com.devindie.vaulty.domain.model.index.VaultLink
import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.repository.VaultSyncSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VaultBackgroundSyncExecutorTest {
    @Test
    fun run_skipsWhenSyncDisabled() = runTest {
        val executor = createExecutor(syncEnabled = false)

        val result = executor.run()

        assertIs<VaultBackgroundSyncResult.Skipped>(result)
        assertEquals(
            VaultBackgroundSyncResult.Skipped.Reason.SyncDisabled,
            result.reason,
        )
    }

    @Test
    fun run_indexesWhenMetadataChanged() = runTest {
        val fileContent = FakeVaultFileContentDataSource(
            entries =
            listOf(
                file("note.md", size = 10, modified = 100),
            ),
        )
        val snapshotStore = FakeVaultFolderSnapshotStore(
            initial =
            VaultFolderMetadataSnapshot.fromEntries(
                listOf(file("note.md", size = 10, modified = 99)),
            ),
        )
        val indexRepository = FakeVaultIndexRepository()
        val executor =
            createExecutor(
                fileContent = fileContent,
                snapshotStore = snapshotStore,
                indexRepository = indexRepository,
            )

        val result = executor.run()

        assertIs<VaultBackgroundSyncResult.Indexed>(result)
        assertEquals(1, indexRepository.indexCalls)
    }

    private fun createExecutor(
        syncEnabled: Boolean = true,
        selection: VaultFolderSelection? =
            VaultFolderSelection(displayName = "Vault", storageKey = "vault-key"),
        fileContent: VaultFileContentDataSource = FakeVaultFileContentDataSource(),
        snapshotStore: VaultFolderSnapshotStore = FakeVaultFolderSnapshotStore(),
        indexRepository: FakeVaultIndexRepository = FakeVaultIndexRepository(),
    ): VaultBackgroundSyncExecutor = VaultBackgroundSyncExecutor(
        syncSettings = FakeVaultSyncSettingsRepository(syncEnabled),
        folderPersistence = FakeVaultFolderPersistenceDataSource(selection),
        indexRepository = indexRepository,
        fileContent = fileContent,
        snapshotStore = snapshotStore,
    )

    private fun file(path: String, size: Long, modified: Long) = VaultFileEntry(
        relativePath = path,
        name = path,
        extension = "md",
        sizeBytes = size,
        modifiedAtEpochMs = modified,
        isDirectory = false,
    )
}

private class FakeVaultSyncSettingsRepository(enabled: Boolean) : VaultSyncSettingsRepository {
    private val state = MutableStateFlow(enabled)

    override fun observeEnabled(): Flow<Boolean> = state

    override suspend fun setEnabled(enabled: Boolean): Result<Unit> {
        state.value = enabled
        return Result.success(Unit)
    }
}

private class FakeVaultFolderPersistenceDataSource(private val selection: VaultFolderSelection?) :
    VaultFolderPersistenceDataSource {
    override fun observeSelection(): Flow<VaultFolderSelection?> = flowOf(selection)

    override suspend fun save(selection: VaultFolderSelection) = Unit

    override suspend fun clear() = Unit

    override fun currentSelection(): VaultFolderSelection? = selection
}

private class FakeVaultFileContentDataSource(private val entries: List<VaultFileEntry> = emptyList()) :
    VaultFileContentDataSource {
    override suspend fun collectFiles(
        storageKey: String,
        ignoreMatcher: com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher,
        onProgress: (Int, String) -> Unit,
    ): Result<List<VaultFileEntry>> = Result.success(entries)

    override suspend fun readTextContent(storageKey: String, relativePath: String, maxBytes: Int): Result<String> =
        Result.success("")
}

private class FakeVaultFolderSnapshotStore(initial: VaultFolderMetadataSnapshot? = null) : VaultFolderSnapshotStore {
    private var stored: VaultFolderMetadataSnapshot? = initial

    override suspend fun load(storageKey: String): VaultFolderMetadataSnapshot? = stored

    override suspend fun save(storageKey: String, snapshot: VaultFolderMetadataSnapshot) {
        stored = snapshot
    }

    override suspend fun clear(storageKey: String) {
        stored = null
    }
}

private class FakeVaultIndexRepository : VaultIndexRepository {
    var indexCalls = 0

    override fun observeIndexStatus(storageKey: String): Flow<VaultIndexStatus> = flowOf(VaultIndexStatus.Ready)

    override fun observeIndexProgress() = flowOf(null)

    override suspend fun indexSelectedVault(fullRebuild: Boolean): Result<Unit> {
        indexCalls++
        return Result.success(Unit)
    }

    override suspend fun search(query: VaultSearchQuery): Result<List<VaultSearchResult>> = Result.success(emptyList())

    override suspend fun getDashboardStats() =
        Result.failure<com.devindie.vaulty.domain.model.index.VaultDashboardStats>(
            UnsupportedOperationException(),
        )

    override suspend fun getBacklinks(fileId: Long): Result<List<VaultFileRef>> = Result.success(emptyList())

    override suspend fun getOutgoingLinks(fileId: Long): Result<List<VaultLink>> = Result.success(emptyList())
}
