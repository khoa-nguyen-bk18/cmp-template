package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.data.coroutines.DispatcherProvider
import com.devindie.vaulty.data.vault.coordinateFolderRead
import com.devindie.vaulty.data.vault.decodeBookmarkData
import com.devindie.vaulty.data.vault.resolveSecurityScopedFolderUrl
import com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLContentModificationDateKey
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.NSURLNameKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970

private const val UTF8_MULTIBYTE_LEADING_MASK = 0xC0
private const val UTF8_CONTINUATION_PREFIX = 0x80

/** Enumerates and reads vault files via security-scoped bookmarks for [VaultFolderIndexer]. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosVaultFileContentDataSource(private val dispatchers: DispatcherProvider) : VaultFileContentDataSource {
    override suspend fun collectFiles(
        storageKey: String,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ): Result<List<VaultFileEntry>> = withContext(dispatchers.io) {
        runCatching {
            val bookmarkData = decodeBookmarkData(storageKey)
            val url = resolveSecurityScopedFolderUrl(bookmarkData)
            val accessed = url.startAccessingSecurityScopedResource()
            try {
                val files = mutableListOf<VaultFileEntry>()
                coordinateFolderRead(url) { coordinatedUrl ->
                    walkDirectory(
                        directoryUrl = coordinatedUrl,
                        prefix = "",
                        fileManager = NSFileManager.defaultManager,
                        files = files,
                        ignoreMatcher = ignoreMatcher,
                        onProgress = onProgress,
                    )
                }
                onProgress(files.size, "")
                files
            } finally {
                if (accessed) {
                    url.stopAccessingSecurityScopedResource()
                }
            }
        }
    }

    override suspend fun readTextContent(storageKey: String, relativePath: String, maxBytes: Int): Result<String> =
        withContext(dispatchers.io) {
            runCatching {
                val bookmarkData = decodeBookmarkData(storageKey)
                val rootUrl = resolveSecurityScopedFolderUrl(bookmarkData)
                val accessed = rootUrl.startAccessingSecurityScopedResource()
                try {
                    val fileUrl = resolveFileUrl(rootUrl, relativePath) ?: return@runCatching ""
                    val fileAccessed = fileUrl.startAccessingSecurityScopedResource()
                    try {
                        val data =
                            NSData.create(
                                contentsOfURL = fileUrl,
                                options = 0u,
                                error = null,
                            ) ?: return@runCatching ""
                        decodeNsDataUtf8(data, maxBytes)
                    } finally {
                        if (fileAccessed) {
                            fileUrl.stopAccessingSecurityScopedResource()
                        }
                    }
                } finally {
                    if (accessed) {
                        rootUrl.stopAccessingSecurityScopedResource()
                    }
                }
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun walkDirectory(
        directoryUrl: NSURL,
        prefix: String,
        fileManager: NSFileManager,
        files: MutableList<VaultFileEntry>,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ) {
        onProgress(files.size, prefix.ifEmpty { "." })
        val children = fileManager.contentsOfDirectoryAtURL(
            url = directoryUrl,
            includingPropertiesForKeys = listOf(
                NSURLIsDirectoryKey,
                NSURLFileSizeKey,
                NSURLContentModificationDateKey,
                NSURLNameKey,
            ),
            options = NSDirectoryEnumerationSkipsHiddenFiles,
            error = null,
        ) ?: return

        for (child in children) {
            if (files.size >= VAULT_INDEX_FILE_LIMIT) return
            processVaultChildEntry(
                childUrl = child as NSURL,
                prefix = prefix,
                fileManager = fileManager,
                files = files,
                ignoreMatcher = ignoreMatcher,
                onProgress = onProgress,
            )
        }
    }

    private fun processVaultChildEntry(
        childUrl: NSURL,
        prefix: String,
        fileManager: NSFileManager,
        files: MutableList<VaultFileEntry>,
        ignoreMatcher: VaultIndexIgnoreMatcher,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ) {
        val values =
            childUrl.resourceValuesForKeys(
                listOf(
                    NSURLIsDirectoryKey,
                    NSURLFileSizeKey,
                    NSURLContentModificationDateKey,
                    NSURLNameKey,
                ),
                error = null,
            ) ?: return
        val name = values[NSURLNameKey] as? String ?: return
        val isDirectory = (values[NSURLIsDirectoryKey] as? NSNumber)?.boolValue ?: false
        val relativePath = if (prefix.isEmpty()) name else "$prefix/$name"
        when {
            isDirectory && !ignoreMatcher.shouldPruneDirectory(relativePath) -> {
                walkDirectory(childUrl, relativePath, fileManager, files, ignoreMatcher, onProgress)
            }
            !isDirectory && !ignoreMatcher.isIgnored(relativePath, isDirectory = false) -> {
                appendVaultFileEntry(
                    files = files,
                    relativePath = relativePath,
                    name = name,
                    values = values,
                    onProgress = onProgress,
                )
            }
        }
    }

    private fun appendVaultFileEntry(
        files: MutableList<VaultFileEntry>,
        relativePath: String,
        name: String,
        values: Map<Any?, *>,
        onProgress: (filesDiscovered: Int, currentPath: String) -> Unit,
    ) {
        val size = (values[NSURLFileSizeKey] as? NSNumber)?.longLongValue ?: 0L
        val modified =
            (
                (values[NSURLContentModificationDateKey] as? platform.Foundation.NSDate)?.timeIntervalSince1970
                    ?: 0.0
                ) * 1000
        val extension = name.substringAfterLast('.', "").lowercase()
        files.add(
            VaultFileEntry(
                relativePath = relativePath,
                name = name,
                extension = extension,
                sizeBytes = size,
                modifiedAtEpochMs = modified.toLong(),
                isDirectory = false,
            ),
        )
        onProgress(files.size, relativePath)
    }

    private fun decodeNsDataUtf8(data: NSData, maxBytes: Int): String {
        val text = NSString.create(data = data, encoding = NSUTF8StringEncoding) as String? ?: ""
        val byteLimit = maxBytes.coerceAtMost(VAULT_INDEX_MAX_CONTENT_BYTES.toInt())
        val encoded = text.encodeToByteArray()
        if (encoded.size <= byteLimit) return text
        return truncateUtf8(encoded, byteLimit)
    }

    private fun truncateUtf8(bytes: ByteArray, maxBytes: Int): String {
        var end = maxBytes.coerceAtMost(bytes.size)
        while (end > 0 && isUtf8ContinuationByte(bytes[end - 1])) {
            end--
        }
        return bytes.decodeToString(0, end)
    }

    private fun isUtf8ContinuationByte(byte: Byte): Boolean =
        byte.toInt() and UTF8_MULTIBYTE_LEADING_MASK == UTF8_CONTINUATION_PREFIX

    private fun resolveFileUrl(rootUrl: NSURL, relativePath: String): NSURL? {
        var current = rootUrl
        for (segment in relativePath.split('/').filter { it.isNotEmpty() }) {
            val next = NSFileManager.defaultManager.contentsOfDirectoryAtURL(
                url = current,
                includingPropertiesForKeys = listOf(NSURLNameKey, NSURLIsDirectoryKey),
                options = NSDirectoryEnumerationSkipsHiddenFiles,
                error = null,
            )?.mapNotNull { it as? NSURL }?.firstOrNull { url ->
                (
                    url.resourceValuesForKeys(listOf(NSURLNameKey), error = null)
                        ?.get(NSURLNameKey) as? String
                    ) == segment
            } ?: return null
            current = next
        }
        return current
    }
}
