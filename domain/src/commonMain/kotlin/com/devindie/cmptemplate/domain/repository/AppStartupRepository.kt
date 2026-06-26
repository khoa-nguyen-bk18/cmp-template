package com.devindie.cmptemplate.domain.repository

interface AppStartupRepository {
    suspend fun ensureReady(): Result<Unit>
}
