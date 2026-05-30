package com.devindie.vaulty.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persists per-vault index exclude rules (gitignore-style text).
 *
 * **Upstream:** Settings use cases.
 * **Downstream:** [com.devindie.vaulty.data.vault.index.VaultIndexIgnoreRepositoryImpl].
 *
 * @see com.devindie.vaulty.domain.model.index.VaultIndexIgnoreMatcher
 */
interface VaultIndexIgnoreRepository {
    fun observeRules(storageKey: String): Flow<String>

    suspend fun getRules(storageKey: String): String

    suspend fun saveRules(storageKey: String, rulesText: String): Result<Unit>

    suspend fun clearRules(storageKey: String): Result<Unit>
}
