package com.devindie.cmptemplate.domain.usecase.browse

import com.devindie.cmptemplate.domain.repository.BrowseCardRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class EnsureBrowseCatalogSeededUseCase(private val repository: BrowseCardRepository) : UseCaseNoParams<Result<Unit>> {
    override suspend fun invoke(): Result<Unit> = repository.ensureCatalogSeeded()
}
