package com.devindie.cmptemplate.fake

import com.devindie.cmptemplate.domain.model.user.UserSession
import com.devindie.cmptemplate.domain.repository.UserRepository

class FakeUserRepository(
    var session: UserSession? = null,
) : UserRepository {
    override suspend fun getSession(): UserSession? = session

    override suspend fun saveSession(session: UserSession) {
        this.session = session
    }

    override suspend fun clearSession() {
        session = null
    }
}
