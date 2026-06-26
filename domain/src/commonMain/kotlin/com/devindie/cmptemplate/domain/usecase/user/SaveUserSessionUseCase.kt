package com.devindie.cmptemplate.domain.usecase.user

import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.UserRepository
import com.devindie.cmptemplate.domain.usecase.UseCase

/**
 * Persists tokens after login or manual session update.
 *
 * @see UserRepository.saveSession
 */
class SaveUserSessionUseCase(private val repository: UserRepository) : UseCase<UserSession, Unit> {
    override suspend fun invoke(parameters: UserSession) {
        repository.saveSession(parameters)
    }
}
