package com.devindie.vaulty.data.platform

import com.devindie.vaulty.data.vault.createFolderBookmarkData
import com.devindie.vaulty.data.vault.encodeBookmarkStorageKey
import com.devindie.vaulty.domain.gateway.VaultFolderPickerGateway
import com.devindie.vaulty.domain.model.VaultFolderSelection
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeFolder
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

/**
 * Opens the system Files picker (On My iPhone, iCloud Drive, third-party cloud providers).
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.vault.SelectVaultFolderUseCase].
 * **Downstream:** Encodes folder as bookmark [storageKey] via bookmark helpers in `data.vault`.
 *
 * @return `null` when the user cancels.
 *
 * @see com.devindie.vaulty.domain.gateway.VaultFolderPickerGateway
 */
@OptIn(ExperimentalForeignApi::class)
class IosVaultFolderPickerGateway : VaultFolderPickerGateway {
    override suspend fun pickFolder(): VaultFolderSelection? = suspendCancellableCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            val picker =
                UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypeFolder),
                    asCopy = false,
                )
            picker.allowsMultipleSelection = false
            picker.delegate = folderPickerDelegate(continuation)

            topViewController()?.presentViewController(picker, animated = true, completion = null)
                ?: continuation.resume(null)
        }
    }

    private fun folderPickerDelegate(
        continuation: CancellableContinuation<VaultFolderSelection?>,
    ): UIDocumentPickerDelegateProtocol = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            resumeIfActive(continuation, url?.let { selectionFromPickedUrl(it) })
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            resumeIfActive(continuation, null)
        }
    }

    private fun selectionFromPickedUrl(url: NSURL): VaultFolderSelection? {
        val accessed = url.startAccessingSecurityScopedResource()
        val bookmarkData =
            try {
                createFolderBookmarkData(url)
            } finally {
                if (accessed) {
                    url.stopAccessingSecurityScopedResource()
                }
            }
        if (bookmarkData == null) return null
        return VaultFolderSelection(
            displayName = url.lastPathComponent ?: "Folder",
            storageKey = encodeBookmarkStorageKey(bookmarkData),
        )
    }

    private fun resumeIfActive(
        continuation: CancellableContinuation<VaultFolderSelection?>,
        selection: VaultFolderSelection?,
    ) {
        if (continuation.isActive) {
            continuation.resume(selection)
        }
    }

    private fun topViewController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        var presented = controller?.presentedViewController
        while (presented != null) {
            controller = presented
            presented = controller.presentedViewController
        }
        return controller
    }
}
