package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultSearchQuery
import com.devindie.vaulty.domain.model.index.VaultSearchResult
import com.devindie.vaulty.domain.repository.VaultIndexRepository
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Searches indexed vault content with optional filters in [VaultSearchQuery].
 *
 * **Flow:** [com.devindie.vaulty.screens.search.SearchViewModel] → this →
 * [VaultIndexRepository.search].
 *
 * @see VaultIndexRepository
 */
class SearchVaultUseCase(private val repository: VaultIndexRepository) :
    UseCase<VaultSearchQuery, Result<List<VaultSearchResult>>> {
    override suspend fun invoke(parameters: VaultSearchQuery): Result<List<VaultSearchResult>> =
        repository.search(parameters)
}
