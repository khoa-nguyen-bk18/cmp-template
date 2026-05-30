package com.devindie.vaulty.domain.model

/**
 * User-chosen vault root folder.
 *
 * @property displayName Human-readable label shown in the UI.
 * @property storageKey Opaque platform key (Android tree URI string; iOS bookmark payload).
 */
data class VaultFolderSelection(val displayName: String, val storageKey: String)
