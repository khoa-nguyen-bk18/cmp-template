package com.devindie.cmptemplate.data.vault.index

import com.devindie.cmptemplate.data.coroutines.testDispatcherProvider
import com.devindie.cmptemplate.domain.model.VaultFolderSelection
import com.devindie.cmptemplate.domain.model.index.VaultIndexIgnoreMatcher
import com.devindie.cmptemplate.domain.model.index.VaultIndexProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VaultFolderIndexerConcurrencyTest {
    private val scheduler = TestCoroutineScheduler()
    private val dispatchers = testDispatcherProvider(scheduler)
    private lateinit var database: VaultDatabase
    private val progressState = MutableStateFlow<VaultIndexProgress?>(null)

    @BeforeTest
    fun setUp() {
        database = createInMemoryVaultDatabase(dispatchers.io)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun index_parallelFiles_persistsAllRows() = runTest(scheduler) {
        val selection = VaultFolderSelection(displayName = "Vault", storageKey = "vault-key-ios")
        val fileContent =
            FakeIndexerFileContentDataSource(
                entries =
                listOf(
                    vaultEntry("a.md", 10, 100),
                    vaultEntry("b.md", 20, 200),
                ),
            )
        val indexer =
            VaultFolderIndexer(
                database = database,
                fileContent = fileContent,
                dispatchers = dispatchers,
                progressState = progressState,
            )

        val result = indexer.index(selection, fullRebuild = true)

        assertTrue(result.isSuccess)
        assertEquals(2, database.vaultFileQueryDao().getAllFiles(selection.storageKey).size)
    }

    private fun vaultEntry(path: String, size: Long, modified: Long) = VaultFileEntry(
        relativePath = path,
        name = path.substringAfterLast('/'),
        extension = "md",
        sizeBytes = size,
        modifiedAtEpochMs = modified,
        isDirectory = false,
    )
}

private class FakeIndexerFileContentDataSource(private val entries: List<VaultFileEntry>) :
    VaultFileContentDataSource {
    override suspend fun collectFiles(
        storageKey: String,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ): Result<List<VaultFileEntry>> {
        entries.forEachIndexed { index, entry -> onProgress(index + 1, entry.relativePath) }
        return Result.success(entries)
    }

    override suspend fun readTextContent(storageKey: String, relativePath: String, maxBytes: Int): Result<String> =
        Result.success("# Note")
}
