package com.devindie.vaulty.data.vault.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

/** Persists background sync enabled in [NSUserDefaults] (default false). */
class IosVaultSyncSettingsDataSource : VaultSyncSettingsDataSource {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val enabledState =
        MutableStateFlow(
            if (defaults.objectForKey(KEY_ENABLED) == null) {
                false
            } else {
                defaults.boolForKey(KEY_ENABLED)
            },
        )

    override fun observeEnabled(): Flow<Boolean> = enabledState

    override suspend fun setEnabled(enabled: Boolean) {
        defaults.setBool(enabled, KEY_ENABLED)
        enabledState.value = enabled
    }

    companion object {
        private const val KEY_ENABLED = "background_sync_enabled"
    }
}
