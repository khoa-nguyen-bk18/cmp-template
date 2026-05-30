package com.devindie.vaulty.data.vault.index

import kotlinx.coroutines.flow.Flow

/**
 * Platform persistence for per-vault index exclude rules text.
 *
 * **Implemented by:** Android SharedPreferences / iOS NSUserDefaults.
 */
interface VaultIndexIgnorePersistenceDataSource {
    fun observeRules(storageKey: String): Flow<String>

    suspend fun getRules(storageKey: String): String

    suspend fun saveRules(storageKey: String, rulesText: String)

    suspend fun clearRules(storageKey: String)
}
