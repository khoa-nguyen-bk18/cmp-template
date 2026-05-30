package com.devindie.vaulty.domain.gateway

import com.devindie.vaulty.domain.model.VaultFolderSelection

/**
 * Platform port to open the system folder picker and return a [VaultFolderSelection].
 *
 * **Upstream:** [com.devindie.vaulty.domain.usecase.vault.SelectVaultFolderUseCase].
 * **Downstream:** iOS [com.devindie.vaulty.data.platform.IosVaultFolderPickerGateway];
 * Android uses [com.devindie.vaulty.platform.VaultFolderPickerLauncher] wired at app entry.
 *
 * @return `null` when the user cancels the picker.
 */
fun interface VaultFolderPickerGateway {
    suspend fun pickFolder(): VaultFolderSelection?
}
