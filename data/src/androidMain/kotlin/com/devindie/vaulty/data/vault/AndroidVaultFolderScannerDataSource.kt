package com.devindie.vaulty.data.vault

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.domain.model.VaultFolderSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Scans a Storage Access Framework tree URI via [DocumentFile].
 *
 * **Upstream:** [VaultFolderRepositoryImpl.scanSelectedFolder].
 * Stops at [VAULT_FOLDER_SCAN_FILE_LIMIT] files.
 */
class AndroidVaultFolderScannerDataSource(private val context: Context, private val dispatchers: DispatcherProvider) :
    VaultFolderScannerDataSource {
    override suspend fun scan(storageKey: String, folderDisplayName: String): Result<VaultFolderSummary> =
        withContext(dispatchers.io) {
            try {
                val root = openReadableRoot(storageKey)
                val (fileCount, totalSize) = countFilesUnderRoot(root)
                Result.success(
                    VaultFolderSummary(
                        folderName = folderDisplayName.ifBlank { root.name ?: "Folder" },
                        fileCount = fileCount,
                        totalSizeBytes = totalSize,
                        scannedAtEpochMs = System.currentTimeMillis(),
                    ),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Result.failure(e)
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

    private suspend fun countFilesUnderRoot(root: DocumentFile): Pair<Int, Long> {
        var fileCount = 0
        var totalSize = 0L
        val queue = ArrayDeque<DocumentFile>()
        queue.add(root)

        while (queue.isNotEmpty() && fileCount < VAULT_FOLDER_SCAN_FILE_LIMIT) {
            yield()
            val dir = queue.removeFirst()
            val children = dir.listFiles() ?: continue
            for (child in children) {
                if (fileCount >= VAULT_FOLDER_SCAN_FILE_LIMIT) break
                when {
                    child.isDirectory -> queue.add(child)
                    child.isFile -> {
                        fileCount++
                        totalSize += child.length().coerceAtLeast(0L)
                    }
                }
            }
        }
        return fileCount to totalSize
    }
}
