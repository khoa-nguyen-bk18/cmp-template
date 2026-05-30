package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.index.dao.VaultIndexedFileTransactionDao
import com.devindie.vaulty.domain.model.VaultFolderSelection
import com.devindie.vaulty.domain.model.index.VaultFileChangeDetector
import com.devindie.vaulty.domain.model.index.VaultFileIndexSnapshot
import com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher
import com.devindie.vaulty.domain.model.index.VaultIndexPhase
import com.devindie.vaulty.domain.model.index.VaultIndexProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/** Parallel per-file indexing and Room upserts for [VaultFolderIndexer]. */
internal class VaultIndexFileBatchProcessor(
    private val indexedFileDao: VaultIndexedFileTransactionDao,
    private val fileContent: VaultFileContentDataSource,
    private val dispatchers: DispatcherProvider,
    private val progressState: MutableStateFlow<VaultIndexProgress?>,
) {
    suspend fun indexDiscoveredFiles(
        selection: VaultFolderSelection,
        files: List<VaultFileEntry>,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        existingByPath: Map<String, VaultFileIndexSnapshot>,
        startedAt: Long,
        pendingLinks: MutableList<PendingVaultLink>,
    ): IndexFileStats = coroutineScope {
        val total = files.size
        val fileSemaphore = Semaphore(IndexingConcurrency.FILE_PARALLELISM)
        val writeSemaphore = Semaphore(IndexingConcurrency.WRITE_PARALLELISM)
        val statsMutex = Mutex()
        val progressMutex = Mutex()
        val pendingLinksMutex = Mutex()
        val scannedPaths = mutableSetOf<String>()
        var processedCount = 0
        var skipped = 0
        var errors = 0

        files.map { entry ->
            async {
                fileSemaphore.withPermit {
                    ensureActive()
                    processDiscoveredFile(
                        selection = selection,
                        entry = entry,
                        ignoreMatcher = ignoreMatcher,
                        existingByPath = existingByPath,
                        startedAt = startedAt,
                        total = total,
                        writeSemaphore = writeSemaphore,
                        progressMutex = progressMutex,
                        onCounted = { path, result, filePendingLinks ->
                            statsMutex.withLock {
                                processedCount++
                                scannedPaths.add(path)
                                when (result) {
                                    FileIndexResult.Skipped -> skipped++
                                    FileIndexResult.Indexed -> Unit
                                    FileIndexResult.Failed -> errors++
                                }
                                if (filePendingLinks.isNotEmpty()) {
                                    pendingLinksMutex.withLock {
                                        pendingLinks.addAll(filePendingLinks)
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }.awaitAll()

        IndexFileStats(
            scannedPaths = scannedPaths,
            processed = processedCount,
            skipped = skipped,
            errors = errors,
        )
    }

    private suspend fun processDiscoveredFile(
        selection: VaultFolderSelection,
        entry: VaultFileEntry,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        existingByPath: Map<String, VaultFileIndexSnapshot>,
        startedAt: Long,
        total: Int,
        writeSemaphore: Semaphore,
        progressMutex: Mutex,
        onCounted: suspend (String, FileIndexResult, List<PendingVaultLink>) -> Unit,
    ) {
        if (ignoreMatcher.isIgnored(entry.relativePath, isDirectory = false)) {
            return
        }
        val normalizedPath = VaultPathResolver.normalizeRelativePath(entry.relativePath)
        val existing = existingByPath[normalizedPath]
        val needsReindex =
            VaultFileChangeDetector.needsReindex(
                stored = existing,
                currentSizeBytes = entry.sizeBytes,
                currentModifiedAtEpochMs = entry.modifiedAtEpochMs,
            )
        progressMutex.withLock {
            val current =
                (progressState.value?.processed ?: 0) + 1
            progressState.value =
                VaultIndexProgress(
                    phase = VaultIndexPhase.Indexing,
                    processed = current,
                    total = total,
                    currentPath = entry.relativePath,
                    startedAtEpochMs = startedAt,
                )
        }
        val resultAndLinks =
            if (needsReindex) {
                runCatching {
                    indexSingleFile(
                        selection = selection,
                        entry = entry,
                        normalizedPath = normalizedPath,
                        existingFileId = existing?.fileId,
                        writeSemaphore = writeSemaphore,
                    )
                }.fold(
                    onSuccess = { links -> FileIndexResult.Indexed to links },
                    onFailure = { FileIndexResult.Failed to emptyList() },
                )
            } else {
                FileIndexResult.Skipped to emptyList()
            }
        onCounted(normalizedPath, resultAndLinks.first, resultAndLinks.second)
    }

    private suspend fun indexSingleFile(
        selection: VaultFolderSelection,
        entry: VaultFileEntry,
        normalizedPath: String,
        existingFileId: Long?,
        writeSemaphore: Semaphore,
    ): List<PendingVaultLink> {
        val rawContent = readFileText(selection.storageKey, entry)
        val payload =
            withContext(dispatchers.default) {
                buildIndexedFilePayload(
                    selection = selection,
                    entry = entry,
                    normalizedPath = normalizedPath,
                    existingFileId = existingFileId,
                    rawContent = rawContent,
                )
            }
        val fileId =
            writeSemaphore.withPermit {
                withContext(dispatchers.io) {
                    indexedFileDao.upsertIndexedFile(
                        existingFileId = existingFileId,
                        file = payload.file,
                        content = payload.content,
                        properties = payload.properties,
                    )
                }
            }
        return payload.parsedLinks.map { link ->
            PendingVaultLink(
                sourceFileId = fileId,
                sourceRelativePath = normalizedPath,
                link = link,
            )
        }
    }

    private suspend fun readFileText(storageKey: String, entry: VaultFileEntry): String {
        val isText = isTextExtension(entry.extension)
        if (!isText) return ""
        return withContext(dispatchers.io) {
            fileContent
                .readTextContent(
                    storageKey = storageKey,
                    relativePath = entry.relativePath,
                    maxBytes = VAULT_INDEX_MAX_CONTENT_BYTES,
                )
                .getOrDefault("")
        }
    }
}

private enum class FileIndexResult {
    Skipped,
    Indexed,
    Failed,
}
