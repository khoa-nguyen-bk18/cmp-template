package com.devindie.vaulty.domain.usecase.index

import com.devindie.vaulty.domain.model.index.VaultSearchDslClock
import com.devindie.vaulty.domain.model.index.VaultSearchDslParseResult
import com.devindie.vaulty.domain.model.index.VaultSearchDslParser
import com.devindie.vaulty.domain.usecase.UseCase

/**
 * Parses vault search mini-DSL into [VaultSearchDslParseResult].
 *
 * **Flow:** [com.devindie.vaulty.screens.search.SearchViewModel] → this → [VaultSearchDslParser].
 *
 * @see com.devindie.vaulty.di.vaultDomainModule provides [VaultSearchDslClock].
 */
class ParseVaultSearchDslUseCase(clock: VaultSearchDslClock) : UseCase<String, VaultSearchDslParseResult> {
    private val parser = VaultSearchDslParser(clock)

    override suspend fun invoke(parameters: String): VaultSearchDslParseResult = parser.parse(parameters)
}
