package com.devindie.vaulty.data.vault.index

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Enumerates and reads vault files via SAF [DocumentFile] for [VaultFolderIndexer]. */
class AndroidVaultFileContentDataSource(private val context: Context, private val dispatchers: DispatcherProvider) :
    VaultFileContentDataSource {
    override suspend fun collectFiles(
        storageKey: String,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ): Result<List<VaultFileEntry>> = withContext(dispatchers.io) {
        try {
            val root = openReadableRoot(storageKey)
            Result.success(collectFilesFromRoot(root, ignoreMatcher, onProgress))
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readTextContent(storageKey: String, relativePath: String, maxBytes: Int): Result<String> =
        withContext(dispatchers.io) {
            runCatching {
                val file = resolveFile(storageKey, relativePath) ?: return@runCatching ""
                context.contentResolver.openInputStream(file.uri)?.use { input ->
                    val buffer = ByteArray(maxBytes.coerceAtMost(VAULT_INDEX_MAX_CONTENT_BYTES))
                    val read = input.read(buffer)
                    if (read <= 0) return@runCatching ""
                    String(buffer, 0, read, Charsets.UTF_8)
                } ?: ""
            }
        }

    private fun openReadableRoot(storageKey: String): DocumentFile {
        val treeUri = Uri.parse(storageKey)
        val root =
            DocumentFile.fromTreeUri(context, treeUri)
                ?: error("Cannot access folder")
        if (!root.isDirectory || !root.canRead()) {
            error("Folder access revoked or unavailable")
        }
        return root
    }

    private suspend fun collectFilesFromRoot(
        root: DocumentFile,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ): List<VaultFileEntry> {
        val files = mutableListOf<VaultFileEntry>()
        val queue = ArrayDeque<Pair<DocumentFile, String>>()
        queue.add(root to "")

        while (queue.isNotEmpty() && files.size < VAULT_INDEX_FILE_LIMIT) {
            yield()
            val (dir, prefix) = queue.removeFirst()
            onProgress(files.size, prefix.ifEmpty { "." })
            val children = dir.listFiles() ?: continue
            for (child in children) {
                if (files.size >= VAULT_INDEX_FILE_LIMIT) break
                enqueueOrCollect(child, prefix, files, queue, ignoreMatcher, onProgress)
            }
        }
        onProgress(files.size, "")
        return files
    }

    private fun enqueueOrCollect(
        child: DocumentFile,
        prefix: String,
        files: MutableList<VaultFileEntry>,
        queue: ArrayDeque<Pair<DocumentFile, String>>,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ) {
        val name = child.name ?: return
        val relativePath = if (prefix.isEmpty()) name else "$prefix/$name"
        when {
            child.isDirectory -> {
                if (!ignoreMatcher.shouldPruneDirectory(relativePath)) {
                    queue.add(child to relativePath)
                }
            }
            child.isFile -> {
                if (!ignoreMatcher.isIgnored(relativePath, isDirectory = false)) {
                    val extension = name.substringAfterLast('.', "").lowercase()
                    files.add(
                        VaultFileEntry(
                            relativePath = relativePath,
                            name = name,
                            extension = extension,
                            sizeBytes = child.length().coerceAtLeast(0L),
                            modifiedAtEpochMs = child.lastModified(),
                            isDirectory = false,
                        ),
                    )
                    onProgress(files.size, relativePath)
                }
            }
        }
    }

    private fun resolveFile(storageKey: String, relativePath: String): DocumentFile? {
        val treeUri = Uri.parse(storageKey)
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return null
        val resolved =
            relativePath
                .split('/')
                .filter { it.isNotEmpty() }
                .fold(root as DocumentFile?) { current, segment ->
                    current?.findFile(segment)
                }
        return resolved?.takeIf { it.isFile }
    }
}
