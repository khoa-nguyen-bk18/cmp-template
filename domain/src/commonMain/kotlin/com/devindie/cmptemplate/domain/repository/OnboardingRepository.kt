package com.devindie.cmptemplate.domain.repository

interface OnboardingRepository {
    suspend fun hasCompleted(): Boolean

    suspend fun markCompleted()
}
