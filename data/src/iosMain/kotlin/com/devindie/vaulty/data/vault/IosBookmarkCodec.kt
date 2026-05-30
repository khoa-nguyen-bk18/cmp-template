package com.devindie.vaulty.data.vault

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.NSError
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSURL
import platform.Foundation.NSURLBookmarkCreationWithSecurityScope
import platform.Foundation.NSURLBookmarkResolutionWithSecurityScope
import platform.Foundation.NSURLBookmarkResolutionWithoutUI
import platform.Foundation.NSURLIsUbiquitousItemKey
import platform.Foundation.NSURLVolumeURLKey
import platform.Foundation.create
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * iOS security-scoped folder bookmarks: encode/decode [storageKey], resolve URL, coordinate reads.
 *
 * Used by folder persistence, scanner, and [com.devindie.vaulty.data.vault.index.IosVaultFileContentDataSource].
 */

/** Bookmark resolution flags for security-scoped folders (local, iCloud Drive, third-party providers). */
@OptIn(ExperimentalForeignApi::class)
internal val folderBookmarkResolutionOptions: ULong =
    NSURLBookmarkResolutionWithSecurityScope or NSURLBookmarkResolutionWithoutUI

/** Resource keys stored in the bookmark so iCloud / provider-backed folders resolve reliably. */
@OptIn(ExperimentalForeignApi::class)
internal val folderBookmarkResourceKeys: List<Any?> =
    listOf(
        NSURLVolumeURLKey,
        NSURLIsUbiquitousItemKey,
    )

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply {
        usePinned { destination ->
            memcpy(destination.addressOf(0), bytes, size.convert())
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
internal fun encodeBookmarkStorageKey(bookmarkData: NSData): String = Base64.encode(bookmarkData.toByteArray())

@OptIn(ExperimentalForeignApi::class, ExperimentalEncodingApi::class)
internal fun decodeBookmarkData(storageKey: String): NSData = NSData.create(
    base64EncodedString = storageKey,
    options = NSDataBase64DecodingIgnoreUnknownCharacters,
) ?: error("Invalid folder bookmark")

/**
 * Creates a security-scoped bookmark while the URL is being accessed (required for iCloud Drive).
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun createFolderBookmarkData(url: NSURL): NSData? = url.bookmarkDataWithOptions(
    NSURLBookmarkCreationWithSecurityScope,
    includingResourceValuesForKeys = folderBookmarkResourceKeys,
    relativeToURL = null,
    error = null,
)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun resolveSecurityScopedFolderUrl(bookmarkData: NSData): NSURL = memScoped {
    val isStale = alloc<BooleanVar>()
    val resolved =
        NSURL.URLByResolvingBookmarkData(
            bookmarkData = bookmarkData,
            options = folderBookmarkResolutionOptions,
            relativeToURL = null,
            bookmarkDataIsStale = isStale.ptr,
            error = null,
        )
    if (isStale.value) {
        error("Folder bookmark is stale; pick the folder again")
    }
    resolved ?: error("Cannot resolve folder bookmark")
}

/**
 * Reads directory contents via [NSFileCoordinator] — required for iCloud and other file providers.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal inline fun coordinateFolderRead(folderUrl: NSURL, crossinline readBlock: (NSURL) -> Unit) {
    val coordinator = NSFileCoordinator(null)
    memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        coordinator.coordinateReadingItemAtURL(
            url = folderUrl,
            options = 0u,
            error = error.ptr,
        ) { coordinatedUrl ->
            val url = coordinatedUrl ?: return@coordinateReadingItemAtURL
            readBlock(url)
        }
        error.value?.let { nsError ->
            error("Cannot read folder: ${nsError.localizedDescription}")
        }
    }
}
