package com.devindie.cmptemplate.domain.fake

import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.UserRepository

class FakeUserRepository : UserRepository {
    var session: UserSession? = null
    var saveSessionCallCount: Int = 0
    var clearSessionCallCount: Int = 0
    var lastSavedSession: UserSession? = null

    override suspend fun getSession(): UserSession? = session

    override suspend fun saveSession(session: UserSession) {
        saveSessionCallCount++
        lastSavedSession = session
        this.session = session
    }

    override suspend fun clearSession() {
        clearSessionCallCount++
        session = null
    }
}
