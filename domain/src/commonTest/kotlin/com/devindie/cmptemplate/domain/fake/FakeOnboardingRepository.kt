package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.repository.OnboardingRepository

class FakeOnboardingRepository(private var completed: Boolean = false) : OnboardingRepository {
    var markCompletedCallCount: Int = 0

    override suspend fun hasCompleted(): Boolean = completed

    override suspend fun markCompleted() {
        markCompletedCallCount++
        completed = true
    }

    fun setCompleted(value: Boolean) {
        completed = value
    }
}
