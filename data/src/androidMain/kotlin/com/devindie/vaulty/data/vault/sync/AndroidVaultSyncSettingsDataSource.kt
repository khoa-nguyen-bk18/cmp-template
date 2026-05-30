package com.devindie.vaulty.data.vault.sync

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
/** Persists background sync enabled in app-private SharedPreferences (default false). */
class AndroidVaultSyncSettingsDataSource(context: Context) : VaultSyncSettingsDataSource {
    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val enabledState =
        MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))

    override fun observeEnabled(): Flow<Boolean> = enabledState

    override suspend fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        enabledState.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "vault_sync_settings"
        private const val KEY_ENABLED = "background_sync_enabled"
    }
}
