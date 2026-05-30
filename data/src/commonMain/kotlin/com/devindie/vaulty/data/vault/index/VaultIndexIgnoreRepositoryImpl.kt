package com.devindie.vaulty.data.vault.index

import com.devindie.vaulty.domain.repository.VaultIndexIgnoreRepository
import kotlinx.coroutines.flow.Flow

/** Implements [VaultIndexIgnoreRepository] via [VaultIndexIgnorePersistenceDataSource]. */
class VaultIndexIgnoreRepositoryImpl(private val persistence: VaultIndexIgnorePersistenceDataSource) :
    VaultIndexIgnoreRepository {
    override fun observeRules(storageKey: String): Flow<String> = persistence.observeRules(storageKey)

    override suspend fun getRules(storageKey: String): String = persistence.getRules(storageKey)

    override suspend fun saveRules(storageKey: String, rulesText: String): Result<Unit> = runCatching {
        persistence.saveRules(storageKey, rulesText)
    }

    override suspend fun clearRules(storageKey: String): Result<Unit> = runCatching {
        persistence.clearRules(storageKey)
    }
}
