package com.devindie.vaulty.data.vault

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.VAULT_FOLDER_SCAN_FILE_LIMIT
import com.devindie.vaulty.data.vault.VaultFolderScannerDataSource
import com.devindie.vaulty.domain.model.VaultFolderSummary
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import platform.Foundation.NSDate
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.NSURLNameKey
import platform.Foundation.timeIntervalSince1970

/**
 * Scans a security-scoped folder from an encoded bookmark [storageKey].
 *
 * Uses [NSFileCoordinator] for provider-backed trees (iCloud, third-party).
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosVaultFolderScannerDataSource(private val dispatchers: DispatcherProvider) : VaultFolderScannerDataSource {
    override suspend fun scan(storageKey: String, folderDisplayName: String): Result<VaultFolderSummary> =
        withContext(dispatchers.io) {
            try {
                val bookmarkData = decodeBookmarkData(storageKey)
                val url = resolveSecurityScopedFolderUrl(bookmarkData)

                val accessed = url.startAccessingSecurityScopedResource()
                try {
                    var fileCount = 0
                    var totalSize = 0L

                    coordinateFolderRead(url) { coordinatedUrl ->
                        walkDirectory(
                            directoryUrl = coordinatedUrl,
                            fileManager = NSFileManager.defaultManager,
                            onFile = { fileUrl ->
                                val fileAccessed = fileUrl.startAccessingSecurityScopedResource()
                                try {
                                    val values =
                                        fileUrl.resourceValuesForKeys(
                                            listOf(NSURLFileSizeKey),
                                            error = null,
                                        )
                                    val size =
                                        (values?.get(NSURLFileSizeKey) as? NSNumber)?.longLongValue
                                            ?: 0L
                                    fileCount++
                                    totalSize += size
                                } finally {
                                    if (fileAccessed) {
                                        fileUrl.stopAccessingSecurityScopedResource()
                                    }
                                }
                            },
                            fileCount = { fileCount },
                        )
                    }

                    val resolvedName =
                        folderDisplayName.ifBlank {
                            val nameValues =
                                url.resourceValuesForKeys(listOf(NSURLNameKey), error = null)
                            (nameValues?.get(NSURLNameKey) as? String) ?: "Folder"
                        }

                    Result.success(
                        VaultFolderSummary(
                            folderName = resolvedName,
                            fileCount = fileCount,
                            totalSizeBytes = totalSize,
                            scannedAtEpochMs = (NSDate().timeIntervalSince1970 * 1000).toLong(),
                        ),
                    )
                } finally {
                    if (accessed) {
                        url.stopAccessingSecurityScopedResource()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Result.failure(e)
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun walkDirectory(
        directoryUrl: NSURL,
        fileManager: NSFileManager,
        onFile: (NSURL) -> Unit,
        fileCount: () -> Int,
    ) {
        fun walk(url: NSURL) {
            if (fileCount() >= VAULT_FOLDER_SCAN_FILE_LIMIT) return
            val children =
                fileManager.contentsOfDirectoryAtURL(
                    url = url,
                    includingPropertiesForKeys = listOf(NSURLIsDirectoryKey, NSURLFileSizeKey),
                    options = NSDirectoryEnumerationSkipsHiddenFiles,
                    error = null,
                ) ?: return

            for (child in children) {
                if (fileCount() >= VAULT_FOLDER_SCAN_FILE_LIMIT) break
                val childUrl = child as NSURL
                val values =
                    childUrl.resourceValuesForKeys(listOf(NSURLIsDirectoryKey), error = null)
                val isDirectory = (values?.get(NSURLIsDirectoryKey) as? NSNumber)?.boolValue ?: false
                when {
                    isDirectory -> walk(childUrl)
                    fileCount() < VAULT_FOLDER_SCAN_FILE_LIMIT -> onFile(childUrl)
                }
            }
        }
        walk(directoryUrl)
    }
}
