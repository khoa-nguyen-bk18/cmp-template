package com.devindie.cmptemplate.domain.usecase.user

import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.UserRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

/**
 * Reads the current authenticated session, if any.
 *
 * @see UserRepository.getSession
 */
class GetUserSessionUseCase(private val repository: UserRepository) : UseCaseNoParams<UserSession?> {
    override suspend fun invoke(): UserSession? = repository.getSession()
}
