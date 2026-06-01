package com.devindie.cmptemplate.domain.usecase.user

import com.devindie.cmptemplate.domain.repository.UserRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

/**
 * Clears stored tokens (logout).
 *
 * @see UserRepository.clearSession
 */
class ClearUserSessionUseCase(private val repository: UserRepository) : UseCaseNoParams<Unit> {
    override suspend fun invoke() {
        repository.clearSession()
    }
}
