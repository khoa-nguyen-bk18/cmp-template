package com.devindie.vaulty.domain.model

/**
 * Domain errors for vault folder selection and scan (mapped in presentation).
 */
sealed interface VaultFolderError {
    data object NoFolderSelected : VaultFolderError

    data object AccessRevoked : VaultFolderError

    data class ScanFailed(val message: String) : VaultFolderError
}
