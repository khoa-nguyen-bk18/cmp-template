package com.devindie.cmptemplate.domain.usecase.startup

import com.devindie.cmptemplate.domain.repository.AppStartupRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class InitializeAppUseCase(private val repository: AppStartupRepository) : UseCaseNoParams<Result<Unit>> {
    override suspend fun invoke(): Result<Unit> = repository.ensureReady()
}
