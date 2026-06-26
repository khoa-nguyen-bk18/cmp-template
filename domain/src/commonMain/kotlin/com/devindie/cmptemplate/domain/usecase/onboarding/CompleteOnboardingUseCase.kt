package com.devindie.cmptemplate.domain.usecase.onboarding

import com.devindie.cmptemplate.domain.repository.OnboardingRepository
import com.devindie.cmptemplate.domain.usecase.UseCaseNoParams

class CompleteOnboardingUseCase(private val repository: OnboardingRepository) : UseCaseNoParams<Unit> {
    override suspend fun invoke() {
        repository.markCompleted()
    }
}
