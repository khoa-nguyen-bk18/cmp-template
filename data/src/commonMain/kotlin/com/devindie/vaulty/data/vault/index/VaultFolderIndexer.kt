package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.index.dao.VaultIndexedFileTransactionDao
import com.devindie.vaulty.data.vault.index.entity.VaultIndexEntity
import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.index.VaultFileIndexSnapshot
import com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher
import com.devindie.vaulty.domain.model.index.VaultIndexPhase
import com.devindie.vaulty.domain.model.index.VaultIndexProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Orchestrates a vault indexing run: collect files, upsert Room rows, extract links, emit progress.
 *
 * **Upstream:** [VaultIndexRepositoryImpl.indexSelectedVault].
 * **Downstream:** [VaultDatabase], [VaultFileContentDataSource], [VaultFileChangeDetector].
 *
 * - **Incremental:** Skips files when size/mtime unchanged; removes DB rows for deleted paths.
 * - **Full rebuild:** Clears vault file rows first ([fullRebuild]).
 * - **Links:** After all files, resolves wiki/markdown edges and bulk-inserts link rows.
 *
 * @see com.devindie.vaulty.domain.repository.VaultIndexRepository
 */
class VaultFolderIndexer(
    private val database: VaultDatabase,
    private val fileContent: VaultFileContentDataSource,
    private val dispatchers: DispatcherProvider,
    private val progressState: MutableStateFlow<VaultIndexProgress?>,
) {
    suspend fun index(
        selection: VaultFolderSelection,
        fullRebuild: Boolean,
        ignoreMatcher: VaultIndexIgnoreMatcher = VaultIndexIgnoreMatcher.EMPTY,
    ): Result<Unit> = try {
        withContext(dispatchers.io) {
            indexVault(selection, fullRebuild, ignoreMatcher)
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
        progressState.value = null
        runCatching {
            database.vaultIndexDao().upsertIndex(
                VaultIndexEntity(
                    storageKey = selection.storageKey,
                    displayName = selection.displayName,
                    indexedAtEpochMs = currentEpochMillis(),
                    schemaVersion = VAULT_DATABASE_SCHEMA_VERSION,
                    status = "failed",
                ),
            )
        }
        Result.failure(error)
    }

    private suspend fun indexVault(
        selection: VaultFolderSelection,
        fullRebuild: Boolean,
        ignoreMatcher: VaultIndexIgnoreMatcher,
    ) {
        val metrics = IndexingStageMetrics()
        val startedAt = currentEpochMillis()
        val runLifecycle =
            VaultIndexRunLifecycle(
                indexDao = database.vaultIndexDao(),
                fileQueryDao = database.vaultFileQueryDao(),
                fileWriteDao = database.vaultFileWriteDao(),
                progressState = progressState,
            )
        val fileBatchProcessor =
            VaultIndexFileBatchProcessor(
                indexedFileDao = database.vaultIndexedFileTransactionDao(),
                fileContent = fileContent,
                dispatchers = dispatchers,
                progressState = progressState,
            )
        val linkIndexPass =
            VaultLinkIndexPass(
                fileQueryDao = database.vaultFileQueryDao(),
                fileGraphDao = database.vaultFileGraphDao(),
                dispatchers = dispatchers,
            )
        runLifecycle.markIndexStarted(selection, startedAt)
        val files =
            metrics.measure("collect") {
                collectVaultFiles(selection, ignoreMatcher, startedAt)
            }
        val runId =
            metrics.measure("beginRun") {
                runLifecycle.beginIndexRun(selection, startedAt, fullRebuild)
            }
        metrics.measure("prepare") {
            runLifecycle.prepareVaultForIndexing(selection.storageKey, fullRebuild)
        }
        val existingByPath =
            metrics.measure("loadSnapshots") {
                runLifecycle.loadExistingSnapshots(selection.storageKey, fullRebuild)
            }
        val pendingLinks = mutableListOf<PendingVaultLink>()
        val fileStats =
            metrics.measure("indexFiles") {
                fileBatchProcessor.indexDiscoveredFiles(
                    selection = selection,
                    files = files,
                    ignoreMatcher = ignoreMatcher,
                    existingByPath = existingByPath,
                    startedAt = startedAt,
                    pendingLinks = pendingLinks,
                )
            }
        if (!fullRebuild) {
            metrics.measure("staleCleanup") {
                removeStaleFiles(
                    indexedFileDao = database.vaultIndexedFileTransactionDao(),
                    scannedPaths = fileStats.scannedPaths,
                    existingByPath = existingByPath,
                )
            }
        }
        metrics.measure("resolveLinks") {
            linkIndexPass.resolve(selection.storageKey, pendingLinks)
        }
        metrics.measure("finalize") {
            runLifecycle.finalizeIndexRun(selection, runId, fileStats)
        }
        metrics.report()
    }

    private suspend fun collectVaultFiles(
        selection: VaultFolderSelection,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        startedAt: Long,
    ): List<VaultFileEntry> = fileContent.collectFiles(
        storageKey = selection.storageKey,
        ignoreMatcher = ignoreMatcher,
    ) { discovered, path ->
        progressState.value =
            VaultIndexProgress(
                phase = VaultIndexPhase.Collecting,
                processed = discovered,
                total = 0,
                currentPath = path,
                startedAtEpochMs = startedAt,
            )
    }.getOrElse { error ->
        throw error
    }

    private suspend fun removeStaleFiles(
        indexedFileDao: VaultIndexedFileTransactionDao,
        scannedPaths: Set<String>,
        existingByPath: Map<String, VaultFileIndexSnapshot>,
    ) {
        val staleIds =
            existingByPath
                .filterKeys { path -> path !in scannedPaths }
                .values
                .map { it.fileId }
        for (chunk in staleIds.chunked(IndexingConcurrency.STALE_DELETE_CHUNK_SIZE)) {
            yield()
            indexedFileDao.deleteFilesByIds(chunk)
        }
    }
}
