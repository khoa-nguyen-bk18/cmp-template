package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.repository.OnboardingRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class HasCompletedOnboardingUseCase(private val repository: OnboardingRepository) : UseCaseNoParams<Boolean> {
    override suspend fun invoke(): Boolean = repository.hasCompleted()
}
